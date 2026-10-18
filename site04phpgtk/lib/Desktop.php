<?php
/*
 * The Skylight OS desktop, built with PHP-GTK2: login, top panel (home button,
 * window list, clock), background and shortcuts, home menu, notifications,
 * dialogs and app windows. Plain PHP 5.2.
 */
class Desktop
{
	public $user = null;
	public $settings = null;

	private $window;          // the desktop GtkWindow
	private $area;            // GtkLayout holding the background and shortcuts
	private $background;      // GtkImage
	private $backgroundFile = '';
	private $backgroundSize = array(0, 0);
	private $taskbar;         // GtkHBox of window buttons
	private $clock;           // GtkLabel
	private $clockTimer = null;
	private $menu;            // home menu GtkWindow
	private $menuApps;        // GtkIconView
	private $menuNotifications;
	private $menuPages = array();
	private $menuOpen = false;
	private $menuClosedAt = 0;
	private $notificationList;
	private $windows = array();  // appID => array('app', 'window', 'button')
	private $nextAppID = 1;
	private $toasts = array();
	private $drag = null;        // shortcut being dragged

	// ---- Start-up, login and logout ----

	function start()
	{
		$rc = SKYLIGHT_ROOT . '/skylight.gtkrc';
		if (is_file($rc)) {
			Gtk::rc_parse($rc);
		}
		$this->showLogin();
	}

	function showLogin()
	{
		$dialog = new GtkDialog('Skylight OS', null, Gtk::DIALOG_MODAL,
			array(Gtk::STOCK_QUIT, Gtk::RESPONSE_CANCEL, 'Log In', Gtk::RESPONSE_OK));
		$dialog->set_default_response(Gtk::RESPONSE_OK);
		$dialog->set_position(Gtk::WIN_POS_CENTER);
		$dialog->set_icon(GdkPixbuf::new_from_file(SKYLIGHT_ROOT . '/images/skylight.png'));

		$table = new GtkTable(3, 2);
		$table->set_border_width(12);
		$table->set_row_spacings(8);
		$table->set_col_spacings(8);
		$username = new GtkEntry();
		$password = new GtkEntry();
		$password->set_visibility(false);
		$password->set_activates_default(true);
		$error = new GtkLabel('');
		$table->attach(new GtkLabel('Username:'), 0, 1, 0, 1);
		$table->attach($username, 1, 2, 0, 1);
		$table->attach(new GtkLabel('Password:'), 0, 1, 1, 2);
		$table->attach($password, 1, 2, 1, 2);
		$table->attach($error, 0, 2, 2, 3);
		$dialog->vbox->pack_start($table);
		$dialog->show_all();

		while ($dialog->run() == Gtk::RESPONSE_OK) {
			if (Storage::checkLogin($username->get_text(), $password->get_text())) {
				$user = $username->get_text();
				$dialog->destroy();
				$this->showDesktop($user);
				return;
			}
			$error->set_text('Either the username or password is incorrect.');
			$password->set_text('');
			$password->grab_focus();
		}
		$dialog->destroy();
		if (Gtk::main_level() > 0) {
			Gtk::main_quit();
		}
	}

	function logout()
	{
		foreach (array_keys($this->windows) as $appID) {
			$this->closeWindow($appID);
		}
		foreach ($this->toasts as $toast) {
			$toast->destroy();
		}
		$this->toasts = array();
		if ($this->clockTimer !== null) {
			Gtk::timeout_remove($this->clockTimer);
			$this->clockTimer = null;
		}
		$this->menu->destroy();
		$this->window->destroy();
		$this->user = null;
		$this->showLogin();
	}

	// ---- Building the desktop ----

	function showDesktop($user)
	{
		$this->user = $user;
		$this->settings = Storage::loadSettings($user);

		$this->window = new GtkWindow();
		$this->window->set_title('Skylight OS');
		$this->window->set_icon(GdkPixbuf::new_from_file(SKYLIGHT_ROOT . '/images/skylight.png'));
		$this->window->set_default_size(1024, 700);
		$this->window->connect('delete-event', array($this, 'onDesktopDelete'));

		$box = new GtkVBox();
		$box->pack_start($this->buildPanel(), false, false);
		$this->area = new GtkLayout();
		$this->area->connect('size-allocate', array($this, 'onAreaResized'));
		$this->background = new GtkImage();
		$this->area->put($this->background, 0, 0);
		$box->pack_start($this->area);
		$this->window->add($box);

		$this->buildMenu();
		$this->window->show_all();
		$this->window->maximize();
		$this->applySettings();
		$this->tickClock();
		$this->clockTimer = Gtk::timeout_add(1000, array($this, 'tickClock'));
	}

