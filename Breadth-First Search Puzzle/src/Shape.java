import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Shape {

    List<Point> points = new ArrayList<>();
    Color color;
    int id;

    public Shape(int id, Color color, int x1, int y1, int x2, int y2, int x3, int y3) {
        this.color = color;
        this.id = id;
        points.add(new Point(x1, y1));
        points.add(new Point(x2, y2));
        points.add(new Point(x3, y3));
    }

    public Shape(int id, Color color, int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4) {
        this(id, color, x1, y1, x2, y2, x3, y3);
        points.add(new Point(x4, y4));
    }

    public Shape(Color color, int x1, int y1) {
        this.color = color;
        points.add(new Point(x1, y1));
    }

    public List<Point> getPoints() {
        return points;
    }


    public int getId() {
        return id;
    }

    public List<Point> getPointByState(GameState state){
        List<Point> pts = new ArrayList<>();
        for(Point pt : getPoints()){
            Point realPt = new Point(pt.getX() + state.getState()[2 * getId()],
                    pt.getY() + state.getState()[2 * getId() + 1]);
            pts.add(realPt);
        }
        return pts;
    }

    public void setPoints(List<Point> points) {
        this.points = points;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setId(int id) {
        this.id = id;
    }
}
