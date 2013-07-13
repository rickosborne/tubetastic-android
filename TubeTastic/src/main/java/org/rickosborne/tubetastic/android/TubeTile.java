package org.rickosborne.tubetastic.android;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

import java.util.ArrayList;

public class TubeTile extends BaseTile {

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
        public ShapeDrawer.LineSegmentLine segment(float size) {
            return new ShapeDrawer.LineSegmentLine(size * x1, size * y1, size * x2, size * y2);
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
        public ShapeDrawer.LineSegmentArc segment(float size) {
            return new ShapeDrawer.LineSegmentArc(size * x, size * y, size * r, a1, a2);
        }
    }

    private static final OutletProbability[] outletProbabilities;
    private static final OutletPathLine[] outletPathLines;
    private static final OutletPathArc[] outletPathArcs;
    public static final float OFFSET_SINGLE = 0.25f;
    public static final float DURATION_VANISH = 0.500f;
    public static final float DURATION_DROP   = 0.250f;
    public static final float DURATION_SPIN   = 0.150f;
    public static final float DEGREES_TO_RADIANS = (float) Math.PI / 180f;
    public static final float DEGREES_SPIN = -90f;
    public static final float DEGREES_CIRCLE = -360f;
    public static final float OPACITY_VANISH = 0f;
    public static final float SCALE_VANISH = 0f;


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
        outletPathArcs[1] = new OutletPathArc(Outlets.BIT_EAST,  Outlets.BIT_SOUTH,  1, -1, 1, DEGREES_EAST, DEGREES_SOUTH);
        outletPathArcs[2] = new OutletPathArc(Outlets.BIT_SOUTH, Outlets.BIT_WEST,  -1, -1, 1, DEGREES_EAST, DEGREES_NORTH);
        outletPathArcs[3] = new OutletPathArc(Outlets.BIT_WEST , Outlets.BIT_NORTH, -1,  1, 1, DEGREES_NORTH2, DEGREES_EAST);
    }

    private int spinRemain = 0;
    private boolean ready = false;

    public TubeTile(int colNum, int rowNum, float x, float y, float size, GameBoard board) {
        super(colNum, rowNum, x, y, size, board);
        init(colNum, rowNum, x, y, size, 0, board);
    }

    public TubeTile(int colNum, int rowNum, float x, float y, float size, int bits, GameBoard board) {
        super(colNum, rowNum, x, y, size, board);
        init(colNum, rowNum, x, y, size, bits, board);
    }

//    @Override
//    public String toString() {
//        return String.format("%s", // %s%s%s%s
//                super.toString() //,        );
//    }

    public void init(int colNum, int rowNum, float x, float y, float size, int bits, final GameBoard board) {
        super.init(colNum, rowNum, x, y, size, board);
        if (bits <= 0) {
            setRandomOutlets();
        }
        else {
            outlets = new Outlets(bits);
        }
        resize(x, y, size);
        ready = true;
    }

    private void setRandomOutlets() {
        double prob = RandomService.getRandom().nextDouble();
        for (OutletProbability outletProb : outletProbabilities) {
            if (prob <= outletProb.probability) {
                outlets = outletProb.getRandomOutlets();
                break;
            }
        }
    }

