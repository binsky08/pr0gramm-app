package com.pr0gramm.app.feed

import com.pr0gramm.app.Instant

fun feedItemOf(id: Long): FeedItem {
    return FeedItem(
        created = Instant(0),
        thumbnail = "",
        path = "",
        fullsize = "",
        user = "",
        userId = 0,
        id = id,
        promotedId = 0,
        width = 1,
        height = 1,
        up = 0,
        down = 0,
        mark = 0,
        flags = 1,
        audio = false,
        deleted = false,
        repostApi = false,
        variants = listOf(),
        subtitles = listOf(),
        placeholder = false,
    )
}
