package com.myproject.room;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

import com.myproject.retrofit.MessageApi;

import java.sql.Time;
import java.util.Date;

@Entity(tableName = "message_table")
@TypeConverters(DateConverter.class)
public class Message {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String value;
    private String topic;
    private String key;
    private Date date;

    public Message(String topic, String key, String value, Date date){
        this.topic = topic;
        this.value = value;
        this.key = key;
        this.date = date;
    }

    public Message(){

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
