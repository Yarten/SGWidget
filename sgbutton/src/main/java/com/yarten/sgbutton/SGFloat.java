package com.yarten.sgbutton;

import android.view.MotionEvent;
import android.view.View;

/**
 * Created by yarten on 2017/10/5.
 * 浮动控件 —— 继承自SG控件
 * 实现的功能：
 * 1. 点击触发用户自定义事件
 * 2. 长按可以拖曳控件
 * Note: 不适合用于布局中
 */

public class SGFloat extends SGWidget
{
    public SGFloat(View view)
    {
        super(view);
        init();
    }

    private boolean canMove = false;
    private boolean firstTimePress = true;

    private void init()
    {
        super.setFirstPressOffsetTime(500);
        super.setLongClickTime(400);
        super.setOnPressListener(new OnActionListener()
        {
            @Override
            public void onAction(View view, MotionEvent event)
            {
                if(firstTimePress)
                {
                    firstTimePress = false;
                    synchronized (this){canMove = true;}
                    view.setAlpha(0.35f);
                }
            }
        });

        super.setOnMoveListener(new OnActionListener()
        {
            @Override
            public void onAction(View view, MotionEvent event)
            {
                synchronized (this){if(!canMove) return;}
                float dx = event.getRawX()-SGFloat.super.getLastX();
                float dy = event.getRawY()-SGFloat.super.getLastY();

                view.setX(view.getX()+dx);
                view.setY(view.getY()+dy);
            }
        });

        super.setOnLongClickListener(new OnActionListener()
        {
            @Override
            public void onAction(View view, MotionEvent event)
            {
                view.setAlpha(1.0f);
                canMove = false;
                firstTimePress = true;
            }
        });
    }
}
