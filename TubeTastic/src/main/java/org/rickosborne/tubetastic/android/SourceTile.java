package org.rickosborne.tubetastic.android;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;
import java.util.List;

public class SourceTile extends BaseTile {

    public static final Color COLOR_SOURCE = new Color(0.25f, 0.5f, 1.0f, 1.0f);

    static {
        CACHE_KEY = "r";
    }

    public SourceTile(int colNum, int rowNum, float x, float y, float size, GameBoard board) {
        super(colNum, rowNum, x, y, size, board);
        init(colNum, rowNum, x, y, size, board);
    }

    @Override
    public void init(int colNum, int rowNum, float x, float y, float size, GameBoard board) {
        super.init(colNum, rowNum, x, y, size, board);
        super.setPower(Power.SOURCED);
        outlets.setBits(Outlets.BIT_EAST);
        resize(x, y, size);
    }

    @Override
    public void resize(float x, float y, float size) {
        super.resize(x, y, size);
    }

    @Override
    public void setPower(Power power) {}

    @Override
    public void setBits(int bits) {}

    @Override
    public void draw(SpriteBatch batch, float parentAlpha) {
        batch.draw(renderer.getTextureForTile(this), getX(), getY(), getWidth(), getHeight());
//        batch.end();
//        ShapeRenderer shape = new ShapeRenderer();
//        ShapeRenderer shape = ShapeRendererSingleton.INSTANCE.getShape();
//        float x = getX();
//        float y = getY();
//        float width = getWidth();
//        float degrees = getRotation();
//        ShapeDrawer.circle(shape, midpoint, midpoint, midpoint - (padding * 2), COLOR_SOURCE, degrees, x, y);
//        ShapeDrawer.line(shape, midpoint, midpoint, width, midpoint, arcWidth, COLOR_ARC, degrees, x, y);
//        batch.begin();
    }

//    @Override
//    public Pixmap getPixmap() {
//        if (pixmap != null) {
//            return pixmap;
//        }
//        int finalSize = MathUtils.nextPowerOfTwo((int) getWidth());
//        int tileSize = finalSize * 2;
//        Gdx.app.log("SourceTile", String.format("getPixmap size:%d", tileSize));
//        int halfSize = tileSize / 2;
//        int radius = halfSize - (int) (tileSize * SIZE_PADDING);
//        int arcSize = (int) (tileSize * SIZE_ARCWIDTH);
//        int halfArcSize = arcSize / 2;
//        Pixmap.setBlending(Pixmap.Blending.SourceOver);
//        Pixmap.setFilter(Pixmap.Filter.BiLinear);
//        Pixmap oversize = new Pixmap(tileSize, tileSize, Pixmap.Format.RGBA8888);
//        oversize.setColor(COLOR_SOURCE);
//        oversize.fillCircle(halfSize, halfSize, radius);
//        oversize.setColor(COLOR_ARC);
//        oversize.fillRectangle(halfSize, halfSize - halfArcSize, halfSize, arcSize);
//        pixmap = new Pixmap(finalSize, finalSize, Pixmap.Format.RGBA8888);
//        pixmap.drawPixmap(oversize, 0, 0, tileSize, tileSize, 0, 0, finalSize, finalSize);
//        oversize.dispose();
//        return pixmap;
//    }

}
