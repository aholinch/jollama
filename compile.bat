rmdir target /s /q
mkdir target
mkdir target\bin
mkdir target\jar

javac -classpath target\bin -d target\bin src\jollama\json\*.java
javac -classpath target\bin -d target\bin src\jollama\*.java

jar cvf target\jar\jollama.jar -C target\bin .
