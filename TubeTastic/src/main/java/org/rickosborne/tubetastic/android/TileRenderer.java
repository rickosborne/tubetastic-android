package org.rickosborne.tubetastic.android;

import android.util.Log;
import android.util.SparseArray;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

public class TileRenderer {

    public static final int BITS_SOURCED = 400;
    public static final int BITS_SUNK    = 100;
    public static final int TILE_COUNT   = (16 * 3) + 2;
    public static final int SCALE_OVERSIZE = 2;
    private static final Color COLOR_ERASE = new Color(TileActor.COLOR_ARC.r, TileActor.COLOR_ARC.g, TileActor.COLOR_ARC.b, 0f);

    private class TileCacheItem {
        int bits;
        int size;
        Texture texture;
        TextureRegion region;
        public TileCacheItem(int bits, int size, Texture texture) {
            this.bits = bits;
            this.size = size;
            this.texture = texture;
            this.region = new TextureRegion(texture);
            this.region.flip(false, true);
        }

        @Override
        protected void finalize() throws Throwable {
            super.finalize();
            if (texture != null) {
                texture.dispose();
                texture = null;
            }
        }
    }

    private SparseArray<TileCacheItem> cache;

    public TileRenderer() {
        cache = new SparseArray<TileCacheItem>(TILE_COUNT);
    }

    public void loadTile(BaseTile tile, int tileSize) {
        getItemForTile(tile, tileSize);
    }

    public TextureRegion getTextureRegionForTile(BaseTile tile, int tileSize) {
        return getItemForTile(tile, tileSize).region;
    }

    public Texture getTextureForTile(BaseTile tile, int tileSize) {
        return getItemForTile(tile, tileSize).texture;
    }

    private static int getBitsForTile(BaseTile tile) {
        int bits;
        if (tile instanceof SourceTile) {
            bits = BITS_SOURCED;
        }
        else if (tile instanceof SinkTile) {
            bits = BITS_SUNK;
        }
        else {
            bits = tile.getBits();
            switch (tile.power) {
                case SOURCED: bits += BITS_SOURCED; break;
                case SUNK:    bits += BITS_SUNK;    break;
            }
        }
        return bits;
    }

    private TileCacheItem getItemForTile(BaseTile tile, int tileSize) {
        int bits = getBitsForTile(tile);
        int size = MathUtils.nextPowerOfTwo(tileSize);
        TileCacheItem item = cache.get(bits, null);
        if ((item != null) && (item.size == size)) {
            return item;
        }
        item = new TileCacheItem(bits, size, renderTextureForTile(tile, size));
        cache.put(bits, item);
        return item;
    }

