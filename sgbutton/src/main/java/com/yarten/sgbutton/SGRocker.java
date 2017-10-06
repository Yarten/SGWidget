package com.yarten.sgbutton;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

import static android.content.ContentValues.TAG;

/**
 * Created by yarten on 2017/10/5.
 * SGRocker类 —— 摇杆控件
 * 组成：
 * 1. 背景，圆环，可以设置颜色与半径
 * 2. 前景，小圆，可以设置颜色
 */

public class SGRocker extends SurfaceView implements SurfaceHolder.Callback, Runnable
{
    //region 构造器
    private SurfaceHolder sfh;
    private Paint bgPainter, fgPainter;

    public SGRocker(Context context)
    {
        super(context);
        sfh = this.getHolder();
        sfh.addCallback(this);
        sfh.setFormat(PixelFormat.TRANSLUCENT);
        super.setZOrderOnTop(true);

        setFocusable(true);
        setFocusableInTouchMode(true);

        initPainter();
    }

    private void initPainter()
    {
        bgPainter = new Paint();
        bgPainter.setAntiAlias(true);
        bgPainter.setStyle(Paint.Style.STROKE);
        float r = radius * 0.25f;
        bgPainter.setStrokeWidth(r);
        bgPainter.setColor(backgroundColor);

        fgPainter = new Paint();
        fgPainter.setAntiAlias(true);
        fgPainter.setStyle(Paint.Style.FILL);
        fgPainter.setColor(foregroundColor);
    }
    //endregion

    //region 渲染线程控制
    private boolean isRunning;

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        isRunning = true;
        rockerX = getX();
        rocketY = getY();
        new Thread(this).start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int widht, int height)
    {
        // TODO
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        isRunning = false;
    }

    @Override
    public void run()
    {
        while(isRunning)
        {
            draw();
            try{Thread.sleep(10);}
            catch (Exception e){}
        }
    }
    //endregion

    //region 渲染线程
    private Canvas canvas;

    private final float offsetScale = 1.6f;

    private float dScale = 0.0f;

    private boolean showing = false;

    private void draw()
    {
        canvas = sfh.lockCanvas();

        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        float x = super.getX();
        float y = super.getY();
        float offset = radius*offsetScale;
        float offset2 = radius*(offsetScale-1);

        if(!effect) dScale = 1.0f;

        float D = 2*radius; //*dScale;
        float E = 0; //2*radius*(1-dScale);

        // 绘制背景
    //    canvas.drawCircle(x+offset, y+offset, radius*dScale, bgPainter);
        RectF rectF = new RectF(x+offset2+E, y+offset2+E, x+offset2+D, y+offset2+D);
        bgPainter.setAlpha((int)(dScale*255));

        for(int i = 0; i < 4; i++)
        {
            canvas.drawArc(rectF, -30+90*i+270*(1.0f-dScale), 60, false, bgPainter);
        }

        if(!showing && dScale > 0)
        {
            dScale -= 0.05;
            if(dScale < 0) dScale = 0;
        }
        else if(showing && dScale < 1)
            dScale += 0.05;


        // 绘制前景
        canvas.drawCircle(rockerX+offset, rocketY+offset, radius*0.6f, fgPainter);

        if(canvas != null) sfh.unlockCanvasAndPost(canvas);
    }


    //endregion

    //region 颜色，半径等设置
    private int backgroundColor = 0xff8bc5ba;
    private int foregroundColor = 0xf84c4cb8;

    private float radius = 300;

    public void setRadius(float radius)
    {
        this.radius = radius;
        float r = radius * 0.25f;
        bgPainter.setStrokeWidth(r);
    }

    public float getRadius(){return radius;}

    public void setBackgroundColor(int color)
    {
        backgroundColor = color;
        bgPainter.setColor(color);
    }

    public int getBackgroundColor(){return backgroundColor;}

    public void setForegroundColor(int color)
    {
        foregroundColor = color;
        fgPainter.setColor(color);
    }

    public int getForegroundColor(){return foregroundColor;}

    public boolean smooth = false;

    public boolean effect = true;
    //endregion

    //region 触摸事件管理
    private float rockerX, rocketY;
    private float lastX, lastY;
    private float lastingX, lastingY;

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        int action = event.getAction();
        float cenX = super.getX();
        float cenY = super.getY();
        float offset = radius * offsetScale;
        float curX = event.getX() - offset;
        float curY = event.getY() - offset;

        switch (action)
        {
            case MotionEvent.ACTION_DOWN:
            {
                lastX = curX;
                lastY = curY;
                lastingX = lastingY = 0;
                showing = true;
            } // no break
            case MotionEvent.ACTION_MOVE:
            {
                if(smooth)
                {
                    lastingX += (curX-lastX);
                    lastingY += (curY-lastY);
                    lastX = curX;
                    lastY = curY;
                    curX = lastingX;
                    curY = lastingY;
                }

                if(length(cenX-curX, cenY-curY) >= radius)
                    //region 当触屏区域不在活动范围内控制小球落在边沿
                {
                    //得到摇杆中心与触屏点所形成的角度
                    float rad = getRad(cenX, cenY, curX, curY);
                    //保证内部小圆运动的长度限制
                    getXYonCircle(cenX, cenY, rad);
                }   //endregion，
                else
                    //region 小球在活动范围内，直接等于当前触点位置
                {
                    rockerX = curX;
                    rocketY = curY;
                }   //endregion
            } break;

            case MotionEvent.ACTION_UP:
            {
                rockerX = cenX;
                rocketY = cenY;
                showing = false;
            } break;
        }

        return true;
    }

    // 得到两点间弧度
    private float getRad(float x1, float y1, float x2, float y2)
    {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float z = length(dx, dy);
        float cosAngle = dx / z;
        float rad = (float)Math.acos(cosAngle);
        if(y2 < y1) rad = -rad;
        return rad;
    }

    private void getXYonCircle(float cenX, float cenY, float rad)
    {
        rockerX = (float)(radius * Math.cos(rad)) + cenX;
        rocketY = (float)(radius * Math.sin(rad)) + cenY;
    }

    private float length(float a, float b)
    {
        return (float)Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
    }
    //endregion
}
