# The Web Browser. $page is the <iframe>'s Swing JEditorPane.
set history {}

proc show {url {remember 1}} {
    global page address history app
    if {[string first : $url] < 0} {
        set url http://$url
    }
    if {[catch {$page setPage $url} error]} {
        $app openDialog "Can't open $url: $error"
        return
    }
    $address setText $url
    if {$remember} {
        lappend history $url
    }
}

proc go {} {
    global address
    show [string trim [$address getText]]
}

proc back {} {
    global history
    set count [llength $history]
    if {$count > 1} {
        set history [lrange $history 0 [expr {$count - 2}]]
        show [lindex $history end] 0
    }
}

proc reload {} {
    global page history
    if {[llength $history] > 0} {
        [$page getDocument] putProperty stream [java::null]   ;# makes setPage load it again
        show [lindex $history end] 0
    }
}

# Swing events, handled in Tcl: a clicked link, and Enter in the address bar.
java::bind $page hyperlinkUpdate {
    if {[string equal [[[java::event] getEventType] toString] ACTIVATED]} {
        show [[[java::event] getURL] toString]
    }
}
java::bind $address actionPerformed go

show [[[$app appFile start.html] toUri] toString]
