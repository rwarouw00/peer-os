<?php
/*
 * Terminal: runs PHP code inside Skylight OS. $app is this app, so
 * $app->postNotification('Hi'); and the rest of the App API work.
 */
class TerminalApp extends App
{
	private $code;
	private $output;

	function build()
	{
		$this->code = $this->textView('#000000', '#ffffff');
		$this->output = $this->textView('#1e1e1e', '#9fe39f');
		$this->output->set_editable(false);

		$run = new GtkButton('Run');
		$run->connect_simple('clicked', array($this, 'run'));
		$clear = new GtkButton('Clear output');
		$clear->connect_simple('clicked', array($this, 'clearOutput'));
		$toolbar = new GtkHBox(false, 4);
		$toolbar->pack_start($run, false, false);
		$toolbar->pack_start($clear, false, false);
		$toolbar->pack_end(new GtkLabel('PHP code. $app is this app.'), false, false);

		$panes = new GtkVPaned();
		$panes->pack1($this->scrolled($this->code), true);
		$panes->pack2($this->scrolled($this->output), true);
		$panes->set_position(200);

		$box = new GtkVBox(false, 4);
		$box->set_border_width(4);
		$box->pack_start($toolbar, false, false);
		$box->pack_start($panes);
		$this->window->add($box);
	}

	function textView($background, $foreground)
	{
		$view = new GtkTextView();
		$view->modify_font(new PangoFontDescription('Monospace 10'));
		$view->modify_base(Gtk::STATE_NORMAL, GdkColor::parse($background));
		$view->modify_text(Gtk::STATE_NORMAL, GdkColor::parse($foreground));
		$view->modify_cursor(GdkColor::parse($foreground), GdkColor::parse($foreground));
		return $view;
	}

	function scrolled($widget)
	{
		$scroller = new GtkScrolledWindow();
		$scroller->set_policy(Gtk::POLICY_AUTOMATIC, Gtk::POLICY_AUTOMATIC);
		$scroller->set_shadow_type(Gtk::SHADOW_IN);
		$scroller->add($widget);
		return $scroller;
	}

	// Runs the code and shows what it printed, returned, warned or threw.
	// Note: a PHP *fatal* error (such as calling an undefined function) stops
	// the whole of Skylight OS, as it would any PHP program.
	function run()
	{
		$buffer = $this->code->get_buffer();
		$source = $buffer->get_text($buffer->get_start_iter(), $buffer->get_end_iter());
		$app = $this;

		set_error_handler(array($this, 'printWarning'));
		ob_start();
		$result = null;
		try {
			$result = eval($source);
		} catch (Exception $error) {
			echo get_class($error) . ': ' . $error->getMessage() . "\n";
		}
		$printed = ob_get_clean();
		restore_error_handler();

		$text = $printed;
		if ($result !== null && $result !== false) {
			$text .= '=> ' . var_export($result, true) . "\n";
		}
		$this->appendOutput($text === '' ? "(done)\n" : $text);
	}

	// Shows warnings and notices in the output, in order, instead of PHP's error log.
	function printWarning($number, $message, $file, $line)
	{
		echo 'Warning: ' . $message . "\n";
		return true;
	}

	function appendOutput($text)
	{
		$buffer = $this->output->get_buffer();
		$buffer->insert($buffer->get_end_iter(), $text);
		$this->output->scroll_to_iter($buffer->get_end_iter(), 0);
	}

	function clearOutput()
	{
		$this->output->get_buffer()->set_text('');
	}
}
