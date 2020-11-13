package engine;

import engine.texture.ITextureMapper;
import util.Int3;
import util.Mat4;
import util.Vec2;
import util.Vec3;

import java.util.ArrayList;
import java.util.List;

public abstract class Mesh implements SceneElement {

    public Vertex[] vertices;
    public Int3[] indexes;
    ITextureMapper texture;
    private boolean hasTexture;
    private final ModelMatrixFunction modelMatrixFunction;
    final Rasterizer rasterizer;
    final List<Mesh> children;

    public Mesh(ModelMatrixFunction modelMatrixFunction, Rasterizer rasterizer) {
        this.modelMatrixFunction = modelMatrixFunction;
        this.rasterizer = rasterizer;
        children = new ArrayList<>();
    }

    public Vertex[] getVertices() {
        return vertices;
    }

    public Int3[] getIndexes() {
        return indexes;
    }

    public Mat4 getM() {
        return modelMatrixFunction.getModelMatrix();
    }

    public void setTexture(ITextureMapper texture) {
        if (texture != null) {
            this.texture = texture;
            hasTexture = true;
        }
    }

    public void paint(Mat4 modelMatrix) {

        var m = modelMatrix.postMultiply(getM());
        rasterizer.paint(this, m);

        for (var child : children) {
            child.paint(m);
        }
    }

    public boolean isTextured() {
        return hasTexture;
    }

    public Vec3 colorAtPoint(Vec2 position) {
        return texture.getColorAtPosition(position);
    }

    public void addChild(Mesh mesh) {
        children.add(mesh);
    }

    public void removeChild(Mesh mesh) {
        children.remove(mesh);
    }
}
