public class LeafNode extends Node
{
    private double[] label;
    private Matrix matrix;

    public LeafNode(Matrix lab) {
        matrix = lab;
        label = new double[lab.cols()];
        for(int i = 0; i < lab.cols(); i++)
        {
            if(lab.valueCount(i) == 0)
                label[i] = lab.columnMean(i);
            else
                label[i] = lab.mostCommonValue(i);
        }
    }

    boolean isLeaf() { return true; }

    public double[] getLabel() {
        return label;
    }

    public void setLabel(double[] label) {
        this.label = label;
    }

    public Matrix getMatrix() {
        return matrix;
    }

    public void setMatrix(Matrix matrix) {
        this.matrix = matrix;
    }
}

