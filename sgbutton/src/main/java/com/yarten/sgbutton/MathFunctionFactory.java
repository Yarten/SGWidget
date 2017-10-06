package com.yarten.sgbutton;

/**
 * Created by yarten on 2017/10/6.
 */

class LinearFunction extends MathFunction
{
    LinearFunction(long duration)
    {
        this(duration, 0, 100);
    }

    LinearFunction(long duration, float rangeMin, float rangeMax)
    {
        this(duration, rangeMin, rangeMax, 1, 0);
    }

    LinearFunction(long duration, float rangeMin, float rangeMax, float k, float b)
    {
        super(duration, 0, 100, 0, 0, rangeMin, rangeMax);
        reset(k, b);
    }

    private float k, b;

    public void reset(float k, float b)
    {
        this.k = k;
        this.b = b;
        Timing func = new Timing()
        {
            @Override
            public float f(float t)
            {
                return LinearFunction.this.k*t+LinearFunction.this.b;
            }
        };

        super.setTimingFunction(func);
        super.setCodomain(func.f(0), func.f(100));
    }
}
