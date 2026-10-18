# Windows Forms primer

A short guide to the C# 5 and Windows Forms ideas Skylight uses. Microsoft's .NET Framework 4.8 documentation for `System.Windows.Forms` covers the rest.

## C# 5, the language csc.exe understands

The compiler in Windows' .NET Framework folder stops at **C# 5** (2012). Everything Skylight needs is there:

| Available in C# 5 | Example |
| --- | --- |
| Object initializers | `new Button { Text = "Save", AutoSize = true }` |
| Lambdas and anonymous methods | `() => Save()`, `delegate { Save(); }` |
| `var`, generics, LINQ | `names.Where(n => n.EndsWith(".txt")).ToList()` |
| Auto-properties | `public string User { get; private set; }` |
| `??` | `ReadFile("notes.txt") ?? ""` |

Newer features that **don't** compile, with the C# 5 way:

| C# 6 and later | C# 5 |
| --- | --- |
| `window?.Close();` | `if (window != null) window.Close();` |
| `$"Hello, {name}"` | `"Hello, " + name` or `string.Format("Hello, {0}", name)` |
| `nameof(Save)` | `"Save"` |
| `public int Width { get; } = 500;` | set it in the constructor |
| `int Area() => w * h;` | `int Area() { return w * h; }` |
| `if (int.TryParse(s, out int n))` | declare `int n;` first |
| `catch (IOException e) when (...)` | test inside the `catch` |

`build.sh` passes `-langversion:5` to Mono's compiler so the same rules apply on Linux.

## Controls and layout

A window is a `Form`; inside it is a tree of **controls**. Containers hold other controls in their `Controls` collection.

| Need | Windows Forms |
| --- | --- |
| Fill a window, or stick to one edge | `Dock = DockStyle.Fill` / `Top` / `Right` ... (controls dock in **reverse** order of adding) |
| Keep a fixed distance from edges as the parent resizes | `Anchor` |
| Rows and columns | `TableLayoutPanel` (the login window) |
| A flowing row or column | `FlowLayoutPanel` (the home menu, Settings) |
| Free positioning (the desktop) | A `Panel` with each control's `Location` set |
| Two resizable panes | `SplitContainer` (the Terminal) |
| Text | `Label`, `TextBox` (`Multiline`, `UseSystemPasswordChar`) |
| Buttons | `Button`; `CheckBox` with `Appearance = Appearance.Button` for a toggle |
| Numbers | `NumericUpDown` |
| Toolbars and menus | `ToolStrip`, `ToolStripButton`, `ToolStripDropDown`, `ContextMenuStrip` |
| Questions | `MessageBox.Show(owner, text, title, buttons, icon)` |
| Web pages | `WebBrowser` |
| Repeating work | `System.Windows.Forms.Timer` (runs on the UI thread) |

`AcceptButton` and `CancelButton` on a form choose the buttons that Enter and Escape press.

## Events

```csharp
button.Click += delegate { Save(); };                    // ignore the event's arguments
note.TextChanged += (sender, e) => changed = true;       // or take them
icon.MouseDown += (sender, e) => { if (e.Button == MouseButtons.Left) ... };
```

## Drawing your own control

Skylight's desktop shortcuts are a small custom control: a class extending `Control` that overrides `OnPaint` and draws with `e.Graphics` (`DrawImage`, `DrawString`). The desktop itself extends `Panel` and draws its background in `OnPaintBackground`. `DoubleBuffered = true` stops flicker.

## Owned windows

`window.Show(owner)` makes `owner` the window's owner: an owned window stays in front of its owner and is minimized with it. Skylight's app windows and toasts are owned by the desktop, and Skylight sets `ShowInTaskbar = false` so they don't crowd the Windows taskbar.

## Compiling code at run time

The Terminal uses `Microsoft.CSharp.CSharpCodeProvider`, which runs the framework's `csc.exe` behind the scenes:

```csharp
CompilerParameters options = new CompilerParameters { GenerateInMemory = true };
options.ReferencedAssemblies.Add("System.dll");
CompilerResults compiled = new CSharpCodeProvider().CompileAssemblyFromSource(options, source);
```

`compiled.Errors` lists any errors with line numbers; otherwise `compiled.CompiledAssembly` holds the new code, called with reflection.

## JSON

The .NET Framework's `System.Web.Script.Serialization.JavaScriptSerializer` (in `System.Web.Extensions.dll`) reads and writes JSON with no extra library:

```csharp
JavaScriptSerializer json = new JavaScriptSerializer();
string text = json.Serialize(settings);
Settings back = json.Deserialize<Settings>(text);
Dictionary<string, object> any = json.Deserialize<Dictionary<string, object>>(text);
```

It maps JSON objects to classes by property name, or to `Dictionary<string, object>` when you don't have a class (numbers become `int`, `long` or `decimal`; arrays become `ArrayList`).
