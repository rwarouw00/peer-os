<?php
/*
 * Settings: desktop background and how long notifications stay on screen.
 */
class SettingsApp extends App
{
	private $buttons = array();   // background path => GtkButton
	private $seconds;

	function build()
	{
		$settings = $this->getSettings();

		$grid = new GtkTable(2, 3, true);
		$grid->set_row_spacings(6);
		$grid->set_col_spacings(6);
		for ($i = 0; $i < 6; $i++) {
			$path = 'images/backgrounds/' . ($i + 1) . '.jpg';
			$pixbuf = GdkPixbuf::new_from_file(SKYLIGHT_ROOT . '/' . $path);
			$button = new GtkButton();
			$button->add(GtkImage::new_from_pixbuf($pixbuf->scale_simple(128, 80, Gdk::INTERP_BILINEAR)));
			$button->connect_simple('clicked', array($this, 'chooseBackground'), $path);
			$grid->attach($button, $i % 3, $i % 3 + 1, (int) ($i / 3), (int) ($i / 3) + 1);
			$this->buttons[$path] = $button;
		}
		$this->markSelected($settings['background']);

		$this->seconds = new GtkSpinButton(new GtkAdjustment($settings['notificationTime'] / 1000, 1, 60, 1, 5, 0), 1, 0);
		$save = new GtkButton('Save');
		$save->connect_simple('clicked', array($this, 'saveNotificationTime'));
		$row = new GtkHBox(false, 6);
		$row->pack_start(new GtkLabel('Show notifications for'), false, false);
		$row->pack_start($this->seconds, false, false);
		$row->pack_start(new GtkLabel('seconds'), false, false);
		$row->pack_start($save, false, false);

		$box = new GtkVBox(false, 8);
		$box->set_border_width(12);
		$box->pack_start($this->heading('Background'), false, false);
		$box->pack_start($grid, false, false);
		$box->pack_start($this->heading('Notifications'), false, false);
		$box->pack_start($row, false, false);
		$this->window->add($box);
	}

	function heading($text)
	{
		$label = new GtkLabel($text);
		$label->modify_font(new PangoFontDescription('Bold'));
		$label->set_alignment(0, 0.5);
		return $label;
	}

	function markSelected($selected)
	{
		foreach ($this->buttons as $path => $button) {
			$button->set_relief($path == $selected ? Gtk::RELIEF_NORMAL : Gtk::RELIEF_NONE);
		}
	}

	function chooseBackground($path)
	{
		if ($this->setBackground($path)) {
			$this->markSelected($path);
		}
	}

	function saveNotificationTime()
	{
		$seconds = $this->seconds->get_value_as_int();
		if ($this->saveSettings(array('notificationTime' => $seconds * 1000))) {
			$this->postNotification('Settings', 'Notifications now stay for ' . $seconds . ' seconds.');
		}
	}
}
