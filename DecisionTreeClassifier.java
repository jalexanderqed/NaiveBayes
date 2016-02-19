import java.net.URI;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Scanner;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;

public class DecisionTreeClassifier{

    static int MAX_ATT_NUM; // 1 to 3
    static double ALPHA; // 0.5 to 2 CONSIDER
    static double ATT_PORTION; // 0.25 to 1
    static double CAT_SKEW_WEIGHT; // 0.5 to 2
    static double OCCUR_WEIGHT; // -1 to 1
    static boolean NORMALIZE_WEIGHTS; // Indicates whether to normalize 0 vs. 1 probabilities
    static boolean STEM;

    static String[] g_training;
    static String[] g_testing;

    static String[] sw =
            {"I","a","about","an","are","as","at","be","by",
                    "for","from","how","in","is","it","of",
                    "on","or","that","the","this","to","was",
                    "what","when","where","who","will","with","the"};
    static HashSet<String> stopwords;

    public static void main(String[] args) {
        String training;
        String testing;

        stopwords = new HashSet<String>();
        for(int i = 0; i < sw.length; i++){
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

        double[][] totalPercents = new double[6][21];
        for (int maxRatio = 0; maxRatio <= totalPercents.length - 1; maxRatio++) {
            for (int minOccurrence = 0; minOccurrence <= totalPercents[0].length - 1; minOccurrence++) {
                totalPercents[maxRatio][minOccurrence] = 0;
            }
        }

        for (int trial = 0; trial < 5; trial++) {
            reshuffle(trainingData, testingData);

            BayesModel model = new BayesModel();

            for (int i = 0; i < g_training.length; i++) {
                model.addToModel(tokenize(g_training[i].substring(1)), g_training[i].charAt(0) == '1');
            }

            System.out.println("Finished constructing model.");

            int optRatio = 0;
            int optOccurrence = 0;
            double optPercent = 0;

            for (int maxRatio = 0; maxRatio <= totalPercents.length - 1; maxRatio++) {
                for (int minOccurrence = 0; minOccurrence <= totalPercents[0].length - 1; minOccurrence++) {
                    int numCorrect = 0;
                    int numWrong = 0;

                    for (int i = 0; i < g_testing.length; i++) {
                        boolean res = model.classify(tokenize(g_testing[i].substring(1)), minOccurrence * 2, maxRatio * 0.1 + 0.5);
                        char resChar = res ? '1' : '0';
                        if (resChar == g_testing[i].charAt(0))
                            numCorrect++;
                        else
                            numWrong++;
                    }

                    double percent = numCorrect / (double) (numCorrect + numWrong);

                    if (percent >= optPercent) {
                        optPercent = percent;
                        optRatio = maxRatio;
                        optOccurrence = minOccurrence;
                    }

                    /*
                    System.out.println("maxRatio value: " + (maxRatio * 0.1 + 0.5));
                    System.out.println("minOccurrence value: " + minOccurrence * 2);
                    System.out.println("Correct: " + numCorrect);
                    System.out.println("Wrong: " + numWrong);
                    System.out.println("Percentage: " + Math.round(percent * 1000) / 10.0 + "\n");
                    */

                    totalPercents[maxRatio][minOccurrence] += percent;
                }
            }

            System.out.println("Optimal ratio: " + (optRatio * 0.1 + 0.5));
            System.out.println("Optimal occurrence: " + optOccurrence * 2);
            System.out.println("Best percentage: " + optPercent + "\n");
        }

        int optRatio = 0;
        int optOccurrence = 0;

        for (int maxRatio = 0; maxRatio <= totalPercents.length - 1; maxRatio++) {
            for (int minOccurrence = 0; minOccurrence <= totalPercents[0].length - 1; minOccurrence++) {
                if(totalPercents[maxRatio][minOccurrence] > totalPercents[optRatio][optOccurrence]){
                    optRatio = maxRatio;
                    optOccurrence = optRatio;
                }
            }
        }

        System.out.println("optRatio: " + (optRatio * 0.1 + 0.5));
        System.out.println("optOccurrence: " + optOccurrence * 2);
        System.out.println("optPercent: " + totalPercents[optRatio][optOccurrence] / 5);
    }

    public static void reshuffle(String[] part1, String[] part2){
        ArrayList<String> posOptions = new ArrayList<String>();
        ArrayList<String> negOptions = new ArrayList<String>();

        for(int i = 0; i < part1.length; i++) {
            if (part1[i].charAt(0) == '1') posOptions.add(part1[i]);
            else negOptions.add(part1[i]);
        }
        for(int i = 0; i < part2.length; i++) {
            if (part2[i].charAt(0) == '1') posOptions.add(part2[i]);
            else negOptions.add(part2[i]);
        }

        g_training = new String[2000];
        g_testing = new String[500];

        for(int i = 0; i < g_training.length / 2; i++){
            int pos = (int)(Math.random() * posOptions.size());
            g_training[i] = posOptions.remove(pos);

            pos = (int)(Math.random() * posOptions.size());
            g_training[(g_training.length / 2) + i] = negOptions.remove(pos);
        }

        for(int i = 0; i < g_testing.length / 2; i++){
            g_testing[i] = posOptions.get(i);
            g_testing[(g_testing.length / 2) + i] = negOptions.get(i);
        }
    }

    public static ArrayList<String> tokenize(String post) {
        post = post.toLowerCase();
        String[] tokens = post.split("[^\\w\\-']|(\\-\\-)");
        ArrayList<String> finalTokens = new ArrayList<>(tokens.length);
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].equals("-") || tokens[i].equals("'")) tokens[i] = new String();

