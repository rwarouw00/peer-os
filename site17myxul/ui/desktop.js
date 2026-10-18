// The desktop's script: the home menu, the taskbar, the shortcuts, the
// notification tray and the clock. The windows themselves are framed by
// the Desktop service, which tells this script when they change.

var Shell = {
    buttons: {},          // the taskbar's buttons, by window id
    shortcuts: null,      // the shortcuts shown, as Settings keeps them
    dragging: null,       // the shortcut being dragged
    unread: 0,            // notifications in the tray

    start: function () {
        Desktop.setArea(document.getElementById("area").node);
        Desktop.setOnChange(function () { Shell.updateTaskbar(); });
        Settings.setOnChange(function () { Shell.paint(); Shell.showShortcuts(); });
        Notifications.setOnPost(function (header, body) { Shell.addNotification(header, body); });
        this.paint();
        this.fillMenu();
        this.showShortcuts();
        this.tick();
        setInterval(function () { Shell.tick(); }, 1000);
    },

    // The background chosen in Settings
    paint: function () {
        document.getElementById("background").setAttribute("style",
            "background-image: url('" + Settings.getBackgroundURL() + "'); background-size: cover; background-position: center;");
    },

    // "2:23 pm   sam", as Skylight's clock shows it
    tick: function () {
        var now = new Date();
        var h = now.getHours(), m = now.getMinutes();
        document.getElementById("clock").value = (h % 12 || 12) + ":" + (m < 10 ? "0" : "") + m +
            (h < 12 ? " am" : " pm") + "   " + Desktop.getUserName();
    },

    launch: function (id) {
        document.getElementById("home-menu").hidePopup();
        Desktop.open(id);
    },

    // ---- The home menu ----

    fillMenu: function () {
        var page = document.getElementById("apps-page");
        var apps = Desktop.getApps();
        var row = null;
        for (var i = 0; i < apps.size(); i++) {
            var app = apps.get(i);
            if (i % 4 === 0) {
                row = document.createElement("hbox");
                row.setAttribute("spacing", "8");
                page.appendChild(row);
            }
            var tile = document.createElement("toolbarbutton");
            tile.setAttribute("id", "app-" + app.id);
            tile.setAttribute("app", app.id);
            tile.setAttribute("label", app.title);
            tile.setAttribute("image", app.icon);
            tile.setAttribute("orient", "vertical");
            tile.setAttribute("class", "desktop-menu-app");
            tile.setAttribute("tooltiptext", app.description);
            tile.setAttribute("context", "app-menu");
            row.appendChild(tile);
            tile.addEventListener("command", function () { Shell.launch(this.getAttribute("app")); });
        }
    },

    toggleMenu: function () {
        var menu = document.getElementById("home-menu");
        if (menu.state === "open") {
            menu.hidePopup();
        } else {
            this.showPage("apps");
            menu.openPopup(document.getElementById("home"), "after_start", 2, 6);
        }
    },

    showPage: function (page) {
        document.getElementById("apps-page").hidden = page !== "apps";
        document.getElementById("notifications-page").hidden = page !== "notifications";
    },

    quit: function () {
        document.getElementById("home-menu").hidePopup();
        if (Desktop.quit()) {
            window.close();
        }
    },

    // ---- The notification tray ----

    addNotification: function (header, body) {
        var list = document.getElementById("notification-list");
        var item = document.createElement("hbox");
        item.setAttribute("class", "desktop-notification");
        item.setAttribute("align", "start");
        var text = document.createElement("vbox");
        text.setAttribute("flex", "1");
        var title = document.createElement("label");
        title.setAttribute("value", header);
        title.setAttribute("style", "font-weight: bold;");
        text.appendChild(title);
        if (body) {
            var words = document.createElement("description");
            words.setAttribute("value", body);
            text.appendChild(words);
        }
        var close = document.createElement("toolbarbutton");
        close.setAttribute("label", "×");
        close.setAttribute("class", "desktop-flat");
        close.setAttribute("tooltiptext", "Remove");
        item.appendChild(text);
        item.appendChild(close);
        list.insertBefore(item, list.firstChild);   // the newest first
        close.addEventListener("command", function () {
            list.removeChild(item);
            Shell.countNotifications(-1);
        });
        this.countNotifications(1);
    },

    clearNotifications: function () {
        var list = document.getElementById("notification-list");
        while (list.firstChild) {
            list.removeChild(list.firstChild);
        }
        this.countNotifications(-this.unread);
    },

    countNotifications: function (change) {
        this.unread += change;
        document.getElementById("show-notifications").label = "Notifications" + (this.unread ? " (" + this.unread + ")" : "");
    },

    // ---- The taskbar: a button for each window ----

    updateTaskbar: function () {
        var bar = document.getElementById("taskbar");
        var windows = Desktop.getWindows();
        var open = {};
        for (var i = 0; i < windows.size(); i++) {
            var w = windows.get(i);
            var id = String(w.id);
            open[id] = true;
            var button = this.buttons[id];
            if (!button) {
                button = document.createElement("toolbarbutton");
                button.setAttribute("type", "checkbox");
                button.setAttribute("id", "task-" + id);
                button.setAttribute("window", id);
                button.setAttribute("class", "desktop-task");
                button.setAttribute("context", "taskbar-menu");
                button.setAttribute("maxwidth", "200");
                bar.appendChild(button);
                button.addEventListener("command", function () {
                    var frame = Desktop.getWindow(Number(this.getAttribute("window")));
                    if (frame) {
                        frame.toggle();
                    }
                });
                this.buttons[id] = button;
            }
            button.label = w.title;
            button.checked = w.active;
            button.setAttribute("image", w.icon);
            button.setAttribute("tooltiptext", w.title);
            button.setAttribute("style", w.minimized ? "opacity: 0.6;" : "");
        }
        for (var gone in this.buttons) {
            if (!open[gone]) {
                bar.removeChild(this.buttons[gone]);
                delete this.buttons[gone];
            }
        }
    },

    closeWindow: function (button) {
        var frame = Desktop.getWindow(Number(button.getAttribute("window")));
        if (frame) {
            frame.close();
        }
    },

    // ---- Shortcuts on the background: double-click to open, drag to move ----

    showShortcuts: function () {
        var saved = String(Settings.getShortcuts());
        if (saved === this.shortcuts) {
            return;   // as shown already (after a drag, say)
        }
        this.shortcuts = saved;
        var area = document.getElementById("area");
        while (area.firstChild) {
            area.removeChild(area.firstChild);
        }
        var list = saved ? saved.split(";") : [];
        for (var i = 0; i < list.length; i++) {
            var parts = list[i].split(" ");
            var app = Desktop.getApp(parts[0]);
            if (!app) {
                continue;
            }
            var item = document.createElement("vbox");
            item.setAttribute("id", "shortcut-" + app.id);
            item.setAttribute("app", app.id);
            item.setAttribute("left", parts[1]);
            item.setAttribute("top", parts[2]);
            item.setAttribute("align", "center");
            item.setAttribute("width", "84");
            item.setAttribute("class", "desktop-shortcut");
            item.setAttribute("context", "shortcut-menu");
            item.setAttribute("tooltiptext", app.description);
            var icon = document.createElement("image");
            icon.setAttribute("src", app.icon);
            var label = document.createElement("description");
            label.setAttribute("value", app.title);
            label.setAttribute("maxwidth", "88");
            item.appendChild(icon);
            item.appendChild(label);
            area.appendChild(item);
            item.addEventListener("dblclick", function () { Shell.launch(this.getAttribute("app")); });
            item.addEventListener("mousedown", this.grab);
            item.addEventListener("mousemove", this.drag);
            item.addEventListener("mouseup", this.drop);
        }
    },

    grab: function (event) {
        if (String(event.button) === "PRIMARY") {
            Shell.dragging = { item: this, x: event.screenX - Number(this.getAttribute("left")),
                y: event.screenY - Number(this.getAttribute("top")), moved: false };
        }
    },

    drag: function (event) {
        var d = Shell.dragging;
        if (!d || d.item !== this || String(event.eventType) !== "MOUSE_DRAGGED") {
            return;
        }
        var area = document.getElementById("area").node;
        this.setAttribute("left", Math.round(Math.max(0, Math.min(area.width - 84, event.screenX - d.x))));
        this.setAttribute("top", Math.round(Math.max(0, Math.min(area.height - 72, event.screenY - d.y))));
        d.moved = true;
    },

    drop: function () {
        var d = Shell.dragging;
        Shell.dragging = null;
        if (d && d.moved) {
            Shell.saveShortcuts();
        }
    },

    saveShortcuts: function () {
        var items = document.getElementById("area").childNodes;
        var parts = [];
        for (var i = 0; i < items.length; i++) {
            parts.push(items[i].getAttribute("app") + " " + items[i].getAttribute("left") + " " + items[i].getAttribute("top"));
        }
        this.shortcuts = parts.join(";");
        try {
            Settings.setShortcuts(this.shortcuts);
        } catch (e) {
            Notifications.post("Shortcuts not saved", e.message);
        }
    },

    addShortcut: function (id) {
        var area = document.getElementById("area");
        if (document.getElementById("shortcut-" + id)) {
            return;   // there's one already
        }
        var count = area.childNodes.length;
        var saved = this.shortcuts ? this.shortcuts + ";" : "";
        try {
            Settings.setShortcuts(saved + id + " " + (24 + 100 * Math.floor(count / 5)) + " " + (16 + 100 * (count % 5)));
        } catch (e) {
            Notifications.post("Shortcut not added", e.message);
        }
    },

    removeShortcut: function (id) {
        var kept = [];
        var list = this.shortcuts ? this.shortcuts.split(";") : [];
        for (var i = 0; i < list.length; i++) {
            if (list[i].split(" ")[0] !== id) {
                kept.push(list[i]);
            }
        }
        Settings.setShortcuts(kept.join(";"));
    }
};
