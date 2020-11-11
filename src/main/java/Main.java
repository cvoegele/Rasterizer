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

        Mat4 p = new Mat4(
                (float) width, 0f, width / 2f, 0f,
                0f, (float) width, height / 2f, 0f,
                0f, 0f, 0f, 0f,
                0f, 0f, 1f, 0f).transpose();


        renderView = new RenderView(width, height, p);
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        var scene = new javafx.scene.Scene(new BorderPane(renderView.getView()));
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
