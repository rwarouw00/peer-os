using System;
using System.Drawing;
using System.Windows.Forms;

namespace Skylight.Apps
{
	/// <summary>Settings: desktop background, and how long notifications stay on screen.</summary>
	public class SettingsApp : App
	{
		readonly FlowLayoutPanel backgrounds = new FlowLayoutPanel { Width = 470, Height = 196 };

		protected internal override Control Build()
		{
			Settings settings = GetSettings();
			for (int i = 1; i <= 6; i++)
			{
				string path = "images/backgrounds/" + i + ".jpg";
				CheckBox choice = new CheckBox
				{
					Appearance = Appearance.Button,
					Image = Thumbnail(path),
					Size = new Size(144, 92),
					Checked = path == settings.Background,
					Tag = path
				};
				choice.Click += delegate
				{
					if (SetBackground(path))
					{
						MarkSelected(path);
					}
					else
					{
						MarkSelected(GetSettings().Background);
					}
				};
				backgrounds.Controls.Add(choice);
			}

			NumericUpDown seconds = new NumericUpDown { Minimum = 1, Maximum = 60, Value = settings.NotificationTime / 1000, Width = 60 };
			Button save = new Button { Text = "Save", AutoSize = true };
			save.Click += delegate
			{
				Settings changed = GetSettings();
				changed.NotificationTime = (int)seconds.Value * 1000;
				if (SaveSettings(changed))
				{
					PostNotification("Settings", "Notifications now stay for " + seconds.Value + " seconds.");
				}
			};
			FlowLayoutPanel time = new FlowLayoutPanel { AutoSize = true };
			time.Controls.Add(new Label { Text = "Show notifications for", AutoSize = true, Anchor = AnchorStyles.Left, Margin = new Padding(3, 7, 3, 0) });
			time.Controls.Add(seconds);
			time.Controls.Add(new Label { Text = "seconds", AutoSize = true, Margin = new Padding(3, 7, 3, 0) });
			time.Controls.Add(save);

			FlowLayoutPanel root = new FlowLayoutPanel { FlowDirection = FlowDirection.TopDown, WrapContents = false, Padding = new Padding(12) };
			root.Controls.Add(Heading("Background"));
			root.Controls.Add(backgrounds);
			root.Controls.Add(Heading("Notifications"));
			root.Controls.Add(time);
			return root;
		}

		Image Thumbnail(string path)
		{
			using (Image original = Image.FromFile(Storage.HomePath(path)))
			{
				return new Bitmap(original, 128, 80);
			}
		}

		void MarkSelected(string path)
		{
			foreach (CheckBox choice in backgrounds.Controls)
			{
				choice.Checked = (string)choice.Tag == path;
			}
		}

		static Label Heading(string text)
		{
			Label label = new Label { Text = text, AutoSize = true, Margin = new Padding(0, 8, 0, 4) };
			label.Font = new Font(label.Font, FontStyle.Bold);
			return label;
		}
	}
}
