# UI components

## Styles

Include the shared stylesheet in your app so it matches the rest of the system:

```html
<link href="../../../system/css/ui.css" rel="stylesheet"/>
```

[`ui.css`](../siteroot/system/css/ui.css) sets the font to Arial and styles the following. No classes are needed for buttons and text boxes.

| Element | Look |
| --- | --- |
| `<button>`, `<input type="button">`, `submit`, `reset` | Light grey gradient, 1px grey border, rounded corners, 10pt text. The gradient flips while pressed. |
| `<input type="text">`, `<input type="password">` | Grey rounded border with a slight inner shadow. |
| `.barEndLeft`, `.barMiddle`, `.barEndRight` | Joined into a segmented button bar (see below). |

![Buttons in a dialog](images/dialog.png)

### Button bar

Put the classes on adjacent inline elements, with no whitespace between them:

```html
<span class="barEndLeft" onclick="history.go(-1);">&lt;</span><span class="barMiddle">Home</span><span class="barEndRight" onclick="history.go(1);">&gt;</span>
```

There are no other components: no lists, tabs or menus for apps. Use your own CSS for anything else.

## Dialogs

Ask the user to confirm something with [`App.openDialog`](app-api.md#appopendialog). The dialog is a 300×200 window with your message (HTML allowed) and **OK** and **Cancel**. Only OK runs your code, which is given as a string and runs in the dialog's window. It isn't modal: the user can keep using other windows while it is open.

## Notifications

Tell the user something happened with [`App.postNotification`](app-api.md#apppostnotification). Use a short header (20 characters show in the toast) and a body (110 characters show). The full text is kept in the home menu's Notifications tab until the user removes it or reloads. The toast stays on screen for the time in the user's settings (7 seconds by default).

![The Notifications tab](images/notification-tray.png)

## Windows

Your app's page fills the window below the 30px title bar. The window's size comes from your [manifest](app-manifest.md) and can only change when the user maximizes it, in which case the page grows to the screen's size. Handle `window.onresize` if your layout depends on size:

```javascript
window.onresize = function () {
	editor.style.height = window.innerHeight - 70 + 'px';
};
```
