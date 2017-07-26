package com.example.erkang.overdraw;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {
    private MyAdapter myAdapter;
    private RecyclerView recyclerView;
    private static final int ITEM_COUNT = 20;
    private static final int ITEM_DISTANCE = 40;
    private LinearLayoutManager layoutManager;
    private MyItemDecoration myItemDecoration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        myAdapter = new MyAdapter(MainActivity.this, ITEM_COUNT);
        layoutManager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false);
        myItemDecoration = new MyItemDecoration(ITEM_DISTANCE,this);
        recyclerView.addItemDecoration(myItemDecoration);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(myAdapter);
    }
}

