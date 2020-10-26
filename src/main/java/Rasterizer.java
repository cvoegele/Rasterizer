import UI.ObservableImage;
import util.Mat4;
import util.Vec2;
import util.Vec3;
import util.Vec4;

import java.awt.*;

public class Rasterizer {

    private final Vec3[] vertices;
    private final Mat4 p;
    private final Vec3[] indexes;

    private final ObservableImage image;

    private final Mat4 v = Mat4.translate(new Vec3(0, 0, 5));

    public Rasterizer(Vec3[] vertices, Mat4 p, Vec3[] indexes, ObservableImage image) {
        this.vertices = vertices;
        this.p = p;
        this.indexes = indexes;
        this.image = image;
    }


    public void paint() {

        var vertices2D = new Vec2[vertices.length];
        var verticesCopy = new Vec3[vertices.length];


        var angle = ((System.currentTimeMillis() / 100 % 360));
        //model matrix
        var m = Mat4.rotate((float) angle, new Vec3(1, 1, 1));
        //var m = Mat4.ID;

        var mvp = p.postMultiply(v).postMultiply(m);

        for (int i = 0, verticesCopyLength = vertices.length; i < verticesCopyLength; i++) {

            var vertex3 = vertices[i];
            var vertexTransformed = mvp.transform(new Vec4(vertex3.x, vertex3.y, vertex3.z, 1));
            //normalize by homogeneous component
            vertexTransformed = vertexTransformed.scale(1 / vertexTransformed.w);
            verticesCopy[i] = new Vec3(vertexTransformed.x, vertexTransformed.y, vertexTransformed.z);
            vertices2D[i] = new Vec2(vertexTransformed.x, vertexTransformed.y);

        }


        for (Vec3 index : indexes) {

            var a3 = verticesCopy[(int) index.x];
            var b3 = verticesCopy[(int) index.y];
            var c3 = verticesCopy[(int) index.z];

            if (drawTriangle(a3, b3, c3)) {

                var a = vertices2D[(int) index.x];
                var b = vertices2D[(int) index.y];
                var c = vertices2D[(int) index.z];

                drawTriangle(a, b, c);
            }
        }

        image.notifyListenersOfFinishedFrame();
    }

    /**
     * draw triangle with already mapped 2d coordinates
     *
     * @param a
     * @param b
     * @param c
     */
    private void drawTriangle(Vec2 a, Vec2 b, Vec2 c) {

        var ab = b.subtract(a);
        var ac = c.subtract(a);

        float scalingFactor = 1f / (ab.x * ac.y - ac.x * ab.y);
        var left = new Vec2(ac.y, -ab.y).scale(scalingFactor);
        var right = new Vec2(-ac.x, ab.x).scale(scalingFactor);

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                var p = new Vec2(x, y);
                var ap = p.subtract(a);

                var uv = left.scale(ap.x).add(right.scale(ap.y));
                var u = uv.x;
                var v = uv.y;

                if (u >= 0 && v >= 0 && (u + v) < 1) {
                    image.setPixel(x, y, new Vec3(255, 0, 0));
                }
            }
        }
    }

    /**
     * backface culling on triangle
     * @param a Side of triangle
     * @param b Side of triangle
     * @param c Side of triangle
     * @return true if should be drawn or false if not
     */
    private boolean drawTriangle(Vec3 a, Vec3 b, Vec3 c) {
        var v1 = b.subtract(a);
        var v2 = c.subtract(a);
        var n = v1.cross(v2);
        var N = new Vec3(0, 0, -1);
        return n.dot(N) < 0;
    }
}
