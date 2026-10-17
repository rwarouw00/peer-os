# Skylight OS for C# 5 and Windows Forms

Skylight OS rebuilt as a Windows desktop program in **C# 5**, compiled by the `csc.exe` that is already part of Windows 11 (.NET Framework 4.8). There is nothing to install: no Visual Studio, no SDK, no NuGet packages. The UI is Windows Forms, so it looks like a classic Windows program. Documentation: [`../wiki6cs`](../wiki6cs/README.md).

## Quick start (Windows 11)

In a Command Prompt, in this folder:

```
build.bat
AddUser.exe admin choose-a-password
run.bat
```

`build.bat` finds `csc.exe` in `%WINDIR%\Microsoft.NET\Framework64\v4.0.30319\`. On Linux or macOS with Mono, use `./build.sh` and `./run.sh` instead (`mono AddUser.exe admin choose-a-password`).

## Layout

```
site6cs\
├── src\Skylight\          Program, LoginForm, Desktop, DesktopArea, App, Settings, Storage
│   └── Apps\              TerminalApp, SettingsApp, WebBrowserApp
├── src\AddUser\           AddUser.cs: the add-user tool
├── apps\                  apps.json, and one folder per app (app.json, icons\)
├── images\                backgrounds, home and power icons
├── data\                  users, settings and user files (created as needed)
├── build.bat / build.sh   compile src\ into Skylight.exe and AddUser.exe
└── run.bat / run.sh       start Skylight OS
```
