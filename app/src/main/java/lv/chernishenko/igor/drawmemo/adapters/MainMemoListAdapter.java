package lv.chernishenko.igor.drawmemo.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.rey.material.widget.CheckBox;

import lv.chernishenko.igor.drawmemo.R;
import lv.chernishenko.igor.drawmemo.model.Memo;
import lv.chernishenko.igor.drawmemo.utils.DrawAndMemoApplication;

/**
 * (C) Copyright 2015 - Present day by Igor Chernishenko.
 * All rights reserved.
 */
public class MainMemoListAdapter extends RecyclerView.Adapter<MainMemoListAdapter.ViewHolder> {

    private static final String TAG = MainMemoListAdapter.class.getCanonicalName();

    private Memo[] memos;

    private Context context;

    private int itemSize;

    private boolean checking = false;

    public MainMemoListAdapter(Context context, int itemSize) {
        this.context = context;
        this.itemSize = itemSize;
    }

    public class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {

        private ImageView image;
        private View alarmIcon;
        private View priorityIcon;
        private CheckBox checkIcon;

        public ViewHolder(View itemView, final int position) {
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
                    Memo memo = memos[position];
                    setMemoAlarmState(memo, alarmIcon);
                }
            });
            priorityIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Memo memo = memos[position];
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
        ViewHolder vh = new ViewHolder(v, i);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Memo memo = memos[position];
        Bitmap bitmap = BitmapFactory.decodeFile(memo.getImgPath());
        holder.image.setImageBitmap(bitmap);
        memo.setAlarmState(Memo.ALARM_ON - memo.getAlarmState());
        setMemoAlarmState(memo, holder.alarmIcon);
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
        int alarmColor;
        if (memo.getAlarmState() == Memo.ALARM_ON) {
            memo.setAlarmState(Memo.ALARM_OFF);
            alarmColor = context.getResources().getColor(android.R.color.black);
        } else {
            memo.setAlarmState(Memo.ALARM_ON);
            alarmColor = context.getResources().getColor(android.R.color.white);
        }
        view.setBackgroundColor(alarmColor);
    }

    private void setMemoPriority(Memo memo, View view) {
        Log.d(TAG, "memo priority before: " + memo.getPriority());
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
        Log.d(TAG, "memo priority after: " + memo.getPriority());
        DrawAndMemoApplication.getAppInstance().getDbHelper().update(memo);
    }
}
