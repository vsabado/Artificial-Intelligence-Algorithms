import java.util.*;

public class RandomForest extends SupervisedLearner {
    private int numberOfTrees;
    private List<DecisionTree> nodes;
    private List<double[]> predictions;
    private double[] lastPrediction;

    public static String NAME = "RandomForest";

    @Override
    public String name() {
        return NAME;
    }

    public RandomForest(int numberOfTrees) {
        this.numberOfTrees = numberOfTrees;
        nodes = new ArrayList<>(numberOfTrees);
        predictions = new ArrayList<>();
    }

    @Override
    public void train(Matrix features, Matrix labels) {
        //Clear the node for the retrain.
        nodes.clear();
        predictions.clear();
        for (int i = 0; i < numberOfTrees; i++) {
            nodes.add(createDecisionTree(features, labels));
        }
    }

    private DecisionTree createDecisionTree(Matrix features, Matrix labels) {
        Matrix bootData = new Matrix();
        Matrix bootDataLabel = new Matrix();
        bootData.copyMetaData(features);
        bootDataLabel.copyMetaData(labels);

        //Create random data set
        Random rand = new Random();
        for (int i = 0; i < features.rows(); i++) {
            int ranNumber = rand.nextInt(features.rows());
            bootData.takeRow(features.row((ranNumber)));
            bootDataLabel.takeRow(labels.row(ranNumber));
        }
        return new DecisionTree(bootData, bootDataLabel);
    }

    @Override
    public double[] predict(double[] features) {
        predictions.clear();

        if (nodes.isEmpty()) {
            return null;
        }
        predictions = new ArrayList<>();

        for (DecisionTree tree : nodes) {
            predictions.add(tree.predict(features));
        }
        double[] consensus = getConsensus();
        return consensus;
    }


    public double[] getConsensus() {
        if(predictions.isEmpty()){
            return null;
        }
        double[] array = new double[predictions.size()];
        for (int i = 0; i < predictions.size(); i++) {
            array[i] = predictions.get(i)[0];
        }

        Map<Double, Integer> map = new HashMap<Double, Integer>();
        for (double i : array) {
            Integer count = map.get(i);
            if( !map.containsKey(i)){
                map.put(i, 0);
            }
            map.put(i, map.get(i) + 1);
        }

        Double popular = Collections.max(map.entrySet(),
                new Comparator<Map.Entry<Double,Integer>>() {
                    @Override
                    public int compare(Map.Entry<Double, Integer> o1, Map.Entry<Double, Integer> o2) {
                        return o1.getValue().compareTo(o2.getValue());
                    }
                }).getKey();
        lastPrediction = new double[]{popular};

        return lastPrediction;
    }
}
