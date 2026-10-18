using System;
using Skylight;

/// <summary>Adds a user, or changes a password: AddUser.exe &lt;username&gt; &lt;password&gt;</summary>
static class AddUser
{
	static int Main(string[] args)
	{
		if (args.Length != 2 || !Storage.IsValidUsername(args[0]) || args[1].Length == 0)
		{
			Console.Error.WriteLine("Usage: AddUser.exe <username> <password>");
			Console.Error.WriteLine("Usernames may use letters, digits, '-' and '_' (up to 32 characters).");
			return 1;
		}
		bool existed = Storage.LoadUsers().ContainsKey(args[0]);
		if (!Storage.SetPassword(args[0], args[1]))
		{
			Console.Error.WriteLine("Could not write " + System.IO.Path.Combine(Storage.Data, "users.json"));
			return 1;
		}
		Console.WriteLine((existed ? "Changed the password for " : "Added user ") + args[0]);
		return 0;
	}
}
