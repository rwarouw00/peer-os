## App.parseXml

This function parses an XML string into a DOMDocument.

```
var doc = App.parseXml(xml);
```

> **In `siteroot`:** if the XML is invalid, a [Dialog](Dialogs.md) appears saying "Skylight OS has encountered a problem" with the parser's error, and the returned document contains a `<parsererror>` element instead of your data. Check for it before using the result:
>
> ```
> var doc = App.parseXml(xml);
> if (doc.getElementsByTagName('parsererror').length) { /* invalid XML */ }
> ```
>
> It is normally paired with [App.openFile](App.openFile.md) to read a file and [App.serializeXml](App.serializeXml.md) to turn the document back into text.

---

_Source: [App.parseXml](https://sourceforge.net/p/skylightos/wiki/App.parseXml/) on the SourceForge wiki · Labels: xml manipulation, app object · Last modified 2014-01-22_

_Updated after reviewing the `siteroot` code._
