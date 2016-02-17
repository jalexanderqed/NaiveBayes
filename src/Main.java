import java.net.URI;
import java.nio.file.Files;
import java.util.Scanner;
import java.nio.file.Paths;
import BayesModel;

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

        // Scanner sc = new Scanner(System.in); // For debugging
        String[] examples = testing.split("\n");
        String[][] tokenized = new String[examples.length][0];
        BayesModel model = new BayesModel();

        for (int i = 0; i < examples.length; i++) {
            tokenized[i] = tokenize(examples[i]);
            ArrayList<String> nonEmpty = new ArrayList(tokenized.length);
            for(int j = 0; j < tokenized.length; j++){
                if(tokenized[i][j].length() != 0) nonEmpty.add(tokenized[i][j]);
            }
            tokenized[i] = nonEmpty.toArray(tokenized[i]);
            model.addToModel(tokenized[i]);
        }

        System.out.println("Finished constructing model.");

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

    public static String[] tokenize(String post) {
        post = post.toLowerCase().substring(1);
        String[] tokens = post.split("[^\\w\\-']|(\\-\\-)");
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].equals("-") || tokens[i].equals("'")) tokens[i] = "";
            if (tokens[i].length() == 0) continue;
        }
        return tokens;
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