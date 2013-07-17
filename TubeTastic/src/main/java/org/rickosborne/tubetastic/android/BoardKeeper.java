package org.rickosborne.tubetastic.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.SparseIntArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BoardKeeper {

    private static String PREFS_BOARD_TILES;

    public final static class SaveGameData {

        public final static String KEY_ROWS = "rows";
        public final static String KEY_COLS = "cols";
        public final static String KEY_TILES = "tiles";
        public final static String KEY_SCORE = "score";

        public int colCount;
        public int rowCount;
        public String tiles;
        public int score;

        public static int getJsonValue(JSONObject obj, String key, int defaultValue) {
            if (obj.has(key)) {
                try {
                    return obj.getInt(key);
                }
                catch (JSONException e) {
                    Log.e("BoardKeeper", String.format("getJsonValue<int> %s %d: %s", key, defaultValue, e.toString()));
                }
            }
            return defaultValue;
        }

        public static String getJsonValue(JSONObject obj, String key, String defaultValue) {
            if (obj.has(key)) {
                try {
                    String s = obj.getString(key);
                    if (s != null) {
                        return s;
                    }
                }
                catch (JSONException e) {
                    Log.e("BoardKeeper", String.format("getJsonValue<String> %s %s: %s", key, defaultValue, e.toString()));
                }
            }
            return defaultValue;
        }

        public SaveGameData() {
        }
        public SaveGameData(int colCount, int rowCount, String tiles, int score) {
            this.colCount = colCount;
            this.rowCount = rowCount;
            this.tiles = tiles;
            this.score = score;
        }
        public Boolean saveTo(SharedPreferences prefs) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(KEY_ROWS, rowCount);
                jsonObject.put(KEY_COLS, colCount);
                jsonObject.put(KEY_TILES, tiles);
                jsonObject.put(KEY_SCORE, score);
                synchronized (ScoreKeeper.class) {
                    prefs.edit().putString(PREFS_BOARD_TILES, jsonObject.toString()).commit();
                }
                return true;
            }
            catch (JSONException e) {
                // don't care
            }
            return false;
        }
        public Boolean loadFrom(SharedPreferences prefs) {
            String json = prefs.getString(PREFS_BOARD_TILES, "");
            if (json.isEmpty()) {
                return false;
            }
            try {
                JSONObject jsonObject = new JSONObject(json);
                rowCount = getJsonValue(jsonObject, KEY_ROWS, 0);
                colCount = getJsonValue(jsonObject, KEY_COLS, 0);
                tiles = getJsonValue(jsonObject, KEY_TILES, "");
                score = getJsonValue(jsonObject, KEY_SCORE, 0);
                int tileCount = tiles.length();
                if ((rowCount > 0) && (colCount > 2) && (tileCount == rowCount * colCount)) {
                    return true;
                }
                else {
                    Log.e("BoardKeeper", String.format("loadBoard MISMATCH %d * %d != %d", colCount, rowCount, tileCount));
                }
            }
            catch (JSONException e) {
                Log.e("BoardKeeper", String.format("loadBoard exception: %s", e));
            }
            return false;
        }
    }

    private SharedPreferences prefs;

    public BoardKeeper (Context context) {
        String prefsName = context.getString(R.string.prefs_name);
        PREFS_BOARD_TILES = context.getString(R.string.prefs_board_tiles);
        prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE);
    }

    public void saveBoard (GameBoard board) {
        int colCount = board.getColCount();
        int rowCount = board.getRowCount();
        StringBuilder tiles = new StringBuilder(rowCount * colCount);
        for (int rowNum = 0; rowNum < rowCount; rowNum++) {
            for (int colNum = 0; colNum < colCount; colNum++) {
                BaseTile tile = board.getTile(colNum, rowNum);
                if (tile == null) {
                    return;
                }
                tiles.append(String.format("%01X", tile.getBits()));
            }
        }
        (new SaveGameData(colCount, rowCount, tiles.toString(), board.getScore())).saveTo(prefs);
    }

    public static GameBoard loadBoard (int maxWidth, int maxHeight, SaveGameData data) {
        GameBoard board = new GameBoard(data.colCount, data.rowCount, maxWidth, maxHeight);
        board.setScore(data.score);
        int at = 0;
        for (int rowNum = 0; rowNum < data.rowCount; rowNum++) {
            for (int colNum = 0; colNum < data.colCount; colNum++) {
                GameBoard.TILE_TYPE type = GameBoard.TILE_TYPE.TUBE;
                if (colNum == 0) { type = GameBoard.TILE_TYPE.SOURCE; }
                else if (colNum >= data.colCount - 1) { type = GameBoard.TILE_TYPE.SINK; }
                String hexBits = data.tiles.substring(at, at + 1);
                at++;
                int bits = Integer.parseInt(hexBits, 16);
                board.setTile(colNum, rowNum, type, bits);
            }
        }
        return board;
    }

    public GameBoard loadBoard (int maxWidth, int maxHeight) {
        SaveGameData data = new SaveGameData();
        if (!data.loadFrom(prefs)) {
            return null;
        }
        return loadBoard(maxWidth, maxHeight, data);
    }

    public static GameBoard loadFixedBoard (int maxWidth, int maxHeight, SaveGameData data) {
        GameBoard board = loadBoard(maxWidth, maxHeight, data);
        SparseIntArray bitCache = new SparseIntArray(47);
        for (int rowNum = 0; rowNum < data.rowCount; rowNum++) {
            for (int colNum = 0; colNum < data.colCount; colNum++) {
                BaseTile tile = board.tileAt(colNum, rowNum);
                if (tile != null) {
                    if (tile instanceof TubeTile) {
                        bitCache.put(tile.getBits(), 0);
                    }
                    else if (tile instanceof SourceTile) {
                        bitCache.put(-1, -1);
                    }
                    else if (tile instanceof SinkTile) {
                        bitCache.put(-2, -2);
                    }
                }
            }
        }
        if (bitCache.size() > 0) {
            int[] bits = new int[bitCache.size()];
            for (int i = 0; i < bitCache.size(); i++) {
                bits[i] = bitCache.keyAt(i);
            }
            board.loadTiles(bits);
        }
        return board;
    }

    public Boolean couldResumeGame() {
        Boolean couldResume = (new SaveGameData()).loadFrom(prefs);
        return couldResume;
    }

}
