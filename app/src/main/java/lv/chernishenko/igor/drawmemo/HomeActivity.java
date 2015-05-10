package lv.chernishenko.igor.drawmemo;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import lv.chernishenko.igor.drawmemo.adapters.MainMemoListAdapter;
import lv.chernishenko.igor.drawmemo.utils.DrawAndMemoApplication;

/**
 * (C) Copyright 2015 - Present day by Igor Chernishenko.
 * All rights reserved.
 */
public class HomeActivity extends ActionBarActivity {

    private static final int CREATE_MEMO_REQUEST = 3333;

    private RecyclerView memosListView;
    private RecyclerView.LayoutManager layoutManager;
    private MainMemoListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Point displaySize = new Point();
        getWindowManager().getDefaultDisplay().getSize(displaySize);

        final int itemSize = displaySize.x;

        Toolbar toolbar = (Toolbar) findViewById(R.id.home_activity_toolbar);
        setSupportActionBar(toolbar);

        memosListView = (RecyclerView) findViewById(R.id.memos_list);
        memosListView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        memosListView.setLayoutManager(layoutManager);

        adapter = new MainMemoListAdapter(this,
                itemSize - 2 * getResources().getDimensionPixelSize(R.dimen.default_margin));
        memosListView.setAdapter(adapter);

        adapter.updateDataSet(DrawAndMemoApplication.getAppInstance().getDbHelper().getAllMemos());

        findViewById(R.id.home_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, CreateMemoActivity.class);
                intent.putExtra(CreateMemoActivity.ITEM_SIZE, itemSize);
                startActivityForResult(intent, CREATE_MEMO_REQUEST);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CREATE_MEMO_REQUEST) {
            if (resultCode == RESULT_OK) {
                adapter.updateDataSet(
                        DrawAndMemoApplication.getAppInstance().getDbHelper().getAllMemos());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}