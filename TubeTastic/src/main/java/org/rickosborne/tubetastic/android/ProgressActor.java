package org.rickosborne.tubetastic.android;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class ProgressActor extends FreetypeActor {

    protected float percent = 0;
    protected Color borderColor = BaseTile.COLOR_ARC;
    protected Color doneColor = SourceTile.COLOR_POWER_SOURCED;
    protected Color leftColor = SinkTile.COLOR_POWER_SUNK;
    protected ShapeRenderer shape = new ShapeRenderer();

    public ProgressActor(String fontName, Alignment alignment, String possibleChars, boolean isFixedWidth, Color color) {
        super(fontName, alignment, possibleChars, isFixedWidth);
        setColor(color);
    }

    public void setPercent(float percent) {
        this.percent = percent;
    }

    @Override
    public void setHeight(float height) {
        super.setHeight(height);
        setFontSize((int) (height / 2f));
    }

    @Override
    public void draw(SpriteBatch batch, float parentAlpha) {
        batch.end();
        Color boxColor = this.borderColor.cpy();
        boxColor.a *= parentAlpha;
        float height = getHeight();
        float width = getWidth();
        float x = getX();
        float y = getY();
        float lineSize = MathUtils.ceil(Math.min(width, height) / 16f);
        float innerWidth = width - lineSize * 2;
        float innerHeight = height - lineSize * 2;
        float doneWidth = MathUtils.round(innerWidth * percent);
        float leftWidth = innerWidth - doneWidth;
        shape.begin(ShapeRenderer.ShapeType.FilledRectangle);
        shape.setColor(boxColor);
        shape.filledRect(x, y, lineSize, height);
        shape.filledRect(x + width - lineSize, y, lineSize, height);
        shape.filledRect(x + lineSize, y, innerWidth, lineSize);
        shape.filledRect(x + lineSize, y + height - lineSize, innerWidth, lineSize);
        if (doneWidth > 0f) {
            shape.setColor(doneColor.r, doneColor.g, doneColor.b, doneColor.a * parentAlpha);
            shape.filledRect(x + lineSize, y + lineSize, doneWidth, innerHeight);
        }
        if (leftWidth > 0f) {
            shape.setColor(leftColor.r, leftColor.g, leftColor.b, leftColor.a * parentAlpha);
            shape.filledRect(x + lineSize + doneWidth, y + lineSize, leftWidth, innerHeight);
        }
        shape.end();
        batch.begin();
        super.draw(batch, parentAlpha);
    }
}
