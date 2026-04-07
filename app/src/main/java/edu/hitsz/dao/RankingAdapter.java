package edu.hitsz.dao;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import edu.hitsz.R;

public class RankingAdapter extends BaseAdapter {

    public interface OnDeleteListener {
        void onDelete(GameRecord record);
    }

    private final List<GameRecord> records;
    private final OnDeleteListener deleteListener;
    private final LayoutInflater inflater;

    public RankingAdapter(Context context, List<GameRecord> records, OnDeleteListener deleteListener) {
        this.records = records;
        this.deleteListener = deleteListener;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() { return records.size(); }

    @Override
    public GameRecord getItem(int position) { return records.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.ranking_list_item, null);
            holder = new ViewHolder();
            holder.tvRank    = convertView.findViewById(R.id.tv_rank);
            holder.tvName    = convertView.findViewById(R.id.tv_name);
            holder.tvScore   = convertView.findViewById(R.id.tv_score);
            holder.tvTime    = convertView.findViewById(R.id.tv_time);
            holder.btnDelete = convertView.findViewById(R.id.btn_delete);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        GameRecord record = records.get(position);
        holder.tvRank.setText(String.valueOf(position + 1));
        holder.tvName.setText(record.getPlayerName());
        holder.tvScore.setText(String.valueOf(record.getScore()));
        holder.tvTime.setText(record.getFormattedTimestamp());
        holder.btnDelete.setFocusable(false);

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(record);
            }
        });

        return convertView;
    }

    private static final class ViewHolder {
        TextView tvRank;
        TextView tvName;
        TextView tvScore;
        TextView tvTime;
        Button btnDelete;
    }
}
