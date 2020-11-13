package texture;

import util.Vec2;
import util.Vec3;

import java.io.IOException;

public class StandardTexture extends AbstractBitmapTexture implements ITextureMapper {

    public StandardTexture(String path) throws IOException {
        setTexture(path);
    }

    @Override
    public Vec3 getColorAtPosition(Vec2 position) {
        return colorAtScaledPosition(position);
    }
}
