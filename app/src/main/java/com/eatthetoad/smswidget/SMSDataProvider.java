package com.eatthetoad.smswidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.telephony.PhoneNumberUtils;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BulletSpan;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import androidx.core.widget.RemoteViewsCompat;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SMSDataProvider implements RemoteViewsService.RemoteViewsFactory {
    public static final String TAG = "SMSDataProvider";
    private final Context mContext;
    private final int appWidgetId;
    private int messageLines = 2;
    private static List<Message> messageList = new ArrayList<>();
    private static String locale;

    public SMSDataProvider(Context context, Intent intent) {
        mContext = context;
        this.appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID);
        locale = context.getResources().getConfiguration().getLocales().get(0).getCountry();
    }
    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
        Log.d(TAG, "onDataSetChanged: START");
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);

        RemoteViews loadingView = new RemoteViews(mContext.getPackageName(), R.layout.sms_widget);
        loadingView.setViewVisibility(R.id.pb_refresh, View.VISIBLE);
        loadingView.setViewVisibility(R.id.iv_refresh, View.INVISIBLE);
        loadingView.setTextViewText(R.id.tv_empty_listview, "Loading");
        appWidgetManager.partiallyUpdateAppWidget(appWidgetId, loadingView);

        int smsMessageLinesPosition = SMSWidgetConfigureActivity.loadPref(mContext, appWidgetId, Constants.KEY_SMS_MESSAGE_LINES, Constants.defaultLinesPosition);
        messageLines = Integer.parseInt(mContext.getResources().getStringArray(R.array.message_lines)[smsMessageLinesPosition]);

        RemoteViews loadingDoneView = new RemoteViews(mContext.getPackageName(), R.layout.sms_widget);
        if (PermissionManager.checkAllPermissions(mContext)) {

//            initTestData();
//            int unreadCount = 1;

            messageList = getSmsMms();
            int unreadCount = getUnreadSmsCount();

            if (messageList.size() == 0) {
                loadingDoneView.setTextViewText(R.id.tv_empty_listview, mContext.getResources().getString(R.string.nothing_found));
            }

            if (unreadCount > 0) {
                loadingDoneView.setTextViewText(R.id.tv_sms_count, Integer.toString(unreadCount));
                loadingDoneView.setViewVisibility(R.id.tv_sms_count, View.VISIBLE);
            } else {
                loadingDoneView.setViewVisibility(R.id.tv_sms_count, View.GONE);
            }

            loadingDoneView.setScrollPosition(R.id.lv_messages,0);
        } else {
            loadingDoneView.setTextViewText(R.id.tv_empty_listview, mContext.getResources().getString(R.string.add_permissions));
            messageList.clear();
        }

        loadingDoneView.setTextViewText(R.id.tv_updated, "Updated " + ZonedDateTime.now().format(DateTimeUtils.dtfTime));
        loadingDoneView.setViewVisibility(R.id.pb_refresh, View.INVISIBLE);
        loadingDoneView.setViewVisibility(R.id.iv_refresh, View.VISIBLE);
        appWidgetManager.partiallyUpdateAppWidget(appWidgetId, loadingDoneView);
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public int getCount() {
        return messageList.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.sms_row);

        try {
            Message message = messageList.get(position);
            BulletSpan bulletSpan = new BulletSpan(10, Color.RED, 10);

            if (message.getRead() == 0) {
                String messageHeader = message.getHeader();
                SpannableStringBuilder mSSBuilder = new SpannableStringBuilder(messageHeader);
                mSSBuilder.setSpan(bulletSpan, 0, messageHeader.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );
                views.setTextViewText(R.id.tv_message_header, mSSBuilder);
            } else {
                views.setTextViewText(R.id.tv_message_header, message.getHeader());
            }

            views.setTextViewText(R.id.tv_message_date, message.getFormattedDate());
            views.setTextViewText(R.id.tv_message_content, message.getContent());
            RemoteViewsCompat.setTextViewMaxLines(views, R.id.tv_message_content, messageLines);

            Intent fillIntent = new Intent();
            fillIntent.setData(Uri.parse(fillIntent.toUri(Intent.URI_INTENT_SCHEME)));
            fillIntent.putExtra(Constants.EXTRA_ITEM_POSITION, position);
            fillIntent.putExtra(Constants.EXTRA_ITEM_NUMBERS, message.getRecipientNumbers());
            fillIntent.putExtra(Constants.EXTRA_ITEM_ID, message.getId());
            views.setOnClickFillInIntent(R.id.layout_message_row, fillIntent);
            return views;
        } catch (Exception e) {
            messageList.clear();
            e.printStackTrace();
            setErrorView("loading messages");
            return views;
        }
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    private void initTestData() {
        messageList.clear();
        Message message = new Message("1", new Date().getTime(),  "you don't have to put on pants today", 0);
        Recipient recipient = new Recipient("Steve", "", "");
        message.addRecipient(recipient);
        messageList.add(message);

        message = new Message("3", (new Date().getTime() - 350000),"I have cake!", 1);
        recipient = new Recipient("Jen & Rob", "Jen's Cell", "");
        message.addRecipient(recipient);
        messageList.add(message);

        message = new Message("4", (new Date().getTime() - 760000),"Hailey Norris (work), Jefferson Matthews (Jeff), Landin Barnett, Mary Burton, Ezequiel Fischer, Jazlene Waller", 1);
        recipient = new Recipient("MMS", "", "");
        message.addRecipient(recipient);
        messageList.add(message);

        message = new Message("5", (new Date().getTime() - 930000),"We miss you, Stacy", 1);
        recipient = new RecipientNoContact("", "", "888-555-1212");
        message.addRecipient(recipient);
        messageList.add(message);
    }

    private List<Message> getSmsMms () {
        final Uri uri = Uri.parse("content://mms-sms/conversations?simple=true");
        final String[] projection = new String[]{"_id", "recipient_ids", "snippet", "date", "type", "message_count", "read"};
        final String selection = "message_count>0 and error<=1";

        int smsMessageCountPosition = SMSWidgetConfigureActivity.loadPref(mContext, appWidgetId, Constants.KEY_SMS_MESSAGE_COUNT, Constants.defaultMessagesPosition);
        String smsMessageCount = mContext.getResources().getStringArray(R.array.message_numbers)[smsMessageCountPosition];
        Log.d(TAG, "getSmsMms: smsMessageCount " + smsMessageCount);
        final String sortOrder = "date DESC LIMIT " + smsMessageCount;

        final List<Message> messages = new ArrayList<>();

        try {
            Cursor smsCursor = mContext.getContentResolver().query(uri, projection, selection, null, sortOrder);

            if (smsCursor.moveToFirst()) {
                do {
                    Message message = new Message(smsCursor.getString(smsCursor.getColumnIndexOrThrow(projection[0])),
                            smsCursor.getLong(smsCursor.getColumnIndexOrThrow(projection[3])),
                            smsCursor.getString(smsCursor.getColumnIndexOrThrow(projection[2])),
                            smsCursor.getInt(smsCursor.getColumnIndexOrThrow(projection[6])));

                    String[] recipientIds = smsCursor.getString(smsCursor.getColumnIndexOrThrow(projection[1])).split(" ");

                    for (String recipientId : recipientIds) {
                        String recipientNumber = getRecipientNumber(Integer.parseInt(recipientId));
                        if (recipientNumber.equals("6245")){ //verizon
                            String[] splitContent = message.getContent().split(" ", 2);
                            Recipient recipient = new Recipient(splitContent[0], "", splitContent[0]);
                            message.setContent(splitContent[1]);
                            message.addRecipient(recipient);
                        } else {
                            Recipient recipient = getRecipientName(recipientNumber);
                            message.addRecipient(recipient);
                        }
                    }
                    messages.add(message);
                } while (smsCursor.moveToNext());
            }
            smsCursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            messageList.clear();
            setErrorView("retrieving messages");
        }
        return messages;
    }

    private String getRecipientNumber(int recipientId){
        Uri uri = Uri.parse("content://mms-sms/canonical-address/" + recipientId);
        String[] projection = {Telephony.CanonicalAddressesColumns.ADDRESS};
        String selectionAdd = Telephony.CanonicalAddressesColumns._ID + "=" + recipientId;
        String number = "0";
        try {
            Cursor recipientCursor = mContext.getContentResolver().query(uri, null, selectionAdd, null, null);

            if (recipientCursor.moveToFirst()) {
                number = recipientCursor.getString(recipientCursor.getColumnIndexOrThrow(projection[0]));
            }
            recipientCursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return number;
    }

    private Recipient getRecipientName(String number) {
        Recipient recipient;
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        String[] projection = new String[]{"display_name","label"};

        try {
            Cursor contactsCursor = mContext.getContentResolver().query(uri, projection, null, null, null);
            if (contactsCursor.moveToFirst()) {
                recipient = new Recipient(contactsCursor.getString(contactsCursor.getColumnIndexOrThrow(projection[0])),
                        contactsCursor.getString(contactsCursor.getColumnIndexOrThrow(projection[1])), number);
            } else {
                recipient = new RecipientNoContact("", "", number);
            }
            contactsCursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            return new RecipientNoContact("", "", number);
        }
        return recipient;
    }

    public int getUnreadSmsCount(){
        int count = 0;
        final Uri mmsSms = Uri.parse("content://mms-sms/complete-conversations");
        final String[] projection = new String[]{"read"};

        try {
            Cursor unreadCursor = mContext.getContentResolver().query(mmsSms, projection, "read = 0", null, null);
            count = unreadCursor.getCount();
            unreadCursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    public void setErrorView(String errorMessage) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
        RemoteViews errorView = new RemoteViews(mContext.getPackageName(), R.layout.sms_widget);
        errorView.setTextViewText(R.id.tv_empty_listview, "ERROR " + errorMessage);
        errorView.setViewVisibility(R.id.tv_empty_listview, View.VISIBLE);
        errorView.setTextViewText(R.id.tv_updated, "Updated " + ZonedDateTime.now().format(DateTimeUtils.dtfTime));
        errorView.setViewVisibility(R.id.pb_refresh, View.INVISIBLE);
        errorView.setViewVisibility(R.id.iv_refresh, View.VISIBLE);
        appWidgetManager.partiallyUpdateAppWidget(appWidgetId, errorView);
    }

    public static String formatNumber(String number) {
        return PhoneNumberUtils.formatNumber(number, locale);
    }
}
