package com.jacob.novelview.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = arrayOf(FileEntity::class), version = 1)
abstract class FileDatabase : RoomDatabase(){
    abstract fun getFileoDAO() : FileDAO

    companion object {
        val databaseName = "db_file"
        var fileDatabase : FileDatabase? = null
        fun getInstance(context : Context) : FileDatabase? {
            if(fileDatabase == null) {
                //동기화 한시점에 오직 한개의 스레드만 접근하도록
                synchronized(FileDatabase::class){
                    fileDatabase = Room.databaseBuilder(context,
                        FileDatabase::class.java,databaseName)
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return fileDatabase
        }
    }
}