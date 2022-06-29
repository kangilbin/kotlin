package com.jaocb.todolist.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity // 어떤 구성요소인지를 알려주려면 꼭 어노테이션을 써주어야 합니다.
data class TodoEntity ( // 1) 중괄호 아닌 소괄호 입니다.
    @PrimaryKey(autoGenerate = true) var id : Int? = null, // 2)
    @ColumnInfo(name="title") val title : String,          // 3)
    @ColumnInfo(name="importance") val importance : Int
)