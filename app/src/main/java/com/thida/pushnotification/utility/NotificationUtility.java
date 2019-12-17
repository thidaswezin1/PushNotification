package com.thida.pushnotification.utility;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import com.thida.pushnotification.R;
import com.thida.pushnotification.config.Config;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class NotificationUtility {

    private Context context;

    public NotificationUtility(Context context){
        this.context = context;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showNotification(NotificationCompat.Builder builder, int icon, String title, String message, String timestamp, PendingIntent pendingIntent, Uri alarm){
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.addLine(message);
        Notification notification = builder.setSmallIcon(icon).setTicker(title)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentIntent(pendingIntent)
                .setSound(alarm)
                .setStyle(inboxStyle)
                .setContentText(message)
                .setWhen(getTimeMilliSec(timestamp))
                .build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String id = "1";
            CharSequence name = "name";
            String description = "description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(id, name, importance);
            channel.setDescription(description);
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(Config.NOTIFICATION_ID,notification);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void showNotificationMessage(String title, String message, String timeStamp, Intent intent, String imageUrl){
        if(TextUtils.isEmpty(message)) return;

        int icon = R.drawable.ic_notifications_active_black_24dp;
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        Uri alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                + "://" + context.getPackageName() + "/raw/notification");

        showNotification(mBuilder,icon,title,message,timeStamp,pendingIntent,alarmSound);
        playNotificationSound();

        Log.e("Show Notification ","is Ok");
    }


    private static long getTimeMilliSec(String timeStamp) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = format.parse(timeStamp);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Playing notification sound
    public void playNotificationSound() {
        try {
            Uri alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                    + "://" + context.getPackageName() + "/raw/notification");
            Ringtone r = RingtoneManager.getRingtone(context, alarmSound);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method checks if the app is in background or not
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }

    // Clears notification tray messages
    public static void clearNotifications(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }
}