    public static Texture renderTextureForTile(BaseTile tile, int size) {
        Pixmap pixmap = renderPixmapForTile(tile, size);
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private static void drawArc(Pixmap target, int x, int y, int innerRadius, int outerRadius) {
        Pixmap.setBlending(Pixmap.Blending.SourceOver);
        target.setColor(TileActor.COLOR_ARC);
        target.fillCircle(x, y, outerRadius);
        Pixmap.setBlending(Pixmap.Blending.None);
        target.setColor(COLOR_ERASE);
        target.fillCircle(x, y, innerRadius);
    }

    private static void drawHLine(Pixmap target, int x, int y, int width, int thickness) {
        Pixmap.setBlending(Pixmap.Blending.SourceOver);
        target.setColor(TileActor.COLOR_ARC);
        int halfThickness = (thickness / 2);
        int bottom = y - halfThickness;
        target.fillRectangle(x, bottom, width, thickness);
    }

    private static void drawVLine(Pixmap target, int x, int y, int height, int thickness) {
        Pixmap.setBlending(Pixmap.Blending.SourceOver);
        target.setColor(TileActor.COLOR_ARC);
        target.fillRectangle(x - (thickness / 2), y, thickness, height);
    }

    public static Pixmap renderPixmapForTile(BaseTile tile, int size) {
        int tileSize = (size * SCALE_OVERSIZE);
        int farthest = tileSize - 1;
        int bits = tile.getBits();
        int halfStep = SCALE_OVERSIZE / 2;
        int halfSize = tileSize / 2 - halfStep;
        int padding = (int) (tileSize * TileActor.SIZE_PADDING);
        int radius = halfSize - (padding * 2);
        Color backColor;
        switch (tile.power) {
            case SOURCED: backColor = TileActor.COLOR_POWER_SOURCED; break;
            case SUNK: backColor = TileActor.COLOR_POWER_SUNK; break;
            default: backColor = TileActor.COLOR_POWER_NONE; break;
        }
        Pixmap.setBlending(Pixmap.Blending.SourceOver);
        Pixmap.setFilter(Pixmap.Filter.BiLinear);
        Pixmap oversize = new Pixmap(tileSize, tileSize, Pixmap.Format.RGBA8888);
        if (tile instanceof SourceTile) {
            oversize.setColor(backColor);
            oversize.fillCircle(halfSize, halfSize, radius);
        }
        else if (tile instanceof SinkTile) {
            oversize.setColor(backColor);
            oversize.fillCircle(halfSize, halfSize, radius);
        }
        else {
            int cornerRadius = padding * 2;
            int leftX = padding;
            int rightX = tileSize - padding;
            int topY = rightX;
            int bottomY = padding;
            int outerSize = topY - bottomY;
            int innerSize = outerSize - (cornerRadius * 2);
            oversize.setColor(backColor);
            oversize.fillRectangle(leftX, bottomY + cornerRadius, cornerRadius, innerSize);
            oversize.fillRectangle(rightX - cornerRadius, bottomY + cornerRadius, cornerRadius, innerSize);
            oversize.fillRectangle(leftX + cornerRadius, bottomY, innerSize, outerSize);
            oversize.fillCircle(leftX + cornerRadius, bottomY + cornerRadius, cornerRadius);
            oversize.fillCircle(leftX + cornerRadius, topY - cornerRadius, cornerRadius);
            oversize.fillCircle(rightX - cornerRadius, bottomY + cornerRadius, cornerRadius);
            oversize.fillCircle(rightX - cornerRadius, topY - cornerRadius, cornerRadius);
        }
        Pixmap arcs = new Pixmap(tileSize, tileSize, Pixmap.Format.RGBA8888);
        int arcSize = (int) (tileSize * TileActor.SIZE_ARCWIDTH);
        int halfArcSize = arcSize / 2;
        int arcLow = halfSize - halfArcSize;
        int arcHigh = halfSize + halfArcSize;
        if (bits == Outlets.BIT_WEST) {
            drawHLine(arcs, 0, halfSize, halfSize, arcSize);
        }
        else if (bits == Outlets.BIT_SOUTH) {
            drawVLine(arcs, halfSize, 0, halfSize, arcSize);
        }
        else if (bits == Outlets.BIT_EAST) {
            drawHLine(arcs, halfSize, halfSize, halfSize, arcSize);
        }
        else if (bits == Outlets.BIT_NORTH) {
            drawVLine(arcs, halfSize, halfSize, halfSize, arcSize);
        }
        else {
            if ((bits & Outlets.BIT_WEST) != 0 && (bits & Outlets.BIT_SOUTH) != 0) {
                drawArc(arcs, 0, 0, arcLow, arcHigh); // WS
            }
            if ((bits & Outlets.BIT_SOUTH) != 0 && (bits & Outlets.BIT_EAST) != 0) {
                drawArc(arcs, farthest, 0, arcLow, arcHigh); // SE
            }
            if ((bits & Outlets.BIT_EAST) != 0 && (bits & Outlets.BIT_NORTH) != 0) {
                drawArc(arcs, farthest, farthest, arcLow, arcHigh); // EN
            }
            if ((bits & Outlets.BIT_WEST) != 0 && (bits & Outlets.BIT_NORTH) != 0) {
                drawArc(arcs, 0, farthest, arcLow, arcHigh); // WN
            }
            if ((bits & Outlets.BIT_WEST) != 0 && (bits & Outlets.BIT_EAST) != 0) {
                drawHLine(arcs, 0, halfSize, tileSize, arcSize);
            }
            if ((bits & Outlets.BIT_SOUTH) != 0 && (bits & Outlets.BIT_NORTH) != 0) {
                drawVLine(arcs, halfSize, 0, tileSize, arcSize);
            }
        }
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        Pixmap.setBlending(Pixmap.Blending.SourceOver);
        Pixmap.setFilter(Pixmap.Filter.BiLinear);
        pixmap.drawPixmap(oversize, 0, 0, tileSize, tileSize, 0, 0, size, size);
        oversize.dispose();
        pixmap.drawPixmap(arcs, 0, 0, tileSize, tileSize, 0, 0, size, size);
        arcs.dispose();
        return pixmap;
    }

}
