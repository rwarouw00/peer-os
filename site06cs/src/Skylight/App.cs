using System;
using System.Collections.Generic;
using System.IO;
using System.Text;
using System.Windows.Forms;

namespace Skylight
{
	/// <summary>The result of WriteFile(): Ok, or an error message for the user.</summary>
	public class Result
	{
		public bool Ok { get; private set; }
		public string Error { get; private set; }

		public Result(string error)
		{
			Ok = error == null;
			Error = error;
		}
	}

	/// <summary>
	/// Base class for Skylight OS apps. An app extends App and returns its
	/// window's contents from Build().
	/// </summary>
	public abstract class App
	{
		protected Desktop Desktop { get; private set; }
		public string AppID { get; private set; }
		protected string AppPath { get; private set; }                     // e.g. "apps/terminal/"
		protected Dictionary<string, object> Manifest { get; private set; }  // app.json
		protected Form Window { get; private set; }                         // this app's window
		protected Dictionary<string, string> Args { get; private set; }      // values passed to OpenWindow()

		internal void Init(Desktop desktop, string appID, string path, Dictionary<string, object> manifest, Form window, Dictionary<string, string> args)
		{
			Desktop = desktop;
			AppID = appID;
			AppPath = path;
			Manifest = manifest;
			Window = window;
			Args = args;
		}

		/// <summary>Returns the window's contents. Called once, before the window is shown.</summary>
		protected internal abstract Control Build();

		/// <summary>Return false to keep the window open, e.g. to ask about unsaved work first.</summary>
		protected internal virtual bool CanClose()
		{
			return true;
		}

		// ---- Windows ----

		public string OpenWindow(string path)
		{
			return Desktop.OpenWindow(path, new Dictionary<string, string>());
		}

		public string OpenWindow(string path, Dictionary<string, string> args)
		{
			return Desktop.OpenWindow(path, args);
		}

		public void CloseWindow()
		{
			Desktop.CloseWindow(AppID);
		}

		public void CloseWindow(string appID)
		{
			Desktop.CloseWindow(appID);
		}

		// ---- Dialogs and notifications ----

		/// <summary>Shows a message with an OK button.</summary>
		public void OpenDialog(string message)
		{
			Desktop.OpenDialog(message, null, Window);
		}

		/// <summary>Asks with OK and Cancel; onOK runs only if the user clicks OK.</summary>
		public void OpenDialog(string message, Action onOK)
		{
			Desktop.OpenDialog(message, onOK, Window);
		}

		public void PostNotification(string header)
		{
			Desktop.Notify(header, "");
		}

		public void PostNotification(string header, string body)
		{
			Desktop.Notify(header, body ?? "");
		}

		// ---- Settings and the desktop ----

		/// <summary>A copy of the user's settings: change it, then pass it to SaveSettings().</summary>
		public Settings GetSettings()
		{
			return Desktop.Settings.Copy();
		}

		/// <summary>Checks, saves and applies settings. Returns false (after telling the user) if they were refused.</summary>
		public bool SaveSettings(Settings settings)
		{
			return Desktop.SaveSettings(settings);
		}

		public bool SetBackground(string imagePath)
		{
			Settings settings = GetSettings();
			settings.Background = imagePath;
			return SaveSettings(settings);
		}

		public void AddShortcut(string appPath)
		{
			Desktop.AddShortcut(appPath);
		}

		// ---- Identity ----

		public string GetAppID()
		{
			return AppID;
		}

		public string GetUsername()
		{
			return Desktop.User;
		}

		// ---- Files ----

		/// <summary>A file in this app's folder, such as AppFile("icons/16.png").</summary>
		public string AppFile(string name)
		{
			return Storage.HomePath(AppPath + name);
		}

		/// <summary>The names of the user's own files.</summary>
		public List<string> ListFiles()
		{
			return Storage.ListFiles(GetUsername());
		}

		/// <summary>One of the user's files as text, or null if there's no such file.</summary>
		public string ReadFile(string name)
		{
			if (Storage.FileNameError(name) != null)
			{
				return null;
			}
			string file = Storage.UserFile(GetUsername(), name);
			try
			{
				return File.Exists(file) ? File.ReadAllText(file, Encoding.UTF8) : null;
			}
			catch (IOException)
			{
				return null;
			}
		}

		/// <summary>Saves text to one of the user's files.</summary>
		public Result WriteFile(string name, string contents)
		{
			string error = Storage.FileNameError(name);
			if (error != null)
			{
				return new Result(error);
			}
			string file = Storage.UserFile(GetUsername(), name);
			try
			{
				Directory.CreateDirectory(Path.GetDirectoryName(file));
				File.WriteAllText(file, contents ?? "", new UTF8Encoding(false));
				return new Result(null);
			}
			catch (Exception)
			{
				return new Result("The file could not be saved.");
			}
		}
	}
}
