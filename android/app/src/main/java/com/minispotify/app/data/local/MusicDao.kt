package com.minispotify.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicDao {
    @Query("SELECT * FROM playlists ORDER BY updated_at DESC")
    fun observePlaylists(): Flow<List<PlaylistEntity>>

    @Query(
        """
        SELECT t.* FROM tracks t
        INNER JOIN playlist_track_order o ON o.track_id = t.id
        WHERE o.playlist_id = :playlistId
        ORDER BY o.position ASC
        """,
    )
    fun observeTracksForPlaylist(playlistId: Long): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE is_favorite = 1 ORDER BY title ASC")
    fun observeFavorites(): Flow<List<TrackEntity>>

    @Query("SELECT id FROM playlists WHERE remote_id = :remote LIMIT 1")
    suspend fun playlistIdByRemote(remote: String): Long?

    @Insert
    suspend fun insertPlaylist(p: PlaylistEntity): Long

    @Update
    suspend fun updatePlaylist(p: PlaylistEntity)

    @Query("SELECT * FROM tracks WHERE remote_id = :rid LIMIT 1")
    suspend fun trackByRemote(rid: String): TrackEntity?

    @Insert
    suspend fun insertTrack(t: TrackEntity): Long

    @Update
    suspend fun updateTrack(t: TrackEntity)

    @Query("UPDATE tracks SET is_favorite = :fav WHERE remote_id = :remoteId")
    suspend fun setFavorite(remoteId: String, fav: Boolean)

    @Query("DELETE FROM playlist_track_order WHERE playlist_id = :playlistId")
    suspend fun clearOrdersForPlaylist(playlistId: Long)

    @Insert
    suspend fun insertOrders(rows: List<PlaylistTrackOrderEntity>)

    @Transaction
    suspend fun replacePlaylistContent(
        playlistId: Long,
        orders: List<PlaylistTrackOrderEntity>,
    ) {
        clearOrdersForPlaylist(playlistId)
        if (orders.isNotEmpty()) insertOrders(orders)
    }
}
