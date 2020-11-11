package texture;

import util.MathUtilities;
import util.Vec2;
import util.Vec3;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public abstract class AbstractBitmapTexture {

    private Vec3[][] image;
    private int width;
    private int height;

    public void setTexture(String path) throws IOException {
        BufferedImage texture = ImageIO.read(getClass().getResource("/" + path));

        width = texture.getWidth();
        height = texture.getHeight();
        image = new Vec3[height][width];

        //gamma correct texture
        for (int v = 0; v < height; v++) {
            for (int u = 0; u < width; u++) {
                int color = texture.getRGB(u, v);
                image[v][u] = colorToRGB(color);
            }
        }
    }

    private Vec3 colorToRGB(int color) {
        int red = (color & 0xFF0000) >> 16;
        int green = (color & 0xFF00) >> 8;
        int blue = (color & 0xFF);

        var sRGB = new Vec3(red, green, blue);
        return sRGB.sRGBtoRGB();
    }

    public Vec3 colorAtScaledPosition(Vec2 position) {
        var u = (int) MathUtilities.clamp(position.x * width, 0, width - 1);
        var v = (int) MathUtilities.clamp(position.x * height, 0, height - 1);

        return image[v][u];
    }
}
