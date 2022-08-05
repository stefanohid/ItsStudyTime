package com.example.itsstudytime.notifications;


import static com.example.itsstudytime.MainActivity.CHANNEL_ID;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.res.ResourcesCompat;

import com.example.itsstudytime.DetailedExamList;
import com.example.itsstudytime.MainActivity;
import com.example.itsstudytime.R;

import java.util.Calendar;

public class NotificationService extends IntentService {



    public NotificationService() {
        super("NotificationService");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onHandleIntent(Intent intent) {
        Notification.Builder builder = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("My Title")
                .setContentText("This is the Body")
                .setSmallIcon(R.drawable.icons8_notification_24)
                .setPriority(Notification.PRIORITY_HIGH);
//        builder.setWhen(Calendar.getInstance().getTimeInMillis()+1000*50);

//        Intent notifyIntent = new Intent(this, DetailedExamList.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 2, notifyIntent, PendingIntent.FLAG_IMMUTABLE);
////                     to be able to launch your activity from the notification
//        builder.setContentIntent(pendingIntent);
        Notification notificationCompat = builder.build();
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);

        managerCompat.notify(1, notificationCompat);
    }


}
