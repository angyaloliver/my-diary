package com.example.mdiary.data

import androidx.room.*

@Dao
interface DiaryItemDAO {

    @Query("SELECT * FROM diary_items")
    fun getAllDiaryItems() : List<DiaryItem>

    @Insert
    fun addDiaryItem(diaryItem: DiaryItem): Long

    @Delete
    fun deleteDiaryItem(diaryItem: DiaryItem)

    @Query("DELETE FROM diary_items")
    fun deleteAll()
}