	function buildPanel()
	{
		$panel = new GtkHBox(false, 4);
		$panel->set_border_width(2);

		$home = new GtkButton();
		$home->set_relief(Gtk::RELIEF_NONE);
		$homeBox = new GtkHBox(false, 4);
		$homeBox->pack_start($this->image(SKYLIGHT_ROOT . '/images/home.png', 22), false, false);
		$homeBox->pack_start(new GtkLabel('Apps'), false, false);
		$home->add($homeBox);
		$home->connect_simple('clicked', array($this, 'toggleMenu'));
		$panel->pack_start($home, false, false);
		$panel->pack_start(new GtkVSeparator(), false, false);

		$this->taskbar = new GtkHBox(false, 2);
		$panel->pack_start($this->taskbar, true, true);

		$this->clock = new GtkLabel('');
		$panel->pack_end($this->clock, false, false, 8);

		$frame = new GtkEventBox();
		$frame->set_name('skylight-panel');
		$frame->add($panel);
		return $frame;
	}

	// A GtkImage of a PNG scaled to $size pixels square.
	function image($file, $size)
	{
		$pixbuf = GdkPixbuf::new_from_file($file);
		return GtkImage::new_from_pixbuf($pixbuf->scale_simple($size, $size, Gdk::INTERP_BILINEAR));
	}

	function tickClock()
	{
		$this->clock->set_text(date('g:i a') . '   ' . $this->user);
		return true;   // keep the timer running
	}

	// ---- Settings, background and shortcuts ----

	function applySettings()
	{
		$this->backgroundSize = array(0, 0);   // force the background to be redrawn
		$this->fitBackground();
		$this->renderShortcuts();
	}

	// Returns true, or tells the user what went wrong and returns false.
	function saveSettings($changes)
	{
		$result = Storage::saveSettings($this->user, $changes);
		if (is_string($result)) {
			$this->notify('Settings not saved', $result);
			return false;
		}
		$this->settings = $result;
		$this->applySettings();
		return true;
	}

	function onAreaResized($area, $allocation)
	{
		$this->fitBackground();
	}

	// Scales the background image to cover the whole desktop area.
	function fitBackground()
	{
		$allocation = $this->area->allocation;
		$width = max(1, $allocation->width);
		$height = max(1, $allocation->height);
		$file = SKYLIGHT_ROOT . '/' . $this->settings['background'];
		if ($this->backgroundSize == array($width, $height) && $this->backgroundFile == $file) {
			return;
		}
		$this->backgroundSize = array($width, $height);
		$this->backgroundFile = $file;
		$this->area->set_size($width, $height);

		$pixbuf = GdkPixbuf::new_from_file($file);
		$scale = max($width / $pixbuf->get_width(), $height / $pixbuf->get_height());
		$scaled = $pixbuf->scale_simple((int) ceil($pixbuf->get_width() * $scale), (int) ceil($pixbuf->get_height() * $scale), Gdk::INTERP_BILINEAR);
		$this->background->set_from_pixbuf($scaled);
		$this->area->move($this->background,
			(int) (($width - $scaled->get_width()) / 2), (int) (($height - $scaled->get_height()) / 2));
	}

