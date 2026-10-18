# -*- coding: utf-8 -*-
"""Loading pictures for Tkinter.

Tk 8.5, which comes with Python 2.7 on Windows, can only read GIF (and PPM)
files. So every icon is a GIF, and each background JPEG has a GIF copy beside
it. If the Pillow library (PIL) is installed, the JPEGs are used instead and
scaled smoothly; without it, GIFs are scaled with Tk's whole-number zoom and
subsample.
"""
import os
import Tkinter

from skylight import storage

try:
    from PIL import Image, ImageTk
except ImportError:
    Image = None

_cache = {}


def photo(relative, size=None):
    """A PhotoImage from a Skylight path such as 'images/home.gif', optionally size x size pixels."""
    key = (relative, size)
    if key not in _cache:
        image = Tkinter.PhotoImage(file=storage.path(relative))
        if size is not None and image.width() != size:
            image = _scale(image, float(size) / image.width())
        _cache[key] = image
    return _cache[key]


def background(relative, width, height):
    """A background picture scaled to cover width x height (some of it may be cut off)."""
    if Image is not None:
        try:
            picture = Image.open(storage.path(relative))
            scale = max(float(width) / picture.size[0], float(height) / picture.size[1])
            size = (int(picture.size[0] * scale + 0.5), int(picture.size[1] * scale + 0.5))
            return ImageTk.PhotoImage(picture.convert('RGB').resize(size, Image.BILINEAR))
        except IOError:
            pass
    image = Tkinter.PhotoImage(file=gif_copy(relative))
    return _scale(image, max(float(width) / image.width(), float(height) / image.height()))


def thumbnail(relative, width, height):
    """A small copy of a background picture, about width x height."""
    if Image is not None:
        try:
            picture = Image.open(storage.path(relative)).convert('RGB')
            picture.thumbnail((width, height * 2), Image.ANTIALIAS)
            return ImageTk.PhotoImage(picture.crop((0, 0, width, min(height, picture.size[1]))))
        except IOError:
            pass
    image = Tkinter.PhotoImage(file=gif_copy(relative))
    return image.subsample(max(1, image.width() // width))


def gif_copy(relative):
    """The GIF beside a JPEG or PNG: images/backgrounds/1.jpg -> images/backgrounds/1.gif."""
    return os.path.splitext(storage.path(relative))[0] + '.gif'


# Tk can only scale by whole numbers, but zooming then subsampling gives
# fractions: zoom(3).subsample(2) is 1.5 times the size.
_FRACTIONS = sorted(set((float(z) / s, z, s) for z in range(1, 5) for s in range(1, 9)))


def _scale(image, factor):
    """The image scaled by the smallest available fraction at least as big as factor."""
    for value, zoom, subsample in _FRACTIONS:
        if value >= factor - 0.001:
            break
    if zoom > 1:
        image = image.zoom(zoom)
    if subsample > 1:
        image = image.subsample(subsample)
    return image
