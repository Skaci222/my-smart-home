package com.myproject.room;

import android.content.Context;

import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Device.class}, version = 7)
public abstract class DeviceDatabase extends RoomDatabase {

    public static DeviceDatabase instance;

    public abstract DeviceDao deviceDao();

    public static synchronized DeviceDatabase getInstance(Context context){
        if(instance == null){
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    DeviceDatabase.class, "device_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}

