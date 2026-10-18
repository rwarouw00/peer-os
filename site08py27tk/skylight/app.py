# -*- coding: utf-8 -*-
"""Base class for Skylight OS apps.

Each app is a class that extends App and builds its window's contents in
build(). The method names match the other versions of Skylight OS.
"""
import copy
import io
import os

from skylight import storage


class App(object):

    def __init__(self, desktop, appID, path, manifest, window, args):
        self.desktop = desktop     # the Desktop that opened us
        self.appID = appID         # this window's ID
        self.path = path           # e.g. 'apps/terminal/'
        self.manifest = manifest   # parsed app.json
        self.window = window       # our Tkinter Toplevel
        self.args = args           # extra values passed to openWindow()

    def build(self):
        """Override: add widgets to self.window."""

    def canClose(self):
        """Override: return False to keep the window open (e.g. to ask about unsaved work)."""
        return True

    # ---- Windows ----

    def openWindow(self, path, args=None):
        return self.desktop.openWindow(path, args or {})

    def closeWindow(self, appID=None):
        self.desktop.closeWindow(self.appID if appID is None else appID)

    # ---- Dialogs and notifications ----

    def openDialog(self, message, onOK=None):
        """Shows a message. With onOK (any function), asks OK or Cancel and calls onOK() on OK."""
        self.desktop.openDialog(message, onOK, self.window)

    def postNotification(self, header, body=''):
        self.desktop.notify(header, body or '')

    # ---- Settings and the desktop ----

    def getSettings(self):
        """A copy of the user's settings: {'background': ..., 'notificationTime': ..., 'shortcuts': [...]}."""
        return copy.deepcopy(self.desktop.settings)

    def saveSettings(self, changes):
        """Checks, saves and applies changes such as {'notificationTime': 3000}.
        Returns True, or tells the user what was wrong and returns False."""
        return self.desktop.saveSettings(changes)

    def setBackground(self, path):
        return self.saveSettings({'background': path})

    def addShortcut(self, path):
        self.desktop.addShortcut(path)

    # ---- Identity ----

    def getAppID(self):
        return self.appID

    def getUsername(self):
        return self.desktop.user

    # ---- Files ----

    def appFile(self, name):
        """Full path of a file in this app's folder, e.g. appFile('icons/16.gif')."""
        return storage.path(self.path + name)

    def listFiles(self):
        """The names of the user's own files."""
        return storage.list_files(self.desktop.user)

    def readFile(self, name):
        """The text of one of the user's files, or None if there's no such file."""
        if storage.file_error(name) is not None:
            return None
        try:
            with io.open(storage.user_file(self.desktop.user, name), encoding='utf-8') as f:
                return f.read()
        except (IOError, UnicodeError):
            return None

    def writeFile(self, name, contents):
        """Saves text. Returns {'ok': True}, or {'ok': False, 'error': '...'}."""
        error = storage.file_error(name)
        if error is not None:
            return {'ok': False, 'error': error}
        file_path = storage.user_file(self.desktop.user, name)
        try:
            if not os.path.isdir(os.path.dirname(file_path)):
                os.makedirs(os.path.dirname(file_path))
            if isinstance(contents, str):
                contents = contents.decode('utf-8')
            with io.open(file_path, 'w', encoding='utf-8', newline='') as f:
                f.write(contents)
            return {'ok': True}
        except (IOError, OSError, UnicodeError):
            return {'ok': False, 'error': 'The file could not be saved.'}
