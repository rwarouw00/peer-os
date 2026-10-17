using System.Collections.Generic;
using System.Linq;

namespace Skylight
{
	/// <summary>A user's settings. Change a copy, then save it with App.SaveSettings().</summary>
	public class Settings
	{
		public string Background { get; set; }
		public int NotificationTime { get; set; }   // milliseconds
		public List<Shortcut> Shortcuts { get; set; }

		public Settings()
		{
			Background = "images/backgrounds/3.jpg";
			NotificationTime = 7000;
			Shortcuts = new List<Shortcut>();
		}

		public Settings Copy()
		{
			return new Settings
			{
				Background = Background,
				NotificationTime = NotificationTime,
				Shortcuts = Shortcuts.Select(s => new Shortcut { Path = s.Path, X = s.X, Y = s.Y }).ToList()
			};
		}
	}

	/// <summary>A desktop shortcut: the app's folder and its position on the desktop.</summary>
	public class Shortcut
	{
		public string Path { get; set; }
		public int X { get; set; }
		public int Y { get; set; }
	}
}
