package org.rickosborne.tubetastic.android;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;
import java.util.List;

public class ShapeDrawer {

    public static final float TWOPI = 2f * (float) Math.PI;
    public static final float HALFPI = 0.5f * (float) Math.PI;
    public static final float EPSILON = 0.00001f;

    public static class LineSegment {}

    public static class LineSegmentLine extends LineSegment {
        public float x1 = 0;
        public float y1 = 0;
        public float x2 = 0;
        public float y2 = 0;
        public LineSegmentLine(float x1, float y1, float x2, float y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }
    }

    public static class LineSegmentArc extends LineSegment {
        public float x = 0;
        public float y = 0;
        public float radius = 0;
        public float startAngle = 0;
        public float endAngle = 0;
        public LineSegmentArc(float x, float y, float radius, float startAngle, float endAngle) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.startAngle = startAngle;
            this.endAngle = endAngle;
        }
    }

    public static class LineSegmentCurve extends LineSegment {
        public float x1 = 0;
        public float y1 = 0;
        public float cx1 = 0;
        public float cy1 = 0;
        public float cx2 = 0;
        public float cy2 = 0;
        public float x2 = 0;
        public float y2 = 0;
        public LineSegmentCurve(float x1, float y1, float cx1, float cy1, float cx2, float cy2, float x2, float y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.cx1 = cx1;
            this.cy1 = cy1;
            this.cx2 = cx2;
            this.cy2 = cy2;
            this.x2 = x2;
            this.y2 = y2;
        }
    }

    public static List<LineSegmentCurve> createCurvesFromArc(float x, float y, float radius, float startAngle, float endAngle) {
        // adapted from code by Hans Muller at:
        // http://hansmuller-flex.blogspot.com/2011/10/more-about-approximating-circular-arcs.html
        // normalize startAngle, endAngle to [-2PI, 2PI]
        float startAngleN = startAngle % TWOPI;
        float endAngleN = endAngle % TWOPI;
        // Compute the sequence of arc curves, up to PI/2 at a time.  Total arc angle
        // is less than 2PI.
        ArrayList<LineSegmentCurve> curves = new ArrayList<LineSegmentCurve>();
        float sgn = (startAngle < endAngle) ? +1 : -1; // clockwise or counterclockwise
        float a1 = startAngle;
        for (float totalAngle = Math.min(TWOPI, Math.abs(endAngleN - startAngleN)); totalAngle > EPSILON; )
        {
            float a2 = a1 + sgn * Math.min(totalAngle, HALFPI);
            curves.add(createCurveFromArc(x, y, radius, a1, a2));
            totalAngle -= Math.abs(a2 - a1);
            a1 = a2;
        }

        return curves;
    }

    public static LineSegmentCurve createCurveFromArc(float x, float y, float radius, float startAngle, float endAngle) {
        // Compute all four points for an arc that subtends the same total angle
        // but is centered on the X-axis

        float a = (endAngle - startAngle) / 2.0f;

        float x4 = radius * (float) Math.cos(a);
        float y4 = radius * (float) Math.sin(a);
        float x1 = x4;
        float y1 = -y4;

        float q1 = (x1 * x1) + (y1 * y1);
        float q2 = q1 + (x1 * x4) + (y1 * y4);
        float k2 = 4f/3f * ((float) Math.sqrt(2 * q1 * q2) - q2) / ((x1 * y4) - (y1 * x4));

        float x2 = x1 - (k2 * y1);
        float y2 = y1 + (k2 * x1);
        float x3 = x2;
        float y3 = -y2;

        // Find the arc points' actual locations by computing x1,y1 and x4,y4
        // and rotating the control points by a + a1

        float ar = a + startAngle;
        float cos_ar = (float) Math.cos(ar);
        float sin_ar = (float) Math.sin(ar);

        return new LineSegmentCurve(
            x + radius * (float) Math.cos(startAngle), y + radius * (float) Math.sin(startAngle),
            x + x2 * cos_ar - y2 * sin_ar, y + x2 * sin_ar + y2 * cos_ar,
            x + x3 * cos_ar - y3 * sin_ar, y + x3 * sin_ar + y3 * cos_ar,
            x + radius * (float) Math.cos(endAngle), y + radius * (float) Math.sin(endAngle)
        );
    }

    public static void renderLineSegments(ShapeRenderer shape, List<LineSegmentLine> segments, Color color, float lineWidth) {
        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.identity();
        shape.setColor(color);
        Gdx.gl.glLineWidth(lineWidth);
        for (LineSegmentLine l : segments) {
            shape.line(l.x1, l.y1, l.x2, l.y2);
        }
        shape.end();
    }

    public static void renderArcSegments(ShapeRenderer shape, List<LineSegmentArc> segments, Color color, float lineWidth) {
        shape.begin(ShapeRenderer.ShapeType.Curve);
        shape.identity();
        shape.setColor(color);
        Gdx.gl.glLineWidth(lineWidth);
        for (LineSegmentArc a : segments) {
            for (LineSegmentCurve c : createCurvesFromArc(a.x, a.y, a.radius, a.startAngle, a.endAngle)) {
                shape.curve(c.x1, c.y1, c.cx1, c.cy1, c.cx2, c.cy2, c.x2, c.y2);
            }
        }
        shape.end();
    }

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
