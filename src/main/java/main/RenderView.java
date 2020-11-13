package main;

import UI.ObservableImage;
import UI.FrameListener;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import engine.Cube;
import engine.Obj;
import engine.Rasterizer;
import engine.RootSceneElement;
import engine.texture.StandardTexture;
import util.Mat4;
import util.Vec3;

import java.io.FileNotFoundException;
import java.io.IOException;

/***
 * main.RenderView that creates a SceneRenderer, that then renders the Image with a given RenderEngine
 *
 */
public class RenderView implements FrameListener {

    /***
     * Pixel writer to save write pixels to ImageView
     */
    private final PixelWriter writer;
    /***
     * javafx.ImageView to show image in gui
     */
    private final ImageView view;

    private final int height;
    private final int width;

    public final Rasterizer rasterizer;

    /***
     * Instantiate main.RenderView with parameters and start render process
     *  @param width of rendered image
     * @param height of rendered image
     */
    public RenderView(int width, int height, Mat4 p) {

        this.width = width;
        this.height = height;

        var observableImage = new ObservableImage(height, width);
        observableImage.addListener(this);

        WritableImage writableImage = new WritableImage(width, height);
        view = new ImageView(writableImage);
        writer = writableImage.getPixelWriter();

        rasterizer = new Rasterizer(p, observableImage);
    }

    /***
     * get view to present as ImageView in any javafx application
     * @return ImageView with rendered/currently rendering image
     */
    public BorderPane getView() {
        return new BorderPane(view);
    }


    @Override
    public void frameDone(Vec3[][] image) {

        var frame = new WritableImage(width, height);
        var writer = frame.getPixelWriter();

        for (int v = 0; v < image.length; v++) {
            for (int u = 0; u < image[v].length; u++) {
                writer.setColor(u, v, image[v][u] == null ? Color.color(1, 1, 1) : image[v][u].toColor());
            }
        }

        view.setImage(frame);
    }
}
