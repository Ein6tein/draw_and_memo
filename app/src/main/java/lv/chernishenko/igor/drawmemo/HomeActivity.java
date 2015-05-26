package lv.chernishenko.igor.drawmemo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.rey.material.widget.CheckBox;

import java.util.Set;

import de.greenrobot.event.EventBus;
import lv.chernishenko.igor.drawmemo.model.AlarmMessage;
import lv.chernishenko.igor.drawmemo.model.Memo;
import lv.chernishenko.igor.drawmemo.utils.MemoApp;
import lv.chernishenko.igor.drawmemo.utils.Utils;

/**
 * (C) Copyright 2015 - Present day by Igor Chernishenko.
 * All rights reserved.
 */
public class HomeActivity extends ActionBarActivity {

    private static final String TAG = HomeActivity.class.getCanonicalName();

    private static final int CREATE_MEMO_REQUEST = 3333;
    private static final int CREATE_ALARM_REQUEST = 6666;

    private RecyclerView memosListView;
    private RecyclerView.LayoutManager layoutManager;
    private MemoListAdapter adapter;

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

        adapter = new MemoListAdapter(this,
                itemSize - 2 * getResources().getDimensionPixelSize(R.dimen.default_margin));
        memosListView.setAdapter(adapter);

        adapter.updateDataSet(MemoApp.getAppInstance().getDbHelper().getAllMemos());

