package com.phicdy.mycuration.presentation.presenter

import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.data.filter.Filter
import com.phicdy.mycuration.presentation.view.FilterListView
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import java.util.ArrayList

class FilterListPresenterTest {

    private val adapter = Mockito.mock(DatabaseAdapter::class.java)
    private lateinit var presenter: FilterListPresenter
    private lateinit var view: FilterListView

    @Before
    fun setup() {
        view = Mockito.mock(FilterListView::class.java)
        presenter = FilterListPresenter(view, adapter)
    }

    @Test
    fun testOnCreate() {
        // For coverage
        presenter.create()
    }

    @Test
    fun `when filter is empty then show empty view`() {
        `when`(adapter.allFilters).thenReturn(arrayListOf())
        presenter.resume()
        verify(view, times(1)).hideFilterList()
        verify(view, times(1)).showEmptyView()
    }

    @Test
    fun `when filter is not empty then show filter list`() {
        val filters = arrayListOf(mock(Filter::class.java))
        `when`(adapter.allFilters).thenReturn(filters)
        presenter.resume()
        verify(view, times(1)).hideEmptyView()
        verify(view, times(1)).showFilterList(filters)
    }

    @Test
    fun testOnPause() {
        // For coverage
        presenter.pause()
    }

    @Test
    fun `when delete invalid position then not delete the filter`() {
        val testFilter1 = Filter(1, "test1", "testKeyword1", "http://test1.com", ArrayList(), -1, Filter.TRUE)
        presenter.onDeleteMenuClicked(-1, testFilter1, 1)
        verify(view, times(0)).remove(-1)
    }

    @Test
    fun `when delete first filter of one then the size is decreased and show empty view`() {
        val testFilter1 = Filter(1, "test1", "testKeyword1", "http://test1.com", ArrayList(), -1, Filter.TRUE)
        presenter.onDeleteMenuClicked(0, testFilter1, 1)
        verify(view, times(1)).remove(0)
        verify(view, times(1)).hideFilterList()
        verify(view, times(1)).showEmptyView()
    }

    @Test
    fun `when delete first filter of two then the size is decreased and not show empty view`() {
        val testFilter1 = Filter(1, "test1", "testKeyword1", "http://test1.com", ArrayList(), -1, Filter.TRUE)
        presenter.onDeleteMenuClicked(0, testFilter1, 2)
        verify(view, times(1)).remove(0)
        verify(view, never()).hideFilterList()
        verify(view, never()).showEmptyView()
    }

    @Test
    fun `when edit invalid ID filter then edit activity does not start`() {
        val invalidFilter = Filter(0, "test1", "testKeyword1", "http://test1.com", ArrayList(), -1, Filter.TRUE)
        presenter.onEditMenuClicked(invalidFilter)
        verify(view, times(0)).startEditActivity(invalidFilter.id)
    }

    @Test
    fun `when edit valid ID filter then edit activity starts`() {
        val validFilter = Filter(1, "test1", "testKeyword1", "http://test1.com", ArrayList(), -1, Filter.TRUE)
        presenter.onEditMenuClicked(validFilter)
        verify(view, times(1)).startEditActivity(validFilter.id)
    }

    @Test
    fun `when check the filter then the filter is enabled`() {
        val validFilter = Filter(1, "test1", "testKeyword1", "http://test1.com", ArrayList(), -1, Filter.TRUE)
        presenter.onFilterCheckClicked(validFilter, true)
        assertTrue(validFilter.isEnabled)
    }

    @Test
    fun `when uncheck the filter then the filter is disabled`() {
        val validFilter = Filter(1, "test1", "testKeyword1", "http://test1.com", ArrayList(), -1, Filter.TRUE)
        presenter.onFilterCheckClicked(validFilter, false)
        assertFalse(validFilter.isEnabled)
    }
}
