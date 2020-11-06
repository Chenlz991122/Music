package com.word.music.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.IBinder;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.word.music.activity.ListActivity;
import com.word.music.activity.PlayerActivity;
import com.word.music.model.MusicData;

import java.util.ArrayList;
import java.util.List;


public class MusicService extends Service implements MediaPlayer.OnCompletionListener {

    public static final String ACTION_OPT_MUSIC_PLAY = "ACTION_OPT_MUSIC_PLAY";
    public static final String ACTION_OPT_MUSIC_PAUSE = "ACTION_OPT_MUSIC_PAUSE";
    public static final String ACTION_OPT_MUSIC_NEXT = "ACTION_OPT_MUSIC_NEXT";
    public static final String ACTION_OPT_MUSIC_LAST = "ACTION_OPT_MUSIC_LAST";
    public static final String ACTION_OPT_MUSIC_SEEK_TO = "ACTION_OPT_MUSIC_SEEK_TO";

    public static final String ACTION_STATUS_MUSIC_PLAY = "ACTION_STATUS_MUSIC_PLAY";
    public static final String ACTION_STATUS_MUSIC_PAUSE = "ACTION_STATUS_MUSIC_PAUSE";
    public static final String ACTION_STATUS_MUSIC_COMPLETE = "ACTION_STATUS_MUSIC_COMPLETE";
    public static final String ACTION_STATUS_MUSIC_DURATION = "ACTION_STATUS_MUSIC_DURATION";

    public static final String PARAM_MUSIC_DURATION = "PARAM_MUSIC_DURATION";
    public static final String PARAM_MUSIC_SEEK_TO = "PARAM_MUSIC_SEEK_TO";
    public static final String PARAM_MUSIC_CURRENT_POSITION = "PARAM_MUSIC_CURRENT_POSITION";
    public static final String PARAM_MUSIC_IS_OVER = "PARAM_MUSIC_IS_OVER";

    private int currentMusicIndex = 0;
    private boolean isMusicPause = false;
    private List<MusicData> musicDatas = new ArrayList<>();

    private MusicReceiver musicReceiver = new MusicReceiver();
    private MediaPlayer mediaPlayer = new MediaPlayer();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initMusicDatas(intent);
        currentMusicIndex = ListActivity.sqlIndex;
        if (currentMusicIndex < 0) {
            currentMusicIndex = 0;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initBoardCastReceiver();
    }

    private void initMusicDatas(Intent intent) {
        if (intent == null) return;
        List<MusicData> musicDatas = (List<MusicData>) intent.getSerializableExtra(PlayerActivity.PARAM_MUSIC_LIST);
        this.musicDatas.addAll(musicDatas);
    }

    private void initBoardCastReceiver() {
        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(ACTION_OPT_MUSIC_PLAY);
        intentFilter.addAction(ACTION_OPT_MUSIC_PAUSE);
        intentFilter.addAction(ACTION_OPT_MUSIC_NEXT);
        intentFilter.addAction(ACTION_OPT_MUSIC_LAST);
        intentFilter.addAction(ACTION_OPT_MUSIC_SEEK_TO);

        LocalBroadcastManager.getInstance(this).registerReceiver(musicReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
        mediaPlayer = null;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(musicReceiver);
    }

    private void play(int index) {
        index = ListActivity.sqlIndex;
        if (index == -1) {
            index = 0;
        }
        if (index >= musicDatas.size()) return;
        if (currentMusicIndex == index && isMusicPause) {
            mediaPlayer.start();
        } else {
            mediaPlayer.stop();
            mediaPlayer = null;

            mediaPlayer = MediaPlayer.create(getApplicationContext(), musicDatas.get(index)
                    .getMusicRes());
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(this);
            currentMusicIndex = index;
            isMusicPause = false;

            int duration = mediaPlayer.getDuration();
            sendMusicDurationBroadCast(duration);
        }
        sendMusicStatusBroadCast(ACTION_STATUS_MUSIC_PLAY);
    }

    private void pause() {
        mediaPlayer.pause();
        isMusicPause = true;
        sendMusicStatusBroadCast(ACTION_STATUS_MUSIC_PAUSE);
    }

    private void stop() {
        mediaPlayer.stop();
    }

    private void next() {
        if (currentMusicIndex + 1 < musicDatas.size()) {
            play(currentMusicIndex + 1);
        } else if (ListActivity.isCycle) {
            play(0);
        } else {
            stop();
        }
    }

    private void last() {
        if (ListActivity.isCycle && currentMusicIndex == 0) {
            currentMusicIndex = musicDatas.size() - 1;
        }
        if (currentMusicIndex != 0) {
            play(currentMusicIndex - 1);
        }
    }

    private void seekTo(Intent intent) {
        if (mediaPlayer.isPlaying()) {
            int position = intent.getIntExtra(PARAM_MUSIC_SEEK_TO, 0);
            mediaPlayer.seekTo(position);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        sendMusicCompleteBroadCast();
    }

    class MusicReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ACTION_OPT_MUSIC_PLAY)) {
                play(currentMusicIndex);
            } else if (action.equals(ACTION_OPT_MUSIC_PAUSE)) {
                pause();
            } else if (action.equals(ACTION_OPT_MUSIC_LAST)) {
                last();
            } else if (action.equals(ACTION_OPT_MUSIC_NEXT)) {
                next();
            } else if (action.equals(ACTION_OPT_MUSIC_SEEK_TO)) {
                seekTo(intent);
            }
        }
    }

    private void sendMusicCompleteBroadCast() {
        Intent intent = new Intent(ACTION_STATUS_MUSIC_COMPLETE);
        intent.putExtra(PARAM_MUSIC_IS_OVER, (currentMusicIndex == musicDatas.size() - 1));
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendMusicDurationBroadCast(int duration) {
        Intent intent = new Intent(ACTION_STATUS_MUSIC_DURATION);
        intent.putExtra(PARAM_MUSIC_DURATION, duration);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendMusicStatusBroadCast(String action) {
        Intent intent = new Intent(action);
        if (action.equals(ACTION_STATUS_MUSIC_PLAY)) {
            intent.putExtra(PARAM_MUSIC_CURRENT_POSITION, mediaPlayer.getCurrentPosition());
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

}
