# -*- coding: utf-8 -*-
"""Settings: desktop background, and how long notifications stay on screen."""
import Tkinter as tk
import ttk

from skylight import images
from skylight.app import App


class SettingsApp(App):

    def build(self):
        settings = self.getSettings()
        frame = ttk.Frame(self.window, padding=14)
        frame.pack(fill='both', expand=True)

        ttk.Label(frame, text='Background', font=('TkDefaultFont', 10, 'bold')).pack(anchor='w')
        grid = ttk.Frame(frame)
        grid.pack(anchor='w', pady=(6, 14))
        self.choice = tk.StringVar(value=settings['background'])
        self.thumbnails = []   # Tkinter forgets images nothing refers to
        for number in range(1, 7):
            path = 'images/backgrounds/%d.jpg' % number
            image = images.thumbnail(path, 128, 80)
            self.thumbnails.append(image)
            button = tk.Radiobutton(grid, image=image, value=path, variable=self.choice, indicatoron=False,
                                    borderwidth=3, selectcolor='#3d8ee0', command=self.chooseBackground)
            button.grid(row=(number - 1) // 3, column=(number - 1) % 3, padx=3, pady=3)

        ttk.Label(frame, text='Notifications', font=('TkDefaultFont', 10, 'bold')).pack(anchor='w')
        row = ttk.Frame(frame)
        row.pack(anchor='w', pady=6)
        ttk.Label(row, text='Show notifications for').pack(side='left')
        self.seconds = tk.Spinbox(row, from_=1, to=60, width=4, justify='right')
        self.seconds.delete(0, 'end')
        self.seconds.insert(0, settings['notificationTime'] // 1000)
        self.seconds.pack(side='left', padx=6)
        ttk.Label(row, text='seconds').pack(side='left')
        ttk.Button(row, text='Save', command=self.saveTime).pack(side='left', padx=10)

    def chooseBackground(self):
        if not self.setBackground(self.choice.get()):
            self.choice.set(self.getSettings()['background'])

    def saveTime(self):
        try:
            seconds = int(self.seconds.get())
        except ValueError:
            seconds = 0
        if self.saveSettings({'notificationTime': seconds * 1000}):
            self.postNotification('Settings', 'Notifications now stay for %d seconds.' % seconds)
