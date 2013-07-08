package org.rickosborne.tubetastic.android;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;

public class TubeTile extends BaseTile {

    public static final float DEGREES_TO_RADIANS = (float) Math.PI / 180f;

    private static final class OutletProbability {
        public double probability;
        public Outlets[] outlets;
        public OutletProbability(double probability, int[] bits) {
            this.probability = probability;
            this.outlets = new Outlets[bits.length];
            for (int i = 0; i < bits.length; i++) {
                this.outlets[i] = new Outlets(bits[i]);
            }
        }
        public Outlets getRandomOutlets() {
            return outlets[RandomService.getRandom().nextInt(outlets.length)];
        }
    }

    private static class OutletPath {
        public int fromBit = 0;
        public int toBit   = 0;
        public OutletPath(int fromBit, int toBit) {
            this.fromBit = fromBit;
            this.toBit = toBit;
        }
    }

    private static class OutletPathLine extends OutletPath {
        private float x1;
        private float y1;
        private float x2;
        private float y2;
        public OutletPathLine(int fromBit, int toBit, float x1, float y1, float x2, float y2) {
            super(fromBit, toBit);
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }
        public ShapeDrawer.LineSegmentLine segment(float size, float cx, float cy) {
            return new ShapeDrawer.LineSegmentLine(cx + size * x1, cy + size * y1, cx + size * x2, cy + size * y2);
        }
    }

    public static class OutletPathArc extends OutletPath {
        private float x;
        private float y;
        private float r;
        private float a1;
        private float a2;
        public OutletPathArc(int fromBit, int toBit, float x, float y, float r, float startDegrees, float endDegrees) {
            super(fromBit, toBit);
            this.x = x;
            this.y = y;
            this.r = r;
            this.a1 = startDegrees * DEGREES_TO_RADIANS;
            this.a2 = endDegrees * DEGREES_TO_RADIANS;
        }
        public ShapeDrawer.LineSegmentArc segment(float size, float cx, float cy) {
            return new ShapeDrawer.LineSegmentArc(cx + size * x, cy + size * y, size * r, a1, a2);
        }
    }

    private static final OutletProbability[] outletProbabilities;
    private static final OutletPathLine[] outletPathLines;
    private static final OutletPathArc[] outletPathArcs;
    public static final float OFFSET_SINGLE = 0.25f;
    public static final float DURATION_VANISH = 0.500f;
    public static final float DURATION_DROP   = 0.250f;
    public static final float DURATION_SPIN   = 0.150f;

    static {
        outletProbabilities = new OutletProbability[4];
        outletProbabilities[0] = new OutletProbability(0.05, new int[]{ Outlets.BIT_NORTH | Outlets.BIT_EAST | Outlets.BIT_SOUTH | Outlets.BIT_WEST });
        outletProbabilities[1] = new OutletProbability(0.50, new int[]{
            Outlets.BIT_EAST  | Outlets.BIT_SOUTH | Outlets.BIT_WEST,
            Outlets.BIT_NORTH | Outlets.BIT_SOUTH | Outlets.BIT_WEST,
            Outlets.BIT_NORTH | Outlets.BIT_EAST  | Outlets.BIT_WEST,
            Outlets.BIT_NORTH | Outlets.BIT_EAST  | Outlets.BIT_SOUTH
        });
        outletProbabilities[2] = new OutletProbability(0.90, new int[]{
            Outlets.BIT_NORTH | Outlets.BIT_EAST,
            Outlets.BIT_NORTH | Outlets.BIT_SOUTH,
            Outlets.BIT_NORTH | Outlets.BIT_WEST,
            Outlets.BIT_EAST  | Outlets.BIT_SOUTH,
            Outlets.BIT_EAST  | Outlets.BIT_WEST,
            Outlets.BIT_SOUTH | Outlets.BIT_WEST,
        });
        outletProbabilities[3] = new OutletProbability(1.00, new int[]{
            Outlets.BIT_NORTH,
            Outlets.BIT_EAST,
            Outlets.BIT_SOUTH,
            Outlets.BIT_WEST
        });
        outletPathLines = new OutletPathLine[6];
        outletPathLines[0] = new OutletPathLine(Outlets.BIT_NORTH, Outlets.BIT_SOUTH, 0, -1, 0, 1);
        outletPathLines[1] = new OutletPathLine(Outlets.BIT_EAST , Outlets.BIT_WEST , -1, 0, 1, 0);
        outletPathLines[2] = new OutletPathLine(0, Outlets.BIT_NORTH, 0, 1, 0, OFFSET_SINGLE);
        outletPathLines[3] = new OutletPathLine(0, Outlets.BIT_EAST , 1, 0, OFFSET_SINGLE, 0);
        outletPathLines[4] = new OutletPathLine(0, Outlets.BIT_SOUTH, 0, -1, 0, -OFFSET_SINGLE);
        outletPathLines[5] = new OutletPathLine(0, Outlets.BIT_WEST , -1, 0, -OFFSET_SINGLE, 0);
        outletPathArcs = new OutletPathArc[4];
        outletPathArcs[0] = new OutletPathArc(Outlets.BIT_NORTH, Outlets.BIT_EAST ,  1,  1, 1, DEGREES_SOUTH, DEGREES_WEST );
        outletPathArcs[1] = new OutletPathArc(Outlets.BIT_EAST,  Outlets.BIT_SOUTH,  1, -1, 1, DEGREES_WEST , DEGREES_NORTH);
        outletPathArcs[2] = new OutletPathArc(Outlets.BIT_SOUTH, Outlets.BIT_WEST,  -1, -1, 1, DEGREES_NORTH, DEGREES_EAST);
        outletPathArcs[3] = new OutletPathArc(Outlets.BIT_WEST , Outlets.BIT_NORTH, -1,  1, 1, DEGREES_EAST,  DEGREES_SOUTH);
    }

