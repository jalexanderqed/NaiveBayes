import java.net.URI;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Scanner;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;

public class NaiveBayesClassifier {
    static String[] sw =
            {"i", "a", "about", "an", "are", "as", "at", "be", "by",
                    "for", "from", "how", "in", "is", "it", "of",
                    "on", "or", "that", "the", "this", "to", "was",
                    "what", "when", "where", "who", "will", "with", "the"};
    static HashSet<String> stopwords;

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        String training;
        String testing;

        stopwords = new HashSet<String>();
        for (int i = 0; i < sw.length; i++) {
            stopwords.add(sw[i]);
        }

        try {
            training = new String(Files.readAllBytes(Paths.get(".", args[0])));
            testing = new String(Files.readAllBytes(Paths.get(".", args[1])));
        } catch (Exception e) {
            System.out.println("File not found");
            System.out.println(e.toString());
            return;
        }

        String regString = "\\<[\\w\\s/]*\\>";
        training = training.replaceAll(regString, "");
        testing = testing.replaceAll(regString, "");

        String[] trainingData = training.split("\n");
        String[] testingData = testing.split("\n");

        BayesModel model = new BayesModel();

        for (int i = 0; i < trainingData.length; i++) {
            model.addToModel(tokenize(trainingData[i].substring(1)), trainingData[i].charAt(0) == '1');
        }

        long trainTime = System.currentTimeMillis() - startTime;

        double maxRatio = 1.0;
        int minOccurrences = 3;

        int numCorrect = 0;
        int numWrong = 0;

        for (int i = 0; i < trainingData.length; i++) {
            boolean res = model.classify(tokenize(trainingData[i].substring(1)), minOccurrences, maxRatio);
            char resChar = res ? '1' : '0';
            if (resChar == trainingData[i].charAt(0))
                numCorrect++;
            else
                numWrong++;
        }
        double trainPercent = numCorrect / (double) (numCorrect + numWrong);

        startTime = System.currentTimeMillis();

        numCorrect = 0;
        numWrong = 0;
        for (int i = 0; i < testingData.length; i++) {
            boolean res = model.classify(tokenize(testingData[i].substring(1)), minOccurrences, maxRatio);
            char resChar = res ? '1' : '0';
            System.out.println(resChar);
            if (resChar == testingData[i].charAt(0))
                numCorrect++;
            else
                numWrong++;
        }
        double testPercent = numCorrect / (double) (numCorrect + numWrong);
        long testTime = System.currentTimeMillis() - startTime;

        System.out.println(Math.round((double) trainTime / 1000) + " seconds (training)");
        System.out.println(Math.round((double) testTime / 1000) + " seconds (labeling)");
        System.out.println(trainPercent + " (training)");
        System.out.println(testPercent + " (testing)");
    }

    public static ArrayList<String> tokenize(String post) {
        post = post.toLowerCase();
        String[] tokens = post.split("[^\\w\\-']|(\\-\\-)");
        ArrayList<String> finalTokens = new ArrayList<>(tokens.length);
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].equals("-") || tokens[i].equals("'")) tokens[i] = new String();

            while (tokens[i].length() > 1 && (tokens[i].charAt(0) < 'a' || tokens[i].charAt(0) > 'z'))
                tokens[i] = tokens[i].substring(1);
            while (tokens[i].length() > 1 && (tokens[i].charAt(tokens[i].length() - 1) < 'a'
                    || tokens[i].charAt(tokens[i].length() - 1) > 'z'))
                tokens[i] = tokens[i].substring(0, tokens[i].length() - 1);
            if (tokens[i].length() == 1 && (tokens[i].charAt(0) < 'a' || tokens[i].charAt(0) > 'z'))
                tokens[i] = new String();
            if (stopwords.contains(tokens[i]))
                tokens[i] = new String();

            tokens[i] = tokens[i].trim();
            if (tokens[i].length() != 0) {
                finalTokens.add(tokens[i]);
            }
        }

        return finalTokens;
    }
}

class BayesModel {
    public Hashtable<String, Integer> posFrequencies;
    public Hashtable<String, Integer> negFrequencies;
    private int numPosDocs;
    private int numNegDocs;

    public BayesModel() {
        posFrequencies = new Hashtable<String, Integer>();
        negFrequencies = new Hashtable<String, Integer>();
        numPosDocs = 0;
    }

