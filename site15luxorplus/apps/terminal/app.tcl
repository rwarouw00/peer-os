# The Terminal: runs Tcl (Jacl 1.3.3, which is Tcl 8.0) in this window's
# interpreter, so $app is the Terminal's App API.
$code setText {$app postNotification Hello {Sent from the Terminal}
puts "Hello, [$app getUsername]"
expr {6 * 7}}

# puts writes to the output box instead of the console.
rename puts tcl_puts
proc puts {args} {
    global output
    set end "\n"
    if {[string equal [lindex $args 0] -nonewline]} {
        set end ""
        set args [lrange $args 1 end]
    }
    $output append "[lindex $args end]$end"
}

# Runs the code at the top level, so variables stay for the next run.
proc run {} {
    global code output
    if {[catch {uplevel #0 [$code getText]} result]} {
        $output append "Error: $result\n"
    } elseif {![string equal $result ""]} {
        $output append "=> $result\n"
    }
    $output append "(done)\n"
}

proc clearOutput {} {
    global output
    $output setText ""
}
