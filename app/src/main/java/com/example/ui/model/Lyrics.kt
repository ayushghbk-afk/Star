package com.example.ui.model

data class LyricLine(
    val timestampMs: Long,
    val text: String
)

object LyricsParser {
    fun parseLyrics(lyricsJsonOrRaw: String): List<LyricLine> {
        if (lyricsJsonOrRaw.isBlank()) return emptyList()
        val lines = lyricsJsonOrRaw.lines()
        val result = mutableListOf<LyricLine>()

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue
            val parts = trimmed.split("|", limit = 2)
            if (parts.size == 2) {
                val time = parts[0].toLongOrNull() ?: 0L
                val text = parts[1]
                result.add(LyricLine(time, text))
            } else if (trimmed.startsWith("[")) {
                // LRC format fallback [mm:ss.xx]Text
                val endBracket = trimmed.indexOf(']')
                if (endBracket > 1) {
                    val timeStr = trimmed.substring(1, endBracket)
                    val text = trimmed.substring(endBracket + 1)
                    val timeMs = parseLrcTime(timeStr)
                    result.add(LyricLine(timeMs, text))
                } else {
                    result.add(LyricLine(0L, trimmed))
                }
            } else {
                result.add(LyricLine(0L, trimmed))
            }
        }
        return result.sortedBy { it.timestampMs }
    }

    private fun parseLrcTime(timeStr: String): Long {
        return try {
            val parts = timeStr.split(":")
            if (parts.size == 2) {
                val min = parts[0].toLongOrNull() ?: 0
                val secParts = parts[1].split(".")
                val sec = secParts[0].toLongOrNull() ?: 0
                val ms = if (secParts.size > 1) (secParts[1].toLongOrNull() ?: 0) * 10 else 0
                (min * 60 + sec) * 1000 + ms
            } else 0L
        } catch (e: Exception) {
            0L
        }
    }
}
