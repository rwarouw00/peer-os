# The Terminal: runs Python (Jython 2.1), with this window's App API as app.
import sys

code.setText("app.postNotification('Hello', 'Sent from the Terminal')\n"
             "print 'Hello,', app.getUsername()")
output.setEditable(0)
scope = {'app': app}   # variables you define stay here between runs

class Output:
    "Sends what print writes to the output box."
    def write(self, text):
        output.append(text)

def run():
    source = code.getText()
    saved = sys.stdout
    sys.stdout = Output()
    try:
        try:
            # A single expression shows its value; anything else runs as statements.
            try:
                value = eval(source, scope)
            except SyntaxError:
                exec source in scope
            else:
                if value is not None:
                    print '=>', repr(value)
        except:
            kind, error = sys.exc_info()[:2]
            print 'Error:', getattr(kind, '__name__', kind), error
    finally:
        sys.stdout = saved
    output.append('(done)\n')

def clearOutput():
    output.setText('')
