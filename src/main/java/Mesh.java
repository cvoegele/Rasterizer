import util.Mat4;
import util.Vec3;

public abstract class Mesh {

    Vertex[] vertices;
    Vec3[] indexes;
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
}
