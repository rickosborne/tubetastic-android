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

    public TubeTile(int colNum, int rowNum, GameBoard board) {
        super(colNum, rowNum, board);
        init(colNum, rowNum, 0, board);
    }

    public TubeTile(int colNum, int rowNum, int bits, GameBoard board) {
        super(colNum, rowNum, board);
        init(colNum, rowNum, bits, board);
    }

    public void init(int colNum, int rowNum, int bits, final GameBoard board) {
        super.init(colNum, rowNum, board);
        if (bits <= 0) {
            setRandomOutlets();
        }
        else {
            outlets = new Outlets(bits);
        }
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

}
