using System;
using System.Windows.Forms;

namespace Skylight
{
	/// <summary>Starts Skylight OS and moves between the login window and the desktop.</summary>
	public static class Program
	{
		[STAThread]
		public static void Main()
		{
			Application.EnableVisualStyles();
			Application.SetCompatibleTextRenderingDefault(false);
			Application.Run(new SkylightContext());
		}
	}

	/// <summary>Keeps Skylight running while it switches between the login window and the desktop.</summary>
	class SkylightContext : ApplicationContext
	{
		public SkylightContext()
		{
			ShowLogin();
		}

		void ShowLogin()
		{
			LoginForm login = new LoginForm();
			login.FormClosed += delegate
			{
				if (login.Username == null)
				{
					ExitThread();   // Quit
				}
				else
				{
					Desktop desktop = new Desktop(login.Username);
					desktop.FormClosed += delegate { ShowLogin(); };   // logging out
					desktop.Show();
				}
			};
			login.Show();
		}
	}
}
