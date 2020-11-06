package com.word.music.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.word.music.R;
import com.word.music.adapter.MusicListAdapter;
import com.word.music.model.MusicData;

import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity {
    private List<MusicData> musicDatas = new ArrayList<>();
    public static int sqlIndex = 0;
    public static boolean isCycle = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        initData();
        setToolBar();

        ListView listView = (ListView) findViewById(R.id.listView);
        MusicListAdapter adapter = new MusicListAdapter(musicDatas, ListActivity.this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                sqlIndex = position - 1;
                Intent intent = new Intent(ListActivity.this, PlayerActivity.class);
                startActivity(intent);
            }
        });

    }

    private void initData() {
        musicDatas.add(new MusicData(R.raw.music1, R.raw.ic_music1, "海之女神", "s.e.n.s."));
        musicDatas.add(new MusicData(R.raw.music2, R.raw.ic_music2, "Lover", "Taylor Swift&Shawn Mendes"));
        musicDatas.add(new MusicData(R.raw.music3, R.raw.ic_music3, "一路向北", "周杰伦"));
        }
    private void setToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        this.setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    //Toolbar的事件---返回
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}
