package com.myproject.room;

import static java.nio.file.attribute.AclEntryPermission.DELETE;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;
import androidx.room.Update;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Dao
@TypeConverters(DateConverter.class)
public interface MessageDao {

    @Insert
    void insert(Message message);

    @Update
    void update(Message message);

    @Delete
    void delete(Message message);

    @Query("DELETE FROM message_table")
    void deleteAllMessages();

    @Query("SELECT * FROM message_table")
        // '*' means all columns
    LiveData<List<Message>> getAllMessages();

    @Query("SELECT * FROM message_table WHERE `key` = :mKey")
    LiveData<List<Message>> getMessagesFromKey(String mKey);

    @Query("SELECT * FROM message_table WHERE `date` / (1000 * 60 * 60 * 24) = (:mDate / (1000 * 60 * 60 *24)) AND `topic` =:mTopic") //formula strips off time and leave just the date
    LiveData<List<Message>> getMessagesFromDate(Long mDate, String mTopic);

    // @Query("SELECT * FROM message_table WHERE `date` = :mDate")
    // LiveData<List<Message>> getMessagesFromDate(Date mDate);


}
