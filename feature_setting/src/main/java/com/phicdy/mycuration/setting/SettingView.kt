package com.phicdy.mycuration.setting

interface SettingView {
    fun initView()
    fun initListener()
    fun setUpdateInterval(index: Int, summary: String)
    fun setAutoUpdateInMainUi(isAutoUpdateInMainUi: Boolean)
    fun setTheme(index: Int, theme: String, mode: Int)
    fun setArticleSort(isNewArticleTop: Boolean)
    fun setInternalBrowser(isEnabled: Boolean)
    fun setAllReadBehavior(index: Int, summary: String)
    fun setSwipeDirection(index: Int, summary: String)
    fun setLaunchTab(index: Int, summary: String)
    fun startLicenseActivity()
}
