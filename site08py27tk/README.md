# Skylight OS for Python 2.7 and Tkinter

Skylight OS rebuilt as a desktop program in Python 2.7 with Tkinter, the GUI toolkit that comes with Python. Nothing else is needed: one Python process draws the desktop, and each app is a Python class with its own window. Documentation: [`../wiki08py27tk`](../wiki08py27tk/README.md).

## Quick start (Windows)

With Python 2.7 installed (the python.org installer includes Tkinter):

```
python add-user.py admin choose-a-password
run.bat
```

`run.bat` starts `start-skylight.pyw` with `pythonw.exe`, which has no console window; double-clicking `start-skylight.pyw` does the same. To see error messages, run `python start-skylight.pyw` from a command prompt instead. On Linux or macOS, use `./run.sh`.

The optional **Pillow** library (`pip install "Pillow<7"`) makes background pictures smoother; Skylight works the same without it.

## Layout

```
site08py27tk/
├── start-skylight.pyw     entry point
├── add-user.py            create users and set passwords
├── run.bat / run.sh       launchers
├── skylight/
│   ├── desktop.py         the desktop: login, panel, menu, notifications, windows
│   ├── app.py             base class for apps (the App API)
│   ├── storage.py         users, settings and files in data/
│   └── images.py          GIF pictures for Tk, with Pillow if it's there
├── apps/                  apps.json, and one folder per app (app.json, app.py, icons/)
├── images/                backgrounds (JPEG, plus GIF copies), home and power icons
└── data/                  users, settings and user files (created as needed)
```
