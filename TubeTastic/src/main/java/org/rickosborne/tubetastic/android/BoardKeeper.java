package org.rickosborne.tubetastic.android;

import android.content.Context;
import android.content.SharedPreferences;
import com.badlogic.gdx.Gdx;
import org.json.JSONException;
import org.json.JSONObject;

public class BoardKeeper {

    private String PREFS_BOARD_TILES;
    private SharedPreferences prefs;
    private String tiles;

    public BoardKeeper (Context context) {
        String prefsName = context.getString(R.string.prefs_name);
        PREFS_BOARD_TILES = context.getString(R.string.prefs_board_tiles);
        prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE);
        tiles = prefs.getString(PREFS_BOARD_TILES, "");
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
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("rows", rowCount);
            jsonObject.put("cols", colCount);
            jsonObject.put("tiles", tiles.toString());
            jsonObject.put("score", board.getScore());
            synchronized (ScoreKeeper.class) {
                prefs.edit().putString(PREFS_BOARD_TILES, jsonObject.toString()).commit();
            }
        }
        catch (JSONException e) {
            // don't care
        }
    }

    public GameBoard loadBoard (int maxWidth, int maxHeight) {
        String json = prefs.getString(PREFS_BOARD_TILES, "");
        if (json.isEmpty()) {
            Gdx.app.log("BoardKeeper", "No tiles");
            return null;
        }
        try {
            JSONObject jsonObject = new JSONObject(json);
            int rowCount = jsonObject.getInt("rows");
            int colCount = jsonObject.getInt("cols");
            String tiles = jsonObject.getString("tiles");
            Gdx.app.log("BoardKeeper", String.format("loadBoard c:%d r:%d t:%s", colCount, rowCount, tiles));
            if ((rowCount > 0) && (colCount > 2) && (tiles.length() == rowCount * colCount)) {
                GameBoard board = new GameBoard(colCount, rowCount, maxWidth, maxHeight);
                int at = 0;
                for (int rowNum = 0; rowNum < rowCount; rowNum++) {
                    for (int colNum = 0; colNum < colCount; colNum++) {
                        GameBoard.TILE_TYPE type = GameBoard.TILE_TYPE.TUBE;
                        if (colNum == 0) { type = GameBoard.TILE_TYPE.SOURCE; }
                        else if (colNum >= colCount - 1) { type = GameBoard.TILE_TYPE.SINK; }
                        String hexBits = tiles.substring(at, at + 1);
                        at++;
                        int bits = Integer.parseInt(hexBits, 16);
                        board.setTile(colNum, rowNum, type, bits);
                    }
                }
                if (jsonObject.has("score")) {
                    board.setScore(jsonObject.getInt("score"));
                }
                Gdx.app.log("BoardKeeper", String.format("loadBoard loaded %d tiles", at));
                return board;
            }
            else {
                Gdx.app.log("BoardKeeper", String.format("loadBoard MISMATCH %d * %d != %d", colCount, rowCount, tiles.length()));
            }
        }
        catch (JSONException e) {
            Gdx.app.error("BoardKeeper", "loadBoard exception: " + e.toString());
        }
        return null;
    }

}
