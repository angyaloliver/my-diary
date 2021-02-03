package com.example.mdiary.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diary_items")
data class DiaryItem(
    @PrimaryKey(autoGenerate = true) var diaryItemId: Long?,
    @ColumnInfo(name = "title") var title: String?,
    @ColumnInfo(name = "description") var description: String?,
    @ColumnInfo(name = "createdate") var createDate: String?,
    @ColumnInfo(name = "createplace") var createPlace: String?,
    @ColumnInfo(name = "longitude") var longitude: Double?,
    @ColumnInfo(name = "latitude") var latitude: Double?,
    @ColumnInfo(name = "isPersonal") var isPersonal: Boolean,
    @ColumnInfo(name = "photoAbsolutePath") var photoAbsolutePath: String?
)