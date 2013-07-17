package org.rickosborne.tubetastic.android;

import android.support.v4.util.LruCache;
import android.util.Log;
import android.util.SparseIntArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class FreetypeActor extends Actor {

    protected static String PATH_FORMAT = "fonts/%s.ttf";

    private static class FreetypeActorCacheItem {
        protected String name;
        protected int size;
        protected BitmapFont font;
        protected String characters;
        protected boolean isFixedWidth = false;
        public FreetypeActorCacheItem(String name, int size, String characters, boolean isFixedWidth) {
            this.name = name;
            this.size = size;
            this.characters = characters;
            this.isFixedWidth = isFixedWidth;
        }
        public BitmapFont getFont() {
            if (font == null) {
                FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(String.format(PATH_FORMAT, name)));
                font = generator.generateFont(size, characters, false);
                if (isFixedWidth) {
                    font.setFixedWidthGlyphs(characters);
                }
                generator.dispose();
            }
            return font;
        }
        public static String makeId(String fontName, int fontSize, String possibleCharacters, boolean isFixedWidth) {
            return String.format("%s:%d:%s:%s", fontName, fontSize, possibleCharacters, isFixedWidth ? "m" : "-");
        }
    }

    public static class TextBounds {
        public float width;
        public float height;
        public TextBounds(float width, float height) {
            this.width = width;
            this.height = height;
        }
        public TextBounds(BitmapFont.TextBounds bounds) {
            this(bounds.width, bounds.height);
        }
    }

    public static String uniqueCharacters(String text) {
        int l = text.length();
        SparseIntArray unique = new SparseIntArray(l);
        for (int i = 0; i < l; i++) {
            unique.put(text.charAt(i), 1);
        }
        int ul = unique.size();
        StringBuilder key = new StringBuilder(ul);
        for (int i = 0; i < ul; i++) {
            key.append((char) unique.keyAt(i));
        }
        return key.toString();
    }

    public static void flushCache() {
        Log.d("FreetypeActor", "flushCache");
        cache.evictAll();
    }

    protected static LruCache<String, FreetypeActorCacheItem> cache = new LruCache<String, FreetypeActorCacheItem>(5);

    public enum Alignment {
        MIDDLE,
        NORTHWEST,
        NORTH,
        NORTHEAST,
        EAST,
        SOUTHEAST,
        SOUTH,
        SOUTHWEST,
        WEST
    }

    protected String fontName;
    protected int fontSize;
    protected String text;
    protected Alignment alignment;
    protected String fontId;
    protected String possibleChars;
    protected boolean isFixedWidth;
    protected boolean validOrigin;

    public FreetypeActor(String fontName, Alignment alignment, String possibleChars, boolean isFixedWidth) {
        this.fontName = fontName;
        this.alignment = alignment;
        this.possibleChars = possibleChars;
        this.isFixedWidth = isFixedWidth;
        validOrigin = false;
    }

    public FreetypeActor(String fontName, Alignment alignment, String possibleChars, boolean isFixedWidth, Color color, String text) {
        this(fontName, alignment, possibleChars, isFixedWidth);
        setColor(color);
        setText(text);
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
        this.fontId = null;
        validOrigin = false;
    }

    public void setText(String text) {
        this.text = text;
        validOrigin = false;
    }

    @Override
    public void setWidth(float width) {
        super.setWidth(width);
        validOrigin = false;
    }

    @Override
    public void setHeight(float height) {
        super.setHeight(height);
        validOrigin = false;
    }

    public TextBounds getBounds() {
        String text = this.text;
        if (text == null) {
            text = "";
        }
        return new TextBounds(getFont().getBounds(text));
    }

    private void resetOrigin() {
        String text = this.text;
        if (text == null) {
            text = "";
        }
        BitmapFont font = getItem().getFont();
        TextBounds bounds = new TextBounds(font.getBounds(text));
//        Log.d("FreeTypeActor", String.format("resetOrigin \"%s\" b(%.0f,%.0f)", text, bounds.width, bounds.height));
        float originX;
        float originY;
        if ((alignment == Alignment.NORTH) || (alignment == Alignment.NORTHEAST) || (alignment == Alignment.NORTHWEST)) {
            originY = getHeight();
        } else if ((alignment == Alignment.EAST) || (alignment == Alignment.WEST) || (alignment == Alignment.MIDDLE)) {
            originY = (getHeight() + bounds.height) / 2;
        } else {
            originY = bounds.height;
        }
        if ((alignment == Alignment.WEST) || (alignment == Alignment.NORTHWEST) || (alignment == Alignment.SOUTHWEST)) {
            originX = 0;
        } else if ((alignment == Alignment.NORTH) || (alignment == Alignment.SOUTH) || (alignment == Alignment.MIDDLE)) {
            originX = (getWidth() - bounds.width) / 2;
        } else {
            originX = getWidth() - bounds.width;
        }
        setOrigin(originX, originY);
        validOrigin = true;
    }

    private FreetypeActorCacheItem getItem() {
        FreetypeActorCacheItem cacheItem = cache.get(fontId);
        if (cacheItem == null) {
            cacheItem = new FreetypeActorCacheItem(fontName, fontSize, possibleChars, isFixedWidth);
            cache.put(fontId, cacheItem);
        }
        return cacheItem;
    }

    protected BitmapFont getFont() {
        if (fontId == null) {
            this.fontId = FreetypeActorCacheItem.makeId(fontName, fontSize, possibleChars, isFixedWidth);
            validOrigin = false;
        }
        if (!validOrigin) {
            resetOrigin();
        }
        return getItem().getFont();
    }

    @Override
    public String toString() {
        TextBounds bounds = getBounds();
        return String.format("sz:%d a:%s f:%b x:%.0f y:%.0f w:%.0f h:%.0f ox:%.0f oy:%.0f b(%.0fx%.0f) \"%s\"", fontSize, alignment, isFixedWidth, getX(), getY(), getWidth(), getHeight(), getOriginX(), getOriginY(), bounds.width, bounds.height, text);
    }

    @Override
    public void draw(SpriteBatch batch, float parentAlpha) {
        if ((text == null) || (text.length() == 0) || (fontSize == 0)) {
            return;
        }
        BitmapFont font = getFont();
        Color textColor = getColor().cpy();
        textColor.a *= parentAlpha;
        font.setColor(textColor);
        font.draw(batch, text, getX() + getOriginX(), getY() + getOriginY());
    }
}
