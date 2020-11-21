package UI;

import engine.*;
import engine.texture.StandardTexture;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import main.RenderView;
import util.Mat4;
import util.Vec3;

import java.io.FileNotFoundException;
import java.io.IOException;

public class EditorModel {

    RootSceneElement rootSceneElement;
    RenderView renderView;
    int renderWindowWidth;
    int renderWindowHeight;
    Mat4 p;
    Thread rasterThread;

    StringProperty fps = new SimpleStringProperty();
    ObservableList<SceneElement> graph;

    long startTime;

    public EditorModel(int renderWindowHeight, int renderWindowWidth) {
        startTime = System.currentTimeMillis();

        p = new Mat4(
                (float) renderWindowWidth, 0f, renderWindowWidth / 2f, 0f,
                0f, (float) renderWindowWidth, renderWindowHeight / 2f, 0f,
                0f, 0f, 0f, 0f,
                0f, 0f, 1f, 0f).transpose();

        this.renderWindowHeight = renderWindowHeight;
        this.renderWindowWidth = renderWindowWidth;
        renderView = new RenderView(renderWindowWidth, renderWindowHeight, p);
        rootSceneElement = new RootSceneElement(renderView);

        graph = FXCollections.observableArrayList(rootSceneElement.getSceneGraph());

        var pot = addTeapot(rootSceneElement);
        addLucy(rootSceneElement);
        var cube = addSimpleCube(pot);
        var cowCube = addTexturedCube("./cowQuad.jpg", pot);
        addBunny(cowCube);
    }

    public Mesh addTeapot(SceneElement parent) {
        Obj teapot = null;
        try {
            teapot = new Obj("./teapot.obj", new Vec3(255, 128,0), () -> {
                var angle = (((System.currentTimeMillis() + startTime) / 10 % 720) - 360);
                var correctionRotation = Mat4.rotate(180,0,0,1);
                var rot = Mat4.rotate(angle, new Vec3(1, 1, 1));
                var translate = Mat4.translate(new Vec3(0, 0, 0));
                var scale = Mat4.scale(1f, 1f, 1f);
                return correctionRotation.preMultiply(rot.preMultiply(scale.preMultiply(translate)));
//                return Mat4.ID;
            }, renderView.rasterizer);
        } catch (
                FileNotFoundException e) {
            e.printStackTrace();
        }
        assert teapot != null;
        parent.addChild(teapot);
        teapot.setName("Teapot");

        graph.clear();
        graph.addAll(rootSceneElement.getSceneGraph());
        return teapot;
    }

    public Mesh addLucy(SceneElement parent) {
        Obj lucy = null;
        try {
            lucy = new Obj("./lucy.obj", new Vec3(0,255, 128),() -> {
                var angle = (((System.currentTimeMillis() + startTime) / 10 % 720) - 360);
                var rot = Mat4.rotate(angle, new Vec3(-1, -1, -1));
                var translate = Mat4.translate(new Vec3(0, 0, 0));
                var scale = Mat4.scale(0.01f, 0.01f, 0.01f);
                return (rot.preMultiply(scale.preMultiply(translate)));
//                return Mat4.ID;
            }, renderView.rasterizer);
        } catch (
                FileNotFoundException e) {
            e.printStackTrace();
        }
        assert lucy != null;
        parent.addChild(lucy);
        lucy.setName("Lucy");

        graph.clear();
        graph.addAll(rootSceneElement.getSceneGraph());
        return lucy;
    }

    public Mesh addBunny(SceneElement parent) {
        Obj buny = null;
        try {
            buny = new Obj("./bunny.obj", new Vec3(0,0, 255),() -> {
                var angle = (((System.currentTimeMillis() + startTime)  % 720) - 360);
                var rot = Mat4.rotate(angle, new Vec3(-1, 0,0));
                var translate = Mat4.translate(new Vec3(-3, -3, -3));
                var scale = Mat4.scale(10f, 10f, 10f);
                return translate.postMultiply(scale.preMultiply(rot));
//                return Mat4.ID;
            }, renderView.rasterizer);
        } catch (
                FileNotFoundException e) {
            e.printStackTrace();
        }
        assert buny != null;
        parent.addChild(buny);
        buny.setName("Bunny");

        graph.clear();
        graph.addAll(rootSceneElement.getSceneGraph());
        return buny;
    }

    public Mesh addSimpleCube(SceneElement parent) {
        var cube = new Cube(() -> {
            var angle = (((System.currentTimeMillis() + startTime) / 10 % 720) - 360);
            var translate = Mat4.translate(5,5,5);
            var rotate = Mat4.rotate(angle, new Vec3(1, 1, 1));
            return rotate.preMultiply(translate);
        }, renderView.rasterizer);
        parent.addChild(cube);
        graph = FXCollections.observableArrayList(rootSceneElement.getSceneGraph());

        graph.clear();
        graph.addAll(rootSceneElement.getSceneGraph());
        return cube;
    }

    public Mesh addTexturedCube(String path ,SceneElement parent) {
        var cube = new Cube(() -> {
            var angle = (((System.currentTimeMillis() + startTime) / 10 % 720) - 360);
            var translate = Mat4.translate(-2,-2,-2);
            var rotate = Mat4.rotate(angle, new Vec3(1, 1, 1));
            return rotate.preMultiply(translate);
        }, renderView.rasterizer);
        try {
            cube.setTexture(new StandardTexture(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        parent.addChild(cube);
        graph = FXCollections.observableArrayList(rootSceneElement.getSceneGraph());
        cube.setName(path + " Cube");
        graph.clear();
        graph.addAll(rootSceneElement.getSceneGraph());
        return cube;
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
