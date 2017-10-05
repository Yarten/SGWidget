package com.yarten.sgbutton;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity
{
    private SGWidget bt, bt2;
    TextView t1, t2;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        t1 = findViewById(R.id.textView1);
        t2 = findViewById(R.id.textView2);

        bt2 = new SGWidget(findViewById(R.id.button2));
        bt2.setOnMoveListener(new SGWidget.OnActionListener()
        {
            private int lastX, lastY;
            @Override
            public void onAction(View view, MotionEvent event)
            {
                
            }
        });


        bt = new SGWidget(findViewById(R.id.button));
        bt.setOnClickListener(new SGWidget.OnActionListener()
        {
            @Override
            public void onAction(View view, MotionEvent event)
            {
                t1.setText("Click");
            }
        });
        bt.setOnLongClickListener(new SGWidget.OnActionListener()
        {
            @Override
            public void onAction(View view, MotionEvent event)
            {
                t1.setText("Long");
            }
        });
        bt.setOnPressListener(new SGWidget.OnActionListener()
        {
            private int num = 0;

            @Override
            public void onAction(View view, MotionEvent event)
            {
                t2.setText(String.format("%d", num++));

            }
        });
    }
}