//    @Override
//    public void setPower(Power power) {
//        if (power == this.power) {
//            return;
//        }
//        this.power = power;
//        // ... animate
//    }

    public void setBits(int bits) { outlets.setBits(bits); }
    public void setReady(boolean ready) { this.ready = ready; }

    public void spin() {
//        Gdx.app.log(toString(), String.format("spin remain:%d", spinRemain));
        final TubeTile self = this;
        if (spinRemain > 0) {
            ready = false;
            spinRemain--;
            setPower(Power.NONE);
            addAction(Actions.sequence(
                    Actions.rotateBy(DEGREES_SPIN, DURATION_SPIN),
                    Actions.run(new Runnable() {
                        @Override
                        public void run() {
//                            Gdx.app.log(self.toString(), String.format("spunTo r:%.0f x:%.0f y:%.0f w:%.0f h:%.0f", self.getRotation(), self.getX(), self.getY(), self.getWidth(), self.getHeight()));
                            self.spin();
                        }
                    })
            ));
            board.interruptSweep();
        }
        else {
            outletRotation = Math.round(getRotation());
//            Gdx.app.log(toString(), String.format("done spinning to:%d", outletRotation));
            int newRotation = outletRotation % 360;
            if (newRotation != outletRotation) {
//                Gdx.app.log(toString(), String.format("rotate reset %d -> %d", outletRotation, newRotation));
                outletRotation = newRotation;
                setRotation(newRotation);
            }
            ready = true;
            board.readyForSweep();
        }
    }

    public void vanish() {
        ready = false;
        setPower(Power.NONE);
        if (board.isSettled()) {
            final TubeTile self = this;
            addAction(Actions.sequence(
                    Actions.parallel(
                            Actions.alpha(OPACITY_VANISH, DURATION_VANISH),
                            Actions.rotateBy(DEGREES_CIRCLE, DURATION_VANISH),
                            Actions.scaleTo(SCALE_VANISH, SCALE_VANISH, DURATION_VANISH)
                    ),
                    Actions.run(new Runnable() {
                        @Override
                        public void run() {
//                            Gdx.app.log(self.toString(), "vanished");
                            self.onVanishComplete();
                        }
                    })
            ));
        }
        else {
//            Gdx.app.log(toString(), "auto-vanished");
            onVanishComplete();
        }
    }

    public void dropTo(final int colNum, final int rowNum, final float x, final float y) {
        ready = false;
        setPower(Power.NONE);
        final TubeTile self = this;
        if (board.isSettled()) {
            addAction(Actions.sequence(
                    Actions.moveTo(x, y, DURATION_DROP),
                    Actions.run(new Runnable() {
                        @Override
                        public void run() {
//                            Gdx.app.log(self.toString(), String.format("droppedTo col:%d row:%d x:%.0f y:%.0f", colNum, rowNum, x, y));
                            self.onDropComplete(colNum, rowNum);
                        }
                    })
            ));
        }
        else {
//            Gdx.app.log(self.toString(), String.format("auto-droppedTo col:%d row:%d x:%.0f y:%.0f", colNum, rowNum, x, y));
            setPosition(x, y);
            onDropComplete(colNum, rowNum);
        }
    }

    public void onDropComplete(int colNum, int rowNum) {
        setColRow(colNum, rowNum);
        board.tileDropComplete(this, colNum, rowNum);
        setReady(true);
    }

    public void onVanishComplete() {
        board.tileVanishComplete(this);
    }

    @Override
    public void draw(SpriteBatch batch, float parentAlpha) {
        batch.end();
        ShapeRenderer shape = new ShapeRenderer();
        float x = getX();
        float y = getY();
        float width = getWidth();
        float height = getHeight();
        Color backColor = arcShadow(power).cpy();
        float alpha = getAlpha();
        float degrees = getRotation();
        float scaleX = getScaleX();
        float scaleY = getScaleY();
        backColor.a = alpha;
        ShapeDrawer.roundRect(shape, x + padding, y + padding, width - (2 * padding), height - (2 * padding), padding * 2, backColor, degrees, scaleX, scaleY);
        ArrayList<ShapeDrawer.LineSegmentLine> lines = new ArrayList<ShapeDrawer.LineSegmentLine>(outletPathLines.length);
        float midX = x + midpoint;
        float midY = y + midpoint;
        int bits = outlets.getBits();
        for (OutletPathLine line : outletPathLines) {
            if (((line.fromBit == 0) && (bits == line.toBit)) || (((bits & line.fromBit) != 0) && ((bits & line.toBit) != 0))) {
                lines.add(line.segment(midpoint));
            }
        }
        Color arcColor = COLOR_ARC.cpy();
        arcColor.a = alpha;
        if (lines.size() > 0) {
            ShapeDrawer.renderLineSegments(shape, lines, arcColor, arcWidth, degrees, midX, midY, scaleX, scaleY);
        }
        ArrayList<ShapeDrawer.LineSegmentArc> arcs = new ArrayList<ShapeDrawer.LineSegmentArc>(outletPathArcs.length);
        for (OutletPathArc arc : outletPathArcs) {
            if (((bits & arc.fromBit) != 0) && ((bits & arc.toBit) != 0)) {
                arcs.add(arc.segment(midpoint));
            }
        }
        if (arcs.size() > 0) {
            ShapeDrawer.renderArcSegments(shape, arcs, arcColor, arcWidth, degrees, midX, midY, scaleX, scaleY);
        }
        batch.begin();
    }

    /*
    @Override
    public Actor hit (float x, float y, boolean touchable) {
        Actor target = super.hit(x, y, touchable);
        Gdx.app.log(toString(), String.format("hit x:%.0f y:%.0f t:%b %s", x, y, touchable, target != null ? "!!!!!" : ""));
        return target;
    }
    */

    public void onTouchDown() {
//        Gdx.app.log(toString(), String.format("touchDown boardReady:%b tileReady:%b remain:%d", board.isReady(), ready, spinRemain));
        if (board.isReady()) {
            spinRemain++;
            if (ready) {
                spin();
            }
        }
    }

}
