import javafx.application.Application;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import util.Mat4;
import util.Vec3;

public class Main extends Application {

    private static final int height = 500;
    private static final int width = 500;

    private static RenderView renderView;

    public static void main(String[] args) {

        Vec3[] vertices = new Vec3[]{
                new Vec3(-1, -1, -1),
                new Vec3(+1, -1, -1),
                new Vec3(+1, +1, -1),
                new Vec3(-1, +1, -1),

                new Vec3(-1, -1, +1),
                new Vec3(+1, -1, +1),
                new Vec3(+1, +1, +1),
                new Vec3(-1, +1, +1)
        };

        Vec3[] indexes = new Vec3[]{
                new Vec3(0, 1, 2),
                new Vec3(0, 2, 3),
                new Vec3(7, 6, 5),
                new Vec3(7, 5, 4),
                new Vec3(0, 3, 7),
                new Vec3(0, 7, 4),
                new Vec3(2, 1, 5),
                new Vec3(2, 5, 6),
                new Vec3(3, 2, 6),
                new Vec3(3, 6, 7),
                new Vec3(1, 0, 4),
                new Vec3(1, 4, 5)
        };

        Mat4 p = new Mat4(
                (float) width, 0f, width / 2f, 0f,
                0f, (float) width, height / 2f, 0f,
                0f, 0f, 0f, 0f,
                0f, 0f, 1f, 0f).transpose();


        renderView = new RenderView(width,height, vertices, p, indexes);
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        var scene = new javafx.scene.Scene(new BorderPane(renderView.getView()));
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
