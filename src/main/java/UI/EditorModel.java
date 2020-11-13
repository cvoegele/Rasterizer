package UI;

import engine.Mesh;
import engine.Obj;
import engine.RootSceneElement;
import engine.SceneElement;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import main.RenderView;
import util.Mat4;
import util.Vec3;

import java.io.FileNotFoundException;
import java.util.ArrayDeque;
import java.util.PriorityQueue;

public class EditorModel {

    RootSceneElement rootSceneElement;
    RenderView renderView;
    int renderWindowWidth;
    int renderWindowHeight;
    Mat4 p;
    Thread rasterThread;

    StringProperty fps = new SimpleStringProperty();
    ObservableList<SceneElement> graph;

    public EditorModel(int renderWindowHeight, int renderWindowWidth) {

        p = new Mat4(
                (float) renderWindowWidth, 0f, renderWindowWidth / 2f, 0f,
                0f, (float) renderWindowWidth, renderWindowHeight / 2f, 0f,
                0f, 0f, 0f, 0f,
                0f, 0f, 1f, 0f).transpose();

        this.renderWindowHeight = renderWindowHeight;
        this.renderWindowWidth = renderWindowWidth;
        renderView = new RenderView(renderWindowWidth, renderWindowHeight, p);
        rootSceneElement = new RootSceneElement(renderView);


        addTeapot();

        graph = FXCollections.observableArrayList(rootSceneElement.getSceneGraph());
    }

    public void addTeapot() {
        Obj teapot = null;
        try {
            teapot = new Obj("./lucy.obj", () -> {
                var angle = ((System.currentTimeMillis() / 10 % 720) - 360);
                var rot = Mat4.rotate(angle, new Vec3(0, -1, 0));
                var translate = Mat4.translate(new Vec3(0, 0, 0));
                var scale = Mat4.scale(0.01f, 0.01f, 0.01f);
                return rot.preMultiply(scale.preMultiply(translate));
//                return Mat4.ID;
            }, renderView.rasterizer);
        } catch (
                FileNotFoundException e) {
            e.printStackTrace();
        }
        assert teapot != null;
        rootSceneElement.addChild(teapot);
    }



//
//    //        var mesh = new meshes.Cube(() -> {
////            var angle = ((System.currentTimeMillis() / 10 % 720) - 360);
////            return Mat4.rotate(angle, new Vec3(0, 1, 1));
////        });
//
//    var mesh1 = new Cube(() -> {
//        var angle = -((System.currentTimeMillis() / 10 % 720) - 360);
//        var translation = Mat4.translate(new Vec3(0, 0, 0));
//        return translation.postMultiply(Mat4.rotate(angle, new Vec3(0, 1, 1)));
////            return Mat4.ID;
//    }, rasterizer);
//
//        try {
//        mesh1.setTexture(new StandardTexture("./cowQuad.jpg"));
//    } catch (
//    IOException e) {
//        e.printStackTrace();
//    }
//

}
