using System;
using System.Diagnostics;
using System.IO;
using System.Windows.Forms;
using Microsoft.Win32;

namespace Skylight.Apps
{
	/// <summary>
	/// Web Browser: the WinForms WebBrowser control, which on Windows uses the
	/// Internet Explorer 11 engine. Where it isn't available, pages open in the
	/// system's browser instead.
	/// </summary>
	public class WebBrowserApp : App
	{
		const string StartPage = "<html><body style='font-family: Segoe UI, sans-serif; color: #444; margin: 40px'>"
			+ "<h2>Skylight Web Browser</h2><p>Type an address above and press Enter.</p></body></html>";

		WebBrowser view;
		readonly ToolStripTextBox address = new ToolStripTextBox { AutoSize = false, Width = 600 };

		protected internal override Control Build()
		{
			UseModernEngine();
			ToolStrip toolbar = new ToolStrip { GripStyle = ToolStripGripStyle.Hidden };
			Control page;
			try
			{
				if (Type.GetType("Mono.Runtime") != null)
				{
					// Mono's WebBrowser needs an engine that is rarely installed: it shows a blank page.
					throw new PlatformNotSupportedException();
				}
				view = new WebBrowser { Dock = DockStyle.Fill, ScriptErrorsSuppressed = true, DocumentText = StartPage };
				// Keep the address bar in step with the links the user follows.
				view.Navigated += delegate { address.Text = view.Url == null || view.Url.ToString() == "about:blank" ? "" : view.Url.ToString(); };
				toolbar.Items.Add("◀", null, delegate { view.GoBack(); });
				toolbar.Items.Add("▶", null, delegate { view.GoForward(); });
				toolbar.Items.Add("↻", null, delegate { view.Refresh(); });
				page = view;
			}
			catch (Exception)
			{
				view = null;
				page = new Label { Text = "Pages open in your default web browser.", Dock = DockStyle.Fill, TextAlign = System.Drawing.ContentAlignment.MiddleCenter };
			}
			address.KeyDown += (sender, e) =>
			{
				if (e.KeyCode == Keys.Enter)
				{
					e.SuppressKeyPress = true;
					Go(address.Text.Trim());
				}
			};
			toolbar.Items.Add(address);

			Panel root = new Panel();
			root.Controls.Add(page);
			root.Controls.Add(toolbar);
			return root;
		}

		void Go(string url)
		{
			if (url.Length == 0)
			{
				return;
			}
			if (!url.Contains("://"))
			{
				url = "http://" + url;
			}
			Uri uri;
			if (!Uri.TryCreate(url, UriKind.Absolute, out uri) || (uri.Scheme != "http" && uri.Scheme != "https"))
			{
				OpenDialog("That doesn't look like a web address.");
				return;
			}
			if (view != null)
			{
				view.Navigate(uri);
			}
			else
			{
				Process.Start(uri.ToString());
				PostNotification("Web Browser", "Opened " + uri);
			}
		}

		/// <summary>
		/// The WebBrowser control imitates Internet Explorer 7 unless told otherwise.
		/// This registry setting, for this program only, selects the Internet Explorer 11 engine.
		/// </summary>
		static void UseModernEngine()
		{
			if (Environment.OSVersion.Platform != PlatformID.Win32NT)
			{
				return;
			}
			try
			{
				string exe = Path.GetFileName(Process.GetCurrentProcess().MainModule.FileName);
				Registry.SetValue(@"HKEY_CURRENT_USER\Software\Microsoft\Internet Explorer\Main\FeatureControl\FEATURE_BROWSER_EMULATION",
					exe, 11001, RegistryValueKind.DWord);
			}
			catch (Exception)
			{
				// without it, pages still show, in IE 7 mode
			}
		}
	}
}
