package com.eatthetoad.smswidget;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

import androidx.annotation.Nullable;

import java.util.Objects;

public class SMSContentObserver extends ContentObserver {
    private long updateTimeSMS;
    private final Context mContext;
    public SMSContentObserver(Context context, Handler handler) {
        super(handler);
        mContext = context;
    }

    @Override
    public void onChange(boolean selfChange, @Nullable Uri uri) {
        super.onChange(selfChange, uri);
        if (Objects.requireNonNull(uri).toString().equals("content://mms-sms/")) {
            if (updateTimeSMS != java.time.Instant.now().getEpochSecond()) {
                Intent smsIntent = new Intent(mContext, SMSWidget.class);
                smsIntent.setAction(SMSWidget.ACTION_REFRESH);
                mContext.sendBroadcast(smsIntent);
                updateTimeSMS = java.time.Instant.now().getEpochSecond();
            }
        }
    }

    public  void unregisterAndStop(Context context) {
        try {
            context.getContentResolver().unregisterContentObserver(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
