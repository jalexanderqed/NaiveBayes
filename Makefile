JC = javac
JFLAGS=
*.class:
	$(JC) $(JFLAGS) ./*.java

CLASSES = \
	./DecisionTreeClassifier.java

default: classes

classes: DecisionTreeClassifier.class

DecisionTreeClassifier.class: DecisionTreeClassifier.java

clean:
	$(RM) *.class
