using System.Drawing;
using System.Windows.Forms;

namespace Skylight
{
	/// <summary>The login window. After it closes, Username is the user, or null if they chose Quit.</summary>
	class LoginForm : Form
	{
		public string Username { get; private set; }

		public LoginForm()
		{
			Text = "Skylight OS";
			Icon = Desktop.LoadIcon("images/skylight.png");
			FormBorderStyle = FormBorderStyle.FixedDialog;
			MaximizeBox = false;
			MinimizeBox = false;
			StartPosition = FormStartPosition.CenterScreen;
			AutoSize = true;
			AutoSizeMode = AutoSizeMode.GrowAndShrink;
			Padding = new Padding(12);

			TextBox username = new TextBox { Width = 180 };
			TextBox password = new TextBox { Width = 180, UseSystemPasswordChar = true };
			Label error = new Label { AutoSize = true, MaximumSize = new Size(260, 0), ForeColor = Color.DarkRed };
			Button quit = new Button { Text = "Quit", AutoSize = true };
			Button logIn = new Button { Text = "Log In", AutoSize = true };
			quit.Click += delegate { Close(); };
			logIn.Click += delegate
			{
				if (Storage.CheckLogin(username.Text, password.Text))
				{
					Username = username.Text;
					Close();
				}
				else
				{
					error.Text = "Either the username or password is incorrect.";
					password.Clear();
					password.Focus();
				}
			};
			AcceptButton = logIn;   // Enter
			CancelButton = quit;    // Escape

			TableLayoutPanel form = new TableLayoutPanel { ColumnCount = 2, AutoSize = true, Location = new Point(12, 12) };
			form.Controls.Add(new Label { Text = "Username:", AutoSize = true, Anchor = AnchorStyles.Left }, 0, 0);
			form.Controls.Add(username, 1, 0);
			form.Controls.Add(new Label { Text = "Password:", AutoSize = true, Anchor = AnchorStyles.Left }, 0, 1);
			form.Controls.Add(password, 1, 1);
			form.Controls.Add(error, 0, 2);
			form.SetColumnSpan(error, 2);
			FlowLayoutPanel buttons = new FlowLayoutPanel { FlowDirection = FlowDirection.RightToLeft, AutoSize = true, Dock = DockStyle.Fill };
			buttons.Controls.Add(logIn);
			buttons.Controls.Add(quit);
			form.Controls.Add(buttons, 0, 3);
			form.SetColumnSpan(buttons, 2);
			Controls.Add(form);
		}
	}
}
