package com.lumi.ai.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "chats")
public class ChatEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String title;
    public long timestamp;

    public ChatEntity(String title, long timestamp) {
        this.title = title;
        this.timestamp = timestamp;
    }
}

