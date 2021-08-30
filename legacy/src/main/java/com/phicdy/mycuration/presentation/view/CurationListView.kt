package com.phicdy.mycuration.presentation.view

import com.phicdy.mycuration.entity.Curation

interface CurationListView {
    fun startEditCurationActivity(editCurationId: Int)
    fun setNoRssTextToEmptyView()
    fun registerContextMenu()
    fun initListBy(curations: List<Curation>)
    fun showRecyclerView()
    fun hideRecyclerView()
    fun showEmptyView()
    fun hideEmptyView()
}
