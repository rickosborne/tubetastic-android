package org.rickosborne.tubetastic.android;

import com.badlogic.gdx.Gdx;

public class ShakeListener {

    protected static final float SHAKE_DELTA = 2f;
    protected static final float SHAKE_INTERVAL = 0.25f;
    protected static final int SHAKE_JERKS = 8;
    protected static final float SHAKE_RESET = -5.0f;

    protected float timeSinceShakeCheck = 0;
    protected float lastAccX = 0;
    protected float lastAccY = 0;
    protected float lastAccZ = 0;
    protected int jerkCount = 0;
    protected ShakeHandler shakeHandler;

    public interface ShakeHandler {
        public void onShake();
    }

    public ShakeListener(ShakeHandler shakeHandler) {
        this.shakeHandler = shakeHandler;
        didShake();
    }

    public void update(float delta) {
        timeSinceShakeCheck += delta;
        if (timeSinceShakeCheck > SHAKE_INTERVAL) {
            if (didShake()) {
                if (shakeHandler != null) {
                    shakeHandler.onShake();
                }
                timeSinceShakeCheck = SHAKE_RESET;
            } else if(timeSinceShakeCheck > 0) {
                timeSinceShakeCheck = 0;
            }
        }
    }

    private boolean didShake() {
        float newAccX = Gdx.input.getAccelerometerX();
        float newAccY = Gdx.input.getAccelerometerY();
        float newAccZ = Gdx.input.getAccelerometerZ();
        float deltaX = Math.abs(newAccX - lastAccX);
        float deltaY = Math.abs(newAccY - lastAccY);
        float deltaZ = Math.abs(newAccZ - lastAccZ);
        lastAccX = newAccX;
        lastAccY = newAccY;
        lastAccZ = newAccZ;
        if ((deltaX > SHAKE_DELTA) || (deltaY > SHAKE_DELTA) || (deltaZ > SHAKE_DELTA)) {
            jerkCount++;
            if (jerkCount >= SHAKE_JERKS) {
                jerkCount = 0;
                return true;
            }
        } else if (jerkCount > 0) {
            jerkCount--;
        }
        return false;
    }

}
