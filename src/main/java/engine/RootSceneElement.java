package engine;

import util.Mat4;

public class RootSceneElement extends Mesh {
    public RootSceneElement(Rasterizer rasterizer) {
        super(() -> Mat4.ID, rasterizer);
    }

    @Override
    public void paint(Mat4 modelMatrix) {
        var m = modelMatrix.postMultiply(getM());

        for (var child : children) {
            child.paint(m);
        }

        rasterizer.frameFinished();
    }
}
