package com.phicdy.mycuration.presentation.view.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.phicdy.mycuration.R
import com.phicdy.mycuration.presentation.presenter.FeedUrlHookPresenter
import com.phicdy.mycuration.presentation.view.FeedUrlHookView
import com.phicdy.mycuration.tracker.TrackerHelper
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.scope.ext.android.bindScope
import org.koin.android.scope.ext.android.getOrCreateScope
import org.koin.core.parameter.parametersOf
import kotlin.coroutines.CoroutineContext

class FeedUrlHookActivity : AppCompatActivity(), FeedUrlHookView, CoroutineScope {

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private val presenter: FeedUrlHookPresenter by inject { parametersOf(
            this,
            if (intent.action == null) "" else intent.action,
            if (intent.dataString == null) "" else intent.dataString,
            if (intent.extras == null) "" else intent.extras?.getCharSequence(Intent.EXTRA_TEXT, "") ?: ""
            )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed_url_hook)
        bindScope(getOrCreateScope("rss_url_hook"))
        launch { presenter.create() }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    override fun showInvalidUrlErrorToast() {
        showToastOnUiThread(R.string.add_rss_error_invalid_url, Toast.LENGTH_SHORT)
    }

    override fun showGenericErrorToast() {
        showToastOnUiThread(R.string.add_rss_error_generic, Toast.LENGTH_SHORT)
    }

    override fun finishView() {
        finish()
    }

    override fun trackFailedUrl(url: String) {
        TrackerHelper.sendFailedParseUrl(getString(R.string.add_rss_from_intent_error), url)
    }

    override fun showSuccessToast() {
        showToastOnUiThread(R.string.add_rss_success, Toast.LENGTH_SHORT)
    }

    @UiThread
    private fun showToastOnUiThread(@StringRes res: Int, toastLength: Int) {
        runOnUiThread { Toast.makeText(applicationContext, res, toastLength).show() }
    }
}
