package engine;

import UI.ObservableImage;
import util.*;

public class Rasterizer {

    private Vec3 light = new Vec3(-20, -20, -20);
    private Vec3 E;
    private final Vec3 lightColor = new Vec3(1, 1, 1);

    private final Mat4 p;
    private final ObservableImage frame;
    private float[][] zBuffer;
    private final Mat4 v = Mat4.translate(new Vec3(0, 0, 15));


    public Rasterizer(Mat4 p, ObservableImage frame) {
        this.p = p;
        this.frame = frame;
        E = v.inverse().transform(Vec3.ZERO);

        zBuffer = new float[frame.getHeight()][frame.getWidth()];
        for (int y = 0; y < frame.getHeight(); y++) {
            for (int x = 0; x < frame.getWidth(); x++) {
                zBuffer[y][x] = Float.MAX_VALUE;
            }
        }
    }



    public void paint(Mesh mesh, Mat4 m) {

        if (mesh.vertices.length == 0) return;

        var vertices = mesh.getVertices();
        var indexes = mesh.getIndexes();

        var mv = v.postMultiply(m);
        var mvp = p.postMultiply(v).postMultiply(m);
        for (Vertex vertex : vertices) {

            var objectCoordinates = vertex.objectCoordinates;
            vertex.worldCoordinates = m.transform(objectCoordinates);
            vertex.viewCoordinates = mv.transform(objectCoordinates);

            var clippedCoordinate = mvp.transform(new Vec4(objectCoordinates.x, objectCoordinates.y, objectCoordinates.z, 1));
            //normalize by homogeneous component
            clippedCoordinate = clippedCoordinate.scale(1 / clippedCoordinate.w);

            vertex.clippedCoordinates = new Vec3(clippedCoordinate.x, clippedCoordinate.y, clippedCoordinate.z);
            vertex.screenPosition = new Vec2(clippedCoordinate.x, clippedCoordinate.y);
        }

        var scaleMatrix = Mat4.scale(m.determinant());
        var mNormal = m.inverse().transpose().preMultiply(scaleMatrix);

        for (Int3 index : indexes) {

            var a = vertices[index.i0];
            var b = vertices[index.i1];
            var c = vertices[index.i2];

            var nA = calculateNormal(a, c, b);
            a.normal = nA;
            a.worldNormal = mNormal.transform(nA);
            var nB = calculateNormal(a, c, b);
            b.normal = nB;
            b.worldNormal = mNormal.transform(nB);
            var nC = calculateNormal(a, c, b);
            c.normal = nC;
            c.worldNormal = mNormal.transform(nC);

            if (mesh instanceof Obj) {
                drawTriangle(a, b, c, mesh);
            } else {
                if (canBeSeen(a, b, c))
                    drawTriangle(a, b, c, mesh);
            }
        }
    }

    public void frameFinished() {
        frame.notifyListenersOfFinishedFrame();
        zBuffer = new float[frame.getHeight()][frame.getWidth()];
        for (int y = 0; y < frame.getHeight(); y++) {
            for (int x = 0; x < frame.getWidth(); x++) {
                zBuffer[y][x] = Float.MAX_VALUE;
            }
        }
    }

