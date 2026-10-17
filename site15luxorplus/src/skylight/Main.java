package skylight;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Starts Skylight OS: Luxor XUL for the windows, Tcl (Jacl) for the apps' scripts, Swing underneath. */
public final class Main {

	private Main() {
	}

	public static void main(String[] args) {
		UIManager.put("swing.boldMetal", Boolean.FALSE);
		SwingUtilities.invokeLater(() -> {
			Xul.init();
			new Desktop().showLogin();
		});
	}
}
