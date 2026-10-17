# Limitations

## Behaviour

* **Single user, single computer.** There's no server; each person runs their own copy.
* **Notifications** are kept only while Skylight is running.
* **Long-running code freezes the desktop.** Everything runs on the Windows Forms UI thread; slow work should go on a background thread (see [App API](app-api.md#threads)).
* **An endless loop in the Terminal** freezes Skylight, since its code runs on the same thread. Close Skylight with Task Manager.
* **Each Terminal run stays loaded.** The .NET Framework can't unload a compiled snippet, so thousands of runs slowly use more memory until Skylight restarts.
* **Dialogs are modal**: while one is open, the rest of Skylight waits for an answer.
* **Apps are loaded once.** After rebuilding an `app.dll`, restart Skylight.
* **The Web Browser** uses Internet Explorer 11's engine, which many current websites no longer support. A modern engine (Microsoft's WebView2) needs a NuGet package and a newer C# than Windows' built-in compiler, so it was left out.

## Tied to C# 5 and the .NET Framework

The code is written for the compiler that comes with Windows, as requested. It also compiles with any newer C# compiler targeting the .NET Framework. Moving it to modern .NET (6 or later) would need:

* a project file (`.csproj`) with `<UseWindowsForms>true</UseWindowsForms>`, built with the .NET SDK;
* a replacement for `JavaScriptSerializer`, such as `System.Text.Json`;
* a replacement for `CSharpCodeProvider` in the Terminal (it isn't supported on modern .NET), such as the Roslyn compiler packages;
* a different web engine, since the `WebBrowser` control exists only on Windows.

## How it was tested

There was no Windows machine available. Skylight was built and tested on Linux with **Mono 6.8**, which runs the same `Skylight.exe`:

* compiled with Mono's `mcs -langversion:5`, which rejects anything newer than C# 5, as Windows' `csc.exe` does;
* run on a virtual X display with **metacity** (GNOME's window manager).

The tests used the real login window (including a wrong password) and drove the desktop with simulated mouse and keyboard input:

* the home menu, opened and closed both ways;
* opening apps from the menu and from desktop shortcuts;
* the Terminal compiling and running C#, including a dialog whose **OK** ran a lambda typed in the Terminal;
* closing a toast;
* desktop shortcuts: double-clicking, dragging and removing;
* the window list: hiding, showing and closing;
* logging out and quitting.

An automated driver also checked every App API method, compile errors and exceptions in the Terminal, settings validation, file storage (including a refused `../` file name), apps that fail to open, toasts expiring, and loading an app from its own `app.dll` (the Notes tutorial, which was also tested by hand: save, reopen, unsaved-changes prompt, Clear). Passwords were checked against Python's PBKDF2-SHA256. The screenshots in this documentation come from those runs.

What wasn't tested:

* **Windows itself.** On Windows 11, Skylight looks different from the screenshots (native controls, Segoe UI, Windows 11 title bars), and `build.bat` and `run.bat` weren't run.
* **The Web Browser's engine.** Mono's `WebBrowser` control needs an engine that is rarely installed, so on Mono Skylight opens addresses in the system's browser instead. The Internet Explorer 11 path, including the registry setting that selects IE 11 mode, is Windows-only and untested.
* **Windows' own compiler for Terminal code.** On Windows, `CSharpCodeProvider` runs `csc.exe` from the framework folder; on Mono it ran Mono's compiler.
