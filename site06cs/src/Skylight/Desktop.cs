using System;
using System.Collections.Generic;
using System.Drawing;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Web.Script.Serialization;
using System.Windows.Forms;

namespace Skylight
{
	/// <summary>
	/// The Skylight OS desktop: top panel (Apps menu, window list, clock), background
	/// and shortcuts, home menu, notifications, dialogs and app windows.
	/// </summary>
	public class Desktop : Form
	{
		/// <summary>One open app: the app object, its window and its window-list button.</summary>
		class AppWindow
		{
			public App App;
			public Form Form;
			public ToolStripButton Button;
		}

		public string User { get; private set; }
		public Settings Settings { get; private set; }

		readonly ToolStrip panel = new ToolStrip { GripStyle = ToolStripGripStyle.Hidden, Padding = new Padding(4, 2, 4, 2) };
		readonly ToolStripDropDownButton appsButton = new ToolStripDropDownButton("Apps");
		readonly ToolStripLabel clock = new ToolStripLabel { Alignment = ToolStripItemAlignment.Right };
		readonly DesktopArea area = new DesktopArea { Dock = DockStyle.Fill };
		readonly Timer clockTimer = new Timer { Interval = 1000 };
		readonly Dictionary<string, AppWindow> windows = new Dictionary<string, AppWindow>();
		readonly Dictionary<string, Dictionary<string, object>> manifests = new Dictionary<string, Dictionary<string, object>>();
		readonly List<Toast> toasts = new List<Toast>();
		ToolStripDropDown menu;
		DateTime menuClosedByButton;   // see BuildMenu()
		Control appsPage;
		Control notificationsPage;
		FlowLayoutPanel notificationList;
		int nextAppID = 1;

		public Desktop(string user)
		{
			User = user;
			Settings = Storage.LoadSettings(user);
			Text = "Skylight OS";
			Icon = LoadIcon("images/skylight.png");
			Size = new Size(1024, 700);
			WindowState = FormWindowState.Maximized;

			appsButton.Image = LoadImage("images/home.png", 20);
			appsButton.ShowDropDownArrow = false;
			panel.Items.Add(appsButton);
			panel.Items.Add(new ToolStripSeparator());
			panel.Items.Add(clock);
			Controls.Add(area);
			Controls.Add(panel);
			BuildMenu();
			ApplySettings();

			clockTimer.Tick += delegate { TickClock(); };
			TickClock();
			clockTimer.Start();
			FormClosing += delegate { LogoutCleanUp(); };
		}

		void TickClock()
		{
			clock.Text = DateTime.Now.ToString("h:mm tt").ToLowerInvariant() + "   " + User;
		}

		// ---- Logging out ----

		/// <summary>Closing the desktop logs out; Program then shows the login window.</summary>
		public void Logout()
		{
			Close();
		}

		void LogoutCleanUp()
		{
			clockTimer.Stop();
			foreach (string appID in windows.Keys.ToList())
			{
				CloseWindow(appID);
			}
			foreach (Toast toast in toasts.ToList())
			{
				RemoveToast(toast);
			}
			menu.Close();
			menu.Dispose();
		}

		// ---- Images ----

		public static Image LoadImage(string relativePath, int size)
		{
			using (Image original = Image.FromFile(Storage.HomePath(relativePath)))
			{
				return new Bitmap(original, size, size);
			}
		}

		public static Icon LoadIcon(string relativePath)
		{
			using (Bitmap bitmap = new Bitmap(Storage.HomePath(relativePath)))
			{
				return Icon.FromHandle(bitmap.GetHicon());
			}
		}

		/// <summary>A small flat × button, used by notifications.</summary>
		public static Button CloseButton()
		{
			Button button = new Button { Text = "×", FlatStyle = FlatStyle.Flat, Size = new Size(24, 24), TabStop = false };
			button.FlatAppearance.BorderSize = 0;
			return button;
		}

