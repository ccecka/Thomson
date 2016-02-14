

build:
	javac -cp obj -O *.java -d obj
	cd obj; jar -cfm ../ShellApplet.jar Manifest *.class com org

clean:
	rm -f obj/*.class *~ ShellApplet.jar

run:
	java -jar ShellApplet.jar

%:
	make build
	cd obj; java -cp ../ShellApplet.jar:. $@
