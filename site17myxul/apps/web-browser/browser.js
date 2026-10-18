// The Web Browser. The <browser> element is a JavaFX WebView; its XUL methods
// (loadURI, goBack, goForward, reload, stop) and properties (currentURI,
// contentTitle) are myxul's, and its JavaFX ones are there too.
var Browser = {
    page: function () {
        return document.getElementById("page");
    },

    // The start page began loading as the window was built, before onlocationchange was listening.
    start: function () {
        document.getElementById("address").value = this.page().currentURI;
        this.update();
    },

    // Enter in the address bar, or Go: "http://" is added when there's no scheme.
    go: function () {
        var url = String(document.getElementById("address").value).trim();
        if (url === "") {
            return;
        }
        if (!/^[a-z][a-z0-9+.-]*:/i.test(url)) {
            url = "http://" + url;
        }
        this.page().loadURI(url);
    },

    back: function () { this.page().goBack(); },
    forward: function () { this.page().goForward(); },
    reload: function () { this.page().reload(); },
    stop: function () { this.page().stop(); },
    home: function () { this.page().loadURI("start.html"); },

    focusAddress: function () {
        var address = document.getElementById("address");
        address.focus();
        address.selectAll();
    },

    // onlocationchange: a new page has started loading.
    started: function () {
        document.getElementById("address").value = this.page().currentURI;
        document.getElementById("status").label = "Loading " + this.page().currentURI + "...";
        document.getElementById("stop").disabled = false;
        this.update();
    },

    // onload: it has loaded.
    loaded: function () {
        var title = this.page().contentTitle;
        window.title = (title ? title + " - " : "") + "Web Browser";
        document.getElementById("status").label = "Done";
        document.getElementById("stop").disabled = true;
        this.update();
    },

    // onerror: it couldn't be loaded.
    failed: function () {
        document.getElementById("status").label = "Couldn't load " + this.page().currentURI;
        document.getElementById("stop").disabled = true;
        this.update();
    },

    update: function () {
        document.getElementById("back").disabled = !this.page().canGoBack();
        document.getElementById("forward").disabled = !this.page().canGoForward();
    }
};
