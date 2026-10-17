# User guide

## The desktop

![The desktop](images/desktop.png)

| | |
| --- | --- |
| **Apps** (top left) | Opens the home menu |
| **Window list** (next to Apps) | A button for each open app window |
| **Clock** (top right) | The time and your username |
| **Shortcuts** | App icons on the desktop. Double-click to open, drag to move, right-click to remove. They are saved with your settings. |
| **Notifications** (top right, below the panel) | Pop-up messages ("toasts") from apps |

### Home menu

![The home menu](images/menu.png)

* **Apps**: click an icon to open the app.
* **Notifications**: everything you've been notified about since you logged in, newest first. Click × to remove one, or **Clear** to remove all.

  ![The Notifications tab](images/notifications.png)

* **Power button** (red): log out and return to the login window.

The menu closes when you click anywhere else, or click **Apps** again.

## Windows

Every app opens in its own window, so moving, resizing, maximizing and minimizing work as they do for any program. App windows stay in front of the desktop.

The desktop itself fills the screen when you log in (JavaFX 2.2 can't maximize a window, so Skylight sizes it to the screen's usable area). Closing it logs you out.

* Click an app's button in the **window list** to hide its window; click again to bring it back.
* Right-click a window-list button for **Close**.

  ![Closing from the window list](images/taskbar-menu.png)

## Dialogs

When an app asks a question, a small window appears over the app's window. Until you answer, that app's window doesn't respond; the rest of Skylight carries on. Enter answers **OK** and Escape **Cancel**.

![A dialog](images/dialog.png)

## Notifications

A notification appears as a toast at the top right for a few seconds (choose how long in Settings), and stays in the Notifications tab until you remove it or log out. Click a toast's × to dismiss it early.

## Built-in apps

### Terminal

![The Terminal](images/terminal.png)

Runs **JavaScript** using the engine built into Java: **Rhino** on Java 7 (Nashorn if you run Skylight on Java 8). Type in the top box and click **Run**; what the code prints (with `print()`), its result, and any error appear below. The variable `app` is the Terminal itself, so the [App API](app-api.md) is available, and JavaScript functions work as callbacks:

```javascript
app.postNotification('Hello', 'From the Terminal');
app.openWindow('apps/settings/');
app.openDialog('Are you sure?', function () { print('You clicked OK'); });
app.listFiles();
```

JavaScript can also reach any Java class, for example `java.lang.System.getProperty('java.version')`. Rhino and Nashorn format some results differently (`6 * 7` shows `=> 42.0` with Rhino and `=> 42` with Nashorn) and word their error messages differently.

### Settings

![Settings](images/settings.png)

* **Background**: click a picture. The desktop changes at once and remembers it.
* **Notifications**: drag the slider to choose how many seconds toasts stay on screen (1 to 60), then click **Save**.

### Web Browser

![The Web Browser](images/web-browser.png)

A real web browser built on JavaFX 2.2's WebView. Type an address and press Enter (`http://` is added if you leave it out); use the arrows to go back and forward and ↻ to reload. The address bar follows the links you click.

JavaFX 2.2's web engine is a WebKit from 2012, so many modern websites won't display correctly, and sites that require recent HTTPS settings may not load at all.
