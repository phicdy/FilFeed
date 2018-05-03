package com.phicdy.mycuration.presentation.view.activity

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.view.MenuItemCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import com.phicdy.mycuration.R
import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.data.rss.Feed
import com.phicdy.mycuration.presentation.view.fragment.ArticlesListFragment
import com.phicdy.mycuration.tracker.GATrackerHelper
import com.phicdy.mycuration.util.PreferenceHelper

class ArticlesListActivity : AppCompatActivity(), ArticlesListFragment.OnArticlesListFragmentListener {

    companion object {
        private const val DEFAULT_CURATION_ID = -1
    }

    private lateinit var searchView: SearchView
    private lateinit var gaTitle: String
    private lateinit var fragment: ArticlesListFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_articles_list)

        fragment = supportFragmentManager.findFragmentById(R.id.fr_article_list) as ArticlesListFragment

        // Set feed id and url from main activity
        val intent = intent
        val feedId = intent.getIntExtra(TopActivity.FEED_ID, Feed.ALL_FEED_ID)
        val curationId = intent.getIntExtra(TopActivity.CURATION_ID, DEFAULT_CURATION_ID)
        intent.putExtra(TopActivity.FEED_ID, feedId)

        val dbAdapter = DatabaseAdapter.getInstance()
        when {
            curationId != DEFAULT_CURATION_ID -> {
                // Curation
                title = dbAdapter.getCurationNameById(curationId)
                gaTitle = getString(R.string.curation)
            }
            feedId == Feed.ALL_FEED_ID -> {
                // All article
                title = getString(R.string.all)
                gaTitle = getString(R.string.all)
            }
            else -> {
                // Select a feed
                val prefMgr = PreferenceHelper
                prefMgr.setSearchFeedId(feedId)
                val selectedFeed = dbAdapter.getFeedById(feedId)
                title = selectedFeed.title
                gaTitle = getString(R.string.ga_not_all_title)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_article, menu)
        // Associate searchable configuration with the SearchView
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchMenuItem = menu.findItem(R.id.search_article)
        searchView = MenuItemCompat.getActionView(searchMenuItem) as SearchView
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(componentName))
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
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.all_read -> {
                GATrackerHelper.sendEvent(getString(R.string.read_all_articles))
                fragment.handleAllRead()
            }
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        GATrackerHelper.sendScreen(gaTitle)
    }
}