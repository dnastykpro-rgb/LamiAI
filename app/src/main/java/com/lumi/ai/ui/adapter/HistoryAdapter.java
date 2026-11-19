package com.lumi.ai.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
        Context ctx = parent.getContext();

        LinearLayout row = new LinearLayout(ctx);
        row.setLayoutParams(new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(8, 24, 8, 24);

        TextView title = new TextView(ctx);
        title.setId(R.id.txtHistoryTitle);
        title.setTextColor(Color.WHITE);
        title.setTextSize(15f);
        title.setMaxLines(1);
        title.setEllipsize(TextUtils.TruncateAt.END);
        title.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        ));

        ImageButton del = new ImageButton(ctx);
        del.setId(R.id.btnDeleteChat);
        del.setImageResource(android.R.drawable.ic_menu_delete);
        del.setColorFilter(Color.GRAY);
        del.setBackgroundColor(Color.TRANSPARENT);

        row.addView(title);
        row.addView(del);

        return new HistoryViewHolder(row);
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
