package com.udacity.stockhawk.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.sync.QuoteSyncJob;
import com.udacity.stockhawk.ui.MainActivity;
import com.udacity.stockhawk.ui.StockDetailsActivity;

public class StockWidgetProwider extends AppWidgetProvider {

    private static PendingIntent getPendingIntent(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        return PendingIntent.getActivity(context, 0, intent, 0);
    }

    private static PendingIntent getPendingIntentTemplate(Context context) {
        Intent clickIntentTemplate = new Intent(context, StockDetailsActivity.class);
        return TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(clickIntentTemplate)
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @NonNull
    private static Intent getServiceIntent(Context context, int[] appWidgetIds) {
        Intent intent = new Intent(context, StockWidgetRemoteViewsService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        return intent;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
            views.setOnClickPendingIntent(R.id.widget_title, getPendingIntent(context));
            views.setRemoteAdapter(R.id.widget_list, getServiceIntent(context, appWidgetIds));
            views.setPendingIntentTemplate(R.id.widget_list, getPendingIntentTemplate(context));
            views.setEmptyView(R.id.widget_list, R.id.widget_empty);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);
        if (QuoteSyncJob.ACTION_DATA_UPDATED.equals(intent.getAction())) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                    new ComponentName(context, getClass()));
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list);
        }
    }

}
