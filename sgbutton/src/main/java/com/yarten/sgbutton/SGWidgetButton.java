package com.yarten.sgbutton;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.os.Handler;

/**
 * Created by yarten on 2017/10/5.
 * 控件按钮 —— 继承自SG控件
 * 描述：
 *    像UI设计程序中，放在工具箱里的控件按钮，用于创建控件。
 * 点击其可以弹出提示信息（未完成），长按时自动新建控件到指
 * 定layout上，并可以拖曳它。创建函数由用户传入。
 */

public class SGWidgetButton extends SGWidget
{
    //region 构造器
    public SGWidgetButton(View view, ViewGroup layout)
    {
        super(view);
        this.layout = layout;
        initEvent();
    }

    private ViewGroup layout;
    private boolean canMove = false;
    private boolean firstTimePress = true;

    private void initEvent()
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
                    if(onAddHandler != null)
                    {
                        View oldView = SGWidgetButton.super.view;
                        newView = onAddHandler.createNewView(SGWidgetButton.this.userdata);
                        newView.setX(oldView.getX()); //100
                        newView.setY(oldView.getY()); //100
                        newView.setAlpha(0.35f);
                        if(layout != null)
                        {
                            layout.addView(newView);
                        }
                    }

                    synchronized (this){canMove = true;}
                }
            }
        });

        super.setOnMoveListener(new OnActionListener()
        {
            @Override
            public void onAction(View view, MotionEvent event)
            {
                synchronized (this){if(!canMove) return;}
                float dx = event.getRawX()-SGWidgetButton.super.getLastX();
                float dy = event.getRawY()-SGWidgetButton.super.getLastY();

                newView.setX(newView.getX()+dx);
                newView.setY(newView.getY()+dy);
            }
        });

        super.setOnLongClickListener(new OnActionListener()
        {
            @Override
            public void onAction(View view, MotionEvent event)
            {
                newView.setAlpha(1.0f);
                canMove = false;
                firstTimePress = true;
            }
        });
    }
    //endregion

    //region 添加控件回调事件的设置
    public interface OnAddHandler
    {
        View createNewView(int userdata);
    }

    private OnAddHandler onAddHandler;

    private View newView;

    private int userdata;

    public void setOnAddHandler(OnAddHandler onAddHandler)
    {
        setOnAddHandler(onAddHandler, -65535);
    }

    public void setOnAddHandler(OnAddHandler onAddHandler, int userdata)
    {
        this.onAddHandler = onAddHandler;
        this.userdata = userdata;
    }
    //endregion
}
