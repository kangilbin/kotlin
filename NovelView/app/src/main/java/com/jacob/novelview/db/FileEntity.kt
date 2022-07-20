package com.jacob.novelview.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FileEntity(
    @PrimaryKey(autoGenerate = true) var id : Int? = null,
    @ColumnInfo(name="name") val name : String,
    @ColumnInfo(name="content") val content : String,
    @ColumnInfo(name="fullPage") val fullPage : Int,
    @ColumnInfo(name="nowPage") val nowPage : Int
)