	function renderShortcuts()
	{
		foreach ($this->area->get_children() as $child) {
			if ($child !== $this->background) {
				$child->destroy();
			}
		}
		foreach ($this->settings['shortcuts'] as $shortcut) {
			$manifest = $this->readManifest($shortcut['path']);
			if (!$manifest) {
				continue;
			}
			$box = new GtkVBox(false, 2);
			$box->pack_start($this->image(SKYLIGHT_ROOT . '/' . $shortcut['path'] . $manifest['icons']['64'], 48), false, false);
			$label = new GtkLabel($manifest['title']);
			$box->pack_start($label, false, false);

			$item = new GtkEventBox();
			$item->set_name('skylight-shortcut');
			$item->set_visible_window(false);
			$item->add_events(Gdk::BUTTON_MOTION_MASK);
			$item->add($box);
			$item->connect('button-press-event', array($this, 'onShortcutPress'), $shortcut['path']);
			$item->connect('motion-notify-event', array($this, 'onShortcutMotion'));
			$item->connect('button-release-event', array($this, 'onShortcutRelease'));
			$this->area->put($item, $shortcut['x'], $shortcut['y']);
			$item->show_all();
		}
	}

	function addShortcut($path)
	{
		$shortcuts = $this->settings['shortcuts'];
		foreach ($shortcuts as $shortcut) {
			if ($shortcut['path'] === $path) {
				return;
			}
		}
		$count = count($shortcuts);
		$shortcuts[] = array('path' => $path, 'x' => 20 + 90 * ($count % 8), 'y' => 20 + 100 * floor($count / 8));
		$this->saveSettings(array('shortcuts' => $shortcuts));
	}

	function removeShortcut($path)
	{
		$shortcuts = array();
		foreach ($this->settings['shortcuts'] as $shortcut) {
			if ($shortcut['path'] !== $path) {
				$shortcuts[] = $shortcut;
			}
		}
		$this->saveSettings(array('shortcuts' => $shortcuts));
	}

	function onShortcutPress($item, $event, $path)
	{
		if ($event->type == Gdk::_2BUTTON_PRESS) {
			$this->drag = null;
			$this->openWindow($path);
		} elseif ($event->button == 1) {
			$this->drag = array('item' => $item, 'path' => $path, 'dx' => $event->x, 'dy' => $event->y, 'moved' => false);
		} elseif ($event->button == 3) {
			$menu = new GtkMenu();
			$remove = new GtkMenuItem('Remove from desktop');
			$remove->connect_simple('activate', array($this, 'removeShortcut'), $path);
			$menu->append($remove);
			$menu->show_all();
			$menu->popup();
		}
		return true;
	}

	function onShortcutMotion($item, $event)
	{
		if ($this->drag !== null && $this->drag['item'] === $item) {
			list($x, $y) = $this->area->get_pointer();
			$this->area->move($item, max(0, (int) ($x - $this->drag['dx'])), max(0, (int) ($y - $this->drag['dy'])));
			$this->drag['moved'] = true;
		}
		return true;
	}

	function onShortcutRelease($item, $event)
	{
		if ($this->drag !== null && $this->drag['moved']) {
			$shortcuts = array();
			foreach ($this->settings['shortcuts'] as $shortcut) {
				if ($shortcut['path'] === $this->drag['path']) {
					list($x, $y) = $this->area->get_pointer();
					$shortcut['x'] = max(0, (int) ($x - $this->drag['dx']));
					$shortcut['y'] = max(0, (int) ($y - $this->drag['dy']));
				}
				$shortcuts[] = $shortcut;
			}
			$this->drag = null;
			$this->saveSettings(array('shortcuts' => $shortcuts));
		}
		$this->drag = null;
		return true;
	}

	// ---- Home menu ----

