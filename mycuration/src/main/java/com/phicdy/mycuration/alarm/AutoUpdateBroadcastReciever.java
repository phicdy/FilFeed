package com.phicdy.mycuration.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.phicdy.mycuration.db.DatabaseAdapter;
import com.phicdy.mycuration.rss.Article;
import com.phicdy.mycuration.rss.Feed;
import com.phicdy.mycuration.task.GetHatenaBookmark;
import com.phicdy.mycuration.task.NetworkTaskManager;
import com.phicdy.mycuration.util.NetworkUtil;
import com.phicdy.mycuration.util.PreferenceHelper;

import java.util.ArrayList;

public class AutoUpdateBroadcastReciever extends BroadcastReceiver {

	public static final String AUTO_UPDATE_ACTION = "autoUpdateFeed";
	public static final String AUTO_UPDATE_HATENA_ACTION = "autoUpdateHatena";

	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent == null) {
			return;
		}
		DatabaseAdapter dbAdapter = DatabaseAdapter.getInstance(context);
		if(intent.getAction().equals(AUTO_UPDATE_ACTION)) {
			NetworkTaskManager updateTask = NetworkTaskManager.getInstance(context);
	
			updateTask.updateAllFeeds(dbAdapter.getAllFeedsWithoutNumOfUnreadArticles());
			AlarmManagerTaskManager manager = new AlarmManagerTaskManager(context);
			manager.setNewHatenaUpdateAlarmAfterFeedUpdate(context);

			// Save new time
			PreferenceHelper helper = PreferenceHelper.INSTANCE;
			int intervalSec = helper.getAutoUpdateIntervalSecond();
			manager.setNewAlarm(intervalSec);
		}else if(intent.getAction().equals(AUTO_UPDATE_HATENA_ACTION)) {
			// Update Hatena point
			ArrayList<Feed> feeds = dbAdapter.getAllFeedsWithNumOfUnreadArticles();
			if (feeds == null || feeds.isEmpty()) {
				return;
			}
			// Update has higher priority
			if (NetworkTaskManager.getInstance(context).isUpdatingFeed()) {
				AlarmManagerTaskManager manager = new AlarmManagerTaskManager(context);
				manager.setNewHatenaUpdateAlarmAfterFeedUpdate(context);
				return;
			}
			boolean isWifiConnected = NetworkUtil.INSTANCE.isWifiConnected(context);
			GetHatenaBookmark getHatenaBookmark = new GetHatenaBookmark(dbAdapter);
            int delaySec = 0;
            int totalNum = 0;
			for (int i = 0; i < feeds.size(); i++) {
				ArrayList<Article> unreadArticles = dbAdapter
						.getUnreadArticlesInAFeed(feeds.get(i).getId(), true);
				if (unreadArticles == null || unreadArticles.isEmpty()) {
					continue;
				}
				for (int l = 0; l < unreadArticles.size(); l++) {
					Article unreadArticle = unreadArticles.get(l);
					if (unreadArticle == null) {
						continue;
					}
					if ((!unreadArticle.getPoint().equals(Article.DEDAULT_HATENA_POINT)) && !isWifiConnected) {
						continue;
					}
					totalNum++;
                    getHatenaBookmark.request(unreadArticle.getUrl(), delaySec);
                    if (totalNum % 10 == 0) delaySec += 2;
				}
			}
		}
	}
}