		// ---- Settings, background and shortcuts ----

		void ApplySettings()
		{
			area.SetBackground(Storage.HomePath(Settings.Background));
			RenderShortcuts();
		}

		/// <summary>Checks, saves and applies settings. Returns false, after telling the user why, if they were refused.</summary>
		public bool SaveSettings(Settings changed)
		{
			string error = Storage.SaveSettings(User, changed);
			if (error != null)
			{
				Notify("Settings not saved", error);
				return false;
			}
			Settings = changed.Copy();
			ApplySettings();
			return true;
		}

		void RenderShortcuts()
		{
			foreach (Control old in area.Controls.Cast<Control>().ToList())
			{
				old.Dispose();
			}
			foreach (Shortcut shortcut in Settings.Shortcuts)
			{
				Dictionary<string, object> manifest = ReadManifest(shortcut.Path);
				if (manifest == null)
				{
					continue;
				}
				ShortcutIcon icon = new ShortcutIcon(shortcut.Path, (string)manifest["title"], LoadImage(shortcut.Path + Icons(manifest)["64"], 64));
				icon.Location = new Point(shortcut.X, shortcut.Y);
				MakeInteractive(icon);
				area.Controls.Add(icon);
			}
		}

		void MakeInteractive(ShortcutIcon icon)
		{
			Point offset = Point.Empty;
			bool pressed = false;
			bool moved = false;
			icon.MouseDown += (sender, e) =>
			{
				if (e.Button == MouseButtons.Left)
				{
					pressed = true;
					moved = false;
					offset = e.Location;
				}
			};
			icon.MouseMove += (sender, e) =>
			{
				if (pressed && e.Button == MouseButtons.Left)
				{
					Point position = area.PointToClient(Cursor.Position);
					icon.Location = new Point(Math.Max(0, position.X - offset.X), Math.Max(0, position.Y - offset.Y));
					moved = true;
				}
			};
			icon.MouseUp += (sender, e) =>
			{
				pressed = false;
				if (moved)
				{
					Settings changed = Settings.Copy();
					Shortcut shortcut = changed.Shortcuts.First(s => s.Path == icon.AppPath);
					shortcut.X = icon.Left;
					shortcut.Y = icon.Top;
					BeginInvoke(new Action(() => SaveSettings(changed)));   // after the mouse event finishes
				}
			};
			icon.DoubleClick += delegate { OpenWindow(icon.AppPath, new Dictionary<string, string>()); };
			ContextMenuStrip shortcutMenu = new ContextMenuStrip();
			shortcutMenu.Items.Add("Remove from desktop", null, delegate { BeginInvoke(new Action(() => RemoveShortcut(icon.AppPath))); });
			icon.ContextMenuStrip = shortcutMenu;
		}

		public void AddShortcut(string path)
		{
			Settings changed = Settings.Copy();
			if (changed.Shortcuts.Any(s => s.Path == path))
			{
				return;
			}
			int count = changed.Shortcuts.Count;
			changed.Shortcuts.Add(new Shortcut { Path = path, X = 20 + 100 * (count / 6), Y = 20 + 100 * (count % 6) });
			SaveSettings(changed);
		}

		void RemoveShortcut(string path)
		{
			Settings changed = Settings.Copy();
			changed.Shortcuts.RemoveAll(s => s.Path == path);
			SaveSettings(changed);
		}

		// ---- Home menu ----

