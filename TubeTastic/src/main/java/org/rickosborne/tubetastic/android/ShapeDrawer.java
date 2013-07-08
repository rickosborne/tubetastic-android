package org.rickosborne.tubetastic.android;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class ShapeDrawer {

    public static void line(ShapeRenderer shape, float x1, float y1, float x2, float y2, float lineWidth, Color color) {
        Gdx.gl.glLineWidth(lineWidth);
        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.identity();
        shape.setColor(color);
        shape.line(x1, y1, x2, y2);
        shape.end();
    }

    public static void circle(ShapeRenderer shape, float x, float y, float radius, Color color) {
        shape.begin(ShapeRenderer.ShapeType.FilledCircle);
        shape.identity();
        shape.setColor(color);
        shape.filledCircle(x, y, radius);
        shape.end();
    }

    public static void roundRect(ShapeRenderer shape, float x, float y, float width, float height, float radius, Color color) {
        float halfWidth = width / 2;
        float halfHeight = height / 2;
        float cornerX = halfWidth - radius;
        float cornerY = halfHeight - radius;
        shape.begin(ShapeRenderer.ShapeType.FilledCircle);
        shape.identity();
        shape.translate(x + halfWidth, y + halfHeight, 0);
        shape.setColor(color);
        shape.filledCircle(-cornerX,  cornerY, radius);
        shape.filledCircle(-cornerX, -cornerY, radius);
        shape.filledCircle( cornerX,  cornerY, radius);
        shape.filledCircle( cornerX, -cornerY, radius);
        shape.end();
        shape.begin(ShapeRenderer.ShapeType.FilledRectangle);
        shape.identity();
        shape.translate(x + halfWidth, y + halfHeight, 0);
        shape.setColor(color);
        shape.filledRect(-cornerX - radius, -cornerY, radius, cornerX * 2);
        shape.filledRect(-cornerX, -cornerY - radius, cornerX * 2, (cornerY + radius) * 2);
        shape.filledRect(cornerX, -cornerY, radius, cornerY * 2);
        shape.end();
    }

}
