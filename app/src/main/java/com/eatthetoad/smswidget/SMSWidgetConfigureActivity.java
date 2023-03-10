package com.eatthetoad.smswidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class SMSWidgetConfigureActivity extends PermissionManager {
    private static final String PREFS_NAME = "com.eatthetoad.smswidget";
    public static final String KEY_SMS_MESSAGE_COUNT = "smsMessageCount";
    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smswidget_configure);

        Intent configIntent = getIntent();
        Bundle extras = configIntent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_CANCELED, resultValue);

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }
        int smsMessageCountPosition = loadPref(this, appWidgetId, KEY_SMS_MESSAGE_COUNT, 2);
        Spinner smsSpinner = findViewById(R.id.spinner_sms);
        ArrayAdapter<CharSequence> smsAdapter = ArrayAdapter.createFromResource(this,
                R.array.message_numbers, android.R.layout.simple_spinner_item);
        smsSpinner.setAdapter(smsAdapter);
        smsSpinner.setSelection(smsMessageCountPosition);
        smsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextSize(14);
                savePref( SMSWidgetConfigureActivity.this,appWidgetId, KEY_SMS_MESSAGE_COUNT, position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        requestAllPermissions();

        Button btn_add_widget = findViewById(R.id.btn_add_widget);
        btn_add_widget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(SMSWidgetConfigureActivity.this);
                SMSWidget smsWidget = new SMSWidget();
                smsWidget.updateAppWidget(SMSWidgetConfigureActivity.this, appWidgetManager, appWidgetId);

                Intent okIntent = new Intent();
                okIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                setResult(RESULT_OK, okIntent);
                finish();
            }
        });
    }

    static void savePref(Context context, int appWidgetId, String key, int text) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        prefs.putInt(key + appWidgetId, text);
        prefs.apply();
    }

    static int loadPref(Context context, int appWidgetId, String key, int defaultVal) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getInt(key + appWidgetId, defaultVal);
    }

    static void deletePref(Context context, int appWidgetId, String key) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        prefs.remove(key + appWidgetId);
        prefs.apply();
    }
}