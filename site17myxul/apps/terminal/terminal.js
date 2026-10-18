// The Terminal. The code runs in the global scope of this window's script
// engine, so variables it defines are still there for the next run.
var Terminal = {
    start: function () {
        document.getElementById("code").value =
            "Notifications.post('Hello', 'Sent from the Terminal');\n" +
            "print('This window is called ' + window.title);\n" +
            "6 * 7";
        document.getElementById("code").focus();
    },

    run: function () {
        var output = document.getElementById("output");
        var printed = [];
        var systemPrint = print;
        print = function () {   // print() writes to the output box instead of the console
            printed.push(Array.prototype.join.call(arguments, " "));
        };
        var text;
        try {
            var result = (0, eval)(String(document.getElementById("code").value));   // "indirect" eval: global scope
            text = printed.join("\n") + (printed.length ? "\n" : "") +
                (result === undefined || result === null ? "" : "=> " + result + "\n");
        } catch (error) {
            text = printed.join("\n") + (printed.length ? "\n" : "") + "Error: " + error + "\n";
        } finally {
            print = systemPrint;
        }
        output.appendText(text === "" ? "(done)\n" : text);
    },

    clear: function () {
        document.getElementById("output").value = "";
    }
};
