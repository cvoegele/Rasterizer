import UI.ObservableImage;
import util.Mat4;
import util.Vec2;
import util.Vec3;
import util.Vec4;

public class Rasterizer {

    private Vec3 light = new Vec3(-2, +1.5, -2);
    private final Vec3 N = new Vec3(0, 0, -1);
    private final Vec3 lightColor = new Vec3(1, 1, 1);

    private final Vertex[] vertices;
    private final Mat4 p;
    private final Vec3[] indexes;

    private final ObservableImage image;

    private final Mat4 v = Mat4.translate(new Vec3(0, 0, 5));
    private Vec3 worldLight;

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
        //var m = Mat4.ID;
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

        var nm = m.inverse().transpose();
        var mNormal = Mat4.scale(nm.determinant());
        worldLight = light;

        for (Vec3 index : indexes) {

            var a = vertices[(int) index.x];
            var b = vertices[(int) index.y];
            var c = vertices[(int) index.z];

            var nA = calculateNormal(a, c, b);
            a.normal = nA;
            a.worldNormal = mNormal.transform(nA);
            var nB = calculateNormal(b, a, c);
            b.normal = nB;
            b.worldNormal = mNormal.transform(nB);
            var nC = calculateNormal(c, b, c);
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
     * @param aa
     * @param bb
     * @param cc
     */
    private void drawTriangle(Vertex aa, Vertex bb, Vertex cc) {

        var a = aa.screenPosition;
        var b = bb.screenPosition;
        var c = cc.screenPosition;

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
                    var hitPoint = interpolateHitPoint(aa, bb, cc, u, v);
                    var lightDistance = worldLight.subtract(hitPoint).normalize();

                    var interpolatedNormal = interpolateNormal(aa, bb, cc, u, v);
                    interpolatedNormal = interpolatedNormal.normalize();

                    //TODO: How to add up these different light? Does the shading even do anything?
                    //calc diffuse color
                    var interpolatedColor = interpolateColor(aa, bb, cc, u, v);
                    var diffuse = interpolatedColor.scale(interpolatedNormal.dot(lightDistance));

                    Vec3 color = Vec3.ZERO;
                    if (interpolatedNormal.dot(lightDistance) > 0) {
                        color = color.add(diffuse);
                    }

                    //TODO:Why is my highlight in one corner?
                    //calc specular Highlight
//                    var r = interpolatedNormal.scale(lightDistance.dot(interpolatedNormal) * 2).subtract(lightDistance);
//                    var k = 100;
//                    var rr = r.normalize().dot(N.subtract(hitPoint));
//                    var specularLight = Vec3.ONE.scale((float) Math.pow(rr, k));
//
//                    if (interpolatedNormal.dot(lightDistance) > 0 && rr > 0){
//                        color = color.add(specularLight);
//                    }

//                    image.setPixel(x, y, interpolatedNormal.scale(0.5f).add(new Vec3(0.5, 0.5, 0.5)).RGBto_sRGB());
//                    image.setPixel(x, y, color.RGBto_sRGB());
                    image.setPixel(x, y, interpolatedColor.RGBto_sRGB());
                }
            }
        }
    }

    private Vec3 calculateNormal(Vertex a, Vertex b, Vertex c) {
        var v1 = b.worldCoordinates.subtract(a.worldCoordinates);
        var v2 = c.worldCoordinates.subtract(a.worldCoordinates);
        return v1.cross(v2);
    }

    private Vec3 interpolateHitPoint(Vertex a, Vertex b, Vertex c, float u, float v) {

        var aWorld = a.worldCoordinates;
        var bWorld = b.worldCoordinates;
        var cWorld = c.worldCoordinates;

        var A_ = new Vec4(aWorld.x, aWorld.y, aWorld.z, 1).scale(1 / a.viewCoordinates.z);
        var B_ = new Vec4(bWorld.x, bWorld.y, bWorld.z, 1).scale(1 / b.viewCoordinates.z);
        var C_ = new Vec4(cWorld.x, cWorld.y, cWorld.z, 1).scale(1 / c.viewCoordinates.z);

        var hitPoint = A_;
        hitPoint = hitPoint.add(B_.subtract(A_).scale(u));
        hitPoint = hitPoint.add(B_.subtract(C_).scale(v));

        var P = hitPoint.scale(1f / hitPoint.w);
        return new Vec3(P);
    }

    private Vec3 interpolateColor(Vertex a, Vertex b, Vertex c, float u, float v) {

        var cColor = c.color.sRGBtoRGB();
        var aColor = a.color.sRGBtoRGB();
        var bColor = b.color.sRGBtoRGB();

        var A_ = new Vec4(aColor.x, aColor.y, aColor.z, 1).scale(1f / a.viewCoordinates.z);
        var B_ = new Vec4(bColor.x, bColor.y, bColor.z, 1).scale(1f / b.viewCoordinates.z);
        var C_ = new Vec4(cColor.x, cColor.y, cColor.z, 1).scale(1f / c.viewCoordinates.z);

        var P_ = A_.add(B_.subtract(A_).scale(u)).add(C_.subtract(A_).scale(v));
        var P = P_.scale(1f / P_.w);
        return new Vec3(P);
    }

    private Vec3 interpolateNormal(Vertex a, Vertex b, Vertex c, float u, float v) {

        var cNormal = c.worldNormal;
        var aNormal = a.worldNormal;
        var bNormal = b.worldNormal;

        var A_ = new Vec4(aNormal.x, aNormal.y, aNormal.z, 1).scale(1 / a.viewCoordinates.z);
        var B_ = new Vec4(bNormal.x, bNormal.y, bNormal.z, 1).scale(1 / b.viewCoordinates.z);
        var C_ = new Vec4(cNormal.x, cNormal.y, cNormal.z, 1).scale(1 / c.viewCoordinates.z);

        var P_ = A_.add(B_.subtract(A_).scale(u)).add(C_.subtract(A_).scale(v));
        var P = P_.scale(1f / P_.w);
        return new Vec3(P);
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
