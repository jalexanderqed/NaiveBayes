package src;

import java.net.URI;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Scanner;
import java.nio.file.Paths;
import src.BayesModel;

public class Main {

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

        System.out.println("Correct: " + numCorrect);
        System.out.println("Wrong: " + numWrong);

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