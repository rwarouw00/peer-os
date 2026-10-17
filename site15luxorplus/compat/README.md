# Jacl 1.3.3 on Java 8

Jacl 1.3.3 (2006) reads and writes text through `sun.io.ByteToCharConverter` and `sun.io.CharToByteConverter`, private JDK classes that Java 8 removed, so on Java 8 it stops as soon as an interpreter starts (`NoClassDefFoundError: sun/io/UnknownCharacterException`).

The five classes under `sun/io/` are small stand-ins with the same names, written for Skylight on top of `java.nio.charset`, with only the methods Jacl calls. `tools/get-libs` compiles them into `lib/jacl-java8.jar`; Jacl's own jars are used exactly as released.
