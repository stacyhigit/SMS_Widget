package com.eatthetoad.smswidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;

import com.eatthetoad.smswidget.databinding.SmsWidgetConfigureBinding;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

/**
 * The configuration screen for the {@link SMSWidget SMSWidget} AppWidget.
 */
public class SMSWidgetConfigureActivity extends PermissionManager {
    private static final String TAG = "SMSWidgetConfigureActivity";
    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        SmsWidgetConfigureBinding binding = SmsWidgetConfigureBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        mAppWidgetText = binding.appwidgetText;
//        binding.addButton.setOnClickListener(mOnClickListener);

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

        int smsMessageCountPosition = loadPref(this, appWidgetId, com.eatthetoad.smswidget.Constants.KEY_SMS_MESSAGE_COUNT, Constants.defaultMessagesPosition);
        Spinner spinnerSmsMessages = binding.spinnerSmsMessages;
        ArrayAdapter<CharSequence> adapterSmsMessages = ArrayAdapter.createFromResource(this,
                R.array.message_numbers, android.R.layout.simple_spinner_item);
        spinnerSmsMessages.setAdapter(adapterSmsMessages);
        spinnerSmsMessages.setSelection(smsMessageCountPosition);
        spinnerSmsMessages.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                savePref( SMSWidgetConfigureActivity.this,appWidgetId, Constants.KEY_SMS_MESSAGE_COUNT, position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        int smsMessageLinesPosition = loadPref(this, appWidgetId, Constants.KEY_SMS_MESSAGE_LINES, Constants.defaultLinesPosition);
        Spinner spinnerSmsLines = findViewById(R.id.spinner_sms_lines);
        ArrayAdapter<CharSequence> adapterMessageLines = ArrayAdapter.createFromResource(this,
                R.array.message_lines, android.R.layout.simple_spinner_item);
        spinnerSmsLines.setAdapter(adapterMessageLines);
        spinnerSmsLines.setSelection(smsMessageLinesPosition);
        spinnerSmsLines.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                savePref( SMSWidgetConfigureActivity.this,appWidgetId, Constants.KEY_SMS_MESSAGE_LINES, position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {
                Log.d(TAG, "onInitializationComplete");
            }
        });

        AdView mAdView = binding.adView;
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        requestAllPermissions();

        Button btn_add_widget = binding.btnAddWidget;
        btn_add_widget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(SMSWidgetConfigureActivity.this);
                SMSWidget.updateAppWidget(SMSWidgetConfigureActivity.this, appWidgetManager, appWidgetId);

                Intent okIntent = new Intent();
                okIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                setResult(RESULT_OK, okIntent);
                finish();
            }
        });
    }

    static void savePref(Context context, int appWidgetId, String key, int value) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE).edit();
        prefs.putInt(key + appWidgetId, value);
        prefs.apply();
    }

    static int loadPref(Context context, int appWidgetId, String key, int value) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        return prefs.getInt(key + appWidgetId, value);
    }

    static void deletePref(Context context, int appWidgetId, String key) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE).edit();
        prefs.remove(key + appWidgetId);
        prefs.apply();
    }
}