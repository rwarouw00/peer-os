# The Web Browser. Luxor's <iframe> can't be steered from a script, so the
# page is a Swing JEditorPane put into the <gadget id="page"> slot.
import sys
from javax.swing import JEditorPane, JScrollPane
from javax.swing.event import HyperlinkEvent

view = JEditorPane()
view.setEditable(0)
page.add(JScrollPane(view))
history = []

def show(url, remember=1):
    if url.find(':') < 0:
        url = 'http://' + url
    try:
        view.setPage(url)
    except:
        app.openDialog("Can't open " + url + ": " + str(sys.exc_info()[1]))
        return
    address.setText(url)
    if remember:
        history.append(url)

def go():
    show(address.getText().strip())

def back():
    if len(history) > 1:
        history.pop()
        show(history[-1], 0)

def reload():
    if history:
        view.getDocument().putProperty('stream', None)   # makes setPage load it again
        show(history[-1], 0)

def followLink(event):
    if event.getEventType() == HyperlinkEvent.EventType.ACTIVATED:
        show(event.getURL().toString())

view.hyperlinkUpdate = followLink        # a Swing event, handled by a Python function
address.actionPerformed = lambda event: go()   # Enter in the address bar
show(app.appFile('start.html').toUri().toString())
