import util.Vec2;
import util.Vec3;
import util.Vec4;

public class Vertex {

    public Vec3 objectCoordinates;
    public Vec3 color;
    public Vec4 homogenousColor;
    public Vec3 worldCoordinates;
    public Vec3 normal;
    public Vec3 worldNormal;
    public Vec2 screenPosition;

    public Vertex(Vec3 objectCoordinates, Vec3 color) {
        this.objectCoordinates = objectCoordinates;
        this.color = color;
    }
}
