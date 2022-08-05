package com.example.itsstudytime;

import static com.example.itsstudytime.MainActivity.CHANNEL_ID;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Chronometer;
import android.widget.Toast;
import android.os.SystemClock;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.DialogFragment;
import androidx.room.Room;

import com.example.itsstudytime.database.Esame;
import com.example.itsstudytime.database.EsameDB;
import com.example.itsstudytime.dates.DateFormatter;
import com.example.itsstudytime.dates.TimestampConverter;
import com.example.itsstudytime.notifications.NotificationReceiver;
import com.example.itsstudytime.notifications.NotificationService;
import com.google.android.material.snackbar.Snackbar;
import com.example.itsstudytime.EsameAdapter;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class DetailedExamList extends AppCompatActivity {
    public static final String SERIA = "EXTRA_SERIALIZZATO";
    public static final String POS = "EXTRA_POSITION";
    public static final String studyTime = "STUDY_TIME";
    static CheckBox superato;
    static ConstraintLayout cL;
    static TextView printVoto;
    static TextView printTime;
    static CheckBox checkSuperato;
    static int position;
    private Chronometer chronometer;
    private long pauseOffset;
    private boolean running;
    private TextView setHours;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    private Intent intent;
    private Esame esame;
    private EsameDB db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.esame_detailed_list);

        intent = getIntent();
        esame = (Esame) intent.getSerializableExtra(SERIA);
        db = Room.databaseBuilder(getApplicationContext(), EsameDB.class, "db_esame").allowMainThreadQueries().fallbackToDestructiveMigration().build();

        cL = findViewById(R.id.details_cl);
        printVoto = findViewById(R.id.print_voto);
        printTime = findViewById(R.id.print_tempostudio);
        checkSuperato = findViewById(R.id.superato_check);

        setHours = findViewById(R.id.hours);
        setHours.setText("" + esame.getStudyTime());

        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        position = intent.getIntExtra(POS, 0);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        TextView printNomeEsame = findViewById(R.id.print_nome);
        TextView printData = findViewById(R.id.print_data);


        printNomeEsame.setText(esame.getNome());

        printData.setText(getResources().getString(R.string.date) + " " + DateFormatter.formatDate(esame.getData()));


        if(esame.getVoto()!=0) {
            checkSuperato.setVisibility(View.GONE);
            if (esame.isLode()) {
                printVoto.setText(getResources().getString(R.string.grade) + " " + esame.getVoto() + "L");
            } else {
                printVoto.setText(getResources().getString(R.string.grade) + " " + esame.getVoto());
            }
        }

        superato = findViewById(R.id.superato_check);

        superato.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExamDetailsFragment detailsFragment = new ExamDetailsFragment();
                detailsFragment.show(getSupportFragmentManager(), "details");
            }
        });

        ///////////////////////////////////////////////
        /*
        Se l'esame è precedente alla data corrente, si chiede di fornire il voto conseguito.
         */

        Date today = new Date();
        Date currentEsame = DateFormatter.fromDateToString(esame.getData());
        if(today.compareTo(currentEsame)>0) {
            esame.setPrevious(true);
            MainActivity.db.esameDAO().updatePrevious(true, esame.getId());
        }


        if(esame.isPrevious()) {
            superato.setChecked(true);
            MainActivity.adapter.getEsami().get(position).setPrevious(false);
            MainActivity.db.esameDAO().update(MainActivity.adapter.getEsami().get(position));
            ExamDetailsFragment detailsFragment = new ExamDetailsFragment();
            detailsFragment.show(getSupportFragmentManager(), "details");
        }

        ///////////////////////////////////////////////

        Button start = findViewById(R.id.start_button);
        Button pause = findViewById(R.id.pause_button);
        Button stop = findViewById(R.id.stop_button);
        Button letsStudy = findViewById(R.id.lets_study);

        /*
        Impostazione del cronometro di studio.
         */
        chronometer = findViewById(R.id.chronometer);
        chronometer.setFormat("%s");
        chronometer.setBase(SystemClock.elapsedRealtime());

        chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
