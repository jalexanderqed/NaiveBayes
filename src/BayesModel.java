package src;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;

public class BayesModel {
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
                posValue += bayesValueFor(key, posFrequencies, numPosDocs);
                negValue += bayesValueFor(key, negFrequencies, numNegDocs);
            }

            if (i > 0) {
                key = tokens.get(i - 1) + " " + tokens.get(i);
                if (posFrequencies.containsKey(key) || negFrequencies.containsKey(key)) {
                    posValue += bayesValueFor(key, posFrequencies, numPosDocs);
                    negValue += bayesValueFor(key, negFrequencies, numNegDocs);
                }
            }

            if (i > 1) {
                key = tokens.get(i - 2) + " " + tokens.get(i - 1) + " " + tokens.get(i);
                if (posFrequencies.containsKey(key) || negFrequencies.containsKey(key)) {
                    posValue += bayesValueFor(key, posFrequencies, numPosDocs);
                    negValue += bayesValueFor(key, negFrequencies, numNegDocs);
                }
            }
        }

        return posValue > negValue;
    }

    public double bayesValueFor(String key, Hashtable<String, Integer> source, int numDocs) {
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