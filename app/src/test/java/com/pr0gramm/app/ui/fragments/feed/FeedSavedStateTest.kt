package com.pr0gramm.app.ui.fragments.feed

import androidx.lifecycle.SavedStateHandle
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertNull

class FeedSavedStateTest {
    @Test
    fun hiddenSeenIdsAreSaved() {
        val handle = SavedStateHandle()
        FeedViewModel.SavedState(handle).hiddenSeenIds = longArrayOf(1, 2)

        // a new accessor reads the values of a restored handle
        assertContentEquals(longArrayOf(1, 2), FeedViewModel.SavedState(handle).hiddenSeenIds)
    }

    @Test
    fun hiddenSeenIdsAreMissingInOldSavedState() {
        assertNull(FeedViewModel.SavedState(SavedStateHandle()).hiddenSeenIds)
    }
}