        findViewById(R.id.home_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, CreateMemoActivity.class);
                intent.putExtra(CreateMemoActivity.ITEM_SIZE, itemSize);
                startActivityForResult(intent, CREATE_MEMO_REQUEST);
            }
        });

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CREATE_MEMO_REQUEST) {
            if (resultCode == RESULT_OK) {
                adapter.updateDataSet(
                        MemoApp.getAppInstance().getDbHelper().getAllMemos());
                Toast.makeText(this, R.string.memo_created, Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == CREATE_ALARM_REQUEST) {
            if (resultCode == RESULT_OK) {
                int memoId = data.getIntExtra(SetupAlarmActivity.MEMO_ID, -1);
                int alarmId = data.getIntExtra(SetupAlarmActivity.ALARM_ID, -1);
                if (memoId != -1 && alarmId != -1) {
                    Memo memo = MemoApp.getAppInstance().getDbHelper().getMemoById(memoId);
                    if (memo != null) {
                        memo.setAlarmState(Memo.ALARM_ON);
                        memo.setAlarmId(alarmId);
                        MemoApp.getAppInstance().getDbHelper().updateMemo(memo);
                        adapter.updateMemo(memo);
                        Toast.makeText(this, R.string.alarm_created, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    private void displayAlarmSetupActivity(Memo memo) {
        Intent intent = new Intent(this, SetupAlarmActivity.class);
        intent.putExtra(SetupAlarmActivity.MEMO_ID, memo.getId());
        startActivityForResult(intent, CREATE_ALARM_REQUEST);
    }

    public void onEvent(AlarmMessage message) {
        if (message != null) {
            adapter.updateDataSet(MemoApp.getAppInstance().getDbHelper().getAllMemos());
        }
    }

    /**
     * Main memos list adapter.
     */
    private class MemoListAdapter extends RecyclerView.Adapter<MemoListAdapter.ViewHolder> {

        private Memo[] memos;

        private Context context;

        private int itemSize;

        private boolean checking = false;

        public MemoListAdapter(Context context, int itemSize) {
            this.context = context;
            this.itemSize = itemSize;
        }

        public class ViewHolder extends RecyclerView.ViewHolder
                implements View.OnClickListener, View.OnLongClickListener {

            private ImageView image;
            private View alarmIcon;
            private View priorityIcon;
            private CheckBox checkIcon;
            private int position = -1;

            public ViewHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);
                itemView.setOnLongClickListener(this);
                ViewGroup.LayoutParams lp = itemView.getLayoutParams();
                lp.width = itemSize;
                lp.height = itemSize;
                itemView.setLayoutParams(lp);
                image = (ImageView) itemView.findViewById(R.id.image);
                alarmIcon = itemView.findViewById(R.id.alarm_icon);
                priorityIcon = itemView.findViewById(R.id.priority_icon);
                checkIcon = (CheckBox) itemView.findViewById(R.id.check_icon);
                alarmIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Memo memo = memos[ViewHolder.this.position];
                        setMemoAlarmState(memo, alarmIcon);
                    }
                });
                priorityIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Memo memo = memos[ViewHolder.this.position];
                        setMemoPriority(memo, priorityIcon);
                    }
                });
            }

            @Override
            public void onClick(View v) {
                if (checking) {
                    checkIcon.setChecked(!checkIcon.isChecked());
                } else {
                    // TODO: open view
                }
            }

            @Override
            public boolean onLongClick(View v) {
                if (!checking) {
                    checking = true;
                    checkIcon.setChecked(true);
                    notifyDataSetChanged();
                } else {
                    checking = false;
                    checkIcon.setCheckedImmediately(false);
                    notifyDataSetChanged();
                }
                return true;
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(context).inflate(R.layout.home_list_item, viewGroup, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Memo memo = memos[position];
            if (holder.position == -1) {
                holder.position = position;
            }
            Bitmap bitmap = BitmapFactory.decodeFile(memo.getImgPath());
            holder.image.setImageBitmap(bitmap);

            int alarmColor;
            if (memo.getAlarmState() == Memo.ALARM_ON) {
                alarmColor = context.getResources().getColor(android.R.color.white);
            } else {
                alarmColor = context.getResources().getColor(android.R.color.black);
            }
            holder.alarmIcon.setBackgroundColor(alarmColor);

            memo.setPriority(memo.getPriority() - 1);
            if (memo.getPriority() == -1) {
                memo.setPriority(Memo.PRIORITY_HIGH);
            }
            setMemoPriority(memo, holder.priorityIcon);

            if (checking) {
                holder.checkIcon.setVisibility(View.VISIBLE);
            } else {
                holder.checkIcon.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return memos.length;
        }

        public void updateDataSet(Memo[] memos) {
            this.memos = memos;
            notifyDataSetChanged();
        }

        private void setMemoAlarmState(Memo memo, View view) {
            if (memo.getAlarmState() == Memo.ALARM_ON) {
                memo.setAlarmState(Memo.ALARM_OFF);
                int alarmColor = context.getResources().getColor(android.R.color.black);
                view.setBackgroundColor(alarmColor);
                memo.setAlarmId(-1);
                Utils.getInstance().removeAlarm(memo.getId());
            } else {
                displayAlarmSetupActivity(memo);
            }
        }

        private void setMemoPriority(Memo memo, View view) {
            int priorityColor;
            if (memo.getPriority() == Memo.PRIORITY_HIGH) {
                memo.setPriority(Memo.PRIORITY_LOW);
                priorityColor = context.getResources().getColor(R.color.priority_low);
            } else if (memo.getPriority() == Memo.PRIORITY_LOW) {
                memo.setPriority(Memo.PRIORITY_MEDIUM);
                priorityColor = context.getResources().getColor(R.color.priority_medium);
            } else if (memo.getPriority() == Memo.PRIORITY_MEDIUM) {
                memo.setPriority(Memo.PRIORITY_HIGH);
                priorityColor = context.getResources().getColor(R.color.priority_high);
            } else {
                priorityColor = context.getResources().getColor(android.R.color.white);
            }
            view.setBackgroundColor(priorityColor);
            MemoApp.getAppInstance().getDbHelper().updateMemo(memo);
        }

        public void updateMemo(Memo memo) {
            for (int i = 0; i < memos.length; i++) {
                if (memos[i].getId() == memo.getId()) {
                    memos[i] = memo;
                    notifyDataSetChanged();
                }
            }
        }
    }
}