# Compared with FXML

JavaFX has its own XML for windows, **FXML**, and this repository has a Skylight OS version built on it ([site11fxml](../wiki11fxml/README.md)): FXML for windows, JavaScript (Nashorn) for their code, on Java 8. myxul uses the same Java 8, JavaFX and Nashorn, but reads XUL. The design spec's reason (section 2) is abstraction: XUL describes an application, FXML describes JavaFX objects.

## The same window, twice

```xml
<!-- FXML: JavaFX's classes and properties -->
<BorderPane xmlns:fx="http://javafx.com/fxml">
    <top>
        <ToolBar>
            <Button text="Save" onAction="save()"/>
            <Region HBox.hgrow="ALWAYS"/>
            <Label fx:id="status"/>
        </ToolBar>
    </top>
    <center>
        <TextArea fx:id="note" wrapText="true"/>
    </center>
    <fx:script source="notes.js"/>
</BorderPane>
```

```xml
<!-- XUL, in myxul: XUL's own elements -->
<window xmlns="http://www.mozilla.org/keymaster/gatekeeper/there.is.only.xul">
    <script src="notes.js"/>
    <toolbar>
        <toolbarbutton label="Save" oncommand="save()"/>
        <spacer flex="1"/>
        <label id="status" value=""/>
    </toolbar>
    <textbox id="note" multiline="true" flex="1"/>
</window>
```

FXML's tags are JavaFX classes and its attributes their properties, so an FXML file is tied to JavaFX, and anything in JavaFX can be used without writing any code. XUL's tags are its own fixed set, mapped to JavaFX by myxul's registry. The same file could be drawn by another toolkit, but each tag needs a builder.

## Side by side

| | FXML (site11fxml) | XUL (myxul) |
| --- | --- | --- |
| Vocabulary | JavaFX's classes: any `Node`, and your own classes | XUL's elements: the ones myxul's registry knows |
| Layout | JavaFX panes: `BorderPane`, `HBox` with `HBox.hgrow`, `GridPane`, ... | XUL boxes with `flex` weights, `align`, `pack`, splitters |
| Events | `onAction="save()"`, JavaFX's event names | `oncommand`, `onclick`, `oninput`, ... XUL's |
| Script access | `fx:id` makes a script variable of the JavaFX object | `document.getElementById`, with XUL's properties (`value`, `checked`) and the JavaFX object's methods |
| Shared actions | Code | `<command>` and `<key>` elements |
| Styles | JavaFX CSS (`-fx-...`), inline or in stylesheets | Web CSS inline, translated; no stylesheets |
| Script engine | One Nashorn engine per FXML load, with full Java access | One per window, limited to JavaFX's UI classes, with files through services |
| Parser | JavaFX's `FXMLLoader` | myxul's own, about 5,200 lines in all |
| Error messages | `FXMLLoader`'s exceptions | The file, line and reason for every mistake |

## Skylight OS's apps, from FXML to XUL

The Terminal, Settings and Web Browser were ported from site11fxml's FXML versions, keeping what they do:

| App | FXML (site11fxml), non-blank lines | XUL (myxul), non-blank lines | What changed |
| --- | --- | --- | --- |
| Terminal | 47 (`app.fxml`, `terminal.js`) | 61 (`terminal.xul`, `terminal.js`) | The same run logic (indirect `eval`, `print` captured). The XUL version adds Ctrl+Enter and Ctrl+L as `<key>`s, and a start-up sample |
| Settings | 62 (`app.fxml` with its script inside) | 70 (`settings.xul`, `settings.js`) | `ToggleButton`s in a `ToggleGroup` became `<toolbarbutton type="checkbox">`s kept in step by the script; the `Spinner` became a text box checked by the script and by `SettingsService`; a **Try one** button. The background is the launcher's and myxul's desktop's |
| Web Browser | 40 (`app.fxml` with its script inside) | 90 (`browser.xul`, `browser.js`) | FXML's script used the `WebEngine` directly; the XUL version uses `<browser>`'s XUL methods and events, and adds Stop, Home, keys, a status bar, buttons that disable when there's no history, and the page title in the window's |

FXML's scripts can reach any Java class, which kept them short (the Web Browser's listens to the `WebEngine` itself); myxul's reach Java only through elements and services, so a little more is written out, and more was added.

## Skylight OS's desktop, in Java and in XUL

site11fxml's desktop is a Java class, `Desktop`, controlling four FXML files. myxul's is a XUL file and its script, with a Java service for what scripts can't do (framing other windows):

| | site11fxml | myxul |
| --- | --- | --- |
| Written in | `Desktop.java` (488 non-blank lines) and four FXML files (80) | `ui/desktop.xul` (57) and `ui/desktop.js` (268), and `DesktopService.java` (518) |
| Logging in | A login window, users and passwords, and a logout button | None: myxul runs as the user who started it, and the clock shows their name |
| App windows | Each in a window with the system's frame, owned by the desktop | Each in a window drawn without the system's frame, in a Skylight frame (title bar, minimize, maximize, close, edges to resize by), kept on the desktop |
| Taskbar | A toggle button per window: hide or show it; right-click to close | A button per window: bring it to the front, or minimize the front one; right-click to close |
| Home menu | A `Popup`: apps, notifications, and logout | A `<panel>`: apps (right-click to add a shortcut), notifications, and a power button that closes everything |
| Shortcuts | Per user; drag, double-click, right-click to remove | Shared by everyone using myxul; drag, double-click, right-click to open or remove, and added from the home menu |
| Apps | `apps/apps.txt` and each app's `app.properties` | The same: `apps/apps.txt` and an `app.properties` per app |

## When to use which

FXML is the better choice for a JavaFX application that wants all of JavaFX, its own controls and Scene Builder. myxul suits what the spec is after: application windows described in an open, toolkit-independent language, with XUL's box model, commands and keys, written as plain files that need no compiling.

See also the [Notes app across every version](../wikicompare/README.md) of Skylight OS.
