package com.phicdy.feature_register_filter

interface RegisterFilterView {
    fun filterKeyword(): String
    fun filterUrl(): String
    fun filterTitle(): String
    fun setFilterTitle(title: String)
    fun setFilterTargetRss(rss: String)
    fun setMultipleFilterTargetRss()
    fun resetFilterTargetRss()
    fun setFilterUrl(url: String)
    fun setFilterKeyword(keyword: String)
    fun handleEmptyTitle()
    fun handleEmptyCondition()
    fun handlePercentOnly()
    fun finish()
    fun showSaveSuccessToast()
    fun showSaveErrorToast()
    fun trackEdit()
    fun trackRegister()
    fun handleEmptyFeed()
}
