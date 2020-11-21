package UI;

import engine.SceneElement;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class EditorView {

    EditorModel model;
    Stage stage;

    Label fpsLabel = new Label();
    ListView<SceneElement> sceneGraph = new ListView<>();

    MenuItem cubeMenuItem;

    public EditorView(EditorModel model, Stage stage) {
        this.model = model;
        this.stage = stage;

        model.fps.bindBidirectional(fpsLabel.textProperty());
        sceneGraph.setItems(model.graph);

        sceneGraph.setPrefHeight(model.renderWindowHeight);
        sceneGraph.setPrefWidth(200);

        var mainBorderPane = new BorderPane(model.renderView.getView());
        mainBorderPane.setBottom(fpsLabel);
        mainBorderPane.setRight(sceneGraph);

        var menubar = new MenuBar();
        var addMenu = new Menu("Add");
        menubar.getMenus().add(addMenu);
        cubeMenuItem = new MenuItem("Add Cube");
        addMenu.getItems().add(cubeMenuItem);

        mainBorderPane.setTop(menubar);

        var scene = new javafx.scene.Scene(mainBorderPane);
        stage.setScene(scene);
    }

    public void show(){
        stage.show();
    }

}
