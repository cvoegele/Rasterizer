import texture.AbstractBitmapTexture;
import texture.ITextureMapper;
import util.Mat4;
import util.Vec2;
import util.Vec3;

public abstract class Mesh {

    Vertex[] vertices;
    Vec3[] indexes;
    ITextureMapper texture;
    private boolean hasTexture;
    private ModelMatrixFunction modelMatrixFunction;

    public Mesh(ModelMatrixFunction modelMatrixFunction) {
        this.modelMatrixFunction = modelMatrixFunction;
    }

    public Vertex[] getVertices() {
        return vertices;
    }

    public Vec3[] getIndexes() {
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

    public boolean isTextured() {
        return hasTexture;
    }

    public Vec3 colorAtPoint(Vec2 position) {
        return texture.getColorAtPosition(position);
    }
}
