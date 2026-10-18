using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Security.Cryptography;
using System.Text;
using System.Text.RegularExpressions;
using System.Web.Script.Serialization;

namespace Skylight
{
	/// <summary>Users, settings and files, kept as JSON under data\.</summary>
	public static class Storage
	{
		/// <summary>The Skylight folder: the one Skylight.exe is in, unless SKYLIGHT_HOME says otherwise.</summary>
		public static readonly string Home = Path.GetFullPath(
			Environment.GetEnvironmentVariable("SKYLIGHT_HOME") ?? AppDomain.CurrentDomain.BaseDirectory);
		public static readonly string Data = Path.Combine(Home, "data");

		static readonly Regex UsernamePattern = new Regex(@"^[A-Za-z0-9_-]{1,32}$");
		static readonly Regex AppPathPattern = new Regex(@"^apps/[a-z0-9-]+/$");
		static readonly Regex BackgroundPattern = new Regex(@"^images/backgrounds/[A-Za-z0-9_-]+\.(jpg|jpeg|png)$", RegexOptions.IgnoreCase);
		// No slashes and no leading dot, so a name can't leave the user's folder.
		static readonly Regex FileNamePattern = new Regex(@"^[A-Za-z0-9_-][A-Za-z0-9_. -]{0,99}$");
		const int Iterations = 20000;

		static readonly JavaScriptSerializer Json = new JavaScriptSerializer();

		// ---- Paths ----

		/// <summary>A path inside the Skylight folder, from a relative path such as "apps/terminal/icons/16.png".</summary>
		public static string HomePath(string relative)
		{
			return Path.Combine(Home, relative.Replace('/', Path.DirectorySeparatorChar));
		}

		// ---- JSON files ----

		public static T ReadJson<T>(string path) where T : class
		{
			try
			{
				return File.Exists(path) ? Json.Deserialize<T>(File.ReadAllText(path, Encoding.UTF8)) : null;
			}
			catch (Exception)
			{
				return null;   // treat a damaged file as missing
			}
		}

		public static bool WriteJson(string path, object data)
		{
			try
			{
				Directory.CreateDirectory(Path.GetDirectoryName(path));
				File.WriteAllText(path, Json.Serialize(data), new UTF8Encoding(false));
				return true;
			}
			catch (Exception)
			{
				return false;
			}
		}

		// ---- Users ----

		public static bool IsValidUsername(string name)
		{
			return name != null && UsernamePattern.IsMatch(name);
		}

		static string UsersFile
		{
			get { return Path.Combine(Data, "users.json"); }
		}

		public static Dictionary<string, string> LoadUsers()
		{
			return ReadJson<Dictionary<string, string>>(UsersFile) ?? new Dictionary<string, string>();
		}

		public static string UserFolder(string user)
		{
			return Path.Combine(Data, "users", user);
		}

		public static bool CheckLogin(string username, string password)
		{
			string stored;
			return IsValidUsername(username) && LoadUsers().TryGetValue(username, out stored) && CheckPassword(password, stored);
		}

		/// <summary>Adds a user or changes their password. Returns false if the file can't be written.</summary>
		public static bool SetPassword(string username, string password)
		{
			Dictionary<string, string> users = LoadUsers();
			users[username] = HashPassword(password);
			return WriteJson(UsersFile, users);
		}

		// ---- Passwords: PBKDF2-SHA256, built from HMACSHA256 (.NET 4's own PBKDF2 only does SHA-1) ----

		static byte[] Pbkdf2(string password, byte[] salt, int iterations)
		{
			using (HMACSHA256 hmac = new HMACSHA256(Encoding.UTF8.GetBytes(password)))
			{
				byte[] block = hmac.ComputeHash(salt.Concat(new byte[] { 0, 0, 0, 1 }).ToArray());
				byte[] result = (byte[])block.Clone();
				for (int i = 1; i < iterations; i++)
				{
					block = hmac.ComputeHash(block);
					for (int j = 0; j < result.Length; j++)
					{
						result[j] ^= block[j];
					}
				}
				return result;
			}
		}

		static string HashPassword(string password)
		{
			byte[] salt = new byte[16];
			using (RandomNumberGenerator random = RandomNumberGenerator.Create())
			{
				random.GetBytes(salt);
			}
			return "pbkdf2-sha256$" + Iterations + "$" + Convert.ToBase64String(salt) + "$"
				+ Convert.ToBase64String(Pbkdf2(password, salt, Iterations));
		}

		static bool CheckPassword(string password, string stored)
		{
			string[] parts = (stored ?? "").Split('$');
			if (parts.Length != 4 || parts[0] != "pbkdf2-sha256")
			{
				return false;
			}
			try
			{
				byte[] expected = Convert.FromBase64String(parts[3]);
				byte[] actual = Pbkdf2(password, Convert.FromBase64String(parts[2]), int.Parse(parts[1]));
				int difference = expected.Length ^ actual.Length;   // compare every byte: constant time
				for (int i = 0; i < Math.Min(expected.Length, actual.Length); i++)
				{
					difference |= expected[i] ^ actual[i];
				}
				return difference == 0;
			}
			catch (FormatException)
			{
				return false;
			}
		}

		// ---- Settings ----

		public static Settings LoadSettings(string user)
		{
			Settings settings = ReadJson<Settings>(Path.Combine(UserFolder(user), "settings.json")) ?? new Settings();
			if (CheckSettings(settings) != null)
			{
				return new Settings();   // a damaged file gives the defaults
			}
			return settings;
		}

		/// <summary>Checks and saves settings. Returns null on success, or a message for the user.</summary>
		public static string SaveSettings(string user, Settings settings)
		{
			string error = CheckSettings(settings);
			if (error != null)
			{
				return error;
			}
			return WriteJson(Path.Combine(UserFolder(user), "settings.json"), settings) ? null : "The settings could not be saved.";
		}

		static string CheckSettings(Settings settings)
		{
			if (settings.Background == null || !BackgroundPattern.IsMatch(settings.Background))
			{
				return "That isn't one of the desktop backgrounds.";
			}
			if (settings.NotificationTime < 1000 || settings.NotificationTime > 60000)
			{
				return "Notifications can stay for 1 to 60 seconds.";
			}
			if (settings.Shortcuts == null || settings.Shortcuts.Count > 50 || settings.Shortcuts.Any(s => s == null || !IsAppPath(s.Path)))
			{
				return "Bad desktop shortcuts.";
			}
			return null;
		}

		// ---- Apps ----

		public static bool IsAppPath(string path)
		{
			return path != null && AppPathPattern.IsMatch(path);
		}

		/// <summary>The app folders listed in apps\apps.json.</summary>
		public static List<string> AppList()
		{
			List<string> paths = ReadJson<List<string>>(HomePath("apps/apps.json")) ?? new List<string>();
			return paths.Where(IsAppPath).ToList();
		}

		// ---- The user's own files (data\users\<name>\files\) ----

		public static string FileNameError(string name)
		{
			return name != null && FileNamePattern.IsMatch(name) ? null
				: "File names may use letters, digits, spaces, \"-\", \"_\" and \".\" (up to 100 characters).";
		}

		public static string UserFile(string user, string name)
		{
			return Path.Combine(UserFolder(user), "files", name);
		}

		public static List<string> ListFiles(string user)
		{
			string folder = Path.Combine(UserFolder(user), "files");
			if (!Directory.Exists(folder))
			{
				return new List<string>();
			}
			return Directory.GetFiles(folder).Select(Path.GetFileName).OrderBy(name => name).ToList();
		}
	}
}