    /**
     * draw triangle with already mapped 2d coordinates
     *
     * @param A
     * @param B
     * @param C
     */
    private void drawTriangle(Vertex A, Vertex B, Vertex C, Mesh mesh) {

        var a = A.screenPosition;
        var b = B.screenPosition;
        var c = C.screenPosition;

        var ab = b.subtract(a);
        var ac = c.subtract(a);

        float scalingFactor = 1f / (ab.x * ac.y - ac.x * ab.y);
        var left = new Vec2(ac.y, -ab.y).scale(scalingFactor);
        var right = new Vec2(-ac.x, ab.x).scale(scalingFactor);

        var xMin = (int) Math.floor(Math.max(0, Math.min(A.screenPosition.x, Math.min(B.screenPosition.x, C.screenPosition.x))));
        var xMax = (int) Math.ceil(Math.min(frame.getWidth(), Math.max(A.screenPosition.x, Math.max(B.screenPosition.x, C.screenPosition.x))));

        var yMin = (int) Math.floor(Math.max(0, Math.min(A.screenPosition.y, Math.min(B.screenPosition.y, C.screenPosition.y))));
        var yMax = (int) Math.ceil(Math.min(frame.getHeight(), Math.max(A.screenPosition.y, Math.max(B.screenPosition.y, C.screenPosition.y))));

        for (int y = yMin; y < yMax; y++) {
            for (int x = xMin; x < xMax; x++) {
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

                    if (hitPoint.z < zBuffer[y][x]) {

                        var pToLight = light.subtract(hitPoint).normalize();
                        var pToEye = E.subtract(hitPoint).normalize();

                        var interpolatedNormal = interpolate(
                                A.worldNormal,
                                B.worldNormal,
                                C.worldNormal,
                                u,
                                v,
                                A.viewCoordinates.z,
                                B.viewCoordinates.z,
                                C.viewCoordinates.z);

                        Vec3 albedo;

                        if (mesh.isTextured()) {
                            var interpolatedTextureCoordinates = interpolateNoNormalize(
                                    A.texturePosition,
                                    B.texturePosition,
                                    C.texturePosition,
                                    u,
                                    v,
                                    A.viewCoordinates.z,
                                    B.viewCoordinates.z,
                                    C.viewCoordinates.z);

                            albedo = mesh.colorAtPoint(new Vec2(interpolatedTextureCoordinates.x, interpolatedTextureCoordinates.y));

                        } else {

                            //calc diffuse color
                            albedo = interpolate(A.color.sRGBtoRGB(),
                                    B.color.sRGBtoRGB(),
                                    C.color.sRGBtoRGB(),
                                    u,
                                    v,
                                    A.viewCoordinates.z,
                                    B.viewCoordinates.z,
                                    C.viewCoordinates.z);
                        }

                        var color = diffuseLambertShading(interpolatedNormal, pToLight, albedo).
                                add(specularPhongHighlight(interpolatedNormal, pToLight, pToEye));

//                    image.setPixel(x, y, interpolatedNormal.scale(0.5f).add(new Vec3(0.5, 0.5, 0.5)).RGBto_sRGB());
                        frame.setPixel(x, y, color.RGBto_sRGB());
                        //image.setPixel(x, y, interpolatedColor.RGBto_sRGB());
//                    image.setPixel(x, y, hitPoint.RGBto_sRGB());
                        zBuffer[y][x] = hitPoint.z;
                    }
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

        var A_ = new Vec4(a.x, a.y, a.z, 1).scale(1 / scalingFactorA);
        var B_ = new Vec4(b.x, b.y, b.z, 1).scale(1 / scalingFactorB);
        var C_ = new Vec4(c.x, c.y, c.z, 1).scale(1 / scalingFactorC);

        var interpolated = A_;
        interpolated = interpolated.add(B_.subtract(A_).scale(u));
        interpolated = interpolated.add(C_.subtract(A_).scale(v));

        var P = interpolated.scale(1f / interpolated.w);
        return new Vec3(P).normalize();
    }

    private Vec3 interpolateNoNormalize(Vec3 a, Vec3 b, Vec3 c, float u, float v, float scalingFactorA, float scalingFactorB, float scalingFactorC) {

        var A_ = new Vec4(a.x, a.y, a.z, 1).scale(1 / scalingFactorA);
        var B_ = new Vec4(b.x, b.y, b.z, 1).scale(1 / scalingFactorB);
        var C_ = new Vec4(c.x, c.y, c.z, 1).scale(1 / scalingFactorC);

        var interpolated = A_;
        interpolated = interpolated.add(B_.subtract(A_).scale(u));
        interpolated = interpolated.add(C_.subtract(A_).scale(v));

        var P = interpolated.scale(1f / interpolated.w);
        return new Vec3(P);
    }

    private Vec3 diffuseLambertShading(Vec3 normalAtPoint, Vec3 pointToLight, Vec3 diffuseColor) {
        var angle = normalAtPoint.dot(pointToLight);
        var diffuse = diffuseColor.scale(angle);

        if (angle > 0) return diffuse;
        return Vec3.ZERO;
    }

    private Vec3 specularPhongHighlight(Vec3 normalAtPoint, Vec3 pointToLight, Vec3 pointToEye) {
        var r = normalAtPoint.scale(pointToLight.dot(normalAtPoint) * 2).subtract(pointToLight);
        var k = 10;
        var rr = r.normalize().dot(pointToEye);
        var specularLight = Vec3.ONE.scale((float) Math.pow(rr, k));

        if (normalAtPoint.dot(pointToLight) > 0 && rr > 0) return specularLight;
        return Vec3.ZERO;
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
