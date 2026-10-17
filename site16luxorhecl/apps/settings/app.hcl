# Settings: the desktop background, and how long notifications stay on screen.
set choices [list $background1 $background2 $background3 $background4 $background5 $background6]

# Java classes, as commands.
java java.awt.Color Color
java javax.swing.BorderFactory BorderFactory

# Outlines the background in use.
proc showChoice {} {
    global app choices
    set current [[$app getSettings] -field background]
    set blue [Color -new [list 48 128 224]]
    set n 1
    foreach choice $choices {
        if {eq $current "images/backgrounds/$n.jpg"} {
            $choice setBorder [BorderFactory createLineBorder $blue 3]
        } else {
            $choice setBorder [BorderFactory createEmptyBorder 3 3 3 3]
        }
        incr $n
    }
}

# command="choose 3" in app.xul calls this with 3.
proc choose {number} {
    global app
    if {$app setBackground "images/backgrounds/$number.jpg"} {
        showChoice
    }
}

proc saveTime {} {
    global app seconds
    set text [strtrim [$seconds getText]]
    # int fails on words, and turns 3.5 into 3, so the number must also read back the same.
    if {catch {set number [int $text]}} {
        set number ""
    }
    if {ne $number $text} {
        $app openDialog "Enter a whole number of seconds."
        return
    }
    set settings [$app getSettings]
    $settings -field notificationTime [* $number 1000]
    if {$app saveSettings $settings} {
        $app postNotification Settings "Notifications now stay for $number seconds."
    }
}

$seconds setText [/ [[$app getSettings] -field notificationTime] 1000]
showChoice
