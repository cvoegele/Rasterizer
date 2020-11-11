import util.Mat4;
import util.Vec3;

public class Cube extends Mesh {
    public Cube(ModelMatrixFunction modelMatrixFunction) {
        super(modelMatrixFunction);
        vertices = new Vertex[]{
                new Vertex(new Vec3(-1, -1, -1), new Vec3(255, 0, 0)), // 0
                new Vertex(new Vec3(+1, -1, -1), new Vec3(0, 255, 0)), // 1
                new Vertex(new Vec3(+1, +1, -1), new Vec3(0, 0, 255)), // 2
                new Vertex(new Vec3(-1, +1, -1), new Vec3(255, 0, 0)), // 3

                new Vertex(new Vec3(-1, -1, +1), new Vec3(0, 255, 0)), // 4
                new Vertex(new Vec3(+1, -1, +1), new Vec3(0, 0, 255)), // 5
                new Vertex(new Vec3(+1, +1, +1), new Vec3(255, 0, 0)), // 6
                new Vertex(new Vec3(-1, +1, +1), new Vec3(0, 255, 0)) // 7
        };

        indexes = new Vec3[]{
                new Vec3(0, 1, 2),
                new Vec3(0, 2, 3),
                new Vec3(7, 6, 5),
                new Vec3(7, 5, 4),
                new Vec3(0, 3, 7),
                new Vec3(0, 7, 4),
                new Vec3(2, 1, 5),
                new Vec3(2, 5, 6),
                new Vec3(3, 2, 6),
                new Vec3(3, 6, 7),
                new Vec3(1, 0, 4),
                new Vec3(1, 4, 5)
        };
    }
}
