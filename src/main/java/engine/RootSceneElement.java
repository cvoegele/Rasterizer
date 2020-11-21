package engine;

import main.RenderView;
import util.Mat4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;

public class RootSceneElement extends Mesh {

    public RootSceneElement(RenderView view) {
        super(() -> Mat4.ID, view.rasterizer);
        setDepth(0);
        name = "Root";
    }

    @Override
    public void paint(Mat4 modelMatrix) {
        var m = modelMatrix.postMultiply(getM());

        for (var child : children) {
            child.paint(m);
        }

        rasterizer.frameFinished();
    }

    public List<SceneElement> getSceneGraph() {

        var graph = new ArrayList<SceneElement>();

        var q = new ArrayList<SceneElement>();
        q.add(this);
        while (!q.isEmpty()) {
            var node = q.remove(q.size() -1);

            graph.add(node);

            q.addAll(Arrays.asList(node.getChildren()));
        }
        return graph;
    }
}
