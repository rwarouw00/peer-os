## Open File Dialog

> **This function is implemented in Prealpha A2 and later.**

The Open File Dialog is a small file browser with which users can select files from the server to open.  It has a counterpart in the [save file dialog](Save-File-Dialog.md).

```
var path = App.selectFileForOpen();
path.then(function(file) {
    var f = App.openFile(file);
});
```

> **In `siteroot`:** there is no Open File Dialog and no `App.selectFileForOpen`. Apps have to know the path of the file they want and pass it to [App.openFile](App.openFile.md). The closest thing in the code is the [Save File Dialog](Save-File-Dialog.md), whose file browser could be reused for opening.

---

_Source: [Open File Dialog](https://sourceforge.net/p/skylightos/wiki/Open%20File%20Dialog/) on the SourceForge wiki · Labels: file I/O · Last modified 2014-03-11_

_Updated after reviewing the `siteroot` code._
