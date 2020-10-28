import UI.ObservableImage;
import util.Mat4;
import util.Vec2;
import util.Vec3;
import util.Vec4;

import java.awt.*;

public class Rasterizer {

    private Vec3 light = new Vec3(-1.5, -1.5, -1.5);
    private final Vec3 N = new Vec3(0, 0, -1);
    private final Vec3 lightColor = new Vec3(1, 1, 1);

    private final Vertex[] vertices;
    private final Mat4 p;
    private final Vec3[] indexes;

    private final ObservableImage image;

    private final Mat4 v = Mat4.translate(new Vec3(0, 0, 5));

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

        var mvp = p.postMultiply(v).postMultiply(m);

        var nm = m.inverse().transpose();
        var mNormal = Mat4.scale(nm.determinant());

        for (int i = 0; i < vertices.length; i++) {

            var vObjectCoordinates = vertices[i].objectCoordinates;
            var vertexTransformed = mvp.transform(new Vec4(vObjectCoordinates.x, vObjectCoordinates.y, vObjectCoordinates.z, 1));
            //normalize by homogeneous component
            vertexTransformed = vertexTransformed.scale(1 / vertexTransformed.w);

            vertices[i].worldCoordinates = new Vec3(vertexTransformed.x, vertexTransformed.y, vertexTransformed.z);
            vertices[i].screenPosition = new Vec2(vertexTransformed.x, vertexTransformed.y);
        }

        for (Vec3 index : indexes) {

            var a = vertices[(int) index.x];
            var b = vertices[(int) index.y];
            var c = vertices[(int) index.z];

            var nA = calculateNormal(a, b, c);
            a.normal = nA;
            a.worldNormal = mNormal.transform(nA);
            var nB = calculateNormal(b, c, a);
            b.normal = nB;
            b.worldNormal = mNormal.transform(nB);
            var nC = calculateNormal(c, a, b);
            c.normal = nC;
            c.worldNormal = mNormal.transform(nC);

            if (nA.dot(N) < 0)
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
                    var hitPoint = aa.objectCoordinates;
                    hitPoint = hitPoint.add(bb.objectCoordinates.subtract(aa.objectCoordinates).scale(u));
                    hitPoint = hitPoint.add(cc.objectCoordinates.subtract(aa.objectCoordinates).scale(v));
                    var lightDistance = hitPoint.subtract(light);
                    var interpolatedNormal = interpolateNormal(aa, bb, cc, u, v);
                    interpolatedNormal = interpolatedNormal.normalize();

                    //TODO: How to add up these different light? Does the shading even do anything?
                    //calc diffuse color
                    var interpolatedColor = interpolateColor(aa, bb, cc, u, v);
                    var diffuse = interpolatedColor.scale(interpolatedNormal.dot(lightDistance));

                    var light = interpolatedNormal.dot(lightDistance) > 0 ? diffuse : interpolatedColor;

                    //TODO:Why is my highlight in one corner?
                    //calc specular Highlight
                    var r = interpolatedNormal.scale(lightDistance.dot(interpolatedNormal) * 2).subtract(lightDistance);
                    var k = 100;
                    var rr = r.normalize().dot(N.subtract(hitPoint));
                    var specularLight = Vec3.ONE.scale((float) Math.pow(rr, k));

                    light = interpolatedNormal.dot(lightDistance) > 0 && rr > 0 ? light.add(specularLight) : diffuse;

                    image.setPixel(x, y, light.RGBto_sRGB());
                }
            }
        }
    }

    private Vec3 calculateNormal(Vertex a, Vertex b, Vertex c) {
        var v1 = b.worldCoordinates.subtract(a.worldCoordinates);
        var v2 = c.worldCoordinates.subtract(a.worldCoordinates);
        return v1.cross(v2);
    }

    private Vec3 interpolateColor(Vertex a, Vertex b, Vertex c, float u, float v) {

        var cColor = c.color.sRGBtoRGB();
        var aColor = a.color.sRGBtoRGB();
        var bColor = b.color.sRGBtoRGB();

        var A_ = new Vec4(aColor.x, aColor.y, aColor.z, 1).scale(1 / Math.abs(a.objectCoordinates.z));
        var B_ = new Vec4(bColor.x, bColor.y, bColor.z, 1).scale(1 / Math.abs(b.objectCoordinates.z));
        var C_ = new Vec4(cColor.x, cColor.y, cColor.z, 1).scale(1 / Math.abs(c.objectCoordinates.z));

        var P_ = A_.add(B_.subtract(A_).scale(u)).add(C_.subtract(A_).scale(v));
        var P = P_.scale(1f / P_.w);
        return new Vec3(P);
    }

    private Vec3 interpolateNormal(Vertex a, Vertex b, Vertex c, float u, float v) {

        var cNormal = c.worldNormal;
        var aNormal = a.worldNormal;
        var bNormal = b.worldNormal;

        var A_ = new Vec4(aNormal.x, aNormal.y, aNormal.z, 1).scale(1 / a.objectCoordinates.z);
        var B_ = new Vec4(bNormal.x, bNormal.y, bNormal.z, 1).scale(1 / b.objectCoordinates.z);
        var C_ = new Vec4(cNormal.x, cNormal.y, cNormal.z, 1).scale(1 / c.objectCoordinates.z);

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
        var v1 = b.worldCoordinates.subtract(a.worldCoordinates);
        var v2 = c.worldCoordinates.subtract(a.worldCoordinates);
        var n = v1.cross(v2);
        var N = new Vec3(0, 0, -1);
        return n.dot(N) < 0;
    }
}
