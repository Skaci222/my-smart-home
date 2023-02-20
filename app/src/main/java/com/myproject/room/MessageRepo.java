package com.myproject.room;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

public class MessageRepo {

    private MessageDao messageDao;
    private LiveData<List<Message>> allMessages;

    public MessageRepo (Application application){
        MessageDatabase messageDatabase = MessageDatabase.getInstance(application);
        messageDao = messageDatabase.messageDao();
        allMessages = messageDao.getAllMessages();
    }

    public void insert(Message message){
        new InsertMessageAsyncTask(messageDao).execute(message);
    }

    public void update(Message message){
        new UpdateMessageAsyncTask(messageDao).execute(message);
    }

    public void delete(Message message){
        new DeleteMessageAsyncTask(messageDao).execute(message);
    }

    public void deleteAllMessages(){
        new DeleteAllMessagesAsyncTask(messageDao).execute();
    }

    public LiveData<List<Message>> getAllMessages() {
        return allMessages;
    }

    private static class InsertMessageAsyncTask extends AsyncTask<Message, Void, Void>{
        private MessageDao messageDao;

        public InsertMessageAsyncTask(MessageDao messageDao){
            this.messageDao = messageDao;
        }

        @Override
        protected Void doInBackground(Message... messages) {
            messageDao.insert(messages[0]);
            return null;
        }
    }

    private static class UpdateMessageAsyncTask extends AsyncTask<Message, Void, Void>{
        private MessageDao messageDao;

        public UpdateMessageAsyncTask(MessageDao messageDao){
            this.messageDao = messageDao;
        }

        @Override
        protected Void doInBackground(Message... messages) {
            messageDao.update(messages[0]);
            return null;
        }
    }

    private static class DeleteMessageAsyncTask extends AsyncTask<Message, Void, Void>{
        private MessageDao messageDao;

        public DeleteMessageAsyncTask(MessageDao messageDao){
            this.messageDao = messageDao;
        }

        @Override
        protected Void doInBackground(Message... messages) {
            messageDao.delete(messages[0]);
            return null;
        }
    }

    private static class DeleteAllMessagesAsyncTask extends AsyncTask<Void, Void, Void>{
        private MessageDao messageDao;

        public DeleteAllMessagesAsyncTask(MessageDao messageDao){
            this.messageDao = messageDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            messageDao.deleteAllMessages();
            return null;
        }
    }
}