	function buildMenu()
	{
		$this->menuOpen = false;
		$this->menu = new GtkWindow();
		$this->menu->set_decorated(false);
		$this->menu->set_skip_taskbar_hint(true);
		$this->menu->set_transient_for($this->window);
		$this->menu->set_default_size(430, 470);
		$this->menu->connect('focus-out-event', array($this, 'onMenuFocusOut'));
		$this->menu->connect('delete-event', array($this, 'onMenuFocusOut'));

		// Apps page: an icon for each app in apps/apps.json
		$store = new GtkListStore(GdkPixbuf::gtype, Gobject::TYPE_STRING, Gobject::TYPE_STRING);
		$paths = Storage::readJson(SKYLIGHT_ROOT . '/apps/apps.json', array());
		foreach ($paths as $path) {
			$manifest = $this->readManifest($path);
			if ($manifest) {
				$icon = GdkPixbuf::new_from_file(SKYLIGHT_ROOT . '/' . $path . $manifest['icons']['48']);
				$store->append(array($icon, $manifest['title'], $path));
			}
		}
		$this->menuApps = new GtkIconView($store);
		$this->menuApps->set_pixbuf_column(0);
		$this->menuApps->set_text_column(1);
		$this->menuApps->set_item_width(90);
		$this->menuApps->connect('button-release-event', array($this, 'onMenuAppClicked'));
		$appsPage = new GtkScrolledWindow();
		$appsPage->set_policy(Gtk::POLICY_NEVER, Gtk::POLICY_AUTOMATIC);
		$appsPage->add($this->menuApps);

		// Notifications page
		$clear = new GtkButton('Clear');
		$clear->connect_simple('clicked', array($this, 'clearNotifications'));
		$toolbar = new GtkHBox();
		$toolbar->set_border_width(4);
		$toolbar->pack_end($clear, false, false);
		$this->notificationList = new GtkVBox(false, 0);
		$scroller = new GtkScrolledWindow();
		$scroller->set_policy(Gtk::POLICY_NEVER, Gtk::POLICY_AUTOMATIC);
		$scroller->add_with_viewport($this->notificationList);
		$this->menuNotifications = new GtkVBox();
		$this->menuNotifications->pack_start($toolbar, false, false);
		$this->menuNotifications->pack_start($scroller);

		// Footer: page buttons and log out
		$footer = new GtkHBox(false, 6);
		$footer->set_border_width(8);
		$appsButton = new GtkButton('Apps');
		$appsButton->connect_simple('clicked', array($this, 'showMenuPage'), 'apps');
		$notificationsButton = new GtkButton('Notifications');
		$notificationsButton->connect_simple('clicked', array($this, 'showMenuPage'), 'notifications');
		$logout = new GtkButton();
		$logout->set_relief(Gtk::RELIEF_NONE);
		if (method_exists($logout, 'set_tooltip_text')) {
			$logout->set_tooltip_text('Log out');
		}
		$logout->add($this->image(SKYLIGHT_ROOT . '/images/power.png', 22));
		$logout->connect_simple('clicked', array($this, 'logout'));
		$footer->pack_start($appsButton, false, false);
		$footer->pack_start($notificationsButton, false, false);
		$footer->pack_end($logout, false, false);

		$box = new GtkVBox();
		$box->pack_start($appsPage);
		$box->pack_start($this->menuNotifications);
		$box->pack_start(new GtkHSeparator(), false, false);
		$box->pack_start($footer, false, false);
		$frame = new GtkFrame();
		$frame->set_shadow_type(Gtk::SHADOW_OUT);
		$frame->add($box);
		$this->menu->add($frame);
		$this->menuPages = array('apps' => $appsPage, 'notifications' => $this->menuNotifications);
	}

	function toggleMenu()
	{
		// Clicking the home button while the menu is open first takes focus from
		// the menu, which closes it; don't then reopen it straight away.
		if ($this->menuOpen || microtime(true) - $this->menuClosedAt < 0.3) {
			$this->hideMenu();
			return;
		}
		$this->menuOpen = true;
		$this->menu->show_all();
		$this->showMenuPage('apps');
		$this->menu->present();
		// Position it after showing: window managers may place new windows themselves.
		list($x, $y) = $this->window->window->get_origin();
		$this->menu->move($x + 4, $y + $this->taskbar->allocation->height + 8);
	}

	function showMenuPage($page)
	{
		foreach ($this->menuPages as $name => $widget) {
			if ($name == $page) {
				$widget->show_all();
			} else {
				$widget->hide();
			}
		}
	}

	function hideMenu()
	{
		if ($this->menuOpen) {
			$this->menuClosedAt = microtime(true);
		}
		$this->menu->hide();
		$this->menuOpen = false;
	}

	function onMenuFocusOut()
	{
		$this->hideMenu();
		return true;
	}

	function onMenuAppClicked($view, $event)
	{
		$path = $view->get_path_at_pos((int) $event->x, (int) $event->y);
		if ($path !== null) {
			$model = $view->get_model();
			$appPath = $model->get_value($model->get_iter($path), 2);
			$this->hideMenu();
			$this->openWindow($appPath);
		}
		return false;
	}

