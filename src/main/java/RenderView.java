
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
import util.Mat4;
import util.Vec3;

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
    public RenderView(int width, int height, Vertex[] vertices, Mat4 p, Vec3[] indexes) {

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

        var renderer = new Rasterizer(vertices, p, indexes, observableImage);

        var t = new Thread(() -> {
            while (true) {
                var start = System.currentTimeMillis();

                renderer.paint();

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
