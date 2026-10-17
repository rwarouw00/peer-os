## App.registerError

App.registerError creates a system wide error, that is reportable to the developer of the app and to the developer of Skylight OS.  It should only be used for very severe errors.  It accepts two arguments: App name, and error text.

```
App.registerError('MyApp', 'This is the error description.');
```

---

_Source: [App.registerError](https://sourceforge.net/p/skylightos/wiki/App.registerError/) on the SourceForge wiki · Labels: error handling, app object · Last modified 2014-01-22_
