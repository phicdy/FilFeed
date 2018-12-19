package com.phicdy.mycuration.presentation.presenter

import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.data.filter.Filter
import com.phicdy.mycuration.data.repository.FilterRepository
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.presentation.view.FilterListView
import kotlinx.coroutines.coroutineScope

class FilterListPresenter(private val view: FilterListView,
                          private val rssRepository: RssRepository,
                          private val filterRepository: FilterRepository,
                          private val dbAdapter: DatabaseAdapter) {

    suspend fun onActivityCreated() = coroutineScope {
        if (rssRepository.getNumOfRss() == 0) {
            view.setRssEmptyMessage()
        }
    }

    suspend fun resume() = coroutineScope {
        filterRepository.getAllFilters().let {
            if (it.isEmpty()) {
                view.hideFilterList()
                view.showEmptyView()
            } else {
                view.hideEmptyView()
                view.showFilterList(it)
            }
        }

    }

    suspend fun onDeleteMenuClicked(position: Int, selectedFilter: Filter, currentSize: Int) = coroutineScope {
        if (position < 0) return@coroutineScope
        filterRepository.deleteFilter(selectedFilter.id)
        view.remove(position)
        view.notifyListChanged()
        if (currentSize == 1) {
            view.hideFilterList()
            view.showEmptyView()
        }
    }

    fun onEditMenuClicked(selectedFilter: Filter) {
        val id = selectedFilter.id
        // Database table ID starts with 1, ID under 1 means invalid
        if (id <= 0) return
        view.startEditActivity(id)
    }

    fun onFilterCheckClicked(clickedFilter: Filter, isChecked: Boolean) {
        clickedFilter.isEnabled = isChecked
        dbAdapter.updateFilterEnabled(clickedFilter.id, isChecked)
    }
}
