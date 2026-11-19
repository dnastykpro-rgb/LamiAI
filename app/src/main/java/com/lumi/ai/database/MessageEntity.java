package com.lumi.ai.database;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(
    tableName = "messages",
    foreignKeys = @ForeignKey(
        entity = ChatEntity.class,
        parentColumns = "id",
        childColumns = "chatId",
        onDelete = CASCADE
    ),
    indices = {@Index("chatId")}
)
public class MessageEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long chatId;
    public String text;
    public boolean isUser;
    public String mediaPath;
    public String mediaType; // "TEXT", "IMAGE", "VIDEO", "THINKING"
    public long timestamp;

    public MessageEntity(long chatId, String text, boolean isUser, String mediaPath, String mediaType, long timestamp) {
        this.chatId = chatId;
        this.text = text;
        this.isUser = isUser;
        this.mediaPath = mediaPath;
        this.mediaType = mediaType;
        this.timestamp = timestamp;
    }
}

