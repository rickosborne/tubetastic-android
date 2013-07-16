package org.rickosborne.tubetastic.android;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

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

//    private static class OutletPath {
//        public int fromBit = 0;
//        public int toBit   = 0;
//        public OutletPath(int fromBit, int toBit) {
//            this.fromBit = fromBit;
//            this.toBit = toBit;
//        }
//    }
//
//    private static class OutletPathLine extends OutletPath {
//        private float x1;
//        private float y1;
//        private float x2;
//        private float y2;
//        public OutletPathLine(int fromBit, int toBit, float x1, float y1, float x2, float y2) {
//            super(fromBit, toBit);
//            this.x1 = x1;
//            this.y1 = y1;
//            this.x2 = x2;
//            this.y2 = y2;
//        }
//        public ShapeDrawer.LineSegmentLine segment(float size) {
//            return new ShapeDrawer.LineSegmentLine(size * x1, size * y1, size * x2, size * y2);
//        }
//    }
//
//    public static class OutletPathArc extends OutletPath {
//        private float x;
//        private float y;
//        private float r;
//        private float a1;
//        private float a2;
//        public OutletPathArc(int fromBit, int toBit, float x, float y, float r, float startDegrees, float endDegrees) {
//            super(fromBit, toBit);
//            this.x = x;
//            this.y = y;
//            this.r = r;
//            this.a1 = startDegrees * DEGREES_TO_RADIANS;
//            this.a2 = endDegrees * DEGREES_TO_RADIANS;
//        }
//        public ShapeDrawer.LineSegmentArc segment(float size) {
//            return new ShapeDrawer.LineSegmentArc(size * x, size * y, size * r, a1, a2);
//        }
//    }

    private static final OutletProbability[] outletProbabilities;
    public static final float DURATION_VANISH = 0.500f;
    public static final float DURATION_DROP   = 0.250f;
    public static final float DURATION_SPIN   = 0.150f;
    public static final float DEGREES_SPIN    = -90f;
    public static final float DEGREES_CIRCLE  = -360f;
    public static final float OPACITY_VANISH  = 0f;
    public static final float SCALE_VANISH    = 0f;


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
    }

    private int spinRemain = 0;
    private boolean isSpinning = false;
    private boolean isVanishing = false;
    private boolean isDropping = false;

    public TubeTile(int colNum, int rowNum, float x, float y, float size, GameBoard board) {
        super(colNum, rowNum, x, y, size, board);
        init(colNum, rowNum, x, y, size, 0, board);
    }

    public TubeTile(int colNum, int rowNum, float x, float y, float size, int bits, GameBoard board) {
        super(colNum, rowNum, x, y, size, board);
        init(colNum, rowNum, x, y, size, bits, board);
    }

    public void init(int colNum, int rowNum, float x, float y, float size, int bits, final GameBoard board) {
        super.init(colNum, rowNum, x, y, size, board);
        if (bits <= 0) {
            setRandomOutlets();
        }
        else {
            outlets = new Outlets(bits);
        }
        resize(x, y, size);
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

    public void setBits(int bits) { outlets.setBits(bits); }

    public void spin() {
//        Gdx.app.log(toString(), String.format("spin remain:%d", spinRemain));
        final TubeTile self = this;
        board.interruptSweep();
        if (spinRemain > 0) {
            isSpinning = true;
            setPower(Power.NONE);
            spinRemain--;
//            Gdx.app.log(toString(), String.format("spin start remain:%d", spinRemain));
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
        }
        else {
            outletRotation = MathUtils.round(getRotation());
            int newRotation = outletRotation % 360;
//            Gdx.app.log(toString(), String.format("done spinning to:%d/%d", outletRotation, newRotation));
            if (newRotation != outletRotation) {
//                Gdx.app.log(toString(), String.format("rotate reset %d -> %d", outletRotation, newRotation));
                outletRotation = newRotation;
                setRotation(newRotation);
            }
            isSpinning = false;
            board.readyForSweep();
        }
    }

    public void vanish() {
        isVanishing = true;
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
        isDropping = true;
        setPower(Power.NONE);
        final TubeTile self = this;
        if (board.isSettled()) {
            addAction(Actions.sequence(
                    Actions.moveTo((int) x, (int) y, DURATION_DROP),
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
            setPosition((int) x, (int) y);
            onDropComplete(colNum, rowNum);
        }
    }

    public void onDropComplete(int colNum, int rowNum) {
        setColRow(colNum, rowNum);
        board.tileDropComplete(this, colNum, rowNum);
        isDropping = false;
    }

    public void onVanishComplete() {
        board.tileVanishComplete(this);
    }

    public void onTouchDown() {
//        Gdx.app.log(toString(), String.format("touchDown boardReady:%b tileReady:%b remain:%d", board.isReady(), ready, spinRemain));
        if (board.isReady() && !isVanishing && !isDropping) {
            if (isSpinning) {
                spinRemain++;
//                Gdx.app.log(toString(), String.format("spin add %d", spinRemain));
            } else {
                spinRemain = 1;
//                Gdx.app.log(toString(), String.format("spin start %d", spinRemain));
                spin();
            }
        }
    }

}