    private int spinRemain = 0;
    private boolean ready = false;

    public TubeTile(int colNum, int rowNum, float x, float y, float size, GameBoard board) {
        super(colNum, rowNum, x, y, size, board);
        init(colNum, rowNum, x, y, size, board);
    }

    @Override
    public void init(int colNum, int rowNum, float x, float y, float size, GameBoard board) {
        super.init(colNum, rowNum, x, y, size, board);
        double prob = RandomService.getRandom().nextDouble();
        for (OutletProbability outletProb : outletProbabilities) {
            if (prob <= outletProb.probability) {
                outlets = outletProb.getRandomOutlets();
                break;
            }
        }
        resize(x, y, size);
        ready = true;
    }

    @Override
    public void resize(float x, float y, float size) {
        super.resize(x, y, size);
    }

    @Override
    public void setPower(Power power) {
        if (power == this.power) {
            return;
        }
        // ...
    }

    public void setBits(int bits) { outlets.setBits(bits); }
    public void setReady(boolean ready) { this.ready = ready; }

    public void spin() {
        final TubeTile self = this;
        TweenCallback onComplete = new TweenCallback() {
            @Override
            public void onEvent(int type, BaseTween<?> source) {
                if (type == TweenCallback.COMPLETE) {
                    self.spin();
                }
            }
        };
        if (spinRemain > 0) {
            ready = false;
            spinRemain--;
            setPower(Power.NONE);
            Tween
                .to(this, BaseTileTweener.ROTATION, DURATION_SPIN)
                .setCallback(onComplete)
                .setCallbackTriggers(TweenCallback.COMPLETE)
                .target(rotation + 90)
                .start(TweenManagers.manager)
            ;
            board.interruptSweep();
        }
        else {
            if (rotation > 360) {
                rotation %= 360;
            }
            outletRotation = (int) rotation;
            ready = true;
            board.readyForSweep();
        }
    }

    public void vanish() {
        ready = false;
        setPower(Power.NONE);
        final TubeTile self = this;
        TweenCallback onComplete = new TweenCallback() {
            @Override
            public void onEvent(int type, BaseTween<?> source) {
                if (type == TweenCallback.COMPLETE) {
                    self.onVanishComplete();
                }
            }
        };
        if (board.isSettled()) {
            Tween
                .to(this, BaseTileTweener.ALPHA, DURATION_VANISH)
                .setCallback(onComplete)
                .setCallbackTriggers(TweenCallback.COMPLETE)
                .target(0)
                .start(TweenManagers.manager)
            ;
        }
        else {
            onComplete.onEvent(TweenCallback.COMPLETE, null);
        }
    }

    public void dropTo(final int colNum, final int rowNum, float x, float y) {
        ready = false;
        setPower(Power.NONE);
        final TubeTile self = this;
        TweenCallback onComplete = new TweenCallback() {
            @Override
            public void onEvent(int type, BaseTween<?> source) {
                if (type == TweenCallback.COMPLETE) {
                    self.setColRow(colNum, rowNum);
                    self.onDropComplete();
                    self.setReady(true);
                }
            }
        };
        if (board.isSettled()) {
            Tween
                .to(this, BaseTileTweener.X | BaseTileTweener.Y, DURATION_DROP)
                .setCallback(onComplete)
                .setCallbackTriggers(TweenCallback.COMPLETE)
                .target(x, y)
                .start(TweenManagers.manager)
            ;
        }
        else {
            this.x = x + this.midpoint;
            this.y = y + this.midpoint;
            onComplete.onEvent(TweenCallback.COMPLETE, null);
        }
    }

    public void onDropComplete() {
        board.tileDropComplete(this, colNum, rowNum);
    }

    public void onVanishComplete() {
        board.tileVanishComplete();
    }

    public void draw(ShapeRenderer shape) {
        ShapeDrawer.roundRect(shape, x + padding, y + padding, size - (2 * padding), size - (2 * padding), padding * 2, arcShadow(power));
        ArrayList<ShapeDrawer.LineSegmentLine> lines = new ArrayList<ShapeDrawer.LineSegmentLine>(outletPathLines.length);
        float midX = x + midpoint;
        float midY = y + midpoint;
        int bits = outlets.getBits();
        for (OutletPathLine line : outletPathLines) {
            if (((line.fromBit == 0) && (bits == line.toBit)) || (((bits & line.fromBit) != 0) && ((bits & line.toBit) != 0))) {
                lines.add(line.segment(midpoint, midX, midY));
            }
        }
        if (lines.size() > 0) {
            ShapeDrawer.renderLineSegments(shape, lines, COLOR_ARC, arcWidth);
        }
        ArrayList<ShapeDrawer.LineSegmentArc> arcs = new ArrayList<ShapeDrawer.LineSegmentArc>(outletPathArcs.length);
        for (OutletPathArc arc : outletPathArcs) {
            if (((bits & arc.fromBit) != 0) && ((bits & arc.toBit) != 0)) {
                arcs.add(arc.segment(midpoint, midX, midY));
            }
        }
        if (arcs.size() > 0) {
            ShapeDrawer.renderArcSegments(shape, arcs, COLOR_ARC, arcWidth);
        }
    }

}
