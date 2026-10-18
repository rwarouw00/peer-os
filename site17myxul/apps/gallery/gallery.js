// Gallery: each XUL element doing something. Messages go to the status bar.

var Gallery = {
	log: function (message) {
		document.getElementById("log").label = message;
	},

	loaded: function () {
		var clock = document.getElementById("clock");
		var tick = function () {
			var now = new Date();
			var mm = now.getMinutes() < 10 ? "0" + now.getMinutes() : now.getMinutes();
			clock.label = now.getHours() + ":" + mm;
		};
		tick();
		setInterval(tick, 10000);
		this.log("Loaded " + document.getElementsByTagName("*").length + " elements");
	},

	greet: function () {
		var name = document.getElementById("name").value;
		document.getElementById("greeting").value = "Hello, " + (name === "" ? "world" : name) + "!";
		this.log("greeted");
	},

	enableGreeting: function (on) {
		// One attribute on the <command>, and every element using it follows.
		document.getElementById("cmd-greet").setAttribute("disabled", on ? "false" : "true");
		this.log(on ? "hello enabled" : "hello disabled");
	},

	nameTyped: function (value) {
		this.log("typing: " + value);
	},

	agreed: function (on) {
		document.getElementById("more").setAttribute("disabled", !on);
	},

	more: function () {
		var bar = document.getElementById("progress");
		var value = Math.min(100, Number(bar.value) + 10);
		bar.value = value;
		this.log("progress " + value);
	},

	ask: function () {
		var answer = prompt("What's your favourite colour?", "green");
		if (answer != null && confirm("Is " + answer + " really your favourite?")) {
			alert("Then " + answer + " it is.");
			this.log("favourite: " + answer);
		} else {
			this.log("no answer");
		}
	},

	addFruit: function () {
		var box = document.getElementById("new-fruit");
		var fruit = String(box.value).trim();
		if (fruit !== "") {
			var list = document.getElementById("fruit");
			list.appendItem(fruit);
			box.value = "";
			this.log(list.getRowCount() + " fruit");
		}
	},

	removeFruit: function () {
		var list = document.getElementById("fruit");
		if (list.selectedIndex >= 0) {
			list.removeItemAt(list.selectedIndex);
			this.log(list.getRowCount() + " fruit");
		}
	},

	theme: function (colour) {
		document.getElementById("pages").setAttribute("style", "background-color: " + colour + ";");
		this.log("theme " + colour);
	},

	restyle: function () {
		var colours = ["#ffd7d7", "#d7ffd9", "#d7e6ff"];
		this.turn = ((this.turn || 0) + 1) % colours.length;
		document.getElementById("restyled").setAttribute("style",
			"background-color: " + colours[this.turn] + "; padding: " + (10 + 6 * this.turn) + "px; border: 1px solid #777777;");
		this.log("restyled " + this.turn);
	}
};
