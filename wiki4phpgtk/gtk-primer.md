# PHP-GTK2 primer

A short guide to the GTK ideas Skylight uses, written for people who know the browser version. The full reference is the PHP-GTK2 manual that comes with your PHP-GTK2 package.

## From web to GTK

| Browser version | PHP-GTK2 |
| --- | --- |
| HTML elements | Widget objects: `new GtkButton('Save')`, `new GtkLabel('Hi')`, `new GtkEntry()` |
| Page layout (blocks, CSS positioning) | Containers: `GtkVBox`/`GtkHBox` stack widgets, `GtkTable` makes a grid, `GtkScrolledWindow` adds scroll bars |
| `<textarea>` | `GtkTextView` with a `GtkTextBuffer` holding the text |
| `<input type="text">` / `"password"` | `GtkEntry`; `set_visibility(false)` for passwords |
| `<img>` | `GtkImage::new_from_file($file)`; for resizing, load a `GdkPixbuf` and `scale_simple()` |
| `element.onclick = function () {...}` | `$button->connect_simple('clicked', array($this, 'method'))` |
| `setTimeout`/`setInterval` | `Gtk::timeout_add($milliseconds, array($this, 'method'))`; return `true` from the method to repeat |
| CSS | Resource files (`skylight.gtkrc`) and widget names, or `modify_font()`/`modify_fg()` in code |
| `alert`/`confirm` | `GtkMessageDialog` (or `$this->openDialog()`) |
| Window manager drawn in JavaScript | Real windows (`GtkWindow`); the operating system does the rest |

## Containers and packing

GTK sizes widgets for you. You put widgets in boxes and say how they share space:

```php
$box = new GtkVBox(false, 6);            // not same-size children, 6px apart
$box->set_border_width(12);              // padding around the edge
$box->pack_start($toolbar, false, false); // only as tall as it needs
$box->pack_start($editor);                // takes all the space left
$this->window->add($box);                 // a window holds exactly one child
```

`pack_end` packs from the other end, which is how the panel puts the clock on the right.

## Signals and callbacks

Widgets send **signals** (`clicked`, `activate`, `changed`, `delete-event`, ...). Connect a method to one:

```php
// connect_simple: the method gets only the extra values you pass
$button->connect_simple('clicked', array($this, 'open'), 'apps/notes/');
function open($path) { ... }

// connect: the method also gets the widget, and the event for *-event signals
$entry->connect('key-press-event', array($this, 'onKey'));
function onKey($entry, $event) { ...; return false; }   // true = handled, stop here
```

PHP 5.2 has no anonymous functions, so every callback is `array($object, 'methodName')` or a function name.

Nothing happens until `Gtk::main()` runs: it waits for events and calls your methods. Keep callbacks quick; while one runs, the whole desktop waits.

## Text views

```php
$view = new GtkTextView();
$buffer = $view->get_buffer();
$buffer->set_text('Hello');
$text = $buffer->get_text($buffer->get_start_iter(), $buffer->get_end_iter());
```

## Styles

Skylight's colours live in [`skylight.gtkrc`](../site4phpgtk/skylight.gtkrc), which works much like CSS. Give a widget a name, and match the name in the file:

```php
$toast->set_name('skylight-toast');
```

```
style "skylight-toast" {
	bg[NORMAL] = "#4a4a4a"
	fg[NORMAL] = "#ffffff"
}
widget "skylight-toast*" style "skylight-toast"
```

`bg` is the background, `fg` the text colour, and `NORMAL`, `PRELIGHT` (mouse over), `ACTIVE` (pressed) and `INSENSITIVE` (disabled) are states. For one-off changes in code: `$label->modify_font(new PangoFontDescription('Bold 11'))`.

## Text encoding

PHP-GTK2 converts PHP strings to GTK's UTF-8 using the `php-gtk.codepage` setting, which defaults to ISO-8859-1. `skylight.php` sets it to UTF-8, so save your PHP files as UTF-8 and any language works.

## PHP 5.2 reminders

Skylight's code avoids features newer than PHP 5.2, so it runs on PHP-GTK 2.0.1's PHP:

* no anonymous functions or closures (5.3);
* no namespaces (5.3);
* `dirname(__FILE__)` instead of `__DIR__` (5.3);
* `array(...)` instead of `[...]` (5.4);
* no `?:` shorthand (5.3) or `??` (7.0).
