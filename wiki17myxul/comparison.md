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
| Parser | JavaFX's `FXMLLoader` | myxul's own, about 3,000 lines in all |
| Error messages | `FXMLLoader`'s exceptions | The file, line and reason for every mistake |

## When to use which

FXML is the better choice for a JavaFX application that wants all of JavaFX, its own controls and Scene Builder. myxul suits what the spec is after: application windows described in an open, toolkit-independent language, with XUL's box model, commands and keys, written as plain files that need no compiling.

See also the [Notes app across every version](../wikicompare/README.md) of Skylight OS.
