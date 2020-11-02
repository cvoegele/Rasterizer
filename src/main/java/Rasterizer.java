import UI.ObservableImage;
import util.Mat4;
import util.Vec2;
import util.Vec3;
import util.Vec4;

public class Rasterizer {

    private Vec3 light = new Vec3(-20, -20, -20);
    private Vec3 E = new Vec3(0, 0, -5);
    private final Vec3 lightColor = new Vec3(1, 1, 1);

    private final Vertex[] vertices;
    private final Mat4 p;
    private final Vec3[] indexes;

    private final ObservableImage image;

    private final Mat4 v = Mat4.translate(new Vec3(0, 0, 5));
    private Vec3 viewLight;

    public Rasterizer(Vertex[] vertices, Mat4 p, Vec3[] indexes, ObservableImage image) {
        this.vertices = vertices;
        this.p = p;
        this.indexes = indexes;
        this.image = image;
    }


    public void paint() {

        var angle = ((System.currentTimeMillis() / 10 % 720) - 360);

        //model matrix
        var m = Mat4.rotate(angle, new Vec3(0, 1, 1));
//        var m = Mat4.ID;
        var mv = v.postMultiply(m);
        var mvp = p.postMultiply(v).postMultiply(m);
        for (int i = 0; i < vertices.length; i++) {

            var objectCoordinates = vertices[i].objectCoordinates;
            vertices[i].worldCoordinates = m.transform(objectCoordinates);
            vertices[i].viewCoordinates = mv.transform(objectCoordinates);

            var clippedCoordinate = mvp.transform(new Vec4(objectCoordinates.x, objectCoordinates.y, objectCoordinates.z, 1));
            //normalize by homogeneous component
            clippedCoordinate = clippedCoordinate.scale(1 / clippedCoordinate.w);

            vertices[i].clippedCoordinates = new Vec3(clippedCoordinate.x, clippedCoordinate.y, clippedCoordinate.z);
            vertices[i].screenPosition = new Vec2(clippedCoordinate.x, clippedCoordinate.y);
        }


        var scaleMatrix = Mat4.scale(m.determinant());
        var mNormal = m.inverse().transpose().preMultiply(scaleMatrix);

        //viewLight = v.transform(light);
        //E = v.transform(E);

        for (Vec3 index : indexes) {

            var a = vertices[(int) index.x];
            var b = vertices[(int) index.y];
            var c = vertices[(int) index.z];

            var nA = calculateNormal(a, c, b);
            a.normal = nA;
            a.worldNormal = mNormal.transform(nA);
            var nB = calculateNormal(a, c, b);
            b.normal = nB;
            b.worldNormal = mNormal.transform(nB);
            var nC = calculateNormal(a, c, b);
            c.normal = nC;
            c.worldNormal = mNormal.transform(nC);

            if (canBeSeen(a, b, c))
                drawTriangle(a, b, c);

        }

        image.notifyListenersOfFinishedFrame();
    }

    /**
     * draw triangle with already mapped 2d coordinates
     *
     * @param A
     * @param B
     * @param C
     */
    private void drawTriangle(Vertex A, Vertex B, Vertex C) {

        var a = A.screenPosition;
        var b = B.screenPosition;
        var c = C.screenPosition;

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
                    var hitPoint = interpolate(A.worldCoordinates,
                            B.worldCoordinates,
                            C.worldCoordinates,
                            u,
                            v,
                            A.viewCoordinates.z,
                            B.viewCoordinates.z,
                            C.viewCoordinates.z);

                    var pToLight = light.subtract(hitPoint).normalize();
                    var pToEye = E.subtract(hitPoint).normalize();

                    var interpolatedNormal = interpolate(A.worldNormal,
                            B.worldNormal,
                            C.worldNormal,
                            u,
                            v,
                            A.viewCoordinates.z,
                            B.viewCoordinates.z,
                            C.viewCoordinates.z);

                    //calc diffuse color
                    var interpolatedColor = interpolate(A.color.sRGBtoRGB(),
                            B.color.sRGBtoRGB(),
                            C.color.sRGBtoRGB(),
                            u,
                            v,
                            A.viewCoordinates.z,
                            B.viewCoordinates.z,
                            C.viewCoordinates.z);

                    var diffuse = interpolatedColor.scale(interpolatedNormal.dot(pToLight));

                    Vec3 color = Vec3.ZERO;
                    if (interpolatedNormal.dot(pToLight) > 0) {
                        color = color.add(diffuse);
                    }

                    //calc specular Highlight


                    var r = interpolatedNormal.scale(pToLight.dot(interpolatedNormal) * 2).subtract(pToLight);
                    var k = 10;
                    var rr = r.normalize().dot(pToEye);
                    var specularLight = Vec3.ONE.scale((float) Math.pow(rr, k));

                    if (interpolatedNormal.dot(pToLight) > 0 && rr > 0) {
                        color = color.add(specularLight);
                    }

//                    image.setPixel(x, y, interpolatedNormal.scale(0.5f).add(new Vec3(0.5, 0.5, 0.5)).RGBto_sRGB());
                    image.setPixel(x, y, color.RGBto_sRGB());
                    //image.setPixel(x, y, interpolatedColor.RGBto_sRGB());
//                    image.setPixel(x, y, hitPoint.RGBto_sRGB());
                }
            }
        }
    }

    private Vec3 calculateNormal(Vertex a, Vertex b, Vertex c) {
        var v1 = b.objectCoordinates.subtract(a.objectCoordinates);
        var v2 = c.objectCoordinates.subtract(a.objectCoordinates);
        return v1.cross(v2);
    }

    private Vec3 interpolate(Vec3 a, Vec3 b, Vec3 c, float u, float v, float scalingFactorA, float scalingFactorB, float scalingFactorC) {

        var A_ = new Vec4(a.x, a.y, a.z, 1).scale(1/scalingFactorA);
        var B_ = new Vec4(b.x, b.y, b.z, 1).scale(1/scalingFactorB);
        var C_ = new Vec4(c.x, c.y, c.z, 1).scale(1/scalingFactorC);

        var interpolated = A_;
        interpolated = interpolated.add(B_.subtract(A_).scale(u));
        interpolated = interpolated.add(C_.subtract(A_).scale(v));

        var P = interpolated.scale(1f / interpolated.w);
        return new Vec3(P).normalize();
    }

    /**
     * backface culling on triangle
     *
     * @param a Side of triangle
     * @param b Side of triangle
     * @param c Side of triangle
     * @return true if should be drawn or false if not
     */
    private boolean canBeSeen(Vertex a, Vertex b, Vertex c) {
        var v1 = b.clippedCoordinates.subtract(a.clippedCoordinates);
        var v2 = c.clippedCoordinates.subtract(a.clippedCoordinates);
        var n = v1.cross(v2);
        var N = new Vec3(0, 0, -1);
        return n.dot(N) < 0;
    }
}
