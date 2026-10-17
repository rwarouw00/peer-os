using System;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Windows.Forms;

namespace Skylight
{
	/// <summary>The desktop surface: draws the background scaled to cover it, and holds the shortcuts.</summary>
	class DesktopArea : Panel
	{
		Image background;

		public DesktopArea()
		{
			DoubleBuffered = true;
			BackColor = Color.FromArgb(32, 32, 32);
			Resize += delegate { Invalidate(); };
		}

		public void SetBackground(string file)
		{
			Image old = background;
			background = Image.FromFile(file);
			if (old != null)
			{
				old.Dispose();
			}
			Invalidate(true);
		}

		// Painting the background here (rather than in OnPaint) lets transparent shortcuts show it.
		protected override void OnPaintBackground(PaintEventArgs e)
		{
			base.OnPaintBackground(e);
			if (background == null)
			{
				return;
			}
			float scale = Math.Max((float)ClientSize.Width / background.Width, (float)ClientSize.Height / background.Height);
			float width = background.Width * scale;
			float height = background.Height * scale;
			e.Graphics.InterpolationMode = InterpolationMode.HighQualityBilinear;
			e.Graphics.DrawImage(background, (ClientSize.Width - width) / 2, (ClientSize.Height - height) / 2, width, height);
		}
	}

	/// <summary>A desktop shortcut: an icon with a white, shadowed label, on a transparent background.</summary>
	class ShortcutIcon : Control
	{
		readonly Image icon;

		public string AppPath { get; private set; }

		public ShortcutIcon(string appPath, string title, Image icon)
		{
			AppPath = appPath;
			Text = title;
			this.icon = icon;
			SetStyle(ControlStyles.SupportsTransparentBackColor | ControlStyles.OptimizedDoubleBuffer
				| ControlStyles.AllPaintingInWmPaint | ControlStyles.UserPaint, true);
			BackColor = Color.Transparent;
			Size = new Size(90, 80);
			Cursor = Cursors.Default;
		}

		protected override void OnPaint(PaintEventArgs e)
		{
			e.Graphics.InterpolationMode = InterpolationMode.HighQualityBicubic;
			e.Graphics.DrawImage(icon, (Width - 48) / 2, 4, 48, 48);
			Rectangle text = new Rectangle(0, 54, Width, Height - 54);
			StringFormat centered = new StringFormat { Alignment = StringAlignment.Center };
			using (Brush shadow = new SolidBrush(Color.FromArgb(200, 0, 0, 0)))
			{
				text.Offset(1, 1);
				e.Graphics.DrawString(Text, Font, shadow, text, centered);
				text.Offset(-1, -1);
			}
			e.Graphics.DrawString(Text, Font, Brushes.White, text, centered);
		}
	}

	/// <summary>A pop-up notification, shown at the top right of the desktop without taking focus.</summary>
	class Toast : Form
	{
		public Toast(string header, string body, EventHandler close)
		{
			FormBorderStyle = FormBorderStyle.None;
			ShowInTaskbar = false;
			StartPosition = FormStartPosition.Manual;
			BackColor = Color.FromArgb(50, 50, 50);
			ForeColor = Color.White;
			Width = 300;
			Padding = new Padding(14, 8, 6, 8);

			Label title = new Label { Text = header, AutoSize = false, Width = 250, Height = 20, AutoEllipsis = true,
				Font = new Font(Font, FontStyle.Bold), Location = new Point(14, 8) };
			Controls.Add(title);
			int bottom = title.Bottom;
			if (body.Length > 0)
			{
				Label message = new Label { Text = body, AutoSize = true, MaximumSize = new Size(250, 54), Location = new Point(14, title.Bottom) };
				Controls.Add(message);
				bottom = message.Bottom;
			}
			Button closeButton = Desktop.CloseButton();
			closeButton.ForeColor = Color.White;
			closeButton.Location = new Point(Width - 30, 4);
			closeButton.Click += close;
			Controls.Add(closeButton);
			Height = bottom + 10;
		}

		// Show toasts without taking the keyboard focus from the user's window.
		protected override bool ShowWithoutActivation
		{
			get { return true; }
		}
	}
}
