import util.Vec2;
import util.Vec3;
import util.Vec4;

public class Vertex {

    public Vec3 objectCoordinates;
    public Vec3 worldCoordinates; //after m
    public Vec3 viewCoordinates;
    public Vec3 clippedCoordinates; // after mvp
    public Vec3 color;
    public Vec3 texturePosition;

    public Vec3 normal;
    public Vec3 worldNormal;
    public Vec2 screenPosition; //clippedCoordinates Vec2

    public Vertex(Vec3 objectCoordinates, Vec3 color, Vec3 texturePosition) {
        this.texturePosition = texturePosition;
        this.objectCoordinates = objectCoordinates;
        this.color = color;
    }
}
