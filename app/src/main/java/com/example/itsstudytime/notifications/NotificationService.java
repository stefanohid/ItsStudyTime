package com.example.itsstudytime.notifications;


import static com.example.itsstudytime.DetailedExamList.NOTIF;
import static com.example.itsstudytime.DetailedExamList.POS;
import static com.example.itsstudytime.DetailedExamList.SERIA;
import static com.example.itsstudytime.MainActivity.CHANNEL_ID;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.SyncStateContract;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.res.ResourcesCompat;

import com.example.itsstudytime.DetailedExamList;
import com.example.itsstudytime.MainActivity;
import com.example.itsstudytime.R;

import java.util.Calendar;

public class NotificationService extends Service {
    public static boolean SERVICE_RUNNING;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent.getBooleanExtra(NOTIF, false)) {
            stopForeground(true);
            stopSelfResult(1);
            SERVICE_RUNNING=false;
        } else {

            Intent notifyIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 2, notifyIntent, PendingIntent.FLAG_IMMUTABLE);

            Notification notif = new Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle("Break Time")
                    .setContentText("È tempo di fare una pausa! Puoi riprendere lo studio tra 20 minuti.")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .build();

            startForeground(1, notif);
            SERVICE_RUNNING = true;

        }

        return START_NOT_STICKY;

    }

}
