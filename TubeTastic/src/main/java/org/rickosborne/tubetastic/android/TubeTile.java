package org.rickosborne.tubetastic.android;

public class TubeTile extends BaseTile {

    private final class OutletProbability {
        public double probability;
        public Outlets[] outlets;
        public void init(double probability, int[] bits) {
            this.probability = probability;
            this.outlets = new Outlets[bits.length];
            for (int i = 0; i < bits.length; i++) {
                this.outlets[i].setBits(bits[i]);
            }
        }
        public Outlets getRandomOutlets() {
            return outlets[RandomService.getRandom().nextInt(outlets.length)];
        }
    }

    private static final OutletProbability[] outletProbabilities;

    static {
        outletProbabilities = new OutletProbability[4];
        outletProbabilities[0].init(0.05, new int[]{ Outlets.BIT_NORTH | Outlets.BIT_EAST | Outlets.BIT_SOUTH | Outlets.BIT_WEST });
        outletProbabilities[1].init(0.50, new int[]{
            Outlets.BIT_EAST  | Outlets.BIT_SOUTH | Outlets.BIT_WEST,
            Outlets.BIT_NORTH | Outlets.BIT_SOUTH | Outlets.BIT_WEST,
            Outlets.BIT_NORTH | Outlets.BIT_EAST  | Outlets.BIT_WEST,
            Outlets.BIT_NORTH | Outlets.BIT_EAST  | Outlets.BIT_SOUTH
        });
        outletProbabilities[2].init(0.90, new int[]{
            Outlets.BIT_NORTH | Outlets.BIT_EAST,
            Outlets.BIT_NORTH | Outlets.BIT_SOUTH,
            Outlets.BIT_NORTH | Outlets.BIT_WEST,
            Outlets.BIT_EAST  | Outlets.BIT_SOUTH,
            Outlets.BIT_EAST  | Outlets.BIT_WEST,
            Outlets.BIT_SOUTH | Outlets.BIT_WEST,
        });
        outletProbabilities[3].init(1.00, new int[]{
            Outlets.BIT_NORTH,
            Outlets.BIT_EAST,
            Outlets.BIT_SOUTH,
            Outlets.BIT_WEST
        });
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

    public void setBits(int bits) {
        outlets.setBits(bits);
    }

    public void spin() {
        if (spinRemain > 0) {
            ready = false;
            spinRemain--;
            setPower(Power.NONE);
        }
        else {
            if (rotation > 360) {
                rotation %= 360;
            }
            outletRotation = (int) rotation;
            ready = true;
        }
    }

    public void vanish() {
        ready = false;
        setPower(Power.NONE);
        if (board.isSettled()) {
            // ...
        }
        else {
            // ...
        }
    }

    public void dropTo(int colNum, int rowNum, float x, float y) {
        ready = false;
        setPower(Power.NONE);
        if (board.isSettled()) {
            // ... tween ...
        }
        else {
            this.x = x + this.midpoint;
            this.y = y + this.midpoint;
            // ...
        }
    }

}
