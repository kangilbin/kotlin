package com.jaocb.todolist.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = arrayOf(TodoEntity::class), version = 1)   // 1) 조건 1
abstract class AppDatabase : RoomDatabase() {                   // 2) 조건 2
    abstract fun getTodoDAO() : ToDoDao                         // 3) 조건 3

    /**
     * 	DB를 만드는 작업은 많은 리소스를 잡아 먹기 때문에 한번만 실행하기위해
     *  싱글톤 패턴으로 만든다
     */
    companion object {
        val databaseName = "db_todo"                            // 데이터베이스 이름
        var appDatabase : AppDatabase? = null
        //context는 현재 사용하고 있는 DB접근 제공
        fun getInstance(context : Context) : AppDatabase? {
            if(appDatabase == null) {
                //동기화 한시점에 오직 한개의 스레드만 접근하도록
                synchronized(AppDatabase::class){
                    appDatabase = Room.databaseBuilder(context,
                        AppDatabase::class.java,databaseName)
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return appDatabase
        }
    }
}