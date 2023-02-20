package com.myproject.room;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "message_table")
public class Message {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String value;


    public Message(String value){
        this.value = value;
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
}
