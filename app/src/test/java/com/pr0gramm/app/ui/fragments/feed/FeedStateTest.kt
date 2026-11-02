package com.pr0gramm.app.ui.fragments.feed

import com.pr0gramm.app.Instant
import com.pr0gramm.app.feed.Feed
import com.pr0gramm.app.feed.FeedFilter
import com.pr0gramm.app.feed.FeedItem
import com.pr0gramm.app.feed.FeedManager
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FeedStateTest {
    @Test
    fun hiddenItemsAreNotVisible() {
        val state = stateOf(FeedFilter(), showSeen = false, hiddenSeenIds = setOf(2))
        assertEquals(listOf(1L, 3L), state.visibleFeed.map { it.id })
    }

    @Test
    fun allItemsAreVisibleIfSeenItemsAreShown() {
        val state = stateOf(FeedFilter(), showSeen = true, hiddenSeenIds = setOf(2))
        assertFalse(state.hidesSeenItems)
        assertEquals(listOf(1L, 2L, 3L), state.visibleFeed.map { it.id })
    }

    @Test
    fun ownUploadsAreNeverHidden() {
        val state = stateOf(
            FeedFilter().basicWithUser("Me"),
            showSeen = false,
            hiddenSeenIds = setOf(2),
            ownUsername = "me"
        )
        assertTrue(state.isOwnFeed)
        assertEquals(listOf(1L, 2L, 3L), state.visibleFeed.map { it.id })
    }

    @Test
    fun uploadsOfOtherUsersAreHidden() {
        val state = stateOf(
            FeedFilter().basicWithUser("other"),
            showSeen = false,
            hiddenSeenIds = setOf(2),
            ownUsername = "me"
        )
        assertFalse(state.isOwnFeed)
        assertEquals(listOf(1L, 3L), state.visibleFeed.map { it.id })
    }

    @Test
    fun ownCollectionsAreNeverHidden() {
        val filter = FeedFilter().basicWithCollection("me", "favoriten", "Favoriten")
        val state = stateOf(filter, showSeen = false, hiddenSeenIds = setOf(2), ownUsername = "me")
        assertTrue(state.isOwnFeed)
        assertEquals(listOf(1L, 2L, 3L), state.visibleFeed.map { it.id })
    }

    @Test
    fun collectionsOfOtherUsersAreHidden() {
        val filter = FeedFilter().basicWithCollection("other", "favoriten", "Favoriten")
        val state = stateOf(filter, showSeen = false, hiddenSeenIds = setOf(2), ownUsername = "me")
        assertFalse(state.isOwnFeed)
        assertEquals(listOf(1L, 3L), state.visibleFeed.map { it.id })
    }

    @Test
    fun tooFewVisibleItemsIfMostAreHidden() {
        val state = stateOf(FeedFilter(), showSeen = false, hiddenSeenIds = setOf(2))
        assertTrue(state.tooFewVisibleItems)
    }

    @Test
    fun notTooFewVisibleItemsIfSeenItemsAreShown() {
        val state = stateOf(FeedFilter(), showSeen = true, hiddenSeenIds = setOf(2))
        assertFalse(state.tooFewVisibleItems)
    }

    @Test
    fun notTooFewVisibleItemsWhileLoading() {
        val state = stateOf(FeedFilter(), showSeen = false, hiddenSeenIds = setOf(2), loading = FeedManager.LoadingSpace.NEXT)
        assertFalse(state.tooFewVisibleItems)
    }

    @Test
    fun notTooFewVisibleItemsAtTheEndOfTheFeed() {
        val state = stateOf(FeedFilter(), showSeen = false, hiddenSeenIds = setOf(2), isAtEnd = true)
        assertFalse(state.tooFewVisibleItems)
    }

    private fun stateOf(
        filter: FeedFilter,
        showSeen: Boolean,
        hiddenSeenIds: Set<Long>,
        ownUsername: String? = null,
        loading: FeedManager.LoadingSpace? = null,
        isAtEnd: Boolean = false,
    ): FeedViewModel.FeedState {
        return FeedViewModel.FeedState(
            ready = true,
            feed = Feed(filter, items = listOf(itemOf(1), itemOf(2), itemOf(3)), isAtEnd = isAtEnd),
            loading = loading,
            markItemsAsSeen = true,
            showSeen = showSeen,
            hiddenSeenIds = hiddenSeenIds,
            ownUsername = ownUsername,
        )
    }

    private fun itemOf(id: Long): FeedItem {
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
}
