package com.example.android.codelabs.paging.model

import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

data class CursorItem(
    @field:SerializedName("cursor") val cursor: String,
)