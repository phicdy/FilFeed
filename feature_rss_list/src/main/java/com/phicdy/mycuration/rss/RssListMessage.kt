package com.phicdy.mycuration.rss

data class RssListMessage(
    val id: Long,
    val type: Type
) {
    enum class Type{
        SUCCEED_TO_EDIT_RSS,
        SUCCEED_TO_DELETE_RSS,
        ERROR_EMPTY_RSS_TITLE_EDIT,
        ERROR_SAVE_RSS_TITLE,
        ERROR_DELETE_RSS,
    }
}
