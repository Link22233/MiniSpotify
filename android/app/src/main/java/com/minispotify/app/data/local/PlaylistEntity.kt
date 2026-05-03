package com.minispotify.app.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "playlists",
    indices = [
        Index(value = ["remote_id"], unique = true),
        Index(value = ["updated_at"]),
    ],
)
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "remote_id") val remoteId: String,
    val name: String,
    @ColumnInfo(name = "updated_at") val updatedAtMillis: Long = System.currentTimeMillis(),
)
