package com.minispotify.app.data.repo

import com.minispotify.app.data.local.AppDatabase
import com.minispotify.app.data.local.PlaylistEntity
import com.minispotify.app.data.local.PlaylistTrackOrderEntity
import com.minispotify.app.data.local.TrackEntity
import com.minispotify.app.data.remote.ApiService
import com.minispotify.app.data.remote.dto.LoginRequest
import com.minispotify.app.data.remote.dto.PlaylistDto
import com.minispotify.app.data.remote.dto.SyncBody
import com.minispotify.app.data.remote.dto.TrackDto
import com.minispotify.app.data.session.TokenStore
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import java.time.Instant

class MusicRepository(
    private val db: AppDatabase,
    private val api: ApiService,
) {
    private val dao = db.musicDao()

    fun observePlaylists(): Flow<List<PlaylistEntity>> = dao.observePlaylists()

    fun observeTracksForPlaylist(playlistId: Long): Flow<List<TrackEntity>> =
        dao.observeTracksForPlaylist(playlistId)

    fun observeFavorites(): Flow<List<TrackEntity>> = dao.observeFavorites()

    suspend fun login(
        email: String,
        password: String,
        tokenStore: TokenStore,
    ): Result<Unit> =
        runCatching {
            val res = api.login(LoginRequest(email, password))
            tokenStore.setToken(res.token)
        }

    suspend fun logout(tokenStore: TokenStore) {
        tokenStore.setToken(null)
    }

    suspend fun pullRemoteAndCache(): Result<Unit> =
        runCatching {
            val env = api.playlists()
            db.withTransaction {
                for (p in env.playlists) {
                    val millis =
                        p.updatedAt?.let { runCatching { Instant.parse(it).toEpochMilli() }.getOrNull() }
                            ?: System.currentTimeMillis()
                    val entity =
                        PlaylistEntity(
                            remoteId = p.remoteId,
                            name = p.name,
                            updatedAtMillis = millis,
                        )
                    val pid = upsertPlaylist(entity)
                    val orders =
                        p.tracks.mapIndexed { index, t ->
                            val tid = upsertTrack(t)
                            PlaylistTrackOrderEntity(playlistId = pid, position = index, trackId = tid)
                        }
                    dao.replacePlaylistContent(pid, orders)
                }
            }
        }

    suspend fun toggleFavorite(
        remoteId: String,
        favorite: Boolean,
    ) {
        dao.setFavorite(remoteId, favorite)
    }

    suspend fun pushSync(playlists: List<PlaylistDto>): Result<Unit> =
        runCatching {
            api.syncPlaylists(SyncBody(playlists))
        }

    private suspend fun upsertPlaylist(p: PlaylistEntity): Long {
        val existing = dao.playlistIdByRemote(p.remoteId)
        return if (existing == null) {
            dao.insertPlaylist(p)
        } else {
            dao.updatePlaylist(p.copy(id = existing))
            existing
        }
    }

    private suspend fun upsertTrack(t: TrackDto): Long {
        val row =
            TrackEntity(
                remoteId = t.remoteId,
                title = t.title,
                artist = t.artist,
                durationMs = t.durationMs,
                streamUrl = t.streamUrl,
            )
        val cur = dao.trackByRemote(t.remoteId)
        return if (cur == null) {
            dao.insertTrack(row)
        } else {
            dao.updateTrack(
                row.copy(
                    id = cur.id,
                    localPath = cur.localPath,
                    isFavorite = cur.isFavorite,
                ),
            )
            cur.id
        }
    }
}
