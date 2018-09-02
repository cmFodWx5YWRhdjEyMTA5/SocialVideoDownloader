package com.v2social.socialdownloader.services;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;

/**
 * JobService to be scheduled by the JobScheduler.
 * start another service
 */
@TargetApi(23)
public class MuiJobService extends JobService {
    private static final String TAG = "SyncService";

    @Override
    public boolean onStartJob(JobParameters params) {
//        Intent service = new Intent(getApplicationContext(), LocalWordService.class);
//        getApplicationContext().startService(service);
//        Util.scheduleJob(getApplicationContext()); // reschedule the job

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {

        return true;
    }

}