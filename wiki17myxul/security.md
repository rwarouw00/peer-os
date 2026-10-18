# Security

* **XUL files are programs.** A XUL file's scripts run inside myxul's Java process, so opening a XUL file is running a program. Only open XUL files you trust.
* **Scripts are limited, but not sandboxed.** Each window's Nashorn engine has a class filter: `Java.type` can name only JavaFX's UI classes (not `javafx.stage`, `javafx.scene.web` or `javafx.scene.media`). `java.lang.System.exit()`, `new Packages.java.io.File(...)` and `Java.type('java.lang.Runtime')` all fail with `ClassNotFoundException`, and `getClass().forName(...)` with `Java reflection not supported when class filter is present`; the tests try each. Scripts reach files only through the services their XUL file asks for. But:
  * the services are powerful: `NativeFileService` reads and writes any file the user can;
  * a XUL file chooses its own services, so the limit is on what myxul's services can do, not on which ones a file may ask for;
  * scripts can still call any public method of the Java objects they're given, including the JavaFX controls, and through them reach other objects (a control's scene, its window).
* **Services are only myxul's own.** `<?xul-service?>` can name only classes in the `myxul.service` package that implement `XulService`, so a XUL file can't make an arbitrary Java object (`<?xul-service F java.io.File?>` is refused).
* **No DOCTYPEs.** The XML parser refuses DOCTYPE declarations, so a XUL file can't use external entities to pull another file's contents into the window (`<!ENTITY secret SYSTEM "file:///etc/hostname">` is refused), and has secure processing on.
* **Web pages run in WebKit, not in myxul.** A page shown in a `<browser>` runs its JavaScript in JavaFX's `WebView`, which can't reach the XUL window's scripts or services. The XUL window's scripts can reach the page (through the `WebView`), not the other way round. A `<browser>` can show any URL it's given, including `file:` URLs of the user's files.
* **Settings are shared.** Every app with the `Settings` service can change the launcher's background and the notification time, though only to the values Settings allows.
* **Errors are shown, not hidden**, including the file and line, which can reveal a path.
* **Java 8 and JavaFX 8 are old.** Use an up-to-date Java 8 build (OpenJDK 8 still gets security updates from several vendors); the test machine had 8u504.
