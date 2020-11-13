package UI;

import javafx.application.Platform;
import util.Mat4;

public class EditorController {

    EditorModel model;
    EditorView view;

    public EditorController(EditorModel model, EditorView view) {
        this.model = model;
        this.view = view;
    }

    public void StartRender() {
        model.rasterThread = new Thread(() -> {
            while (true) {
                var start = System.currentTimeMillis();

                model.rootSceneElement.paint(Mat4.ID);

                var frameTime = System.currentTimeMillis() - start;
                Platform.runLater(() -> model.fps.setValue(String.format("FPS: %.0f", 1_000d / frameTime)));
            }
        });
        model.rasterThread.start();
    }
}
