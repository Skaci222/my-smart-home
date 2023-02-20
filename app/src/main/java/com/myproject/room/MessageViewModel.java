package com.myproject.room;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class MessageViewModel extends AndroidViewModel {

    private MessageRepo messageRepo;
    private LiveData<List<Message>> allMessages;

    public MessageViewModel(@NonNull Application application) {
        super(application);
        messageRepo = new MessageRepo(application);
        allMessages = messageRepo.getAllMessages();
    }
    public void insert(Message message){
        messageRepo.insert(message);
    }

    public void update(Message message){
        messageRepo.update(message);
    }

    public void delete(Message message){
        messageRepo.delete(message);
    }

    public void deleteAllMessages(){
        messageRepo.deleteAllMessages();
    }

    public LiveData<List<Message>> getAllMessages(){
        return  allMessages;
    }
}
