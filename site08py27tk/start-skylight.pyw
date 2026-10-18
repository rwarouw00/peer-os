# -*- coding: utf-8 -*-
"""Skylight OS for Python 2.7 and Tkinter. Start it with:
    pythonw start-skylight.pyw     (Windows, no console window; or double-click this file)
    python start-skylight.pyw      (anywhere, with a console for error messages)
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from skylight.desktop import Desktop

if __name__ == '__main__':
    Desktop().start()
