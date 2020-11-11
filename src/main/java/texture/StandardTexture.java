package texture;

import util.Vec2;
import util.Vec3;

public class StandardTexture extends AbstractBitmapTexture implements ITextureMapper {
    @Override
    public Vec3 getColorAtPosition(Vec2 position) {
        return colorAtScaledPosition(position);
    }
}
