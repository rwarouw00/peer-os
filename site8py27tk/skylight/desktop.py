# -*- coding: utf-8 -*-
"""The Skylight OS desktop: login, top panel (Apps button, window list, clock),
background and shortcuts, home menu, notifications, dialogs and app windows."""
import imp
import time
import traceback
import Tkinter as tk
import ttk

from skylight import images
from skylight import storage
from skylight.app import App

SHORTCUT_WIDTH = 90


class Desktop(object):

    def __init__(self):
        self.root = tk.Tk()
        self.root.withdraw()   # the root window stays hidden; every window is a Toplevel
        self.root.report_callback_exception = self.reportError
        self.user = None
        self.settings = None
        self.windows = {}      # appID -> {'app', 'window', 'button'}
        self.manifests = {}    # app path -> parsed app.json, or None if it's broken
        self.appClasses = {}   # app path -> its class, once loaded
        self.toasts = []
        self.nextAppID = 1

    def start(self):
        self.showLogin()
        self.root.mainloop()

    # ---- Login and logout ----

    def showLogin(self):
        login = tk.Toplevel(self.root)
        login.title('Skylight OS')
        login.resizable(False, False)
        setIcons(login, ['images/skylight.gif'])

        form = ttk.Frame(login, padding=20)
        form.pack(fill='both', expand=True)
        username = ttk.Entry(form, width=24)
        password = ttk.Entry(form, width=24, show=u'\u2022')
        error = ttk.Label(form, foreground='#a00000', wraplength=260)
        ttk.Label(form, text='Username:').grid(row=0, column=0, sticky='w', pady=4)
        username.grid(row=0, column=1, pady=4, padx=(10, 0))
        ttk.Label(form, text='Password:').grid(row=1, column=0, sticky='w', pady=4)
        password.grid(row=1, column=1, pady=4, padx=(10, 0))
        error.grid(row=2, column=0, columnspan=2, sticky='w', pady=4)

        def logIn(event=None):
            if storage.check_login(username.get(), password.get()):
                user = username.get()
                login.destroy()
                self.showDesktop(user)
            else:
                error.configure(text='Either the username or password is incorrect.')
                password.delete(0, 'end')
                password.focus_set()

        buttons = ttk.Frame(form)
        buttons.grid(row=3, column=0, columnspan=2, sticky='e', pady=(8, 0))
        ttk.Button(buttons, text='Quit', command=self.quit).pack(side='left', padx=(0, 6))
        ttk.Button(buttons, text='Log In', command=logIn, default='active').pack(side='left')
        login.bind('<Return>', logIn)
        login.bind('<Escape>', lambda event: self.quit())
        login.protocol('WM_DELETE_WINDOW', self.quit)

        centre(login)
        username.focus_set()

    def quit(self):
        self.root.destroy()

    def logout(self):
        for appID in list(self.windows):
            self.closeWindow(appID)
        for toast in list(self.toasts):
            self.removeToast(toast)
        self.menu.destroy()
        self.root.after_cancel(self.clockTimer)
        self.desktop.destroy()
        self.user = None
        self.showLogin()

    # ---- Building the desktop ----

    def showDesktop(self, user):
        self.user = user
        self.settings = storage.load_settings(user)

        self.desktop = desktop = tk.Toplevel(self.root)
        desktop.title('Skylight OS')
        setIcons(desktop, ['images/skylight.gif'])
        desktop.geometry('1024x700')
        maximize(desktop)
        desktop.protocol('WM_DELETE_WINDOW', self.logout)

        self.panel = ttk.Frame(desktop, padding=(4, 3))
        self.panel.pack(side='top', fill='x')
        self.appsButton = ttk.Button(self.panel, text='Apps', image=images.photo('images/home.gif', 20),
                                     compound='left', command=self.toggleMenu)
        self.appsButton.pack(side='left')
        ttk.Separator(self.panel, orient='vertical').pack(side='left', fill='y', padx=6)
        self.windowList = ttk.Frame(self.panel)
        self.windowList.pack(side='left')
        self.clock = ttk.Label(self.panel)
        self.clock.pack(side='right', padx=6)

        self.area = tk.Canvas(desktop, background='#202020', highlightthickness=0)
        self.area.pack(fill='both', expand=True)
        self.area.bind('<Configure>', lambda event: self.drawBackground())
        self.backgroundImage = None
        self.backgroundSize = None

        self.buildMenu()
        self.applySettings()
        self.tickClock()

    def tickClock(self):
        self.clock.configure(text=time.strftime('%I:%M %p').lstrip('0').lower() + '   ' + self.user)
        self.clockTimer = self.root.after(1000, self.tickClock)

    # ---- Settings, background and shortcuts ----

    def applySettings(self):
        self.backgroundSize = None   # redraw it
        self.drawBackground()
        self.renderShortcuts()

    def saveSettings(self, changes):
        result = storage.save_settings(self.user, changes)
        if not isinstance(result, dict):
            self.notify('Settings not saved', result)
            return False
        self.settings = result
        self.applySettings()
        return True

    def drawBackground(self):
        size = (self.area.winfo_width(), self.area.winfo_height())
        if size == self.backgroundSize or size[0] < 2:
            return
        self.backgroundSize = size
        self.backgroundImage = images.background(self.settings['background'], size[0], size[1])
        self.area.delete('background')
        self.area.create_image(size[0] // 2, size[1] // 2, image=self.backgroundImage, tags='background')
        self.area.tag_lower('background')

    def renderShortcuts(self):
        self.area.delete('shortcut')
        for number, shortcut in enumerate(self.settings['shortcuts']):
            manifest = self.readManifest(shortcut['path'])
            if manifest is None:
                continue
            try:
                icon = images.photo(shortcut['path'] + manifest['icons']['48'])
            except tk.TclError:
                continue   # not a GIF
            tag = 'shortcut%d' % number
            x = shortcut['x'] + SHORTCUT_WIDTH // 2
            y = shortcut['y']
            tags = ('shortcut', tag)
            self.area.create_image(x, y + 4, anchor='n', tags=tags, image=icon)
            # White text with a dark shadow, readable on any picture
            for dx, colour in ((1, '#000000'), (0, '#ffffff')):
                self.area.create_text(x + dx, y + 56 + dx, anchor='n', text=manifest['title'], fill=colour,
                                      width=SHORTCUT_WIDTH - 4, justify='center', tags=tags)
            self.makeShortcutInteractive(tag, shortcut['path'])

    def makeShortcutInteractive(self, tag, path):
        drag = {}

        def press(event):
            drag.update(x=event.x, y=event.y, dx=0, dy=0)

        def move(event):
            self.area.move(tag, event.x - drag['x'], event.y - drag['y'])
            drag['dx'] += event.x - drag['x']
            drag['dy'] += event.y - drag['y']
            drag.update(x=event.x, y=event.y)

        def release(event):
            if drag.get('dx') or drag.get('dy'):
                changed = [dict(s) for s in self.settings['shortcuts']]
                for shortcut in changed:
                    if shortcut['path'] == path:
                        shortcut['x'] = max(0, shortcut['x'] + drag['dx'])
                        shortcut['y'] = max(0, shortcut['y'] + drag['dy'])
                # after the mouse event, because saving redraws every shortcut
                self.root.after_idle(lambda: self.saveSettings({'shortcuts': changed}))

        def openApp(event):
            self.openWindow(path, {})

        def showMenu(event):
            menu = tk.Menu(self.area, tearoff=False)
            menu.add_command(label='Remove from desktop', command=lambda: self.removeShortcut(path))
            menu.tk_popup(event.x_root, event.y_root)

        self.area.tag_bind(tag, '<ButtonPress-1>', press)
        self.area.tag_bind(tag, '<B1-Motion>', move)
        self.area.tag_bind(tag, '<ButtonRelease-1>', release)
        self.area.tag_bind(tag, '<Double-Button-1>', openApp)
        self.area.tag_bind(tag, '<Button-3>', showMenu)

    def addShortcut(self, path):
        shortcuts = self.settings['shortcuts']
        if any(s['path'] == path for s in shortcuts):
            return
        count = len(shortcuts)
        self.saveSettings({'shortcuts': shortcuts + [
            {'path': path, 'x': 20 + 100 * (count // 6), 'y': 20 + 100 * (count % 6)}]})

    def removeShortcut(self, path):
        self.saveSettings({'shortcuts': [s for s in self.settings['shortcuts'] if s['path'] != path]})

    # ---- Home menu ----

    def buildMenu(self):
        self.menu = menu = tk.Toplevel(self.desktop)
        menu.withdraw()
        menu.overrideredirect(True)
        keepOnTop(menu)
        frame = ttk.Frame(menu, width=430, height=470, relief='raised', borderwidth=1)
        frame.pack(fill='both', expand=True)
        frame.pack_propagate(False)

        footer = ttk.Frame(frame, padding=6)
        footer.pack(side='bottom', fill='x')
        ttk.Button(footer, text='Apps', command=lambda: self.showMenuPage(self.appsPage)).pack(side='left')
        ttk.Button(footer, text='Notifications',
                   command=lambda: self.showMenuPage(self.notificationsPage)).pack(side='left', padx=6)
        ttk.Button(footer, image=images.photo('images/power.gif', 20), command=self.logout).pack(side='right')
        ttk.Separator(frame).pack(side='bottom', fill='x')

        pages = ttk.Frame(frame)
        pages.pack(fill='both', expand=True)

        self.appsPage = ttk.Frame(pages, padding=10)
        for number, path in enumerate(storage.app_list()):
            manifest = self.readManifest(path)
            if manifest is None:
                continue
            try:
                icon = images.photo(path + manifest['icons']['48'])
            except tk.TclError:
                continue   # not a GIF
            button = ttk.Button(self.appsPage, text=manifest['title'], compound='top', width=10, image=icon,
                                command=lambda path=path: self.openFromMenu(path))
            button.grid(row=number // 4, column=number % 4, padx=4, pady=4)

        self.notificationsPage = ttk.Frame(pages)
        top = ttk.Frame(self.notificationsPage, padding=6)
        top.pack(side='top', fill='x')
        ttk.Button(top, text='Clear', command=self.clearNotifications).pack(side='right')
        self.notificationList = ScrollFrame(self.notificationsPage)
        self.notificationList.pack(fill='both', expand=True)

        for page in (self.appsPage, self.notificationsPage):
            page.place(x=0, y=0, relwidth=1, relheight=1)
        menu.bind('<Escape>', lambda event: self.hideMenu())
        # Close the menu when the user clicks anywhere else in Skylight.
        self.root.bind_all('<ButtonPress>', self.onAnyClick, add='+')

    def openFromMenu(self, path):
        self.hideMenu()
        self.openWindow(path, {})

    def toggleMenu(self):
        if self.menu.winfo_viewable():
            self.hideMenu()
            return
        self.showMenuPage(self.appsPage)
        x = self.panel.winfo_rootx() + 4
        y = self.panel.winfo_rooty() + self.panel.winfo_height() + 4
        self.menu.geometry('+%d+%d' % (x, y))
        self.menu.deiconify()
        self.menu.lift()

    def hideMenu(self):
        self.menu.withdraw()

    def showMenuPage(self, page):
        page.lift()

    def onAnyClick(self, event):
        if not self.user or not self.menu.winfo_exists() or not self.menu.winfo_viewable():
            return
        widget = str(event.widget)
        # Clicks inside the menu, and on the Apps button (which toggles it), are left alone.
        if not widget.startswith(str(self.menu)) and widget != str(self.appsButton):
            self.hideMenu()

    # ---- Notifications ----

    def notify(self, header, body=''):
        self.addToTray(header, body)
        self.showToast(header, body)

    def addToTray(self, header, body):
        row = tk.Frame(self.notificationList.inner, background='#ffffff', padx=10, pady=6)
        self.notificationBox(row, header, body, lambda: row.destroy())
        children = self.notificationList.inner.pack_slaves()
        if children:
            row.pack(fill='x', before=children[0])   # newest first
        else:
            row.pack(fill='x')

    def notificationBox(self, frame, header, body, close, dark=False):
        colours = {'background': '#323232' if dark else '#ffffff', 'foreground': '#ffffff' if dark else '#000000'}
        closeButton = tk.Label(frame, text=u'\u00d7', font=('TkDefaultFont', 14), cursor='hand2', **colours)
        closeButton.pack(side='right', anchor='n')
        closeButton.bind('<Button-1>', lambda event: close())
        text = tk.Frame(frame, background=colours['background'])
        text.pack(side='left', fill='x', expand=True)
        tk.Label(text, text=header, font=('TkDefaultFont', 10, 'bold'), anchor='w', justify='left',
                 wraplength=250, **colours).pack(fill='x')
        if body:
            tk.Label(text, text=body, anchor='w', justify='left', wraplength=250, **colours).pack(fill='x')

    def clearNotifications(self):
        for row in self.notificationList.inner.pack_slaves():
            row.destroy()

    def showToast(self, header, body):
        toast = tk.Toplevel(self.desktop, background='#323232')
        toast.withdraw()
        toast.overrideredirect(True)
        keepOnTop(toast)
        frame = tk.Frame(toast, background='#323232', padx=14, pady=8, width=300)
        frame.pack(fill='both', expand=True)
        self.notificationBox(frame, header, body, lambda: self.removeToast(toast), dark=True)
        self.toasts.append(toast)
        self.layoutToasts()
        toast.deiconify()
        toast.after(self.settings['notificationTime'], lambda: self.removeToast(toast))

    def layoutToasts(self):
        """Stacks toasts down the right-hand side, just below the panel."""
        right = self.panel.winfo_rootx() + self.panel.winfo_width()
        y = self.panel.winfo_rooty() + self.panel.winfo_height() + 10
        for toast in self.toasts:
            toast.update_idletasks()
            toast.geometry('300x%d+%d+%d' % (toast.winfo_reqheight(), right - 320, y))
            y += toast.winfo_reqheight() + 10

    def removeToast(self, toast):
        if toast in self.toasts:
            self.toasts.remove(toast)
            toast.destroy()
            self.layoutToasts()

    # ---- Dialogs ----

    def openDialog(self, message, onOK=None, parent=None):
        """With onOK: OK and Cancel, and onOK() runs on OK. Without: an OK button only."""
        parent = parent or self.desktop
        dialog = tk.Toplevel(parent)
        dialog.withdraw()
        dialog.title('Skylight OS')
        dialog.resizable(False, False)
        dialog.transient(parent)
        frame = ttk.Frame(dialog, padding=20)
        frame.pack(fill='both', expand=True)
        ttk.Label(frame, text=message, wraplength=360, justify='left').pack(anchor='w')

        def answer(ok):
            dialog.destroy()
            if ok and onOK is not None:
                onOK()

        buttons = ttk.Frame(frame)
        buttons.pack(anchor='e', pady=(16, 0))
        if onOK is not None:
            ttk.Button(buttons, text='OK', default='active', command=lambda: answer(True)).pack(side='left', padx=(0, 6))
            ttk.Button(buttons, text='Cancel', command=lambda: answer(False)).pack(side='left')
        else:
            ttk.Button(buttons, text='OK', default='active', command=lambda: answer(False)).pack(side='left')
        dialog.bind('<Return>', lambda event: answer(True))
        dialog.bind('<Escape>', lambda event: answer(False))
        dialog.protocol('WM_DELETE_WINDOW', lambda: answer(False))

        centre(dialog, parent)
        dialog.focus_set()
        dialog.wait_visibility()
        dialog.grab_set()   # until it's answered, the rest of Skylight waits

    # ---- Apps and windows ----

    def readManifest(self, path):
        """Reads apps/<name>/app.json, or returns None if it's missing or incomplete."""
        if not storage.is_app_path(path):
            return None
        if path not in self.manifests:
            manifest = storage.read_json(storage.path(path + 'app.json'), None)
            complete = (isinstance(manifest, dict) and 'title' in manifest and 'class' in manifest
                        and isinstance(manifest.get('icons'), dict)
                        and all(size in manifest['icons'] for size in ('16', '32', '48', '64')))
            self.manifests[path] = manifest if complete else None
        return self.manifests[path]

    def loadAppClass(self, path, manifest):
        """Loads apps/<name>/app.py (once) and returns the class named in app.json."""
        module = imp.load_source('skylight_app_' + path.split('/')[1].replace('-', '_'), storage.path(path + 'app.py'))
        cls = getattr(module, manifest['class'], None)
        if not (isinstance(cls, type) and issubclass(cls, App)):
            raise ValueError('%sapp.py has no class %s extending App.' % (path, manifest['class']))
        return cls

    def openWindow(self, path, args=None):
        """Opens the app in folder path (e.g. 'apps/terminal/'). Returns its AppID, or None."""
        manifest = self.readManifest(path)
        if manifest is None:
            self.notify('Cannot open app', '%s is not an app folder with a complete app.json.' % path)
            return None
        appID = str(self.nextAppID)
        self.nextAppID += 1
        window = tk.Toplevel(self.desktop)
        window.withdraw()
        window.title(manifest['title'])
        window.transient(self.desktop)   # stays in front of the desktop
        setIcons(window, [path + manifest['icons'][size] for size in ('64', '32', '16')])
        try:
            if path not in self.appClasses:
                self.appClasses[path] = self.loadAppClass(path, manifest)
            app = self.appClasses[path](self, appID, path, manifest, window, args or {})
            app.build()
            icon = images.photo(path + manifest['icons']['16'])
        except Exception as error:
            window.destroy()
            self.notify('Cannot open app', describe(error))
            return None

        width, height = manifest.get('width', 500), manifest.get('height', 350)
        x = self.desktop.winfo_rootx() + (self.desktop.winfo_width() - width) // 2
        y = self.desktop.winfo_rooty() + (self.desktop.winfo_height() - height) // 2
        window.geometry('%dx%d+%d+%d' % (width, height, max(0, x), max(0, y)))
        window.protocol('WM_DELETE_WINDOW', lambda: app.canClose() and self.closeWindow(appID))

        shown = tk.IntVar(value=1)
        button = ttk.Checkbutton(self.windowList, text=manifest['title'], compound='left', style='Toolbutton',
                                 image=icon, variable=shown,
                                 command=lambda: self.toggleWindow(appID))
        button.shown = shown
        button.pack(side='left', padx=2)

        def showButtonMenu(event):
            menu = tk.Menu(button, tearoff=False)
            menu.add_command(label='Close', command=lambda: self.closeWindow(appID))
            menu.tk_popup(event.x_root, event.y_root)
        button.bind('<Button-3>', showButtonMenu)

        self.windows[appID] = {'app': app, 'window': window, 'button': button}
        window.deiconify()
        window.lift()
        window.focus_set()
        return appID

    def toggleWindow(self, appID):
        entry = self.windows[appID]
        if entry['button'].shown.get():
            entry['window'].deiconify()
            entry['window'].lift()
        else:
            entry['window'].withdraw()

    def closeWindow(self, appID):
        """Closes a window without asking the app."""
        entry = self.windows.pop(appID, None)
        if entry is not None:
            entry['window'].destroy()
            entry['button'].destroy()

    def reportError(self, kind, error, trace):
        """An error in an app's event handler: tell the user instead of stopping."""
        traceback.print_exception(kind, error, trace)
        if self.user is not None:
            self.notify('Error in an app', describe(error))


# ---- Helpers ----

class ScrollFrame(ttk.Frame):
    """A frame with a vertical scroll bar; put things in .inner."""

    def __init__(self, parent):
        ttk.Frame.__init__(self, parent)
        canvas = tk.Canvas(self, highlightthickness=0, background='#ffffff')
        bar = ttk.Scrollbar(self, orient='vertical', command=canvas.yview)
        canvas.configure(yscrollcommand=bar.set)
        bar.pack(side='right', fill='y')
        canvas.pack(side='left', fill='both', expand=True)
        self.inner = tk.Frame(canvas, background='#ffffff')
        item = canvas.create_window(0, 0, window=self.inner, anchor='nw')
        self.inner.bind('<Configure>', lambda event: canvas.configure(scrollregion=canvas.bbox('all')))
        canvas.bind('<Configure>', lambda event: canvas.itemconfigure(item, width=event.width))


def describe(error):
    """'ValueError: message' for an exception."""
    return traceback.format_exception_only(type(error), error)[-1].strip()


def setIcons(window, paths):
    try:
        window.tk.call('wm', 'iconphoto', str(window), *[images.photo(p) for p in paths])
    except tk.TclError:
        pass


def maximize(window):
    for attempt in (lambda: window.state('zoomed'),              # Windows and macOS
                    lambda: window.attributes('-zoomed', True)):  # Linux (X11)
        try:
            attempt()
            return
        except tk.TclError:
            pass
    window.geometry('%dx%d+0+0' % (window.winfo_screenwidth(), window.winfo_screenheight()))


def keepOnTop(window):
    try:
        window.attributes('-topmost', True)
    except tk.TclError:
        pass


def centre(window, over=None):
    """Shows a withdrawn window in the middle of the screen, or of another window."""
    window.update_idletasks()
    width, height = window.winfo_reqwidth(), window.winfo_reqheight()
    if over is None:
        x = (window.winfo_screenwidth() - width) // 2
        y = (window.winfo_screenheight() - height) // 3
    else:
        x = over.winfo_rootx() + (over.winfo_width() - width) // 2
        y = over.winfo_rooty() + (over.winfo_height() - height) // 2
    window.geometry('+%d+%d' % (max(0, x), max(0, y)))
    window.deiconify()
