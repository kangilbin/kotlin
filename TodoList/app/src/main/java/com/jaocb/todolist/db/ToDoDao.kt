package com.jaocb.todolist.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao    // 어떤 구성요소인지 알려주려면 꼭 어노테이션을 사용해야함
interface ToDoDao {
    @Query("SELECT * FROM TodoEntity") // 1)
    fun getAll() : List<TodoEntity>

    @Insert                                  // 2)
    fun insertTodo(todo : TodoEntity)

    @Delete                                  // 3)
    fun deleteTodo(todo : TodoEntity)
}