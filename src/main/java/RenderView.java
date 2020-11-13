
import UI.ObservableImage;
import UI.FrameListener;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import texture.StandardTexture;
import util.Mat4;
import util.Vec3;

import java.io.FileNotFoundException;
import java.io.IOException;

/***
 * RenderView that creates a SceneRenderer, that then renders the Image with a given RenderEngine
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
    private final Label fpsField;
    private final StringProperty fps;

    private final int height;
    private final int width;

    /***
     * Instantiate RenderView with parameters and start render process
     *  @param width of rendered image
     * @param height of rendered image
     */
    public RenderView(int width, int height, Mat4 p) {

        this.width = width;
        this.height = height;
        this.fpsField = new Label();

        fps = new SimpleStringProperty("FPS 0");
        fpsField.textProperty().bind(fps);

        var observableImage = new ObservableImage(height, width);
        observableImage.addListener(this);

        WritableImage writableImage = new WritableImage(width, height);
        view = new ImageView(writableImage);
        writer = writableImage.getPixelWriter();

        var renderer = new Rasterizer(p, observableImage);

        //        var mesh = new Cube(() -> {
//            var angle = ((System.currentTimeMillis() / 10 % 720) - 360);
//            return Mat4.rotate(angle, new Vec3(0, 1, 1));
//        });

        var mesh1 = new Cube(() -> {
            var angle = -((System.currentTimeMillis() / 10 % 720) - 360);
            var translation = Mat4.translate(new Vec3(0, 0, 0));
            return translation.postMultiply(Mat4.rotate(angle, new Vec3(0, 1, 1)));
//            return Mat4.ID;
        }, renderer);

        try {
            mesh1.setTexture(new StandardTexture("./cowQuad.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Obj teapot = null;
        try {
            teapot = new Obj("./lucy.obj", () -> {
                var angle = ((System.currentTimeMillis() / 10 % 720) - 360);
                var rot = Mat4.rotate(angle, new Vec3(0, -1, 0));
                var translate = Mat4.translate(new Vec3(0, 0, 0));
                var scale = Mat4.scale(0.01f,0.01f,0.01f);
                return rot.preMultiply(scale.preMultiply(translate));
//                return Mat4.ID;
            }, renderer);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        var root = new RootSceneElement(renderer);
        root.addChild(mesh1);
        mesh1.addChild(teapot);

        var t = new Thread(() -> {
            while (true) {
                var start = System.currentTimeMillis();

                root.paint(Mat4.ID);

                var frameTime = System.currentTimeMillis() - start;
                Platform.runLater(() -> fps.setValue(String.format("FPS: %.0f", 1_000d / frameTime)));
            }
        });
        t.start();

    }

    /***
     * get view to present as ImageView in any javafx application
     * @return ImageView with rendered/currently rendering image
     */
    public BorderPane getView() {
        var pane = new BorderPane(view);
        pane.setBottom(fpsField);
        return pane;
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
