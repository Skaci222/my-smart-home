package com.myproject.model;

import java.util.Date;

public class MqttMsg {

    private int id;
    private String value;
    private String topic;
    private String hashKey;
    private String date;

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
        return hashKey;
    }

    public void setHashKey(String hashKey) {
        this.hashKey = hashKey;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "MqttMessage{" +
                "id=" + id +
                ", value='" + value + '\'' +
                ", topic='" + topic + '\'' +
                ", key='" + hashKey + '\'' +
                ", date=" + date +
                '}';
    }
}
