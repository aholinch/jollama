#!/bin/sh
rm -rf target
mkdir -p target
mkdir -p target/bin
mkdir -p target/jar

javac -classpath target/bin -d target/bin src/jollama/json/*.java
javac -classpath target/bin -d target/bin src/jollama/*.java
javac -classpath target/bin -d target/bin src/jollama/ui/*.java

jar cvf target/jar/jollama.jar -C target/bin .
