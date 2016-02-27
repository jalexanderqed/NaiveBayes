JC = javac
JFLAGS=
*.class:
	$(JC) $(JFLAGS) ./*.java

CLASSES = \
	./NaiveBayesClassifier.java

default: classes

classes: NaiveBayesClassifier.class

DecisionTreeClassifier.class: NaiveBayesClassifier.java

BayesModel.class: NaiveBayesClassifier.java

clean:
	$(RM) *.class
