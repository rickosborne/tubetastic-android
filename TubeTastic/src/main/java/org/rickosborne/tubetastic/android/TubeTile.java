package org.rickosborne.tubetastic.android;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;

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

    private static final OutletProbability[] outletProbabilities;
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

}
