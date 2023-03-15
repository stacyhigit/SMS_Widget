package com.eatthetoad.smswidget;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.work.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class SMSJobService extends JobService {
    static final Uri MMS_SMS_URI = Uri.parse("content://mms-sms/");
    static JobInfo JOB_INFO;
    static final int JOB_ID = 9;

    private static boolean isRegistered(Context context) {
        JobScheduler js = context.getSystemService(JobScheduler.class);
        List<JobInfo> jobs = js.getAllPendingJobs();
        if (jobs == null) {
            return false;
        }
        for (int i = 0; i < jobs.size(); i++) {
            if (jobs.get(i).getId() == JOB_ID) {
                return true;
            }
        }
        return false;
    }

    public static void startJobService(Context context) {
        if (!isRegistered(context)) {
            new Configuration.Builder().setJobSchedulerJobIdRange(0, 1000).build();
            JobInfo.Builder builder = new JobInfo.Builder(JOB_ID,
                    new ComponentName(context, SMSJobService.class.getName()));
            builder.addTriggerContentUri(new JobInfo.TriggerContentUri(MMS_SMS_URI, 0));
            builder.setTriggerContentUpdateDelay(TimeUnit.SECONDS.toMillis(1));
            builder.setTriggerContentMaxDelay(TimeUnit.SECONDS.toMillis(10));

            JOB_INFO = builder.build();

            JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            scheduler.schedule(JOB_INFO);
        }
    }

    public static void stopJobService(Context context) {
        JobScheduler js = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        js.cancel(JOB_ID);
        isRegistered(context);
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        if (params.getJobId() == JOB_ID) {
            Intent smsIntent = new Intent(getApplicationContext(), SMSWidget.class);
            smsIntent.setAction(Constants.ACTION_REFRESH);
            getApplicationContext().sendBroadcast(smsIntent);
        }
        this.jobFinished(params, false);

        ((JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE)).cancel(JOB_ID);
        startJobService(getApplicationContext());
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
