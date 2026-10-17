## Save File Dialog
> **This function has not yet been implemented.  Do not use it for building apps.**

The Save File Dialog is a small file browser with which users can select files from the server to save to.  It accepts one argument.  It has a counterpart in the [open file dialog](Open-File-Dialog.md).

```
var path = App.selectFileForSave();
path.then(function(file) {
    App.saveFile(file, contents);
});
```

---

_Source: [Save File Dialog](https://sourceforge.net/p/skylightos/wiki/Save%20File%20Dialog/) on the SourceForge wiki · Labels: app object, file I/O · Last modified 2014-03-11_
