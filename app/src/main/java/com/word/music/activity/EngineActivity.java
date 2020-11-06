package com.word.music.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.word.music.R;

import java.util.Timer;
import java.util.TimerTask;

public class EngineActivity extends AppCompatActivity implements View.OnClickListener {
    private int recLen = 4; //
    private TextView tv;
    Timer timer = new Timer();
    private Handler handler;
    private Runnable runnable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setFlags(flag, flag);

        setContentView(R.layout.activity_splash);
        initView();
        timer.schedule(task, 1000, 1000);
        handler = new Handler();
        handler.postDelayed(runnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(EngineActivity.this, ListActivity.class);
                startActivity(intent);
                finish();
            }
        }, 4000);

    }

    private void initView() {
        tv = findViewById(R.id.tv);
        tv.setOnClickListener(this);
    }

    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    recLen--;
                    tv.setText("关闭广告 " + recLen + "秒");
                    if (recLen < 0) {
                        timer.cancel();
                        tv.setVisibility(View.GONE);
                    }
                }
            });
        }
    };

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv:
                Intent intent = new Intent(EngineActivity.this, ListActivity.class);
                startActivity(intent);
                finish();
                if (runnable != null) {
                    handler.removeCallbacks(runnable);
                }
                break;
            default:
                break;
        }
    }
}