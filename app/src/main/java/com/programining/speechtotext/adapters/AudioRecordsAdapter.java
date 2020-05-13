package com.programining.speechtotext.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.programining.speechtotext.R;
import com.programining.speechtotext.model.MyAudioRecord;

import java.util.ArrayList;

public class AudioRecordsAdapter extends RecyclerView.Adapter<AudioRecordsAdapter.MyHolder> {

    private ArrayList<MyAudioRecord> mAudioRecords;
    private AdapterListener mListener;

    public AudioRecordsAdapter() {
        mAudioRecords = new ArrayList<>();
    }

    public void update(ArrayList<MyAudioRecord> items) {
        mAudioRecords = items;
        notifyDataSetChanged();
    }

    public void update(MyAudioRecord item) {
        mAudioRecords.add(item);
        notifyItemChanged(mAudioRecords.indexOf(item), item);
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View listItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_sound_record, parent, false);
        return new MyHolder(listItem);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        final MyAudioRecord a = mAudioRecords.get(position);
        Log.d("log-onBind", "working");
        String currentIndex = (holder.getAdapterPosition() + 1) + "";
        holder.tvCount.setText(currentIndex);
        holder.tvTitle.setText("AudioRecord-" + currentIndex + ".3gp");
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onItemClick(a);
                }
            }
        });

    }

    public void setupAdapterListener(AdapterListener listener) {
        mListener = listener;
    }

    @Override
    public int getItemCount() {
        return mAudioRecords.size();
    }


    public interface AdapterListener {
        void onItemClick(MyAudioRecord audioRecord);
    }

    class MyHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvCount;

        MyHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvCount = itemView.findViewById(R.id.tv_count);
        }
    }

}
