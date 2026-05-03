package com.minispotify.app.data.cache

import android.util.LruCache
import java.io.File

/**
 * 离线媒体 LRU：按估算字节数约束总占用，淘汰最久未使用的缓存条目。
 * 与 Room 中 [com.minispotify.app.data.local.TrackEntity.localPath] 配合，用于缩短冷启动读盘路径。
 */
class MediaLruCache(
    maxBytes: Int = 512 * 1024 * 1024,
    private val defaultBytesPerEntry: Int = 4 * 1024 * 1024,
) {
    private val lru =
        object : LruCache<String, String>(maxBytes) {
            override fun sizeOf(key: String, localPath: String): Int {
                val f = File(localPath)
                val len = if (f.isFile) f.length().toInt().coerceAtLeast(1) else defaultBytesPerEntry
                return len.coerceAtMost(maxBytes / 4)
            }

            override fun entryRemoved(evicted: Boolean, key: String?, oldValue: String?, newValue: String?) {
                if (evicted && oldValue != null) {
                    runCatching { File(oldValue).delete() }
                }
            }
        }

    fun getCachedPath(remoteId: String): String? = lru.get(remoteId)

    fun put(remoteId: String, localPath: String) {
        lru.put(remoteId, localPath)
    }

    fun remove(remoteId: String) {
        lru.remove(remoteId)
    }

    fun snapshotHitRate(): Float {
        val h = lru.hitCount()
        val m = lru.missCount()
        val d = h + m
        return if (d == 0) 0f else h.toFloat() / d
    }
}