            while(tokens[i].length() > 1 && (tokens[i].charAt(0) < 'a' || tokens[i].charAt(0) > 'z'))
                tokens[i] = tokens[i].substring(1);
            while(tokens[i].length() > 1 && (tokens[i].charAt(tokens[i].length() - 1) < 'a' || tokens[i].charAt(tokens[i].length() - 1) > 'z'))
                tokens[i] = tokens[i].substring(0, tokens[i].length() - 1);
            if(tokens[i].length() == 1 && (tokens[i].charAt(0) < 'a' || tokens[i].charAt(0) > 'z'))
                tokens[i] = new String();
            if(stopwords.contains(tokens[i]))
                tokens[i] = new String();

            tokens[i] = tokens[i].trim();
            if (tokens[i].length() != 0){
                finalTokens.add(tokens[i]);
            }
        }

        return finalTokens;
    }
}

class BayesModel {
    private Hashtable<String, Integer> posFrequencies;
    private Hashtable<String, Integer> negFrequencies;
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
                double ratio = (double)Math.min((posContains ? posFrequencies.get(key).intValue() : 0), (negContains ? negFrequencies.get(key).intValue() : 0)) /
                        Math.max((posContains ? posFrequencies.get(key).intValue() : 0), (negContains ? negFrequencies.get(key).intValue() : 0));
                if(totalOccurrence >= minOccurrence && ratio <= maxRatio) {
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
                    if(totalOccurrence >= minOccurrence && ratio <= maxRatio) {
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
                    if(totalOccurrence >= minOccurrence && ratio <= maxRatio) {
                        posValue += bayesValueFor(key, true);
                        negValue += bayesValueFor(key, false);
                    }
                }
            }
        }

        posValue += Math.log((double)numPosDocs / (numNegDocs + numPosDocs));
        negValue += Math.log((double)numNegDocs / (numNegDocs + numPosDocs));

        return posValue > negValue;
    }

    public double bayesValueFor(String key, boolean positive) {
        Hashtable<String, Integer> source;
        int numDocs;

        if(positive){
            source = posFrequencies;
            numDocs = numPosDocs;
        }
        else{
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

    public void addOneForKey(String key, boolean positive) {
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