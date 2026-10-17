# Settings: the desktop background, and how long notifications stay on screen.
set choices [list $background1 $background2 $background3 $background4 $background5 $background6]

# Outlines the background in use.
proc showChoice {} {
    global app choices
    set current [java::field [$app getSettings] background]
    set blue [java::new {java.awt.Color int int int} 48 128 224]
    set n 1
    foreach choice $choices {
        if {[string equal $current images/backgrounds/$n.jpg]} {
            $choice setBorder [java::call javax.swing.BorderFactory createLineBorder $blue 3]
        } else {
            $choice setBorder [java::call javax.swing.BorderFactory createEmptyBorder 3 3 3 3]
        }
        incr n
    }
}

# command="choose 3" in app.xul calls this with 3.
proc choose {number} {
    global app
    if {[$app setBackground images/backgrounds/$number.jpg]} {
        showChoice
    }
}

proc saveTime {} {
    global app seconds
    set text [string trim [$seconds getText]]
    if {![regexp {^[0-9]+$} $text] || [scan $text %d number] != 1} {
        $app openDialog "Enter a whole number of seconds."
        return
    }
    set settings [$app getSettings]
    java::field $settings notificationTime [expr {$number * 1000}]
    if {[$app saveSettings $settings]} {
        $app postNotification Settings "Notifications now stay for $number seconds."
    }
}

$seconds setText [expr {[java::field [$app getSettings] notificationTime] / 1000}]
showChoice
