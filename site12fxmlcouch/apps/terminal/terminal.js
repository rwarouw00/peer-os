// The Terminal's Run button. The code runs in the global scope of this window's
// script engine, so variables it defines are still there for the next run.
function run() {
	var printed = [];
	var systemPrint = print;
	print = function () {
		printed.push(Array.prototype.join.call(arguments, ' '));
	};
	var text;
	try {
		var result = (0, eval)(code.getText());   // "indirect" eval: global scope
		text = printed.join('\n') + (printed.length ? '\n' : '') +
			(result === undefined || result === null ? '' : '=> ' + result + '\n');
	} catch (error) {
		text = printed.join('\n') + (printed.length ? '\n' : '') + 'Error: ' + error + '\n';
	} finally {
		print = systemPrint;
	}
	output.appendText(text === '' ? '(done)\n' : text);
}
