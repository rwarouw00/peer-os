# Settings: the desktop background, and how long notifications stay on screen.
from java.awt import Color
from javax.swing import BorderFactory

choices = [background1, background2, background3, background4, background5, background6]

def showChoice():
    "Outlines the background in use."
    current = app.getSettings().background
    for i in range(len(choices)):
        if current == 'images/backgrounds/%d.jpg' % (i + 1):
            choices[i].setBorder(BorderFactory.createLineBorder(Color(0x3080e0), 3))
        else:
            choices[i].setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3))

def choose(number):
    if app.setBackground('images/backgrounds/%d.jpg' % number):
        showChoice()

# One command per picture: a command calls a function with no arguments.
def choose1(): choose(1)
def choose2(): choose(2)
def choose3(): choose(3)
def choose4(): choose(4)
def choose5(): choose(5)
def choose6(): choose(6)

def saveTime():
    try:
        number = int(seconds.getText())
    except ValueError:
        app.openDialog('Enter a whole number of seconds.')
        return
    settings = app.getSettings()
    settings.notificationTime = number * 1000
    if app.saveSettings(settings):
        app.postNotification('Settings', 'Notifications now stay for %d seconds.' % number)

seconds.setText(str(app.getSettings().notificationTime / 1000))
showChoice()
