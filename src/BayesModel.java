import java.util.Hashtable;

public class BayesModel{
    private HashTable<String, Integer> frequencies;

    public BayesModel(){
        frequencies = new HashTable();
    }

    public void addToModel(String[] tokens){
        HashSet added = new HashSet();
        for(int i = 0; i < tokens.length; i++){
            if(!added.contains(tokens[i])) {
                addOneForKey(tokens[i]);
                added.add(tokens[i]);
            }

            if(i > 0){
                String key = tokens[i - 1] + " " + tokens[i];
                addOneForKey(key);
                added.add(key);
            }

            if(i > 1){
                String key = tokens[i - 2] + " " + tokens[i - 1] + " " + tokens[i];
                addOneForKey(key);
                added.add(key);
            }
        }
    }

    public void addOneForKey(String key) {
        Integer count = frequencies.get(key);
        Integer newVal;
        if(count == null) newVal = new Integer(1);
        else newVal = new Integer(count.intValue() + 1);
        frequencies.put(key, newVal);
    }
}