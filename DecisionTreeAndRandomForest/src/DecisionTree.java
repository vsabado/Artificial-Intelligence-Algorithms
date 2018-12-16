import java.sql.SQLOutput;
import java.util.Random;

public class DecisionTree extends SupervisedLearner {
    private Node root;
    private Random rand = new Random();
    private double[] lastPrediction = null;

    public DecisionTree(Matrix features, Matrix labels) {
        train(features, labels);
    }

    public DecisionTree() {
        super();
    }

    @Override
    String name() {
        return "DecisionTree";
    }

    public double[] getLastPrediction() {
        return lastPrediction;
    }

    @Override
    public void train(Matrix features, Matrix labels) {
        root = buildTree(features, labels);
    }

    private Node buildTree(Matrix features, Matrix labels) {
        if (features.rows() != labels.rows()) {
            throw new RuntimeException("Mismatching features and labels");
        }

        Matrix featuresA = null;
        Matrix featuresB = null;
        Matrix labelsA = null;
        Matrix labelsB = null;
        int col = 0;
        double pivot = 0;
        boolean isCategorical = false;

        for (int tryAgain = 8; tryAgain > 0; tryAgain--) {
            (featuresA = new Matrix()).copyMetaData(features);
            (featuresB = new Matrix()).copyMetaData(features);
            (labelsA = new Matrix()).copyMetaData(labels);
            (labelsB = new Matrix()).copyMetaData(labels);
            col = rand.nextInt(features.cols() - 1);
            int checkCategorical = features.valueCount(col);
            pivot = getPivot(features, col);

            for (int i = 0; i < features.rows(); i++) {
                if (checkCategorical == 0) {
                    if (features.row(i)[col] < pivot) {
                        featuresA.takeRow(features.row((i)));
                        labelsA.takeRow(labels.row(i));
                    } else {
                        featuresB.takeRow(features.row(i));
                        labelsB.takeRow(labels.row(i));
                    }
                } else {
                    isCategorical = true;
                    if (pivot == features.row(i)[col]) {
                        featuresA.takeRow(features.row(i));
                        labelsA.takeRow(labels.row(i));
                    } else {
                        featuresB.takeRow(features.row(i));
                        labelsB.takeRow(labels.row(i));
                    }
                }
            }
            if (featuresA.rows() != 0 && featuresB.rows() != 0) {
                break;
            }
        }

        if (featuresA.rows() == 0 || featuresB.rows() == 0) {
            return new LeafNode(labels);
        }

        //Make node
        Node a = buildTree(featuresA, labelsA);
        Node b = buildTree(featuresB, labelsB);
        return new InteriorNode(a, b, col, pivot, isCategorical);
    }

    public double getPivot(Matrix features, int col) {
        return features.row(rand.nextInt(features.rows()))[col];
    }


    @Override
    public double[] predict(double[] features) {
        Node node = root;
        while (true) {
            if (node instanceof InteriorNode) {
                if (((InteriorNode) node).isCategorical()) {
                    if (features[((InteriorNode) node).getAttribute()] == ((InteriorNode) node).getPivot()) {
                        node = ((InteriorNode) node).getA();
                    } else {
                        node = ((InteriorNode) node).getB();
                    }
                } else {
                    if (features[((InteriorNode) node).getAttribute()] < ((InteriorNode) node).getPivot()) {
                        node = ((InteriorNode) node).getA();
                    } else {
                        node = ((InteriorNode) node).getB();
                    }
                }
            } else {
                //Prediction
                lastPrediction = ((LeafNode) node).getLabel();
                return lastPrediction;
            }
        }
    }


    public Node getRoot() {
        return root;
    }

    public void setRoot(Node root) {
        this.root = root;
    }

    public Random getRand() {
        return rand;
    }

    public void setRand(Random rand) {
        this.rand = rand;
    }
}