		void BuildMenu()
		{
			FlowLayoutPanel apps = new FlowLayoutPanel { Dock = DockStyle.Fill, Padding = new Padding(10), BackColor = SystemColors.Window };
			foreach (string path in Storage.AppList())
			{
				Dictionary<string, object> manifest = ReadManifest(path);
				if (manifest == null)
				{
					continue;
				}
				string appPath = path;
				Button app = new Button
				{
					Text = (string)manifest["title"],
					Image = LoadImage(path + Icons(manifest)["48"], 48),
					TextImageRelation = TextImageRelation.ImageAboveText,
					FlatStyle = FlatStyle.Flat,
					Size = new Size(96, 90)
				};
				app.FlatAppearance.BorderSize = 0;
				app.Click += delegate
				{
					menu.Close();
					OpenWindow(appPath, new Dictionary<string, string>());
				};
				apps.Controls.Add(app);
			}
			appsPage = apps;

			Button clear = new Button { Text = "Clear", AutoSize = true };
			clear.Click += delegate { notificationList.Controls.Clear(); };
			FlowLayoutPanel toolbar = new FlowLayoutPanel { Height = 34, Dock = DockStyle.Top, FlowDirection = FlowDirection.RightToLeft };
			toolbar.Controls.Add(clear);
			notificationList = new FlowLayoutPanel { Dock = DockStyle.Fill, FlowDirection = FlowDirection.TopDown, WrapContents = false, AutoScroll = true, BackColor = SystemColors.Window };
			Panel notifications = new Panel { Dock = DockStyle.Fill, Visible = false };
			notifications.Controls.Add(notificationList);
			notifications.Controls.Add(toolbar);
			notificationsPage = notifications;

			Button showApps = new Button { Text = "Apps", AutoSize = true };
			showApps.Click += delegate { ShowMenuPage(appsPage); };
			Button showNotifications = new Button { Text = "Notifications", AutoSize = true };
			showNotifications.Click += delegate { ShowMenuPage(notificationsPage); };
			Button logout = new Button { Image = LoadImage("images/power.png", 20), Size = new Size(32, 30), FlatStyle = FlatStyle.Flat, Dock = DockStyle.Right };
			logout.FlatAppearance.BorderSize = 0;
			logout.Click += delegate
			{
				menu.Close();
				BeginInvoke(new Action(Logout));
			};
			FlowLayoutPanel footerButtons = new FlowLayoutPanel { Dock = DockStyle.Fill };
			footerButtons.Controls.Add(showApps);
			footerButtons.Controls.Add(showNotifications);
			Panel footer = new Panel { Height = 40, Dock = DockStyle.Bottom, Padding = new Padding(4) };
			footer.Controls.Add(footerButtons);
			footer.Controls.Add(logout);

			Panel content = new Panel { Size = new Size(430, 470) };
			content.Controls.Add(apps);
			content.Controls.Add(notifications);
			content.Controls.Add(footer);

			// A drop-down closes itself when the user clicks anywhere else.
			menu = new ToolStripDropDown { Padding = Padding.Empty };
			menu.Items.Add(new ToolStripControlHost(content) { AutoSize = false, Size = content.Size, Margin = Padding.Empty, Padding = Padding.Empty });
			// Clicking the Apps button while the menu is open closes it, and must not open it again.
			menu.Closed += (sender, e) =>
			{
				if (e.CloseReason == ToolStripDropDownCloseReason.AppClicked && appsButton.Bounds.Contains(panel.PointToClient(Cursor.Position)))
				{
					menuClosedByButton = DateTime.Now;
				}
			};
			menu.Opening += (sender, e) =>
			{
				if ((DateTime.Now - menuClosedByButton).TotalMilliseconds < 400)
				{
					e.Cancel = true;
					return;
				}
				ShowMenuPage(appsPage);
			};
			appsButton.DropDown = menu;
		}

		void ShowMenuPage(Control page)
		{
			appsPage.Visible = page == appsPage;
			notificationsPage.Visible = page == notificationsPage;
		}

		// ---- Notifications ----

		public void Notify(string header, string body)
		{
			AddToTray(header, body ?? "");
			ShowToast(header, body ?? "");
		}

