package com.phicdy.mycuration.articlelist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.phicdy.mycuration.entity.Article
import com.phicdy.mycuration.entity.Feed
import com.phicdy.mycuration.glide.GlideApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.security.InvalidParameterException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ArticleListAdapter(
        private val coroutineScope: CoroutineScope,
        private val presenter: ArticleListPresenter
) : ListAdapter<Article, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ArticleListPresenter.VIEW_TYPE_FOOTER -> {
                val footer = LayoutInflater.from(parent.context)
                        .inflate(R.layout.footer_article_list_activity, parent, false)
                FooterViewHolder(footer)
            }
            ArticleListPresenter.VIEW_TYPE_ARTICLE -> {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.articles_list, parent, false)
                ArticleViewHolder(view)
            }
            else -> throw InvalidParameterException("Invalid view type for article list")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ArticleViewHolder) {
            holder.mView.setOnClickListener {
                coroutineScope.launch {
                    presenter.onListItemClicked(holder.getAdapterPosition())
                }
            }
            holder.mView.setOnLongClickListener {
                presenter.onListItemLongClicked(holder.getAdapterPosition())
                true
            }

            val article = getItem(position)

            holder.articleTitle.text = article.title
            holder.articleUrl.text = article.url

            val format = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US)
            val dateString = format.format(Date(article.postedDate))
            holder.articlePostedTime.text = dateString

            // Set RSS Feed unread article count
            holder.articlePoint.text = if (article.point == Article.DEDAULT_HATENA_POINT) {
                holder.itemView.context.getString(R.string.not_get_hatena_point)
            } else {
                article.point
            }

            if (article.feedTitle.isEmpty()) {
                holder.feedTitleView.visibility = View.GONE
                holder.feedIconView.visibility = View.GONE
            } else {
                holder.feedTitleView.text = article.feedTitle

                val iconPath = article.feedIconPath
                if (iconPath.isNotBlank() && iconPath != Feed.DEDAULT_ICON_PATH) {
                    GlideApp.with(holder.feedIconView)
                            .load(article.feedIconPath)
                            .placeholder(R.drawable.ic_rss)
                            .circleCrop()
                            .error(R.drawable.ic_rss)
                            .into(holder.feedIconView)
                } else {
                    holder.feedIconView.setImageResource(R.drawable.ic_rss)
                }
            }

            // Change color if already be read
            val color = if (article.status == Article.TOREAD || article.status == Article.READ) {
                ContextCompat.getColor(holder.itemView.context, R.color.text_read)
            } else {
                ContextCompat.getColor(holder.itemView.context, R.color.text_primary)
            }
            holder.articleTitle.setTextColor(color)
            holder.articlePostedTime.setTextColor(color)
            holder.articlePoint.setTextColor(color)
            holder.feedTitleView.setTextColor(color)
        }
    }

    private class FooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
   
    private class ArticleViewHolder(
            val mView: View
    ) : RecyclerView.ViewHolder(mView) {
        val articleTitle: TextView = mView.findViewById(R.id.articleTitle) as TextView
        val articlePostedTime: TextView = mView.findViewById(R.id.articlePostedTime) as TextView
        val articlePoint: TextView = mView.findViewById(R.id.articlePoint) as TextView
        val articleUrl: TextView = mView.findViewById(R.id.tv_articleUrl) as TextView
        val feedTitleView: TextView = mView.findViewById(R.id.feedTitle) as TextView
        val feedIconView: ImageView = mView.findViewById(R.id.iv_feed_icon) as ImageView
    }
}


private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Article>() {
    override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
        return oldItem == newItem
    }

}


