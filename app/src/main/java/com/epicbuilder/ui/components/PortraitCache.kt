package com.epicbuilder.ui.components

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.util.concurrent.ConcurrentHashMap

/**
 * Nạp ảnh chân dung hero từ assets/portraits/{id}.png|webp (đóng gói lúc build CI).
 * Kết quả (kể cả "không có ảnh") được cache để không decode lại khi cuộn danh sách.
 */
object PortraitCache {

    private val NO_PORTRAIT = Any()
    private val cache = ConcurrentHashMap<String, Any>()

    fun load(context: Context, heroId: String): ImageBitmap? {
        val cached = cache[heroId]
        if (cached != null) {
            return cached as? ImageBitmap
        }
        val loaded = decode(context, heroId)
        cache[heroId] = loaded ?: NO_PORTRAIT
        return loaded
    }

    private fun decode(context: Context, heroId: String): ImageBitmap? {
        val assets = context.applicationContext.assets
        for (name in listOf("portraits/$heroId.png", "portraits/$heroId.webp")) {
            try {
                assets.open(name).use { stream ->
                    val bitmap = BitmapFactory.decodeStream(stream)
                    if (bitmap != null) return bitmap.asImageBitmap()
                }
            } catch (_: Exception) {
                // Không có file hoặc decode lỗi — thử tên tiếp theo / fallback chữ cái
            }
        }
        return null
    }
}
