package mcjty.efab.blockentity;

/**
 * Client-side flywheel/piston spin state. Held by a block entity and advanced once per client
 * tick via {@link #tick(boolean)}; the renderer reads {@link #angle(float)} with the frame's
 * partial tick for smooth interpolation.
 *
 * <p>The wheel always turns (slow at idle, fast while working) so that its speed represents the
 * machine's work status, mirroring the original EFab behaviour.
 */
public class SpinAnimation {

    private final float idleSpeed;
    private final float maxSpeed;
    private final float spinUp;
    private final float spinDown;
    private final float degreesPerUnit;

    private float speed;
    private float angle;
    private float prevAngle;

    public SpinAnimation(float idleSpeed, float maxSpeed, float spinUp, float spinDown, float degreesPerUnit) {
        this.idleSpeed = idleSpeed;
        this.maxSpeed = maxSpeed;
        this.spinUp = spinUp;
        this.spinDown = spinDown;
        this.degreesPerUnit = degreesPerUnit;
        this.speed = idleSpeed;
    }

    public void tick(boolean working) {
        prevAngle = angle;
        float target = working ? maxSpeed : idleSpeed;
        if (speed < target) {
            speed = Math.min(target, speed + spinUp);
        } else if (speed > target) {
            speed = Math.max(target, speed - spinDown);
        }
        angle = (angle + speed * degreesPerUnit) % 360.0f;
    }

    /** Interpolated rotation in degrees for the current frame. */
    public float angle(float partialTick) {
        float from = prevAngle;
        float to = angle;
        if (to < from) {
            to += 360.0f;
        }
        return from + (to - from) * partialTick;
    }

    /** Current spin speed (idle..max), used to phase the pistons. */
    public float speed() {
        return speed;
    }
}
