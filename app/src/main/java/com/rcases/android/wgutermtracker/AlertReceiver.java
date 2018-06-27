package com.rcases.android.wgutermtracker;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.arch.lifecycle.LiveData;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.rcases.android.wgutermtracker.database.AppDatabase;
import com.rcases.android.wgutermtracker.database.Assessment;
import com.rcases.android.wgutermtracker.database.Course;

public class AlertReceiver extends BroadcastReceiver {

    public static final String courseAlarmFile = "courseAlarms";
    public static final String assessmentAlarmFile = "assessmentAlarms";
    public static final String alarmFile = "alarmFile";
    public static final String nextAlarmField = "nextAlarmId";
    public static final String ALERT_CHANNEL = "alert";

    private static final String TAG = AlertReceiver.class.getSimpleName();

    private AppDatabase mDb;

    public static void scheduleCourseStartAlarm(Context context, int id, long time, String title, String text) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        int startId = 1000 + id;
        Intent intentAlarm = new Intent(context, AlertReceiver.class);
        intentAlarm.putExtra("id", id);
        intentAlarm.putExtra("title", title);
        intentAlarm.putExtra("text", text);
        intentAlarm.putExtra("destination", "courseStart");
        intentAlarm.putExtra("nextAlarmId", startId);
        alarmManager.set(AlarmManager.RTC_WAKEUP, time, PendingIntent
                .getBroadcast(context, startId, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
    }

    public static void scheduleCourseEndAlarm(Context context, int id, long time, String title, String text) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        int endId = 2000 + id;
        Intent intentAlarm = new Intent(context, AlertReceiver.class);
        intentAlarm.putExtra("id", id);
        intentAlarm.putExtra("title", title);
        intentAlarm.putExtra("text", text);
        intentAlarm.putExtra("destination", "courseEnd");
        intentAlarm.putExtra("nextAlarmId", endId);
        alarmManager.set(AlarmManager.RTC_WAKEUP, time, PendingIntent
                .getBroadcast(context, endId, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
    }

    public static void scheduleAssessmentAlarm(Context context, int id, long time, String title, String body) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        int assessmentAlarmId = 3000 + id;
        Intent intentAlarm = new Intent(context, AlertReceiver.class);
        intentAlarm.putExtra("id", id);
        intentAlarm.putExtra("title", title);
        intentAlarm.putExtra("text", body);
        intentAlarm.putExtra("destination", "assessment");
        intentAlarm.putExtra("nextAlarmId", assessmentAlarmId);
        alarmManager.set(AlarmManager.RTC_WAKEUP, time, PendingIntent
                .getBroadcast(context, 1, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));


    }

    @Override
    public void onReceive(Context context, Intent intent) {

        mDb = AppDatabase.getsInstance(context);

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Alerts";
            String description = "Alert Channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(ALERT_CHANNEL, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager.createNotificationChannel(channel);
        }

        String destination = intent.getStringExtra("destination");
        if (destination == null || destination.isEmpty()) {
            destination = "";
        }

        int id = intent.getIntExtra("id", 0);
        String alarmTitle = intent.getStringExtra("title");
        String alarmText = intent.getStringExtra("text");
        int nextAlarmId = intent.getIntExtra("nextAlarmId", 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, ALERT_CHANNEL)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(alarmTitle)
                .setContentText(alarmText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        Intent resultIntent;

        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        Boolean assessmentSwitch = sharedPreferences.getBoolean("assessmentAlert" + id, false);
        Boolean courseStartSwitch = sharedPreferences.getBoolean("courseStartAlert" + id, false);
        Boolean courseEndSwitch = sharedPreferences.getBoolean("courseEndAlert" + id, false);
        switch (destination) {
            case "courseStart":
                LiveData<Course> courseStart = mDb.courseDao().loadCourseById(id);
                if (courseStart != null && courseStartSwitch) {
                    resultIntent = new Intent(context, AddCourseActivity.class);
                    resultIntent.putExtra(AddCourseActivity.EXTRA_COURSE_ID, id);
                } else {
                    return;
                }
                break;
            case "courseEnd":
                LiveData<Course> courseEnd = mDb.courseDao().loadCourseById(id);
                if (courseEnd != null && courseEndSwitch) {
                    resultIntent = new Intent(context, AddCourseActivity.class);
                    resultIntent.putExtra(AddCourseActivity.EXTRA_COURSE_ID, id);
                } else {
                    return;
                }
                break;
            case "assessment":
                LiveData<Assessment> assessment = mDb.assessmentDao().loadAssessmentById(id);
                if (assessment != null && assessmentSwitch) {
                    resultIntent = new Intent(context, AddAssessmentActivity.class);
                    resultIntent.putExtra(AddAssessmentActivity.EXTRA_ASSESSMENT_ID, id);
                } else {
                    return;
                }
                break;
            default:
                resultIntent = new Intent(context, MainActivity.class);
                break;
        }

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent).setAutoCancel(true);
        notificationManager.notify(nextAlarmId, builder.build());
    }

}
