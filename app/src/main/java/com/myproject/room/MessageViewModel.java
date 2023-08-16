package com.myproject.room;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MessageViewModel extends AndroidViewModel {

    private MessageRepo messageRepo;
    private LiveData<List<Message>> allMessages;
    private LiveData<List<Message>> messagesFromKey;
    private LiveData<List<Message>> messagesFromDate;
    public static final String TAG = "ViewModelClass";
    private Context context;

    public MessageViewModel(@NonNull Application application) {
        super(application);
        messageRepo = new MessageRepo(application);
        allMessages = messageRepo.getAllMessages();
        context = application.getApplicationContext();
    }

    public void insert(Message message) {
        messageRepo.insert(message);
    }

    public void update(Message message) {
        messageRepo.update(message);
    }

    public void delete(Message message) {
        messageRepo.delete(message);
    }

    public void deleteAllMessages() {
        messageRepo.deleteAllMessages();
    }

    public LiveData<List<Message>> getAllMessages() {
        return allMessages;
    }

    public LiveData<List<Message>> getMessagesFromKey(String mKey){
        messagesFromKey = messageRepo.getMessagesFromKey(mKey);
        return messagesFromKey;
    }
     public LiveData<List<Message>> getMessagesFromDate(Long date, String topic){
        messagesFromDate = messageRepo.getMessagesFromDate(date, topic);
        return messagesFromDate;
     }


}