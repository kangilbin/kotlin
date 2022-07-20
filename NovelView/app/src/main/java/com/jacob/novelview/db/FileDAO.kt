package com.jacob.novelview.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query

@Dao
interface FileDAO {
    @Query("select * from FileEntity where id = :fileId")
    fun getFile(fileId: Int) : FileEntity

    @Insert(onConflict = REPLACE)
    fun insert(file : FileEntity)
}
