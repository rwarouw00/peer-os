# The Web Browser. $page is the <iframe>'s Swing JEditorPane.
set history {}

# Shows a page; remember is 1 to add it to the history (Back and Reload pass 0).
proc show {url remember} {
    global page address history app
    if {< [strfind : $url] 0} {
        set url "http://$url"
    }
    if {catch {$page setPage $url} error} {
        $app openDialog "Can't open $url: [lindex [lindex $error 0] 1]"
        return
    }
    $address setText $url
    if {= $remember 1} {
        lappend $history $url
    }
}

proc go {} {
    global address
    show [strtrim [$address getText]] 1
}

proc back {} {
    global history
    set count [llen $history]
    if {> $count 1} {
        set history [lrange $history 0 [- $count 2]]
        show [lindex $history -1] 0
    }
}

proc reload {} {
    global page history
    if {> [llen $history] 0} {
        [$page getDocument] putProperty stream [null]   ;# makes setPage load it again
        show [lindex $history -1] 0
    }
}

# Swing events, handled in Hecl: a clicked link, and Enter in the address bar.
bind $page hyperlinkUpdate {
    if {eq [[$event getEventType] toString] ACTIVATED} {
        show [[$event getURL] toString] 1
    }
}
bind $address actionPerformed go

show [[[$app appFile start.html] toUri] toString] 1