	// ---- Notifications ----

	function notify($header, $body = '')
	{
		$this->addToTray($header, $body);
		$this->showToast($header, $body);
	}

	function addToTray($header, $body)
	{
		$text = new GtkVBox(false, 2);
		$title = new GtkLabel($header);
		$title->modify_font(new PangoFontDescription('Bold 11'));
		$title->set_alignment(0, 0.5);
		$text->pack_start($title, false, false);
		if ($body !== '') {
			$message = new GtkLabel($body);
			$message->set_line_wrap(true);
			$message->set_alignment(0, 0.5);
			$message->set_size_request(330, -1);
			$text->pack_start($message, false, false);
		}
		$row = new GtkHBox(false, 6);
		$row->set_border_width(8);
		$row->pack_start($text);
		$remove = $this->closeButton();
		$remove->connect_simple('clicked', array($this, 'destroyWidget'), $row);
		$row->pack_end($remove, false, false);
		$this->notificationList->pack_start($row, false, false);
		$this->notificationList->reorder_child($row, 0);   // newest first
		$row->show_all();
	}

	function clearNotifications()
	{
		foreach ($this->notificationList->get_children() as $row) {
			$row->destroy();
		}
	}

	function showToast($header, $body)
	{
		$toast = new GtkWindow(Gtk::WINDOW_POPUP);
		$toast->set_name('skylight-toast');
		$toast->set_size_request(300, -1);

		$title = new GtkLabel($header);
		$title->modify_font(new PangoFontDescription('Bold 10'));
		$title->set_alignment(0, 0.5);
		$title->set_ellipsize(Pango::ELLIPSIZE_END);
		$text = new GtkVBox(false, 2);
		$text->pack_start($title, false, false);
		if ($body !== '') {
			$message = new GtkLabel($body);
			$message->set_line_wrap(true);
			$message->set_alignment(0, 0);
			$message->set_size_request(240, -1);
			$text->pack_start($message, false, false);
		}
		$close = $this->closeButton();
		$close->connect_simple('clicked', array($this, 'removeToast'), $toast);
		$row = new GtkHBox(false, 4);
		$row->set_border_width(10);
		$row->pack_start($text);
		$row->pack_end($close, false, false);
		$toast->add($row);

		$this->toasts[] = $toast;
		$this->layoutToasts();
		$toast->show_all();
		Gtk::timeout_add($this->settings['notificationTime'], array($this, 'removeToast'), $toast);
	}

	// Stacks toasts down the right-hand side, just below the panel.
	function layoutToasts()
	{
		list($left, $top) = $this->window->window->get_origin();
		$y = $top + $this->taskbar->allocation->height + 16;
		foreach ($this->toasts as $toast) {
			$size = $toast->size_request();
			$toast->move($toast->get_screen()->get_width() - 320, $y);
			$y += $size->height + 10;
		}
	}

	function removeToast($toast)
	{
		$remaining = array();
		foreach ($this->toasts as $existing) {
			if ($existing !== $toast) {
				$remaining[] = $existing;
			}
		}
		if (count($remaining) < count($this->toasts)) {
			$toast->destroy();
			$this->toasts = $remaining;
			$this->layoutToasts();
		}
		return false;   // run once
	}

	// A small flat button with GTK's standard close icon.
	function closeButton()
	{
		$button = new GtkButton();
		$button->set_relief(Gtk::RELIEF_NONE);
		$button->add(GtkImage::new_from_stock(Gtk::STOCK_CLOSE, Gtk::ICON_SIZE_MENU));
		return $button;
	}

	function destroyWidget($widget)
	{
		$widget->destroy();
	}

	// ---- Dialogs ----

	// With $onOK: OK and Cancel, and $onOK runs on OK. Without: an OK button only.
	// Returns the GtkMessageDialog.
	function openDialog($message, $onOK = null, $parent = null)
	{
		$dialog = new GtkMessageDialog($parent ? $parent : $this->window, 0,
			$onOK ? Gtk::MESSAGE_QUESTION : Gtk::MESSAGE_INFO,
			$onOK ? Gtk::BUTTONS_OK_CANCEL : Gtk::BUTTONS_OK, $message);
		$dialog->set_title('Skylight OS');
		$dialog->connect('response', array($this, 'onDialogResponse'), $onOK);
		$dialog->show_all();
		return $dialog;
	}

