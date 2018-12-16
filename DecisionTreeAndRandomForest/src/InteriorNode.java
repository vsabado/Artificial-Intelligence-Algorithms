public class InteriorNode extends Node
{
    private int attribute; // which attribute to divide on
    private double pivot; // which value to divide on
    private Node a;
    private Node b;
    private boolean isCategorical = false;

    public InteriorNode(Node a, Node b, int col, double pivot, boolean isCategorical) {
        this.a = a;
        this.b = b;
        this.attribute = col;
        this.pivot = pivot;
        this.isCategorical = isCategorical;
    }
    boolean isLeaf() { return false; }

    public int getAttribute() {
        return attribute;
    }

    public void setAttribute(int attribute) {
        this.attribute = attribute;
    }

    public double getPivot() {
        return pivot;
    }

    public void setPivot(double pivot) {
        this.pivot = pivot;
    }

    public Node getA() {
        return a;
    }

    public void setA(Node a) {
        this.a = a;
    }

    public Node getB() {
        return b;
    }

    public void setB(Node b) {
        this.b = b;
    }

    public boolean isCategorical() {
        return isCategorical;
    }

    public void setCategorical(boolean categorical) {
        isCategorical = categorical;
    }
}
