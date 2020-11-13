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
    public final Rasterizer rasterizer;
    public final List<Mesh> children;
    private int depth;
    public String name;

    public Mesh(ModelMatrixFunction modelMatrixFunction, Rasterizer rasterizer) {
        this.modelMatrixFunction = modelMatrixFunction;
        this.rasterizer = rasterizer;
        children = new ArrayList<>();
        name = "Mesh";
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
        mesh.setDepth(depth + 1);
        children.add(mesh);
    }

    public void removeChild(Mesh mesh) {
        mesh.setDepth(-1);
        children.remove(mesh);
    }

    public boolean hasChildren() {
        return children.size() > 0;
    }

    @Override
    public SceneElement[] getChildren() {
        return children.toArray(SceneElement[]::new);
    }


    @Override
    public void setDepth(int depth) {
        this.depth = depth;
    }

    @Override
    public int getDepth() {
        return depth;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(" ".repeat(Math.max(0, depth)));
        if (depth > 0) result.append(" > ");
        result.append(name);
        return result.toString();
    }
}
