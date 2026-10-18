// Settings: shows the current settings, and saves changes. Settings (the
// service) checks them and tells every window that asked, such as the launcher
// and the desktop.
var SettingsApp = {
    load: function () {
        this.showBackground();
        document.getElementById("seconds").value = String(Settings.getNotificationTime() / 1000);
    },

    // Outlines the background in use: its button stays pressed.
    showBackground: function () {
        var current = Settings.getBackground();
        for (var n = 1; n <= 6; n++) {
            document.getElementById("bg" + n).checked = (current == "images/backgrounds/" + n + ".jpg");
        }
    },

    choose: function (n) {
        Settings.setBackground("images/backgrounds/" + n + ".jpg");
        this.showBackground();   // a picture stays chosen, even when clicked again
    },

    saveTime: function () {
        var text = String(document.getElementById("seconds").value).trim();
        if (!/^[0-9]+$/.test(text)) {
            alert("Enter a whole number of seconds.");
            return;
        }
        try {
            Settings.setNotificationTime(Number(text) * 1000);
            Notifications.post("Settings", "Notifications now stay for " + text + " seconds.");
        } catch (e) {
            alert(e.message);
        }
    }
};
