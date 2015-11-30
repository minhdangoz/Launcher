package com.android.launcher3.interpolator;

import android.view.animation.Interpolator;

public class BackInterpolator implements Interpolator {

    /**
     * value is 0
     */
    public static final byte IN = 0;
    /**
     * value is 1
     */
    public static final byte OUT = 1;
    /**
     * value is 2
     */
    public static final byte INOUT = 2;

    byte _mode = 0;

    /**
     * @param mode one of {@link #IN}, {@link #OUT} or {@link #INOUT}
     */
    public BackInterpolator(byte mode) {
        if (mode > -1 && mode < 3) {
            _mode = mode;
        } else {
            throw new IllegalArgumentException("The mode must be 0, 1 or 2. See the doc");
        }
    }

    protected float param_s = 1.70158f;

    public BackInterpolator s(float s) {
        param_s = s;
        return this;
    }

    @Override
    public float getInterpolation(float input) {
        return getDeepFloatValue(input);
    }

    private float getDeepFloatValue(float input) {
        float s;
        switch (_mode) {
            case IN:
                s = param_s;
                return input * input * ((s + 1) * input - s);
            case OUT:
                s = param_s;
                return (input -= 1) * input * ((s + 1) * input + s) + 1;
            case INOUT:
                s = param_s;
                if ((input *= 2) < 1)
                    return 0.5f * (input * input * (((s *= (1.525f)) + 1) * input - s));
                return 0.5f * ((input -= 2) * input * (((s *= (1.525f)) + 1) * input + s) + 2);
        }
        return input;
    }

}
