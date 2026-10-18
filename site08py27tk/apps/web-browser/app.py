# -*- coding: utf-8 -*-
"""Web Browser: Tkinter has no web page widget, so this opens addresses in the
computer's default browser."""
import re
import webbrowser
import ttk

from skylight.app import App

WEB_ADDRESS = re.compile(r'^https?://[A-Za-z0-9._~:/?#\[\]@!$&\'()*+,;=%-]+$', re.IGNORECASE)


class WebBrowserApp(App):

    def build(self):
        frame = ttk.Frame(self.window, padding=12)
        frame.pack(fill='both', expand=True)
        row = ttk.Frame(frame)
        row.pack(fill='x')
        self.address = ttk.Entry(row)
        self.address.insert(0, 'https://')
        self.address.pack(side='left', fill='x', expand=True)
        self.address.bind('<Return>', lambda event: self.go())
        ttk.Button(row, text='Open', command=self.go).pack(side='left', padx=(6, 0))
        ttk.Label(frame, text='Pages open in your default web browser.').pack(anchor='w', pady=(10, 0))
        self.address.focus_set()

    def go(self):
        url = self.address.get().strip()
        if not re.match(r'^https?://', url, re.IGNORECASE):
            url = 'http://' + url
        if not WEB_ADDRESS.match(url):
            self.openDialog("That doesn't look like a web address.")
            return
        webbrowser.open(url)
        self.postNotification('Web Browser', 'Opened ' + url)