		void AddToTray(string header, string body)
		{
			Panel row = new Panel { Width = 390, Padding = new Padding(10, 6, 4, 6) };
			Label title = new Label { Text = header, AutoSize = true, MaximumSize = new Size(330, 0), Font = new Font(Font.FontFamily, 11, FontStyle.Bold), Location = new Point(10, 6) };
			row.Controls.Add(title);
			int bottom = title.Bottom;
			if (body.Length > 0)
			{
				Label message = new Label { Text = body, AutoSize = true, MaximumSize = new Size(330, 0), Location = new Point(10, title.Bottom + 2) };
				row.Controls.Add(message);
				bottom = message.Bottom;
			}
			Button remove = CloseButton();
			remove.Location = new Point(row.Width - 32, 4);
			remove.Click += delegate { row.Dispose(); };
			row.Controls.Add(remove);
			row.Height = bottom + 8;
			notificationList.Controls.Add(row);
			notificationList.Controls.SetChildIndex(row, 0);   // newest first
		}

		void ShowToast(string header, string body)
		{
			Toast toast = null;
			toast = new Toast(header, body, delegate { RemoveToast(toast); });
			toasts.Add(toast);
			LayoutToasts();
			toast.Show(this);
			Timer timer = new Timer { Interval = Settings.NotificationTime };
			timer.Tick += delegate
			{
				timer.Dispose();
				RemoveToast(toast);
			};
			timer.Start();
		}

		/// <summary>Stacks toasts down the right-hand side, just below the panel.</summary>
		void LayoutToasts()
		{
			Point corner = PointToScreen(new Point(ClientSize.Width - 320, panel.Bottom + 10));
			int y = corner.Y;
			foreach (Toast toast in toasts)
			{
				toast.Location = new Point(corner.X, y);
				y += toast.Height + 10;
			}
		}

		void RemoveToast(Toast toast)
		{
			if (toasts.Remove(toast))
			{
				toast.Close();
				LayoutToasts();
			}
		}

		// ---- Dialogs ----

		/// <summary>With onOK: OK and Cancel, and onOK runs on OK. Without: an OK button only.</summary>
		public void OpenDialog(string message, Action onOK, IWin32Window owner)
		{
			DialogResult result = MessageBox.Show(owner ?? this, message, "Skylight OS",
				onOK == null ? MessageBoxButtons.OK : MessageBoxButtons.OKCancel,
				onOK == null ? MessageBoxIcon.Information : MessageBoxIcon.Question);
			if (onOK != null && result == DialogResult.OK)
			{
				onOK();
			}
		}

		// ---- Apps and windows ----

		static Dictionary<string, object> Icons(Dictionary<string, object> manifest)
		{
			return (Dictionary<string, object>)manifest["icons"];
		}

		/// <summary>Reads apps\&lt;name&gt;\app.json, or returns null if it's missing or incomplete.</summary>
		Dictionary<string, object> ReadManifest(string path)
		{
			if (!Storage.IsAppPath(path))
			{
				return null;
			}
			if (!manifests.ContainsKey(path))
			{
				Dictionary<string, object> manifest = Storage.ReadJson<Dictionary<string, object>>(Storage.HomePath(path + "app.json"));
				bool complete = manifest != null && manifest.ContainsKey("title") && manifest.ContainsKey("class")
					&& manifest.ContainsKey("icons") && manifest["icons"] is Dictionary<string, object>
					&& new[] { "16", "32", "48", "64" }.All(Icons(manifest).ContainsKey);
				manifests[path] = complete ? manifest : null;
			}
			return manifests[path];
		}

