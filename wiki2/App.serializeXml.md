## App.serializeXml

This function serializes a DOMDocument into an XML string.

```
var xmlstring = App.serializeXml(DOMDocument);
```

> **In `siteroot`:** the Settings app uses the three XML helpers together to change one value and save it:
>
> ```
> var settings = App.parseXml(App.openFile('../../../system/settings/' + App.getUsername() + '.xml'));
> settings.getElementsByTagName('background')[0].setAttribute('src', 'system/images/desktop/desktop_1.jpg');
> App.saveFile('system/settings/' + App.getUsername() + '.xml', App.serializeXml(settings));
> ```

---

_Source: [App.serializeXml](https://sourceforge.net/p/skylightos/wiki/App.serializeXml/) on the SourceForge wiki · Labels: xml manipulation, app object · Last modified 2014-01-22_

_Updated after reviewing the `siteroot` code._
