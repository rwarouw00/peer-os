## Open File Dialog
> **This function is implemented in Prealpha A2 and later.**

The Open File Dialog is a small file browser with which users can select files from the server to open.  It has a counterpart in the [save file dialog](Save-File-Dialog.md).

```
var path = App.selectFileForOpen();
path.then(function(file) {
    var f = App.openFile(file);
});
```

---

_Source: [Open File Dialog](https://sourceforge.net/p/skylightos/wiki/Open%20File%20Dialog/) on the SourceForge wiki · Labels: file I/O · Last modified 2014-03-11_