		/// <summary>Opens the app in folder path (e.g. "apps/terminal/"). Returns its AppID, or null.</summary>
		public string OpenWindow(string path, Dictionary<string, string> args)
		{
			Dictionary<string, object> manifest = ReadManifest(path);
			App app;
			try
			{
				if (manifest == null)
				{
					throw new ArgumentException(path + " is not an app folder with a complete app.json.");
				}
				app = (App)Activator.CreateInstance(LoadAppType(path, manifest));
			}
			catch (Exception e)
			{
				Exception reason = e is TargetInvocationException && e.InnerException != null ? e.InnerException : e;
				Notify("Cannot open app", reason is ArgumentException ? reason.Message : reason.GetType().Name + ": " + reason.Message);
				return null;
			}
			string appID = (nextAppID++).ToString();

			Form window = new Form
			{
				Text = (string)manifest["title"],
				Icon = LoadIcon(path + Icons(manifest)["32"]),
				ShowInTaskbar = false,
				StartPosition = FormStartPosition.Manual,
				ClientSize = new Size(Number(manifest, "width", 500), Number(manifest, "height", 350))
			};
			app.Init(this, appID, path, manifest, window, args ?? new Dictionary<string, string>());
			Control content;
			try
			{
				content = app.Build();
			}
			catch (Exception e)
			{
				window.Dispose();
				Notify("Cannot open app", e.GetType().Name + ": " + e.Message);
				return null;
			}
			content.Dock = DockStyle.Fill;
			window.Controls.Add(content);
			// Centre it over the desktop.
			Rectangle bounds = RectangleToScreen(ClientRectangle);
			window.Location = new Point(bounds.Left + (bounds.Width - window.Width) / 2, bounds.Top + (bounds.Height - window.Height) / 2);
			window.FormClosing += (sender, e) =>
			{
				// The desktop closes windows by removing them from the list first.
				if (windows.ContainsKey(appID) && e.CloseReason == CloseReason.UserClosing && !app.CanClose())
				{
					e.Cancel = true;
				}
			};
			window.FormClosed += delegate { RemoveWindow(appID); };

			ToolStripButton button = new ToolStripButton((string)manifest["title"], LoadImage(path + Icons(manifest)["16"], 16)) { CheckOnClick = true, Checked = true };
			button.Click += delegate
			{
				if (button.Checked)
				{
					window.Show();
					window.Activate();
				}
				else
				{
					window.Hide();
				}
			};
			button.MouseUp += (sender, e) =>
			{
				if (e.Button == MouseButtons.Right)
				{
					ContextMenuStrip buttonMenu = new ContextMenuStrip();
					buttonMenu.Items.Add("Close", null, delegate { CloseWindow(appID); });
					buttonMenu.Show(Cursor.Position);
				}
			};
			panel.Items.Insert(panel.Items.IndexOf(clock), button);

			windows[appID] = new AppWindow { App = app, Form = window, Button = button };
			window.Show(this);   // owned by the desktop: stays in front of it
			window.Activate();
			return appID;
		}

		static int Number(Dictionary<string, object> manifest, string key, int fallback)
		{
			object value;
			return manifest.TryGetValue(key, out value) && value is int ? (int)value : fallback;
		}

		/// <summary>Built-in apps are in Skylight.exe; others bring their own DLL, named by "assembly" in app.json.</summary>
		static Type LoadAppType(string path, Dictionary<string, object> manifest)
		{
			Assembly assembly = Assembly.GetExecutingAssembly();
			object dll;
			if (manifest.TryGetValue("assembly", out dll))
			{
				assembly = Assembly.LoadFrom(Storage.HomePath(path + (string)dll));
			}
			Type type = assembly.GetType((string)manifest["class"], true);
			if (!typeof(App).IsAssignableFrom(type))
			{
				throw new ArgumentException((string)manifest["class"] + " doesn't extend Skylight.App.");
			}
			return type;
		}

		/// <summary>Closes a window without asking the app.</summary>
		public void CloseWindow(string appID)
		{
			AppWindow window;
			if (windows.TryGetValue(appID, out window))
			{
				RemoveWindow(appID);
				window.Form.Close();
			}
		}

		void RemoveWindow(string appID)
		{
			AppWindow window;
			if (windows.TryGetValue(appID, out window))
			{
				windows.Remove(appID);
				panel.Items.Remove(window.Button);
				window.Button.Dispose();
			}
		}
	}
}
