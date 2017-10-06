package com.yarten.sgbutton;

/**
 * Created by yarten on 2017/10/6.
 * 数学函数发生器
 * 描述：
 *     根据时间变量，输出函数值，函数可以自定义。时间变量
 * 由该类管理。时间可以被重置、加快减慢、逆转、暂停等操作。
 */

public class MathFunction
{
    //region 构造器
    MathFunction()
    {
        this(0);
    }

    MathFunction(long duration)
    {
        this(duration, 0, 0, 0, 0, 0, 0);
    }

    MathFunction(long duration, float domainMin, float domainMax, float codomainMin, float codomainMax)
    {
        this(duration, domainMin, domainMax, codomainMin, codomainMax, 0, 100);
    }

    MathFunction(long duration, float domainMin, float domainMax, float codomainMin, float codomainMax, float rangeMin, float rangeMax)
    {
        this.duration = duration;
        this.domainMin = domainMin;
        this.domainMax = domainMax;
        this.codomainMin = codomainMin;
        this.codomainMax = codomainMax;
        this.yMin = rangeMin;
        this.yMax = rangeMax;
    }
    //endregion

    //region 数学函数与定义域、值域、输出范围设置
    public interface Timing
    {
        float f(float t);
    }

    private Timing Ftime;

    public void setTimingFunction(Timing func)
    {
        this.Ftime = func;
    }

    public float timing()
    {
        if(Ftime == null) return 0;
        update(true);

        if(lastingTime > duration) lastingTime = duration;
        if(lastingTime < 0) lastingTime = 0;

        float t = 0;
        if(domainMin == 0 && domainMax == 0) t = lastingTime;
        else if(duration != 0) t = domainMin + (domainMax-domainMin)*(lastingTime*1.0f/duration);

        float f = Ftime.f(t);
        float d = codomainMax-codomainMin;
        if(!(yMax == 0 && yMin == 0) && d != 0) f = yMin + (yMax-yMin)*(f-codomainMin)/(d);

        return f;
    }

    private float domainMin, domainMax;
    private float codomainMin, codomainMax;
    private float yMin, yMax;

    public void setDomian(float min, float max)
    {
        if(min > max)
        {
            float t = min;
            min = max;
            max = t;
        }
        domainMin = min; domainMax = max;
    }

    public void setCodomain(float min, float max)
    {
        if(min > max)
        {
            float t = min;
            min = max;
            max = t;
        }
        codomainMin = min; codomainMax = max;
    }

    public void setRange(float min, float max)
    {
        if(min > max)
        {
            float t = min;
            min = max;
            max = t;
        }
        yMin = min; yMax = max;
    }
    //endregion

    //region 状态转移管理
    private enum State
    {
        Stop, Ready, Pause, Runing
    }

    private State state = State.Stop;
    private boolean forward = true;

    public void ready()
    {
        state = State.Ready;
        update(false);
    }

    public void start()
    {
        switch (state)
        {
            case Runing:
                return;
            case Stop:
                ready();
            default:
                state = State.Runing;
                break;
        }

        update(false);
    }

    public void pause()
    {
        switch (state)
        {
            case Runing:
                state = State.Pause;
                break;
            default: return;
        }

        update(false);
    }

    public void stop()
    {
        switch (state)
        {
            case Stop: return;
            default:
                state = State.Stop;
                break;
        }

        update(false);
    }

    public void restart()
    {
        stop();
        start();
    }

    public void direction(boolean forward)
    {
        this.forward = forward;
    }

    public void reverse()
    {
        forward = !forward;
    }
    //endregion

    //region 时间管理
    private long duration = 0;
    private long lastTime = 0;
    private long lastingTime = 0;
    private long currentTime = 0;
    private boolean isResume = false;

    private void update(boolean inFunc)
    {
        currentTime = System.currentTimeMillis();

        switch (state)
        {
            case Ready:
                lastingTime = 0;
                isResume = false;
                if(inFunc) state = State.Runing;
                break;
            case Runing:
                if(isResume) isResume = false;
                else if(forward) lastingTime += (currentTime-lastTime);
                else lastingTime -= (currentTime-lastTime);
                break;
            case Pause:
                if(!isResume)
                {
                    if(forward) lastingTime += (currentTime-lastTime);
                    else lastingTime -= (currentTime-lastTime);
                    isResume = true;
                }
                break;
            case Stop:
                isResume = false;
                break;
        }

        lastTime = currentTime;
    }

    public void setDuration(long duration)
    {
        this.duration = duration;
    }

    public long getDuration()
    {
        return duration;
    }
    //endregion
}
