package org.rickosborne.tubetastic.android;

import android.util.Log;
import android.util.SparseIntArray;

import java.util.ArrayDeque;

public class TileLoaderActor extends ProgressActor {

    public interface TileLoaderWatcher {
        public void onTileLoadComplete();
    }

    private TileLoaderWatcher watcher;
    private TileRenderer renderer;
    private int tileSize;
    private int tileNum;
    private int tileCount;
    private SparseIntArray tiles;
    private static String labelUnique = "0123456789:%abcdefghijklmnopqrstuvwxyz ";
    private static String[] labelTexts = new String[]{
            "reticulating splines: %.0f%%",
            "calling your mom: %.0f%%",
            "loading: %.0f%%",
            "embiggening: %.0f%%",
            "thinking about it: %.0f%%",
            "commercial break: %.0f%%",
            "confidence: %.0f%%",
            "wait for it: %.0f%%",
            "contemplating navel: %.0f%%",
            "rendering awesomeness: %.0f%%",
            "making a snack: %.0f%%"
    };
    private int labelTextNum = RandomService.getRandom().nextInt(labelTexts.length);
    private String labelText = labelTexts[labelTextNum];

    public TileLoaderActor(int tileSize, TileRenderer renderer, TileLoaderWatcher watcher) {
        super(SplashActivity.FONT_SCOREINST, FreetypeActor.Alignment.MIDDLE, labelUnique, false, TileActor.COLOR_ARC);
        // Log.d("TileLoaderActor", String.format("ctor tileSize:%d", tileSize));
        this.tileSize = tileSize;
        this.renderer = renderer;
        this.watcher = watcher;
        tileNum = 0;
        tileCount = 47;
        tiles = new SparseIntArray();
        tiles.put(-1, 1);
        tiles.put(-2, 2);
        for (int power = 0; power < 3; power++) {
            for (int bits = 1; bits < 16; bits++) {
                tiles.put(power * 16 + bits, power);
            }
        }
    }

    public void setBits(int... bits) {
        // Log.d("TileLoaderActor", String.format("setBits n:%d", bits.length));
        tiles.clear();
        tileCount = 0;
        tileNum = 0;
        for (int b : bits) {
            if (b > 0) {
                for (int power = 0; power < 3; power++) {
                    tiles.put(power * 16 + b, power);
                    tileCount++;
                }
            } else if ((b == -1) || (b == -2)) {
                tiles.put(b, b);
                tileCount++;
            }
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (tileNum < tileCount) {
            setPercent((float) tileNum / (float) tileCount);
            setText(String.format(labelText, percent * 100f));
            // Log.d("TileLoader", String.format("act %.0f num:%d", percent * 100f, tileNum));
            int tileDigit = tiles.keyAt(tileNum);
            int tilePower = tiles.valueAt(tileNum);
            int bits = (tileDigit % 16);
            if (tileDigit == -1) {
                renderer.loadTile(new SourceTile(0, 0, null), tileSize);
            }
            else if (tileDigit == -2) {
                renderer.loadTile(new SinkTile(0, 0, null), tileSize);
            }
            else {
                Power power;
                switch (tilePower) {
                    case 1: power = Power.SOURCED; break;
                    case 2: power = Power.SUNK; break;
                    default: power = Power.NONE; break;
                }
                TubeTile tile = new TubeTile(0, 0, bits, null);
                tile.setPower(power);
                renderer.loadTile(tile, tileSize);
            }
            tileNum++;
            if (tileNum >= tileCount) {
                watcher.onTileLoadComplete();
            }
        }
    }



}
