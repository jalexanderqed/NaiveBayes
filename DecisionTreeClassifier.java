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

    public static void main(String[] args) {
        setDefaults();
        String training;
        String testing;

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
        // String[] trainingData = testing.split("\n");
        // String[] testingData = training.split("\n");

        BayesModel model = new BayesModel();

        for (int i = 0; i < trainingData.length; i++) {
            model.addToModel(tokenize(trainingData[i].substring(1)), trainingData[i].charAt(0) == '1');
        }

        System.out.println("Finished constructing model.");

        int numCorrect = 0;
        int numWrong = 0;

        for (int i = 0; i < testingData.length; i++) {
            boolean res = model.classify(tokenize(testingData[i].substring(1)));
            char resChar = res ? '1' : '0';
            if(resChar == testingData[i].charAt(0))
                numCorrect++;
            else
                numWrong++;
        }

        double percent = numCorrect / (double)(numCorrect + numWrong);
        System.out.println("Correct: " + numCorrect);
        System.out.println("Wrong: " + numWrong);
        System.out.println("Percentage: " + Math.round(percent * 1000) / 10.0);

        if (true) return;
        int count = 0;
        for (int man = 1; man <= 3; man++) {
            MAX_ATT_NUM = man;
            for (double al = 0.5; al <= 2; al += 0.1) {
                ALPHA = al;
                for (double ap = 0.25; ap <= 1; ap += 0.05) {
                    ATT_PORTION = ap;
                    for (double csw = 0.5; csw <= 2; csw += 0.1) {
                        CAT_SKEW_WEIGHT = csw;
                        for (double ow = -1.0; ow <= 1.0; ow += 0.2) {
                            OCCUR_WEIGHT = ow;
                            for (int nw = 0; nw <= 1; nw++) {
                                NORMALIZE_WEIGHTS = (nw == 1);
                                for (int s = 0; s <= 1; s++) {
                                    STEM = (s == 1);





                                }
                                break;
                            }
                            break;
                        }
                        break;
                    }
                    break;
                }
                break;
            }
            break;
        }

        System.out.println(count);
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

            tokens[i] = tokens[i].trim();
            if (tokens[i].length() != 0){
                finalTokens.add(tokens[i]);
            }
        }

        return finalTokens;
    }

    public static void setDefaults() {
        MAX_ATT_NUM = 3;
        ALPHA = 0.5;
        ATT_PORTION = 0.75;
        CAT_SKEW_WEIGHT = 0.25;
        OCCUR_WEIGHT = 1;
        NORMALIZE_WEIGHTS = true;
        STEM = true;
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

    public boolean classify(ArrayList<String> tokens) {
        double posValue = 0.0;
        double negValue = 0.0;

        for (int i = 0; i < tokens.size(); i++) {
            String key = tokens.get(i);
            if (posFrequencies.containsKey(key) || negFrequencies.containsKey(key)) {
                posValue += bayesValueFor(key, true);
                negValue += bayesValueFor(key, false);
            }

            if (i > 0) {
                key = tokens.get(i - 1) + " " + tokens.get(i);
                if (posFrequencies.containsKey(key) || negFrequencies.containsKey(key)) {
                    posValue += bayesValueFor(key, true);
                    negValue += bayesValueFor(key, false);
                }
            }

            if (i > 1) {
                key = tokens.get(i - 2) + " " + tokens.get(i - 1) + " " + tokens.get(i);
                if (posFrequencies.containsKey(key) || negFrequencies.containsKey(key)) {
                    posValue += bayesValueFor(key, true);
                    negValue += bayesValueFor(key, false);
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