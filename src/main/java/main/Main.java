package main;

import UI.EditorController;
import UI.EditorModel;
import UI.EditorView;
import javafx.application.Application;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import util.Mat4;

public class Main extends Application {

    private static final int height = 500;
    private static final int width = 500;

    private static EditorModel model;
    private static EditorView view;
    private static EditorController controller;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        model = new EditorModel(height, width);
        view = new EditorView(model, primaryStage);
        controller = new EditorController(model, view);

        view.show();
        controller.StartRender();
    }
}
