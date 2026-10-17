# Finds the Java tools and JavaFX's jfxrt.jar, for build.sh and run.sh.
# Java 7 keeps jfxrt.jar in jre/lib (not on the class path); Java 8 in jre/lib/ext.
JAVA_BIN="${JAVA_HOME:+$JAVA_HOME/bin}"
JAVA_BIN="${JAVA_BIN:-$(dirname "$(command -v java)")}"
JFXRT=""
for f in "$JAVA_HOME/jre/lib/jfxrt.jar" "$JAVA_HOME/lib/jfxrt.jar" "$JAVA_HOME/jre/lib/ext/jfxrt.jar" "$JAVA_HOME/lib/ext/jfxrt.jar"; do
	if [ -n "$JAVA_HOME" ] && [ -f "$f" ]; then JFXRT="$f"; break; fi
done
