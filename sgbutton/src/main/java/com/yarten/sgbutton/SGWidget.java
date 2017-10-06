package com.yarten.sgbutton;

import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import java.lang.ref.WeakReference;

/**
 * Created by yarten on 2017/10/5.
 * 控件修饰器，用控件初始化该类的对象，可使得对象获得特殊能力
 * 根据不同事件过程，控件可以定制为以下几种：
 * 1. 普通点击的控件
 * 2. 长点击触发的控件，可定制长按时间
 * 3. 触点移动时做出动作的控件
 * 4. 触点离开时做出动作的控件
 * 5. 按住时持续触发动作的控件（不管是否有在移动），可定制时间间隔
 * 6. 要长按后才能触发连续动作的控件，可定制长按时间
 * 7. 刚按下时触发动作，长按之后又连续触发另一个动作的控件，可定制长按时间
 * 8. 以上功能任意组合的控件！！
 */

public class SGWidget
{
    //region 构造器
    protected View view;

    public SGWidget(View view)
    {
        this.view = view;
        if(view != null)
            //region 设置onTouch监听器
            this.view.setOnTouchListener(new View.OnTouchListener()
            {
                @Override
                public boolean onTouch(View view, MotionEvent event)
                {
                    //region 事件预处理
                    int action = event.getAction();
                    long currentTime = System.currentTimeMillis();
                    //endregion

                    if(action == MotionEvent.ACTION_DOWN)
                        //region 触发按压：重置时间，开启或重启按压线程
                    {
                        lastingTime = 0;
                        lastTime = currentTime;
                        lastX = event.getRawX();
                        lastY = event.getRawY();

                        if(singlePressThread == null)
                            singlePressThread = new SGThread();
                        else if(singlePressThread.isAlive())
                            singlePressThread.interrupt();

                        onTouch = true;
                        if(onPressListener != null) singlePressThread.start();
                        if(onDownListener != null) onDownListener.onAction(SGWidget.this.view, event);
                    } //endregion
                    else
                        //region 正在按压：刷新按压时间
                    {
                        lastingTime += (currentTime-lastTime);
                        lastTime = currentTime;
                    } //endregion

                    switch (event.getAction())
                    {
                        //region Case：在控件上移动
                        case MotionEvent.ACTION_MOVE:
                        {
                            if(onMoveListener != null)
                                onMoveListener.onAction(SGWidget.this.view, event);
                        } break;
                        //endregion

                        //region Case：取消控件事件
                        case MotionEvent.ACTION_CANCEL:
                        {
                            onTouch = false;
                            singlePressThread.interrupt();
                        } break;
                        //endregion

                        //region Case：离开控件
                        case MotionEvent.ACTION_UP:
                        {
                            onTouch = false;
                            singlePressThread.interrupt();

                            if(onClickListener != null && lastingTime < longClickTime && !noClick)
                                onClickListener.onAction(SGWidget.this.view, event);
                            else if(onLongClickListener != null && lastingTime >= longClickTime && !noLongClick)
                                onLongClickListener.onAction(SGWidget.this.view, event);
                            else if(noClick && noLongClick && onLeaveListener != null)
                                onLeaveListener.onAction(SGWidget.this.view, event);

                        } break;
                        //endregion
                    }

                    //region 更新事件信息
                    lastX = event.getRawX();
                    lastY = event.getRawY();
                    //endregion
                    return false;
                }
            });
        //endregion
        handler = new SGHandler(this);
    }

    public View getView(){return view;}
    //endregion

    //region 事件相关信息获取
    private float lastX, lastY;
    private boolean onTouch = false;

    public float getLastX(){return lastX;}
    public float getLastY(){return lastY;}
    public boolean isOnTouch(){return onTouch;}
    //endregion

    //region 各类监听器：点击，长点击，按压，移动（不要使用原生监听器）
    public interface OnActionListener
    {
        void onAction(View view, MotionEvent event);
    }

    private OnActionListener onClickListener;
    private OnActionListener onLongClickListener;
    private OnActionListener onDownListener;
    private OnActionListener onPressListener;
    private OnActionListener onMoveListener;
    private OnActionListener onLeaveListener;

    public void setOnClickListener(OnActionListener onClickListener)
    {
        this.onClickListener = onClickListener;
    }

    public void setOnLongClickListener(OnActionListener onLongClickListener)
    {
        this.onLongClickListener = onLongClickListener;
    }

    public void setOnPressListener(OnActionListener onPressListener)
    {
        this.onPressListener = onPressListener;
    }

    public void setOnMoveListener(OnActionListener onMoveListener)
    {
        this.onMoveListener = onMoveListener;
    }

    public void setOnLeaveListener(OnActionListener onLeaveListener)
    {
        this.onLeaveListener = onLeaveListener;
    }

    public void setOnDownListener(OnActionListener onDownListener)
    {
        this.onDownListener = onDownListener;
    }

    public boolean noLongClick = false;
    public boolean noClick = false;
    //endregion

    //region 按压线程控制模块
    // 用于按压线程调用按钮（this）的按压监听器
    private static class SGHandler extends Handler
    {
        private WeakReference<SGWidget> ref;

        SGHandler(SGWidget widget){ref = new WeakReference<SGWidget>(widget);}

        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            SGWidget widget = ref.get();
            if (widget != null && widget.onPressListener != null)
            {
                widget.onPressListener.onAction(widget.view, null);
            }
        }
    }

    private SGHandler handler;

    private class SGThread extends Thread
    {
        private boolean isFirstTime = true;

        @Override
        public void run()
        {
            if(firstPressOffsetTime != 0)
                try{Thread.sleep(firstPressOffsetTime);}
                catch (InterruptedException e) {}

            while(onTouch)
            {
                handler.sendEmptyMessage(1);

                try
                {
                    if(isFirstTime)
                    {
                        Thread.sleep(firstIntervalTime);
                        isFirstTime = false;
                    }
                    else Thread.sleep(intervalTime);
                }
                catch (InterruptedException e) {}
            }
        }
    }

    private SGThread singlePressThread;
    //endregion

    //region 按压过程定制
    private long lastingTime;
    private long lastTime;
    private int longClickTime = 500;
    private int intervalTime = 50;
    private int firstIntervalTime = 500;
    private int firstPressOffsetTime = 0;


    public void setLongClickTime(int ms)
    {
        if(ms <= 0) ms = 500;
        longClickTime = ms;
    }

    public int getLongClickTime()
    {
        return longClickTime;
    }

    public void setIntervalTime(int ms)
    {
        if(ms <= 0) ms = 50;
        intervalTime = ms;
    }

    public int getIntervalTime()
    {
        return intervalTime;
    }

    public void setFirstIntervalTime(int ms)
    {
        if(ms <= 0) ms = 50;
        firstIntervalTime = ms;
    }

    public int getFirstIntervalTime()
    {
        return firstIntervalTime;
    }

    public void setFirstPressOffsetTime(int ms)
    {
        if(ms < 0) ms = 0;
        firstPressOffsetTime = ms;
    }

    public int getFirstPressOffsetTime()
    {
        return firstPressOffsetTime;
    }
    //endregion
}
