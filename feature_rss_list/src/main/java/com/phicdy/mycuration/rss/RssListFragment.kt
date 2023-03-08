package com.phicdy.mycuration.rss

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.phicdy.mycuration.entity.RssListMode
import com.phicdy.mycuration.entity.RssUpdateIntervalCheckDate
import kotlinx.coroutines.launch
import org.koin.android.scope.currentScope
import java.util.Date

class RssListFragment : Fragment() {

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView

    private lateinit var rssFeedListAdapter: RssListAdapter
    private var mListener: OnFeedListFragmentListener? = null

    private val fetchAllRssListActionCreator: FetchAllRssListActionCreator by currentScope.inject()
    private val rssListStateStore: RSSListStateStore by currentScope.inject()

    private val fetchRssStartUpdateStateActionCreator: FetchRssStartUpdateStateActionCreator by currentScope.inject()
    private val rssListStartUpdateStateStore: RssListStartUpdateStateStore by currentScope.inject()

    private val updateAllRssListActionCreator: UpdateAllRssActionCreator by currentScope.inject()
    private val rssListUpdateStateStore: RssListUpdateStateStore by currentScope.inject()

    private val changeRssListModeActionCreator: ChangeRssListModeActionCreator by currentScope.inject()

    private val changeRssTitleActionCreator: ChangeRssTitleActionCreator by currentScope.inject()

    private val deleteRssActionCreator: DeleteRssActionCreator by currentScope.inject()

    private fun init(items: List<RssListItem>) {
        rssFeedListAdapter = RssListAdapter(viewLifecycleOwner.lifecycleScope, changeRssListModeActionCreator, rssListStateStore, mListener)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = rssFeedListAdapter
        rssFeedListAdapter.submitList(items)
    }

    private fun onRefreshCompleted() {
        swipeRefreshLayout.isRefreshing = false
    }

    private fun hideRecyclerView() {
        recyclerView.visibility = View.GONE
    }

    private fun showEmptyView() {
        emptyView.visibility = View.VISIBLE
    }

    private fun setAllListener() {
        swipeRefreshLayout.setOnRefreshListener {
            viewLifecycleOwner.lifecycleScope.launch {
                rssListStateStore.state.value?.let { value ->
                    updateAllRssListActionCreator.run(value.rss, value.mode)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_rss_list, container, false)
        recyclerView = view.findViewById(R.id.rv_rss)
        emptyView = view.findViewById(R.id.emptyView) as TextView
        swipeRefreshLayout = view.findViewById(R.id.srl_container) as SwipeRefreshLayout
        registerForContextMenu(recyclerView)
        setAllListener()
        rssListStateStore.state.observe(viewLifecycleOwner, Observer {
            if (it.item.isEmpty()) {
                hideRecyclerView()
                showEmptyView()
            } else {
                init(it.item)
            }
        })
        rssListStartUpdateStateStore.state.observe(viewLifecycleOwner, Observer {
            if (it.shouldStart)
                viewLifecycleOwner.lifecycleScope.launch {
                    rssListStateStore.state.value?.let { value ->
                        updateAllRssListActionCreator.run(value.rss, value.mode)
                    }
                }
        })
        rssListUpdateStateStore.state.observe(viewLifecycleOwner, Observer {
            when (it) {
                RssListUpdateState.Started -> swipeRefreshLayout.isRefreshing = true
                is RssListUpdateState.Updating -> {
                    rssFeedListAdapter.submitList(it.rss)
                }
                RssListUpdateState.Finished -> {
                    onRefreshCompleted()
                }
                else -> {
                }
            }
        })
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            fetchAllRssListActionCreator.run(RssListMode.UNREAD_ONLY)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            mListener = context as OnFeedListFragmentListener
        } catch (e: ClassCastException) {
            throw ClassCastException(context.toString() + " must implement OnFragmentInteractionListener")
        }

    }

    override fun onResume() {
        super.onResume()
        viewLifecycleOwner.lifecycleScope.launch {
            fetchRssStartUpdateStateActionCreator.run(RssUpdateIntervalCheckDate(Date()))
        }
    }
   
    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    fun updateFeedTitle(rssId: Int, newTitle: String) {
        rssListStateStore.state.value?.let {
            viewLifecycleOwner.lifecycleScope.launch {
                changeRssTitleActionCreator.run(rssId, newTitle, it)
            }
        }
    }

    fun removeRss(rssId: Int) {
        rssListStateStore.state.value?.let {
            viewLifecycleOwner.lifecycleScope.launch {
                deleteRssActionCreator.run(rssId, it)
            }
        }
    }

    interface OnFeedListFragmentListener {
        fun onListClicked(feedId: Int)
        fun onEditRssClicked(rssId: Int, feedTitle: String)
        fun onDeleteRssClicked(rssId: Int, position: Int)
        fun onAllUnreadClicked()
        fun onFavoriteClicked()
    }
}
