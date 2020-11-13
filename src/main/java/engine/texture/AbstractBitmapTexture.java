package engine.texture;

import util.MathUtilities;
import util.Vec2;
import util.Vec3;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public abstract class AbstractBitmapTexture {

    public boolean hasTexture = false;
    private Vec3[][] image;
    private int width;
    private int height;

    public void setTexture(String path) throws IOException {
        BufferedImage texture = ImageIO.read(getClass().getResource("/" + path));

        width = texture.getWidth();
        height = texture.getHeight();
        image = new Vec3[height][width];

        //gamma correct engine.texture
        for (int v = 0; v < height; v++) {
            for (int u = 0; u < width; u++) {
                int color = texture.getRGB(u, v);
                image[v][u] = colorToRGB(color);
            }
        }
        hasTexture = true;
    }

    private Vec3 colorToRGB(int color) {
        int red = (color & 0xFF0000) >> 16;
        int green = (color & 0xFF00) >> 8;
        int blue = (color & 0xFF);

        var sRGB = new Vec3(red, green, blue);
        return sRGB.sRGBtoRGB();
    }


    Vec3 colorAtScaledPosition(Vec2 position) {

        var v = (int) MathUtilities.clamp((position.y * height), 0, height - 1);
        var u = (int) MathUtilities.clamp((position.x * width), 0, width - 1);

        var uDiff = (position.x * width) - u;
        var vDiff = (position.y * height) - v;
        assert (uDiff < 1);
        assert (vDiff < 1);

        var u1 = MathUtilities.clamp(u + 1, 0, width - 1);
        var v1 = MathUtilities.clamp(v + 1, 0, height - 1);

        Vec3 uv = image[v][u];
        Vec3 u1v = image[v][u1];
        Vec3 uv1 = image[v1][u];
        Vec3 u1v1 = image[v1][u1];

        Vec3 left = Vec3.lerp(uv, uv1, vDiff);
        Vec3 right = Vec3.lerp(u1v, u1v1, vDiff);
        Vec3 result = Vec3.lerp(left, right, uDiff);

        return result;
//        return image[v][u];
    }
}
