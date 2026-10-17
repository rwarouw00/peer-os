# The Terminal: runs Hecl in this window's interpreter, so $app is the
# Terminal's App API.
$code setText {$app postNotification Hello {Sent from the Terminal}
puts "Hello, [$app getUsername]"
* 6 7}

# puts writes to the output box instead of the console.
rename puts heclputs
proc puts {text} {
    global output
    $output append "$text\n"
}

# Runs the code at the top level (upeval 0), so variables stay for the next run.
proc run {} {
    global code output
    if {catch {upeval 0 [$code getText]} result} {
        # A caught error is a list: {ERROR message line} {command line} ...
        set error [lindex $result 0]
        set line [lindex $error 2]
        if {eq $line ""} {
            $output append "Error: [lindex $error 1]\n"
        } else {
            $output append "Error: [lindex $error 1] (line $line)\n"
        }
    } elseif {ne $result ""} {
        $output append "=> $result\n"
    }
    $output append "(done)\n"
}

proc clearOutput {} {
    global output
    $output setText ""
}
