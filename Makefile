JC = javac
JFLAGS=
*.class:
	$(JC) $(JFLAGS) src/*.java

CLASSES = \
	src/Main.java

default: classes
	java src.Main training.txt testing.txt

classes: $(CLASSES:.java=.class)

clean:
	$(RM) src/*.class
