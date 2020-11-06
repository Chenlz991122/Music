package com.word.music.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.word.music.R;
import com.word.music.model.MusicData;
import com.word.music.service.MusicService;
import com.word.music.utils.DisplayUtil;
import com.word.music.utils.FastBlurUtil;
import com.word.music.widget.BackgourndAnimationRelativeLayout;
import com.word.music.widget.DiscView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.word.music.widget.DiscView.DURATION_NEEDLE_ANIAMTOR;


public class PlayerActivity extends AppCompatActivity implements DiscView.IPlayInfo, View
        .OnClickListener {

    private DiscView discView;
    private Toolbar toolbar;
    private SeekBar seekBar;
    private ImageView statusChange, next, former, isCycle;
    private TextView timeSite, totalDuration;
    private BackgourndAnimationRelativeLayout rootLayout;

    public static final int MUSIC_MESSAGE = 0;
    public static final String PARAM_MUSIC_LIST = "PARAM_MUSIC_LIST";

    private Handler musicHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            seekBar.setProgress(seekBar.getProgress() + 1000);
            timeSite.setText(duration2Time(seekBar.getProgress()));
            startUpdateSeekBarProgress();
        }
    };

    private MusicReceiver musicReceiver = new MusicReceiver();
    private List<MusicData> musicDatas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        initMusicDatas();
        initView();
        initMusicReceiver();
        setToolBar();
        makeStatusBarTransparent();
        initIndex();
    }

    private void setToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        this.setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    //Toolbar的事件---返回
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initIndex() {
        rootLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                optMusic(MusicService.ACTION_OPT_MUSIC_PLAY);
            }
        }, DURATION_NEEDLE_ANIAMTOR);
        stopUpdateSeekBarProgree();
        discView.playIndex(ListActivity.sqlIndex);
        timeSite.setText(duration2Time(0));
        totalDuration.setText(duration2Time(0));
    }

    private void initMusicReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MusicService.ACTION_STATUS_MUSIC_PLAY);
        intentFilter.addAction(MusicService.ACTION_STATUS_MUSIC_PAUSE);
        intentFilter.addAction(MusicService.ACTION_STATUS_MUSIC_DURATION);
        intentFilter.addAction(MusicService.ACTION_STATUS_MUSIC_COMPLETE);
        LocalBroadcastManager.getInstance(this).registerReceiver(musicReceiver, intentFilter);
    }

    private void initView() {
        discView = (DiscView) findViewById(R.id.discview);
        next = (ImageView) findViewById(R.id.ivNext);
        former = (ImageView) findViewById(R.id.ivLast);
        isCycle = (ImageView) findViewById(R.id.ivCycle);
        statusChange = (ImageView) findViewById(R.id.ivPlayOrPause);
        timeSite = (TextView) findViewById(R.id.tvCurrentTime);
        totalDuration = (TextView) findViewById(R.id.tvTotalTime);
        seekBar = (SeekBar) findViewById(R.id.musicSeekBar);
        rootLayout = (BackgourndAnimationRelativeLayout) findViewById(R.id.rootLayout);

        toolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

        if (ListActivity.isCycle) {
            isCycle.setImageResource(R.drawable.ic_type2);
        } else {
            isCycle.setImageResource(R.drawable.ic_type1);
        }

        discView.setPlayInfoListener(this);
        former.setOnClickListener(this);
        isCycle.setOnClickListener(this);
        next.setOnClickListener(this);
        statusChange.setOnClickListener(this);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                timeSite.setText(duration2Time(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                stopUpdateSeekBarProgree();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekTo(seekBar.getProgress());
                startUpdateSeekBarProgress();
            }
        });

        timeSite.setText(duration2Time(0));
        totalDuration.setText(duration2Time(0));
        discView.setMusicDataList(musicDatas);
    }

    private void stopUpdateSeekBarProgree() {
        musicHandler.removeMessages(MUSIC_MESSAGE);
    }

    private void makeStatusBarTransparent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    Intent intentService;

    private void initMusicDatas() {
        initData();

        intentService = new Intent(this, MusicService.class);
        intentService.putExtra(PARAM_MUSIC_LIST, (Serializable) musicDatas);
        startService(intentService);

    }

    private void initData() {
        musicDatas.add(new MusicData(R.raw.music1, R.raw.ic_music1, "海之女神", "s.e.n.s."));
        musicDatas.add(new MusicData(R.raw.music2, R.raw.ic_music2, "Lover", "Taylor Swift&Shawn Mendes"));
        musicDatas.add(new MusicData(R.raw.music3, R.raw.ic_music3, "一路向北", "周杰伦"));
    }

    private void try2UpdateMusicPicBackground(final int musicPicRes) {
        if (rootLayout.isNeed2UpdateBackground(musicPicRes)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final Drawable foregroundDrawable = getForegroundDrawable(musicPicRes);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            rootLayout.setForeground(foregroundDrawable);
                            rootLayout.beginAnimation();
                        }
                    });
                }
            }).start();
        }
    }

    private Drawable getForegroundDrawable(int musicPicRes) {
        final float widthHeightSize = (float) (DisplayUtil.getScreenWidth(PlayerActivity.this)
                * 1.0 / DisplayUtil.getScreenHeight(this) * 1.0);

        Bitmap bitmap = getForegroundBitmap(musicPicRes);
        int cropBitmapWidth = (int) (widthHeightSize * bitmap.getHeight());
        int cropBitmapWidthX = (int) ((bitmap.getWidth() - cropBitmapWidth) / 2.0);

        Bitmap cropBitmap = Bitmap.createBitmap(bitmap, cropBitmapWidthX, 0, cropBitmapWidth,
                bitmap.getHeight());
        Bitmap scaleBitmap = Bitmap.createScaledBitmap(cropBitmap, bitmap.getWidth() / 50, bitmap
                .getHeight() / 50, false);
        final Bitmap blurBitmap = FastBlurUtil.doBlur(scaleBitmap, 8, true);

        final Drawable foregroundDrawable = new BitmapDrawable(blurBitmap);
        foregroundDrawable.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
        return foregroundDrawable;
    }

    private Bitmap getForegroundBitmap(int musicPicRes) {
        int screenWidth = DisplayUtil.getScreenWidth(this);
        int screenHeight = DisplayUtil.getScreenHeight(this);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeResource(getResources(), musicPicRes, options);
        int imageWidth = options.outWidth;
        int imageHeight = options.outHeight;

        if (imageWidth < screenWidth && imageHeight < screenHeight) {
            return BitmapFactory.decodeResource(getResources(), musicPicRes);
        }

        int sample = 2;
        int sampleX = imageWidth / DisplayUtil.getScreenWidth(this);
        int sampleY = imageHeight / DisplayUtil.getScreenHeight(this);

        if (sampleX > sampleY && sampleY > 1) {
            sample = sampleX;
        } else if (sampleY > sampleX && sampleX > 1) {
            sample = sampleY;
        }

        options.inJustDecodeBounds = false;
        options.inSampleSize = sample;
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        return BitmapFactory.decodeResource(getResources(), musicPicRes, options);
    }

    @Override
    public void onMusicInfoChanged(String musicName, String musicAuthor) {
        getSupportActionBar().setTitle(musicName);
        getSupportActionBar().setSubtitle(musicAuthor);
    }

    @Override
    public void onMusicPicChanged(int musicPicRes) {
        try2UpdateMusicPicBackground(musicPicRes);
    }

    @Override
    public void onMusicChanged(DiscView.MusicChangedStatus musicChangedStatus) {
        switch (musicChangedStatus) {
            case PLAY: {
                play();
                break;
            }
            case PAUSE: {
                pause();
                break;
            }
            case NEXT: {
                next();
                break;
            }
            case LAST: {
                last();
                break;
            }
            case STOP: {
                stop();
                break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v == statusChange) {
            discView.playOrPause();
        } else if (v == next) {
            discView.next();
        } else if (v == former) {
            discView.last();
        } else if (v == isCycle) {
            setCycle();
        }
    }

    private void setCycle() {
        ListActivity.isCycle = !ListActivity.isCycle;
        if (ListActivity.isCycle) {
            isCycle.setImageResource(R.drawable.ic_type2);
            Toast.makeText(this, "循环播放", Toast.LENGTH_LONG).show();
        } else {
            isCycle.setImageResource(R.drawable.ic_type1);
            Toast.makeText(this, "顺序播放", Toast.LENGTH_LONG).show();
        }
    }


    private void play() {
        optMusic(MusicService.ACTION_OPT_MUSIC_PLAY);
        startUpdateSeekBarProgress();
    }

    private void pause() {
        optMusic(MusicService.ACTION_OPT_MUSIC_PAUSE);
        stopUpdateSeekBarProgree();
    }

    private void stop() {
        stopUpdateSeekBarProgree();
        statusChange.setImageResource(R.drawable.ic_play);
        timeSite.setText(duration2Time(0));
        totalDuration.setText(duration2Time(0));
        seekBar.setProgress(0);
    }

    private void next() {
        rootLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                optMusic(MusicService.ACTION_OPT_MUSIC_NEXT);
            }
        }, DURATION_NEEDLE_ANIAMTOR);
        stopUpdateSeekBarProgree();
        timeSite.setText(duration2Time(0));
        totalDuration.setText(duration2Time(0));
    }


    private void last() {
        rootLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                optMusic(MusicService.ACTION_OPT_MUSIC_LAST);
            }
        }, DURATION_NEEDLE_ANIAMTOR);
        stopUpdateSeekBarProgree();
        timeSite.setText(duration2Time(0));
        totalDuration.setText(duration2Time(0));
    }

    private void complete(boolean isOver) {
        if (isOver && !ListActivity.isCycle) {
            discView.stop();
        } else {
            discView.next();
        }
    }

    private void optMusic(final String action) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(action));
    }

    private void seekTo(int position) {
        Intent intent = new Intent(MusicService.ACTION_OPT_MUSIC_SEEK_TO);
        intent.putExtra(MusicService.PARAM_MUSIC_SEEK_TO, position);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void startUpdateSeekBarProgress() {
        stopUpdateSeekBarProgree();
        musicHandler.sendEmptyMessageDelayed(0, 1000);
    }

    private String duration2Time(int duration) {
        int min = duration / 1000 / 60;
        int sec = duration / 1000 % 60;

        return (min < 10 ? "0" + min : min + "") + ":" + (sec < 10 ? "0" + sec : sec + "");
    }

    private void updateMusicDurationInfo(int totalDuration) {
        seekBar.setProgress(0);
        seekBar.setMax(totalDuration);
        this.totalDuration.setText(duration2Time(totalDuration));
        timeSite.setText(duration2Time(0));
        startUpdateSeekBarProgress();
    }

    class MusicReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(MusicService.ACTION_STATUS_MUSIC_PLAY)) {
                statusChange.setImageResource(R.drawable.ic_pause);
                int currentPosition = intent.getIntExtra(MusicService.PARAM_MUSIC_CURRENT_POSITION, 0);
                seekBar.setProgress(currentPosition);
                if (!discView.isPlaying()) {
                    discView.playOrPause();
                }
            } else if (action.equals(MusicService.ACTION_STATUS_MUSIC_PAUSE)) {
                statusChange.setImageResource(R.drawable.ic_play);
                if (discView.isPlaying()) {
                    discView.playOrPause();
                }
            } else if (action.equals(MusicService.ACTION_STATUS_MUSIC_DURATION)) {
                int duration = intent.getIntExtra(MusicService.PARAM_MUSIC_DURATION, 0);
                updateMusicDurationInfo(duration);
            } else if (action.equals(MusicService.ACTION_STATUS_MUSIC_COMPLETE)) {
                boolean isOver = intent.getBooleanExtra(MusicService.PARAM_MUSIC_IS_OVER, true);
                complete(isOver);
            }
        }
    }

    @Override
    protected void onDestroy() {
        try {
            stopService(intentService);
            stop();
            LocalBroadcastManager.getInstance(this).unregisterReceiver(musicReceiver);
        } catch (Exception e) {

        }
        super.onDestroy();
    }
}
