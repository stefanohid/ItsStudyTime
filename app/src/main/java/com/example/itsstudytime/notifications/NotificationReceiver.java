package com.example.itsstudytime.notifications;

import static android.content.Intent.getIntent;
import static com.example.itsstudytime.DetailedExamList.NOTIF;
import static com.example.itsstudytime.MainActivity.CHANNEL_ID;
import static com.example.itsstudytime.notifications.NotificationService.SERVICE_RUNNING;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.itsstudytime.R;


public class NotificationReceiver extends BroadcastReceiver {

    public NotificationReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent intent1 = new Intent(context, NotificationService.class);
        intent1.putExtra(NOTIF, intent.getBooleanExtra(NOTIF, false));

        ContextCompat.startForegroundService(context, intent1);


    }
}
