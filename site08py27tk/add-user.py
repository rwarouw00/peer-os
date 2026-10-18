# -*- coding: utf-8 -*-
"""Adds a user, or changes an existing user's password.
Usage: python add-user.py <username> <password>"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from skylight import storage

if len(sys.argv) != 3 or not storage.is_valid_username(sys.argv[1]) or not sys.argv[2]:
    sys.exit("Usage: python add-user.py <username> <password>\n"
             "Usernames may use letters, digits, '-' and '_' (up to 32 characters).")
existed = sys.argv[1] in storage.load_users()
if not storage.set_password(sys.argv[1], sys.argv[2].decode(sys.getfilesystemencoding() or 'utf-8', 'replace')):
    sys.exit('Could not write ' + os.path.join(storage.DATA, 'users.json'))
print (('Changed the password for ' if existed else 'Added user ') + sys.argv[1])
