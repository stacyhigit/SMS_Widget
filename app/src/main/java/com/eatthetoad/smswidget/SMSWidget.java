package com.eatthetoad.smswidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;

import java.time.ZonedDateTime;

public class SMSWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        Intent refreshIntent = new Intent(context, SMSWidget.class);
        refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        refreshIntent.setAction(Constants.ACTION_REFRESH);
        PendingIntent refreshPendingIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            refreshPendingIntent = PendingIntent.getBroadcast(
                    context, appWidgetId, refreshIntent,  PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            refreshPendingIntent = PendingIntent.getBroadcast(
                    context, appWidgetId, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        Intent createSmsIntent = new Intent(context, SMSWidget.class);
        createSmsIntent.setAction(Constants.ACTION_CREATE_SMS);
        PendingIntent createSmsPendingIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            createSmsPendingIntent = PendingIntent.getBroadcast(context, appWidgetId, createSmsIntent, 0 | PendingIntent.FLAG_IMMUTABLE);
        } else {
            createSmsPendingIntent = PendingIntent.getBroadcast(context, appWidgetId, createSmsIntent, 0);
        }

        Intent serviceIntent = new Intent(context, SMSWidgetService.class);
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));

        Intent clickIntent = new Intent(context, SMSWidget.class);
        clickIntent.setAction(Constants.ACTION_VIEW_SMS);
        PendingIntent clickPendingIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            clickPendingIntent = PendingIntent.getBroadcast(context, appWidgetId, clickIntent, 0 | PendingIntent.FLAG_MUTABLE);
        } else {
            clickPendingIntent = PendingIntent.getBroadcast(context, appWidgetId, clickIntent, 0);
        }

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.sms_widget);
        views.setViewVisibility(R.id.pb_refresh, View.GONE);
        views.setViewVisibility(R.id.iv_refresh, View.VISIBLE);

        if (!PermissionManager.checkAllPermissions(context)) {
            views.setTextViewText(R.id.tv_empty_listview, context.getResources().getString(R.string.add_permissions));
            views.setViewVisibility(R.id.tv_empty_listview, View.VISIBLE);

        } else {
            views.setTextViewText(R.id.tv_updated, "Updated " + ZonedDateTime.now().format(DateTimeUtils.dtfTime));
            views.setEmptyView(R.id.lv_messages, R.id.tv_empty_listview);
            views.setRemoteAdapter(R.id.lv_messages, serviceIntent);
            views.setPendingIntentTemplate(R.id.lv_messages, clickPendingIntent);
        }

        views.setOnClickPendingIntent(R.id.fl_refresh, refreshPendingIntent);
        views.setOnClickPendingIntent(R.id.iv_edit_sms, createSmsPendingIntent);
        appWidgetManager.updateAppWidget(appWidgetId, views);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.lv_messages);
        SMSJobService.startJobService(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Bundle extras = intent.getExtras();
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisAppWidgetComponentName = new ComponentName(context.getPackageName(), getClass().getName());
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidgetComponentName);
        int appWidgetId = 0;
        if (extras != null) {
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        SMSJobService.startJobService(context);

        switch (intent.getAction()) {
            case Constants.ACTION_REFRESH:
                SMSJobService.startJobService(context);
                if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
                    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.lv_messages);
                } else {
                    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.lv_messages);
                }
                break;
            case Constants.ACTION_VIEW_SMS:
                int clickPosition = intent.getIntExtra(Constants.EXTRA_ITEM_POSITION, 0);
                String numbers = intent.getExtras().getString(Constants.EXTRA_ITEM_NUMBERS, "0");

                try {
                    Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                    smsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    smsIntent.setData(Uri.fromParts("sms", numbers, null));
                    context.startActivity(smsIntent);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
                break;
            case Constants.ACTION_CREATE_SMS:
                try {
                    Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
                    smsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    smsIntent.setData(Uri.parse("smsto:"));
                    context.startActivity(smsIntent);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            SMSWidgetConfigureActivity.deletePref(context, appWidgetId,
                    Constants.KEY_SMS_MESSAGE_COUNT);
            SMSWidgetConfigureActivity.deletePref(context, appWidgetId, Constants.KEY_SMS_MESSAGE_LINES);
        }
    }

    @Override
    public void onEnabled(Context context) {
        SMSJobService.startJobService(context);
    }

    @Override
    public void onDisabled(Context context) {
        SMSJobService.stopJobService(context);
    }
}