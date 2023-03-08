package com.phicdy.mycuration.articlelist

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.phicdy.mycuration.data.preference.PreferenceHelper
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.entity.Feed
import com.phicdy.mycuration.feature.util.changeTheme
import com.phicdy.mycuration.feature.util.getThemeColor
import com.phicdy.mycuration.tracker.TrackerHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ArticlesListActivity : AppCompatActivity(), ArticlesListFragment.OnArticlesListFragmentListener {

    companion object {
        private const val TAG_FRAGMENT = "TAG_FRAGMENT"
        private const val RSS_ID = "RSS_ID"

        fun createIntent(context: Context, rssId: Int) =
                Intent(context, ArticlesListActivity::class.java).apply {
                    putExtra(RSS_ID, rssId)
                }
    }

    private lateinit var searchView: SearchView
    private lateinit var fbTitle: String
    private lateinit var fab: FloatingActionButton

    @Inject
    lateinit var rssRepository: RssRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_articles_list)

        // Set feed id and url from main activity
        val intent = intent
        val feedId = intent.getIntExtra(RSS_ID, Feed.ALL_FEED_ID)

        if (savedInstanceState == null) {
            val fragment = ArticlesListFragment.newInstance(feedId)
            supportFragmentManager.beginTransaction()
                    .add(R.id.container, fragment, TAG_FRAGMENT)
                    .commit()
        }

        lifecycleScope.launch {
            when (feedId) {
                Feed.ALL_FEED_ID -> {
                    // All article
                    title = getString(R.string.all)
                    fbTitle = getString(R.string.all)
                }
                else -> {
                    // Select a feed
                    val prefMgr = PreferenceHelper
                    prefMgr.setSearchFeedId(feedId)
                    val selectedFeed = rssRepository.getFeedById(feedId)
                    title = selectedFeed?.title
                    fbTitle = getString(R.string.ga_not_all_title)
                }
            }
            TrackerHelper.sendUiEvent(fbTitle)
            initToolbar()
            fab = findViewById(R.id.fab_article_list)
            fab.setOnClickListener {
                val fragment = supportFragmentManager.findFragmentByTag(TAG_FRAGMENT) as? ArticlesListFragment
                fragment?.onFabButtonClicked()
                TrackerHelper.sendButtonEvent(getString(R.string.scroll_article_list))
            }
        }
    }

    private fun initToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar_article_list)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            // Show back arrow icon
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setDisplayShowHomeEnabled(true)
            actionBar.title = title
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_article, menu)
        // Associate searchable configuration with the SearchView
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchMenuItem = menu.findItem(R.id.search_article)
        searchView = searchMenuItem.actionView as SearchView
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(componentName))
        searchView.queryHint = getString(R.string.search_article)
        searchView.setOnQueryTextFocusChangeListener { _, queryTextFocused ->
            if (!queryTextFocused) {
                searchMenuItem.collapseActionView()
                searchView.setQuery("", false)
            }
        }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query == null) return false
                val intent = Intent(this@ArticlesListActivity, ArticleSearchResultActivity::class.java)
                intent.action = Intent.ACTION_SEARCH
                intent.putExtra(SearchManager.QUERY, query)
                startActivity(intent)
                return false
            }
        })
        val color = getThemeColor(R.attr.colorPrimary)
        val searchAutoComplete = searchView
                .findViewById(androidx.appcompat.R.id.search_src_text) as SearchView.SearchAutoComplete
        searchAutoComplete.setTextColor(color)
        searchAutoComplete.setHintTextColor(color)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.all_read -> {
                TrackerHelper.sendButtonEvent(getString(R.string.read_all_articles))
                val fragment = supportFragmentManager.findFragmentByTag(TAG_FRAGMENT) as? ArticlesListFragment
                fragment?.handleAllRead()
            }
            android.R.id.home -> finish()
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        changeTheme()
    }
}