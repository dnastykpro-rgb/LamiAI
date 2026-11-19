package com.lumi.ai.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lumi.ai.R;
import com.lumi.ai.database.ChatEntity;

import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    public interface OnChatActionListener {
        void onChatSelected(ChatEntity chat);
        void onChatDeleted(ChatEntity chat);
    }

    private final List<ChatEntity> chats = new ArrayList<>();
    private final OnChatActionListener listener;

    public HistoryAdapter(OnChatActionListener listener) {
        this.listener = listener;
    }

    public void setChats(List<ChatEntity> newChats) {
        chats.clear();
        chats.addAll(newChats);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_history, parent, false);
        return new HistoryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        ChatEntity chat = chats.get(position);
        holder.txtTitle.setText(chat.title);
        holder.itemView.setOnClickListener(v -> listener.onChatSelected(chat));
        holder.btnDelete.setOnClickListener(v -> listener.onChatDeleted(chat));
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle;
        ImageButton btnDelete;

        HistoryViewHolder(View v) {
            super(v);
            txtTitle = v.findViewById(R.id.txtHistoryTitle);
            btnDelete = v.findViewById(R.id.btnDeleteChat);
        }
    }
}

