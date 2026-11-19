package com.lumi.ai.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ChatDao {
    @Insert
    long insertChat(ChatEntity chat);

    @Update
    void updateChat(ChatEntity chat);

    @Delete
    void deleteChat(ChatEntity chat);

    @Query("SELECT * FROM chats ORDER BY timestamp DESC")
    List<ChatEntity> getAllChats();

    @Query("SELECT * FROM chats WHERE id = :chatId LIMIT 1")
    ChatEntity getChatById(long chatId);

    @Insert
    long insertMessage(MessageEntity message);

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    List<MessageEntity> getMessagesForChat(long chatId);

    @Query("DELETE FROM chats")
    void deleteAllChats();
}

