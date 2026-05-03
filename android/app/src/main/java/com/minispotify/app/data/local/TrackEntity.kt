package com.minispotify.app.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tracks",
    indices = [
        Index(value = ["remote_id"], unique = true),
        Index(value = ["is_favorite"]),
    ],
)
data class TrackEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "remote_id") val remoteId: String,
    val title: String,
    val artist: String? = null,
    @ColumnInfo(name = "duration_ms") val durationMs: Long? = null,
    @ColumnInfo(name = "stream_url") val streamUrl: String? = null,
    /** 离线缓存文件路径；有值时可无网起播 */
    @ColumnInfo(name = "local_path") val localPath: String? = null,
    @ColumnInfo(name = "is_favorite") val isFavorite: Boolean = false,
)
