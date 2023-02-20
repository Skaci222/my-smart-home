package com.myproject.room;

import static java.nio.file.attribute.AclEntryPermission.DELETE;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface MessageDao {

    @Insert
    void insert(Message message);

    @Update
    void update(Message message);

    @Delete
    void delete(Message message);

    @Query("DELETE FROM message_table")
    void deleteAllMessages();

    @Query("SELECT * FROM message_table") // '*' means all columns
    LiveData<List<Message>> getAllMessages();


}