    public boolean classify(ArrayList<String> tokens, int minOccurrence, double maxRatio) {
        double posValue = 0.0;
        double negValue = 0.0;

        for (int i = 0; i < tokens.size(); i++) {
            String key = tokens.get(i);
            boolean posContains = posFrequencies.containsKey(key);
            boolean negContains = negFrequencies.containsKey(key);

            if ((posContains || negContains)) {
                int totalOccurrence = (posContains ? posFrequencies.get(key).intValue() : 0) +
                        (negContains ? negFrequencies.get(key).intValue() : 0);
                double ratio = (double) Math.min((posContains ? posFrequencies.get(key).intValue() : 0), (negContains ? negFrequencies.get(key).intValue() : 0)) /
                        Math.max((posContains ? posFrequencies.get(key).intValue() : 0), (negContains ? negFrequencies.get(key).intValue() : 0));
                if (totalOccurrence >= minOccurrence && ratio <= maxRatio) {
                    posValue += bayesValueFor(key, true);
                    negValue += bayesValueFor(key, false);
                }
            }

            if (i > 0) {
                key = tokens.get(i - 1) + " " + tokens.get(i);
                posContains = posFrequencies.containsKey(key);
                negContains = negFrequencies.containsKey(key);

                if (posContains || negContains) {
                    int totalOccurrence = (posContains ? posFrequencies.get(key).intValue() : 0) +
                            (negContains ? negFrequencies.get(key).intValue() : 0);
                    double ratio = Math.min((posContains ? posFrequencies.get(key).intValue() : 0), (negContains ? negFrequencies.get(key).intValue() : 0)) /
                            Math.max((posContains ? posFrequencies.get(key).intValue() : 0), (negContains ? negFrequencies.get(key).intValue() : 0));
                    if (totalOccurrence >= minOccurrence && ratio <= maxRatio) {
                        posValue += bayesValueFor(key, true);
                        negValue += bayesValueFor(key, false);
                    }
                }
            }

            if (i > 1) {
                key = tokens.get(i - 2) + " " + tokens.get(i - 1) + " " + tokens.get(i);
                posContains = posFrequencies.containsKey(key);
                negContains = negFrequencies.containsKey(key);

                if (posContains || negContains) {
                    int totalOccurrence = (posContains ? posFrequencies.get(key).intValue() : 0) +
                            (negContains ? negFrequencies.get(key).intValue() : 0);
                    double ratio = Math.min((posContains ? posFrequencies.get(key).intValue() : 0), (negContains ? negFrequencies.get(key).intValue() : 0)) /
                            Math.max((posContains ? posFrequencies.get(key).intValue() : 0), (negContains ? negFrequencies.get(key).intValue() : 0));
                    if (totalOccurrence >= minOccurrence && ratio <= maxRatio) {
                        posValue += bayesValueFor(key, true);
                        negValue += bayesValueFor(key, false);
                    }
                }
            }
        }

        posValue += Math.log((double) numPosDocs / (numNegDocs + numPosDocs));
        negValue += Math.log((double) numNegDocs / (numNegDocs + numPosDocs));

        return posValue > negValue;
    }

    private double bayesValueFor(String key, boolean positive) {
        Hashtable<String, Integer> source;
        int numDocs;

        if (positive) {
            source = posFrequencies;
            numDocs = numPosDocs;
        } else {
            source = negFrequencies;
            numDocs = numNegDocs;
        }

        double prob;
        Integer freq = source.get(key);
        if (freq == null) freq = new Integer(0);

        prob = (1 + freq.intValue()) / (double) numDocs;

        return Math.log(prob);
    }

    public void addToModel(ArrayList<String> tokens, boolean positive) {
        HashSet<String> added = new HashSet<String>();
        for (int i = 0; i < tokens.size(); i++) {
            if (!added.contains(tokens.get(i))) {
                addOneForKey(tokens.get(i), positive);
                added.add(tokens.get(i));
            }

            if (i > 0) {
                String key = tokens.get(i - 1) + " " + tokens.get(i);
                if (!added.add(key)) {
                    addOneForKey(key, positive);
                    added.add(key);
                }
            }

            if (i > 1) {
                String key = tokens.get(i - 2) + " " + tokens.get(i - 1) + " " + tokens.get(i);
                if (!added.contains(key)) {
                    addOneForKey(key, positive);
                    added.add(key);
                }
            }
        }
        if (positive) numPosDocs++;
        else numNegDocs++;
    }

    private void addOneForKey(String key, boolean positive) {
        Integer count;
        if (positive)
            count = posFrequencies.get(key);
        else
            count = negFrequencies.get(key);

        Integer newVal;
        if (count == null) newVal = new Integer(1);
        else newVal = new Integer(count.intValue() + 1);

        if (positive)
            posFrequencies.put(key, newVal);
        else
            negFrequencies.put(key, newVal);
    }

    public void printOccurrences() {
        for (String key : posFrequencies.keySet()) {
            System.out.println(key + ": " + posFrequencies.get(key));
        }

        System.out.println("\n" + numNegDocs);
        System.out.println("\n" + numPosDocs);
    }
}