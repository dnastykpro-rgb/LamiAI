package com.lumi.ai.ui.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.lumi.ai.R;
import com.lumi.ai.database.MessageEntity;
import com.lumi.ai.utils.AnimationHelper;
import com.lumi.ai.utils.MediaStorageHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.noties.markwon.Markwon;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_USER = 1;
    private static final int TYPE_LUMI = 2;
    private static final int TYPE_THINKING = 3;

    private final List<MessageEntity> messages = new ArrayList<>();
    private final Markwon markwon;
    private int lastAnimatedPosition = -1;
    private TextView activeThinkingTextView;

    public ChatAdapter(Context context) {
        this.markwon = Markwon.create(context);
    }

    public void setMessages(List<MessageEntity> newMessages) {
        messages.clear();
        messages.addAll(newMessages);
        lastAnimatedPosition = messages.size() - 1;
        notifyDataSetChanged();
    }

    public void addMessage(MessageEntity message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    public void removeThinkingIndicator() {
        for (int i = messages.size() - 1; i >= 0; i--) {
            if ("THINKING".equals(messages.get(i).mediaType)) {
                messages.remove(i);
                notifyItemRemoved(i);
                activeThinkingTextView = null;
                break;
            }
        }
    }

    public void updateThinkingStatus(String newStatus) {
        if (activeThinkingTextView != null) {
            AnimationHelper.crossfadeText(activeThinkingTextView, newStatus);
        }
    }

    @Override
    public int getItemViewType(int position) {
        MessageEntity msg = messages.get(position);
        if ("THINKING".equals(msg.mediaType)) return TYPE_THINKING;
        return msg.isUser ? TYPE_USER : TYPE_LUMI;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context ctx = parent.getContext();
        int crimson = Color.parseColor("#E51A1A");
        int surface = Color.parseColor("#1F1F1F");

        if (viewType == TYPE_USER) {
            LinearLayout row = new LinearLayout(ctx);
            row.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            row.setGravity(Gravity.END);
            row.setPadding(80, 12, 8, 12);

            LinearLayout bubble = new LinearLayout(ctx);
            bubble.setOrientation(LinearLayout.VERTICAL);
            bubble.setBackgroundColor(crimson);
            bubble.setPadding(28, 20, 28, 20);

            ImageView img = new ImageView(ctx);
            img.setId(R.id.imgUserAttached);
            img.setLayoutParams(new LinearLayout.LayoutParams(450, 450));
            img.setScaleType(ImageView.ScaleType.CENTER_CROP);
            img.setVisibility(View.GONE);

            TextView txt = new TextView(ctx);
            txt.setId(R.id.txtUserMessage);
            txt.setTextColor(Color.WHITE);
            txt.setTextSize(15f);

            bubble.addView(img);
            bubble.addView(txt);
            row.addView(bubble);
            return new UserViewHolder(row);
        } else if (viewType == TYPE_THINKING) {
            LinearLayout row = new LinearLayout(ctx);
            row.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            row.setPadding(8, 12, 80, 12);

            LinearLayout bubble = new LinearLayout(ctx);
            bubble.setOrientation(LinearLayout.HORIZONTAL);
            bubble.setGravity(Gravity.CENTER_VERTICAL);
            bubble.setBackgroundColor(surface);
            bubble.setPadding(28, 20, 28, 20);

            View dot = new View(ctx);
            dot.setId(R.id.viewThinkingDot);
            dot.setLayoutParams(new LinearLayout.LayoutParams(24, 24));
            dot.setBackgroundColor(crimson);

            TextView status = new TextView(ctx);
            status.setId(R.id.txtThinkingStatus);
            status.setTextColor(Color.LTGRAY);
            status.setTextSize(14f);
            status.setPadding(20, 0, 0, 0);

            bubble.addView(dot);
            bubble.addView(status);
            row.addView(bubble);
            return new ThinkingViewHolder(row);
        } else {
            LinearLayout row = new LinearLayout(ctx);
            row.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            row.setPadding(8, 12, 48, 12);

            LinearLayout bubble = new LinearLayout(ctx);
            bubble.setOrientation(LinearLayout.VERTICAL);
            bubble.setBackgroundColor(surface);
            bubble.setPadding(32, 24, 32, 24);

            TextView header = new TextView(ctx);
            header.setText("LUMI AI");
            header.setTextColor(crimson);
            header.setTextSize(11f);
            header.setTypeface(Typeface.DEFAULT_BOLD);

            TextView txt = new TextView(ctx);
            txt.setId(R.id.txtLumiMessage);
            txt.setTextColor(Color.WHITE);
            txt.setTextSize(15f);
            txt.setTextIsSelectable(true);
            txt.setPadding(0, 8, 0, 8);

            ImageView img = new ImageView(ctx);
            img.setId(R.id.imgLumiGenerated);
            img.setLayoutParams(new LinearLayout.LayoutParams(550, 550));
            img.setScaleType(ImageView.ScaleType.FIT_CENTER);
            img.setVisibility(View.GONE);

            VideoView vid = new VideoView(ctx);
            vid.setId(R.id.videoLumiGenerated);
            vid.setLayoutParams(new LinearLayout.LayoutParams(550, 550));
            vid.setVisibility(View.GONE);

            LinearLayout actions = new LinearLayout(ctx);
            actions.setOrientation(LinearLayout.HORIZONTAL);
            actions.setPadding(0, 12, 0, 0);

            TextView btnCopy = new TextView(ctx);
            btnCopy.setId(R.id.btnCopyText);
            btnCopy.setText("Копировать   ");
            btnCopy.setTextColor(Color.GRAY);
            btnCopy.setTextSize(12f);

            TextView btnSave = new TextView(ctx);
            btnSave.setId(R.id.btnSaveMedia);
            btnSave.setText("Сохранить в галерею");
            btnSave.setTextColor(crimson);
            btnSave.setTextSize(12f);
            btnSave.setVisibility(View.GONE);

            actions.addView(btnCopy);
            actions.addView(btnSave);

            bubble.addView(header);
            bubble.addView(txt);
            bubble.addView(img);
            bubble.addView(vid);
            bubble.addView(actions);
            row.addView(bubble);
            return new LumiViewHolder(row);
        }
    }
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageEntity msg = messages.get(position);

        if (holder instanceof UserViewHolder) {
            UserViewHolder vh = (UserViewHolder) holder;
            if (msg.text != null && !msg.text.isEmpty()) {
                vh.txtMessage.setVisibility(View.VISIBLE);
                vh.txtMessage.setText(msg.text);
            } else {
                vh.txtMessage.setVisibility(View.GONE);
            }

            if (msg.mediaPath != null && !msg.mediaPath.isEmpty()) {
                vh.imgAttached.setVisibility(View.VISIBLE);
                Glide.with(vh.itemView.getContext()).load(Uri.parse(msg.mediaPath)).into(vh.imgAttached);
            } else {
                vh.imgAttached.setVisibility(View.GONE);
            }
        } else if (holder instanceof ThinkingViewHolder) {
            ThinkingViewHolder vh = (ThinkingViewHolder) holder;
            vh.txtStatus.setText(msg.text);
            activeThinkingTextView = vh.txtStatus;
            AnimationHelper.startPulseAnimation(vh.dotView);
        } else if (holder instanceof LumiViewHolder) {
            LumiViewHolder vh = (LumiViewHolder) holder;
            Context ctx = vh.itemView.getContext();

            if (msg.text != null && !msg.text.isEmpty()) {
                vh.txtMessage.setVisibility(View.VISIBLE);
                markwon.setMarkdown(vh.txtMessage, msg.text);
                vh.btnCopy.setVisibility(View.VISIBLE);
                vh.btnCopy.setOnClickListener(v -> {
                    ClipboardManager cm = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
                    cm.setPrimaryClip(ClipData.newPlainText("LUMI AI", msg.text));
                    Toast.makeText(ctx, "Текст скопирован", Toast.LENGTH_SHORT).show();
                });
            } else {
                vh.txtMessage.setVisibility(View.GONE);
                vh.btnCopy.setVisibility(View.GONE);
            }

            vh.imgGenerated.setVisibility(View.GONE);
            vh.videoGenerated.setVisibility(View.GONE);
            vh.btnSaveMedia.setVisibility(View.GONE);

            if ("IMAGE".equals(msg.mediaType) && msg.mediaPath != null) {
                vh.imgGenerated.setVisibility(View.VISIBLE);
                vh.btnSaveMedia.setVisibility(View.VISIBLE);
                Glide.with(ctx).load(new File(msg.mediaPath)).into(vh.imgGenerated);
                vh.btnSaveMedia.setOnClickListener(v -> {
                    boolean saved = MediaStorageHelper.exportMediaToGallery(ctx, msg.mediaPath, false);
                    Toast.makeText(ctx, saved ? "Фото сохранено в Галерею" : "Ошибка сохранения", Toast.LENGTH_SHORT).show();
                });
            } else if ("VIDEO".equals(msg.mediaType) && msg.mediaPath != null) {
                vh.videoGenerated.setVisibility(View.VISIBLE);
                vh.btnSaveMedia.setVisibility(View.VISIBLE);
                vh.videoGenerated.setVideoPath(msg.mediaPath);
                MediaController mc = new MediaController(ctx);
                mc.setAnchorView(vh.videoGenerated);
                vh.videoGenerated.setMediaController(mc);
                vh.videoGenerated.setOnPreparedListener(mp -> {
                    mp.setLooping(true);
                    vh.videoGenerated.start();
                });
                vh.btnSaveMedia.setOnClickListener(v -> {
                    boolean saved = MediaStorageHelper.exportMediaToGallery(ctx, msg.mediaPath, true);
                    Toast.makeText(ctx, saved ? "Видео сохранено в Галерею" : "Ошибка сохранения", Toast.LENGTH_SHORT).show();
                });
            }
        }

        if (holder.getAdapterPosition() > lastAnimatedPosition) {
            AnimationHelper.animateMessageAppear(holder.itemView, msg.isUser);
            lastAnimatedPosition = holder.getAdapterPosition();
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView txtMessage;
        ImageView imgAttached;

        UserViewHolder(View v) {
            super(v);
            txtMessage = v.findViewById(R.id.txtUserMessage);
            imgAttached = v.findViewById(R.id.imgUserAttached);
        }
    }

    static class LumiViewHolder extends RecyclerView.ViewHolder {
        TextView txtMessage, btnCopy, btnSaveMedia;
        ImageView imgGenerated;
        VideoView videoGenerated;

        LumiViewHolder(View v) {
            super(v);
            txtMessage = v.findViewById(R.id.txtLumiMessage);
            imgGenerated = v.findViewById(R.id.imgLumiGenerated);
            videoGenerated = v.findViewById(R.id.videoLumiGenerated);
            btnCopy = v.findViewById(R.id.btnCopyText);
            btnSaveMedia = v.findViewById(R.id.btnSaveMedia);
        }
    }

    static class ThinkingViewHolder extends RecyclerView.ViewHolder {
        View dotView;
        TextView txtStatus;

        ThinkingViewHolder(View v) {
            super(v);
            dotView = v.findViewById(R.id.viewThinkingDot);
            txtStatus = v.findViewById(R.id.txtThinkingStatus);
        }
    }
}
