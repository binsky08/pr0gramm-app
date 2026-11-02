package com.pr0gramm.app.feed

import kotlin.test.Test
import kotlin.test.assertEquals

class FeedWindowTest {
    private val feed = Feed(items = (0L until 100L).map { feedItemOf(it) })

    @Test
    fun windowHas16ItemsAroundThePivot() {
        assertEquals(34 until 66, feed.windowAround(50, hiddenIds = setOf()))
    }

    @Test
    fun windowIsCutAtTheEdges() {
        assertEquals(0 until 18, feed.windowAround(2, hiddenIds = setOf()))
        assertEquals(82 until 100, feed.windowAround(98, hiddenIds = setOf()))
    }

    @Test
    fun hiddenItemsDoNotCount() {
        // every second item is hidden
        val hiddenIds = (0L until 100L).filterTo(HashSet()) { it % 2 == 1L }
        val window = feed.windowAround(50, hiddenIds)

        assertEquals(18 until 81, window)
        assertEquals(32, window.count { it.toLong() !in hiddenIds })
    }

    @Test
    fun emptyFeedHasEmptyWindow() {
        assertEquals(0 until 0, Feed().windowAround(0, hiddenIds = setOf()))
    }
}