	function onDialogResponse($dialog, $response, $onOK)
	{
		$dialog->destroy();
		if ($onOK && $response == Gtk::RESPONSE_OK) {
			call_user_func($onOK);
		}
	}

	// ---- Apps and windows ----

	function readManifest($path)
	{
		if (!Storage::isAppPath($path)) {
			return null;
		}
		$manifest = Storage::readJson(SKYLIGHT_ROOT . '/' . $path . 'app.json', null);
		return (is_array($manifest) && isset($manifest['title'], $manifest['class'], $manifest['icons'])) ? $manifest : null;
	}

	// Opens the app in folder $path (e.g. "apps/terminal/"). Returns its AppID, or null.
	function openWindow($path, $args = array())
	{
		$manifest = $this->readManifest($path);
		if (!$manifest) {
			$this->notify('Cannot open app', $path . ' is not an app folder with a valid app.json.');
			return null;
		}
		require_once SKYLIGHT_ROOT . '/' . $path . 'app.php';
		$class = $manifest['class'];
		$appID = (string) $this->nextAppID++;

		$window = new GtkWindow();
		$window->set_title($manifest['title']);
		$window->set_icon(GdkPixbuf::new_from_file(SKYLIGHT_ROOT . '/' . $path . $manifest['icons']['32']));
		$window->set_default_size((int) $manifest['width'], (int) $manifest['height']);
		$window->set_transient_for($this->window);
		$window->set_skip_taskbar_hint(true);
		if (isset($manifest['position']) && $manifest['position'] == 'centered') {
			$window->set_position(Gtk::WIN_POS_CENTER_ON_PARENT);
		}
		$window->connect('delete-event', array($this, 'onWindowDelete'), $appID);

		$app = new $class($this, $appID, $path, $manifest, $window, $args);

		$button = new GtkButton();
		$buttonBox = new GtkHBox(false, 4);
		$buttonBox->pack_start($this->image(SKYLIGHT_ROOT . '/' . $path . $manifest['icons']['16'], 16), false, false);
		$buttonBox->pack_start(new GtkLabel($manifest['title']), false, false);
		$button->add($buttonBox);
		$button->connect_simple('clicked', array($this, 'toggleWindow'), $appID);
		$button->connect('button-press-event', array($this, 'onTaskbarPress'), $appID);
		$this->taskbar->pack_start($button, false, false);
		$button->show_all();

		$this->windows[$appID] = array('app' => $app, 'window' => $window, 'button' => $button, 'visible' => true);
		$app->build();
		$window->show_all();
		return $appID;
	}

	function closeWindow($appID)
	{
		if (isset($this->windows[$appID])) {
			$this->windows[$appID]['window']->destroy();
			$this->windows[$appID]['button']->destroy();
			unset($this->windows[$appID]);
		}
	}

	function onWindowDelete($window, $event, $appID)
	{
		if (isset($this->windows[$appID]) && $this->windows[$appID]['app']->canClose()) {
			$this->closeWindow($appID);
		}
		return true;   // we destroy the window ourselves
	}

	// Taskbar click: hide a visible window, bring back a hidden one.
	function toggleWindow($appID)
	{
		$entry =& $this->windows[$appID];
		if ($entry['visible']) {
			$entry['window']->hide();
			$entry['button']->set_relief(Gtk::RELIEF_NONE);
		} else {
			$entry['window']->present();
			$entry['button']->set_relief(Gtk::RELIEF_NORMAL);
		}
		$entry['visible'] = !$entry['visible'];
	}

	function onTaskbarPress($button, $event, $appID)
	{
		if ($event->button != 3) {
			return false;
		}
		$menu = new GtkMenu();
		$close = new GtkMenuItem('Close');
		$close->connect_simple('activate', array($this, 'closeWindow'), $appID);
		$menu->append($close);
		$menu->show_all();
		$menu->popup();
		return true;
	}

	function onDesktopDelete()
	{
		$this->logout();
		return true;
	}
}
