package com.pr0gramm.app.ui.fragments.pager

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pr0gramm.app.feed.Feed
import com.pr0gramm.app.feed.FeedItem
import com.pr0gramm.app.feed.FeedManager
import com.pr0gramm.app.feed.FeedService
import com.pr0gramm.app.services.SeenService
import com.pr0gramm.app.ui.SavedStateAccessor
import com.pr0gramm.app.ui.fragments.feed.MAX_AUTO_LOADS_IN_ROW
import com.pr0gramm.app.ui.fragments.feed.hiddenSeenIds
import com.pr0gramm.app.ui.fragments.feed.withoutItems
import com.pr0gramm.app.util.observeChangeEx
import com.pr0gramm.app.util.trace
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Shows the items of the feed. If [hideSeenItems] is set, it hides seen items
 * the same way as the feed does.
 */
class PostPagerViewModel(
    private val savedState: SavedState,
    feedService: FeedService,
    private val seenService: SeenService,
    private val hideSeenItems: Boolean,
) : ViewModel() {
    private val mutableState = MutableStateFlow(initialState())
    val state: StateFlow<State> = mutableState

    var currentItem: FeedItem by observeChangeEx(savedState.currentItem) { _, newValue ->
        savedState.currentItem = newValue
        val state = mutableState.value
        savedState.fp = state.feed.parcelAroundId(newValue.id, state.hiddenSeenIds)
    }

    // initialize loader based on the input feed
    private val loader = FeedManager(viewModelScope, feedService, mutableState.value.feed)

    private var autoLoadsInRow = 0

    init {
        viewModelScope.launch { observeFeedUpdates() }
    }

    private fun initialState(): State {
        val feed = savedState.fp.feed.withoutPlaceholderItems()
        val itemIds = feed.mapTo(HashSet()) { it.id }
        return State(feed, savedState.hiddenSeenIds.filterTo(HashSet()) { it in itemIds })
    }

    private suspend fun observeFeedUpdates() {
        var lastLoadingSpace = FeedManager.LoadingSpace.NEXT

        loader.updates.collect { update ->
            trace { "observeFeedUpdates($update)" }

            if (update is FeedManager.Update.LoadingStarted) {
                lastLoadingSpace = update.where
            }

            if (update is FeedManager.Update.NewFeed) {
                val previousState = mutableState.value

                mutableState.value = previousState.copy(
                    feed = update.feed,
                    hiddenSeenIds = hiddenSeenIdsOf(previousState, update.feed),
                )

                savedState.hiddenSeenIds = mutableState.value.hiddenSeenIds.toLongArray()

                // all new items are hidden, so the user could not page any further.
                val nothingNewVisible = mutableState.value.visibleFeed.size == previousState.visibleFeed.size
                if (update.remote && nothingNewVisible && autoLoadsInRow < MAX_AUTO_LOADS_IN_ROW) {
                    autoLoadsInRow++

                    when (lastLoadingSpace) {
                        FeedManager.LoadingSpace.NEXT -> loader.next()
                        FeedManager.LoadingSpace.PREV -> loader.previous()
                    }
                }
            }
        }
    }

    private fun hiddenSeenIdsOf(previousState: State, feed: Feed): Set<Long> {
        if (!hideSeenItems) {
            return setOf()
        }

        val itemIds = feed.map { it.id }

        return hiddenSeenIds(
            itemIds = itemIds,
            knownIds = previousState.feed.mapTo(HashSet()) { it.id },
            previousHiddenIds = previousState.hiddenSeenIds,
            seenIds = itemIds.filterTo(HashSet()) { id -> seenService.isSeen(id) },
            keepId = currentItem.id,
        )
    }

    class SavedState(handle: SavedStateHandle, feed: Feed, initialItem: FeedItem, hiddenSeenIds: LongArray) : SavedStateAccessor(handle) {
        var currentItem: FeedItem by savedStateValue("currentItem", initialItem)
        var fp: Feed.FeedParcel by savedStateValue("feed", feed.parcelAroundId(initialItem.id, hiddenSeenIds.toSet()))
        var hiddenSeenIds: LongArray by savedStateValue("hiddenSeenIds", hiddenSeenIds)
    }

    fun triggerLoadNext() {
        autoLoadsInRow = 0
        loader.next()
    }

    fun triggerLoadPrev() {
        autoLoadsInRow = 0
        loader.previous()
    }

    data class State(
            val feed: Feed,
            val hiddenSeenIds: Set<Long> = setOf(),
    ) {
        /**
         * The feed without the hidden seen items. The pager shows this feed.
         */
        val visibleFeed: Feed by lazy(LazyThreadSafetyMode.NONE) { feed.withoutItems(hiddenSeenIds) }
    }
}