//                if ((SystemClock.elapsedRealtime() - chronometer.getBase()) >= 10000) {
//                    chronometer.setBase(SystemClock.elapsedRealtime());
//                    Toast.makeText(DetailedExamList.this, "Bing!", Toast.LENGTH_SHORT).show();
//                }
            }
        });

        letsStudy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start.setVisibility(View.VISIBLE);
                pause.setVisibility(View.VISIBLE);
                stop.setVisibility(View.VISIBLE);
                startChronometer(v);
            }
        });

        setHours.setText("" + esame.getStudyTime());

    }



    @Override
    protected void onPause() {
        super.onPause();

        if (running) {
            chronometer.stop();
            pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();

            esame.setStudyTime(pauseOffset + esame.getStudyTime());
            db.esameDAO().update(esame);

            running = false;



        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (running) {
            chronometer.stop();
            pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();

            esame.setStudyTime(pauseOffset + esame.getStudyTime());
            db.esameDAO().update(esame);

            running = false;

        }

    }

    @Override
    protected void onStop() {
        super.onStop();

        if (running) {
            chronometer.stop();
            pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();

            esame.setStudyTime(pauseOffset + esame.getStudyTime());
            db.esameDAO().update(esame);

            running = false;

        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        setHours.setText("" + esame.getStudyTime());
    }

    public static class ExamDetailsFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
            builder.setView(R.layout.details_dialog_layout);

            builder.setPositiveButton(getResources().getString(R.string.positive_button), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    EditText num = getDialog().findViewById(R.id.edit_voto);
                    EditText ore = getDialog().findViewById(R.id.edit_ore);
                    CheckBox checkLode = getDialog().findViewById(R.id.check_lode);
                    if(num.getText().toString().replaceAll("\\s+","").isEmpty() || num.getText().toString().equals("0")) {
                        dialog.cancel();
                        superato.setChecked(false);
                        Snackbar.make(cL, getResources().getString(R.string.empty_grade), Snackbar.LENGTH_LONG).show();
                    } else {
                        MainActivity.adapter.getEsami().get(position).setVoto(Integer.parseInt(num.getText().toString()));
                        printVoto.setText(printVoto.getText().toString() + " " + num.getText().toString());

                        if(ore.getText().toString().replaceAll("\\s+","").isEmpty() || ore.getText().toString().equals("0")) {
                            printTime.setText(printTime.getText().toString() + " 0" );
                        } else {
                            printTime.setText(printTime.getText().toString() + " " + ore.getText().toString());
                        }


                        if (checkLode.isChecked()) {
                            MainActivity.adapter.getEsami().get(position).setLode(true);
                            printVoto.setText(printVoto.getText().toString() + "L");
                        }
                        checkSuperato.setVisibility(View.GONE);
                        MainActivity.db.esameDAO().update(MainActivity.adapter.getEsami().get(position));
                    }
                }
            });

            builder.setNegativeButton(getResources().getString(R.string.negative_button), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    superato.setChecked(false);
                }
            });

            setCancelable(false);
            return builder.create();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void startChronometer(View v) {
        if (!running) {

//            Notification.Builder builder = new Notification.Builder(this, CHANNEL_ID)
//                    .setContentTitle("My Title")
//                    .setContentText("This is the Body")
//                    .setSmallIcon(R.drawable.icons8_notification_24)
//                    .setPriority(Notification.PRIORITY_HIGH);
//        builder.setWhen(Calendar.getInstance().getTimeInMillis()+1000*50);

            Intent notifyIntent = new Intent(this, NotificationReceiver.class);
            notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 1, notifyIntent, PendingIntent.FLAG_IMMUTABLE);
            try {
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
            //                     to be able to launch your activity from the notification
//            builder.setContentIntent(pendingIntent);
//            Notification notificationCompat = builder.build();
//            NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
//            managerCompat.notify(1, notificationCompat);

            chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
            chronometer.start();
            running = true;
        }
    }

    public void pauseChronometer(View v) {

        if (running) {
            chronometer.stop();
            pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();

            esame.setStudyTime(pauseOffset + esame.getStudyTime());
            db.esameDAO().update(esame);

            setHours.setText("" + esame.getStudyTime());
            running = false;

        }
    }

    public void resetChronometer(View v) {
        chronometer.setBase(SystemClock.elapsedRealtime());
        pauseOffset = 0;
    }


}

