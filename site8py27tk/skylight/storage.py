# -*- coding: utf-8 -*-
"""Skylight OS: users, settings and files, stored as JSON under data/.

The file formats match the PHP-GTK2 version, so a data/ folder can move
between the two.
"""
import copy
import hashlib
import hmac
import io
import json
import os
import re
import binascii

ROOT = os.path.abspath(os.environ.get('SKYLIGHT_HOME') or os.path.join(os.path.dirname(__file__), '..'))
DATA = os.path.join(ROOT, 'data')

USERNAME = re.compile(r'^[A-Za-z0-9_-]{1,32}$')
APP_PATH = re.compile(r'^apps/[a-z0-9-]+/$')
BACKGROUND = re.compile(r'^images/backgrounds/[A-Za-z0-9_-]+\.(jpg|jpeg|png)$', re.IGNORECASE)
# No slashes and no leading dot, so a name can't leave the user's folder.
FILE_NAME = re.compile(r'^[A-Za-z0-9_-][A-Za-z0-9_. -]{0,99}$')
ITERATIONS = 20000

DEFAULT_SETTINGS = {
    'background': 'images/backgrounds/3.jpg',
    'notificationTime': 7000,
    'shortcuts': [],
}


def path(relative):
    """A full path from a Skylight path such as 'apps/terminal/icons/16.gif'."""
    return os.path.join(ROOT, *relative.split('/'))


# ---- JSON files ----

def read_json(file_path, default):
    try:
        with io.open(file_path, encoding='utf-8') as f:
            return json.load(f)
    except (IOError, ValueError):
        return default   # a missing or damaged file counts as empty


def write_json(file_path, data):
    try:
        folder = os.path.dirname(file_path)
        if not os.path.isdir(folder):
            os.makedirs(folder)
        with io.open(file_path, 'w', encoding='utf-8') as f:
            f.write(unicode(json.dumps(data, ensure_ascii=False)))
        return True
    except (IOError, OSError):
        return False


# ---- Users ----

def is_valid_username(name):
    return isinstance(name, basestring) and USERNAME.match(name) is not None


def load_users():
    users = read_json(os.path.join(DATA, 'users.json'), {})
    return users if isinstance(users, dict) else {}


def user_folder(user):
    return os.path.join(DATA, 'users', user)


def check_login(username, password):
    users = load_users()
    if not is_valid_username(username) or username not in users:
        return False
    return check_password(password, users[username].get('password', ''))


def set_password(username, password):
    """Adds a user or changes their password. Returns False if the file can't be written."""
    users = load_users()
    users[username] = {'password': hash_password(password)}
    return write_json(os.path.join(DATA, 'users.json'), users)


# ---- Passwords: PBKDF2-SHA256 (hashlib.pbkdf2_hmac, Python 2.7.8 and later) ----

def pbkdf2(password, salt, iterations):
    if isinstance(password, unicode):
        password = password.encode('utf-8')
    return binascii.hexlify(hashlib.pbkdf2_hmac('sha256', password, salt, iterations))


def hash_password(password):
    salt = binascii.hexlify(os.urandom(16))   # the hex text is the salt, as in the PHP version
    return 'pbkdf2-sha256$%d$%s$%s' % (ITERATIONS, salt, pbkdf2(password, salt, ITERATIONS))


def check_password(password, stored):
    parts = (stored or '').split('$')
    if len(parts) != 4 or parts[0] != 'pbkdf2-sha256':
        return False
    try:
        actual = pbkdf2(password, parts[2].encode('ascii'), int(parts[1]))
    except (ValueError, UnicodeError):
        return False
    return hmac.compare_digest(actual, parts[3].encode('ascii'))   # constant time


# ---- Settings ----

def load_settings(user):
    settings = copy.deepcopy(DEFAULT_SETTINGS)
    saved = read_json(os.path.join(user_folder(user), 'settings.json'), {})
    if isinstance(saved, dict):
        for name, value in saved.items():
            clean = clean_setting(name, value)
            if clean is not None:
                settings[name] = clean   # bad values in the file are ignored
    return settings


def save_settings(user, changes):
    """Applies changes after checking each one. Returns the new settings, or an error message."""
    settings = load_settings(user)
    for name, value in changes.items():
        clean = clean_setting(name, value)
        if clean is None:
            return 'Bad value for %s.' % name
        settings[name] = clean
    if not write_json(os.path.join(user_folder(user), 'settings.json'), settings):
        return 'The settings could not be saved.'
    return settings


def clean_setting(name, value):
    """Returns the checked value, or None if it isn't acceptable."""
    if name == 'background' and isinstance(value, basestring) and BACKGROUND.match(value):
        return value
    if name == 'notificationTime' and isinstance(value, (int, long, float)) and not isinstance(value, bool) \
            and 1000 <= value <= 60000:
        return int(value)
    if name == 'shortcuts' and isinstance(value, list) and len(value) <= 50:
        clean = []
        for shortcut in value:
            if not isinstance(shortcut, dict) or not is_app_path(shortcut.get('path')):
                return None
            try:
                clean.append({'path': shortcut['path'],
                              'x': int(shortcut.get('x', 20)), 'y': int(shortcut.get('y', 20))})
            except (TypeError, ValueError):
                return None
        return clean
    return None


# ---- Apps ----

def is_app_path(app_path):
    return isinstance(app_path, basestring) and APP_PATH.match(app_path) is not None


def app_list():
    """The app folders listed in apps/apps.json."""
    paths = read_json(path('apps/apps.json'), [])
    return [p for p in paths if is_app_path(p)] if isinstance(paths, list) else []


# ---- The user's own files (data/users/<name>/files/) ----

def file_error(name):
    if not isinstance(name, basestring) or not FILE_NAME.match(name):
        return 'File names may use letters, digits, spaces, "-", "_" and "." (up to 100 characters).'
    return None


def user_file(user, name):
    return os.path.join(user_folder(user), 'files', name)


def list_files(user):
    folder = os.path.join(user_folder(user), 'files')
    if not os.path.isdir(folder):
        return []
    return sorted(name for name in os.listdir(folder) if os.path.isfile(os.path.join(folder, name)))
