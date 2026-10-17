# Desktop internals

The `Desktop` class ([`Desktop.cs`](../site6cs/src/Skylight/Desktop.cs)) builds and runs the whole desktop. Apps reach it through the [App API](app-api.md); this page is for people changing Skylight OS itself.

## Sections

| Section | Members |
| --- | --- |
| Building the desktop | constructor, `TickClock` |
| Logging out | `Logout`, `LogoutCleanUp` |
| Images | `LoadImage`, `LoadIcon`, `CloseButton` |
| Settings, background and shortcuts | `ApplySettings`, `SaveSettings`, `RenderShortcuts`, `MakeInteractive`, `AddShortcut`, `RemoveShortcut` |
| Home menu | `BuildMenu`, `ShowMenuPage` |
| Notifications | `Notify`, `AddToTray`, `ShowToast`, `LayoutToasts`, `RemoveToast` |
| Dialogs | `OpenDialog` |
| Apps and windows | `ReadManifest`, `OpenWindow`, `LoadAppType`, `CloseWindow`, `RemoveWindow` |

## The control tree

```
Desktop : Form "Skylight OS" (maximized)
├── ToolStrip panel (Dock Top): Apps (ToolStripDropDownButton) | separator | window-list ToolStripButtons | clock ToolStripLabel (right)
└── DesktopArea : Panel (Dock Fill), background drawn in OnPaintBackground
    └── ShortcutIcon : Control per shortcut, transparent, at its saved Location

ToolStripDropDown menu (attached to the Apps button)
└── ToolStripControlHost
    └── Panel 430×470
        ├── apps page: FlowLayoutPanel of Buttons
        ├── notifications page: FlowLayoutPanel (Clear) above a FlowLayoutPanel of rows
        └── footer: Apps | Notifications | power button

Toast : Form per notification (no border, shown without activation), owned by the desktop
Form per app window, owned by the desktop
MessageBox per dialog, owned by the app's window
```

## State

| Field | Holds |
| --- | --- |
| `User`, `Settings` | The logged-in user and their settings |
| `windows` | AppID → `AppWindow` (app, form, window-list button) |
| `manifests` | App path → parsed `app.json` (cached; `null` for broken apps) |
| `toasts` | Toasts on screen, top to bottom |

## Details worth knowing

* **The login–desktop cycle** lives in `SkylightContext` ([`Program.cs`](../site6cs/src/Skylight/Program.cs)), not in `Desktop`: closing the desktop for any reason (power button, the window's ×, Alt+F4) logs out.
* **The home menu** is a `ToolStripDropDown`, which closes itself when the user clicks elsewhere. Some Windows Forms implementations (Mono's) reopen it straight away when that click lands on the Apps button, so `BuildMenu` notes when the menu was closed by a click on the button and cancels the reopening that follows within 400 ms.
* **Shortcuts** draw their own icon and shadowed label (`ShortcutIcon.OnPaint`) on a transparent background, so the desktop picture shows through. Dragging follows `Cursor.Position`; the position is saved on mouse-up only if the shortcut moved. Saving re-creates all shortcuts, so it's posted with `BeginInvoke` to run after the mouse event finishes.
* **Toasts** are borderless forms that override `ShowWithoutActivation`, so they don't take the focus. Each has its own `Timer`.
* **App windows** are owned by the desktop (`Show(this)`), with `ShowInTaskbar = false`. `FormClosing` asks the app's `CanClose()` only when the user is closing the window; `CloseWindow` removes the window from the list first, so it closes without asking.
* **Dialogs** are `MessageBox.Show`, which is modal: it returns the answer, and `OpenDialog` runs `onOK` straight after.
* **Logging out** stops the clock, closes all app windows (without asking), removes the toasts and disposes the menu.
* **App classes** come from `Skylight.exe`, or from `apps\<name>\<assembly>` via `Assembly.LoadFrom`. The DLL references `Skylight.exe`, which is already loaded, so both share the same `Skylight.App` type.

## Extending the desktop

**A new App method that needs the desktop:** add an `internal` method to `Desktop` and a `public` one to `App` that calls it:

```csharp
// Desktop.cs
internal void SetWindowTitle(string appID, string title)
{
	AppWindow window = windows[appID];
	window.Form.Text = title;
	window.Button.Text = title;
}

// App.cs
public void SetTitle(string title)
{
	Desktop.SetWindowTitle(AppID, title);
}
```

**A new setting:** add a property to `Settings` (and to `Copy()`, and a default in the constructor), check it in `Storage.CheckSettings`, and apply it in `Desktop.ApplySettings()`. `JavaScriptSerializer` saves and loads the new property automatically.
