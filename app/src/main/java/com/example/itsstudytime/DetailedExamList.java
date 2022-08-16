package com.example.itsstudytime;

import static com.example.itsstudytime.notifications.NotificationService.SERVICE_RUNNING;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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
import android.os.SystemClock;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;
import androidx.room.Room;

import com.example.itsstudytime.database.Esame;
import com.example.itsstudytime.database.EsameDB;
import com.example.itsstudytime.dates.DateFormatter;
import com.example.itsstudytime.notifications.NotificationReceiver;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Locale;

public class DetailedExamList extends AppCompatActivity {
    public static final String SERIA = "EXTRA_SERIALIZZATO";
    public static final String POS = "EXTRA_POSITION";
    public static final String NOTIF = "NOTIF";
    static CheckBox superato;
    static ConstraintLayout cL;
    static TextView printVoto;
    static TextView printTime;
    static CheckBox checkSuperato;
    static int position;
    private Chronometer chronometer;
    private long pauseOffset;
    private long oldOffset;
    public boolean running;
    private TextView setHours;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Intent intent;
    private Esame esame;
    private EsameDB db;
    private Button start;
    private Button pause;
    private Button reset;
    private static Button letsStudy;
    private  DecimalFormat df;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        db = Room.databaseBuilder(getApplicationContext(), EsameDB.class, "db_esame").allowMainThreadQueries().fallbackToDestructiveMigration().build();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.esame_detailed_list);

        pauseOffset = 0;
        oldOffset = 0;

        intent = getIntent();
        esame = (Esame) intent.getSerializableExtra(SERIA);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = prefs.edit();

        cL = findViewById(R.id.details_cl);
        printVoto = findViewById(R.id.print_voto);
        printTime = findViewById(R.id.print_tempostudio);
        checkSuperato = findViewById(R.id.superato_check);
        setHours = findViewById(R.id.hours);

        df = new DecimalFormat("#.###");
        df.setRoundingMode(RoundingMode.CEILING);

        double hours  = (double) esame.getStudyTime() / (1000*60*60);
        setHours.setText("" + df.format(hours));

        position = intent.getIntExtra(POS, 0);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        TextView printNomeEsame = findViewById(R.id.print_nome);
        TextView printData = findViewById(R.id.print_data);


        printNomeEsame.setText(esame.getNome());

        printData.setText(getResources().getString(R.string.date) + " " + DateFormatter.formatDate(esame.getData()));

        letsStudy = findViewById(R.id.lets_study);
        if(esame.getVoto()!=0) {
            checkSuperato.setVisibility(View.GONE);
            letsStudy.setVisibility(View.GONE);
            printVoto.setVisibility(View.VISIBLE);
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

        start = findViewById(R.id.start_button);
        pause = findViewById(R.id.pause_button);
        reset = findViewById(R.id.reset_button);

        pause.setVisibility(View.GONE);
        start.setVisibility(View.GONE);
        reset.setVisibility(View.GONE);

        /*
        Impostazione del cronometro di studio.
         */
        chronometer = findViewById(R.id.chronometer);
        chronometer.setFormat("%s");
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.setVisibility(View.GONE);

        letsStudy.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                letsStudy.setVisibility(View.GONE);
                start.setVisibility(View.VISIBLE);
                pause.setVisibility(View.VISIBLE);
                reset.setVisibility(View.VISIBLE);
                chronometer.setVisibility(View.VISIBLE);
                startChronometer();
            }
        });

        start.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                startChronometer();
            }
        });

        pause.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                pauseChronometer();
            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                resetChronometer();
            }
        });

        FloatingActionButton fab = findViewById(R.id.send_mail);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                composeEmail();
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();

        if (running) {
            chronometer.stop();
            oldOffset = pauseOffset;
            pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();

            esame.setStudyTime(pauseOffset - oldOffset + esame.getStudyTime());

            editor.putBoolean("isRunning" + esame.getNome(), true);
            editor.putLong("pauseOffset" + esame.getNome(), pauseOffset);
            editor.putLong("pauseOffset" + esame.getNome(), oldOffset);

            editor.apply();

        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onResume() {
        super.onResume();

        if(prefs.getBoolean("isRunning" + esame.getNome(), false)==true) {
            letsStudy.setVisibility(View.GONE);
            reset.setVisibility(View.VISIBLE);
            pause.setVisibility(View.VISIBLE);
            chronometer.setVisibility(View.VISIBLE);
            start.setVisibility(View.GONE);

            int elapsed = (int) prefs.getLong("elapsed" + esame.getNome(), 0);
            int pofs = (int) prefs.getLong("pauseOffset" + esame.getNome(), 0);
            int oofs = (int) prefs.getLong("oldOffset" + esame.getNome(), 0);

            int currentElapsed = (int) SystemClock.elapsedRealtime();
            editor.putLong("elapsed" + esame.getNome(), currentElapsed);

            int minutes = (int) ( ( currentElapsed - elapsed + pofs - oofs) / 1000 * 60);
            int seconds = (int) ( ( currentElapsed - elapsed + pofs - oofs) / 1000 % 60);

            oldOffset = pofs;
            pauseOffset = currentElapsed - elapsed + pofs;

            chronometer.setText(String.format(Locale.getDefault(), "%s", minutes, seconds));

            esame.setStudyTime(pauseOffset - oldOffset + esame.getStudyTime());
            db.esameDAO().update(esame);

            double hours  = (double) esame.getStudyTime() / (1000*60*60);
            setHours.setText("" + df.format(hours));

            startChronometer();
            
        } else {
            double hours  = (double) esame.getStudyTime() / (1000*60*60);
            setHours.setText("" + df.format(hours));

        }


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
                    CheckBox checkLode = getDialog().findViewById(R.id.check_lode);
                    if(num.getText().toString().replaceAll("\\s+","").isEmpty() || num.getText().toString().equals("0")) {
                        dialog.cancel();
                        superato.setChecked(false);
                        Snackbar.make(cL, getResources().getString(R.string.empty_grade), Snackbar.LENGTH_LONG).show();
                    } else {
                        MainActivity.adapter.getEsami().get(position).setVoto(Integer.parseInt(num.getText().toString()));
                        printVoto.setText(printVoto.getText().toString() + " " + num.getText().toString());

                        if (checkLode.isChecked()) {
                            MainActivity.adapter.getEsami().get(position).setLode(true);
                            printVoto.setText(printVoto.getText().toString() + "L");
                        }
                        checkSuperato.setVisibility(View.GONE);
                        printVoto.setVisibility(View.VISIBLE);
                        letsStudy.setVisibility(View.GONE);
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
    public void startChronometer() {
        if (!running) {
            /*
            Gestione della notifica
             */
            PendingIntent pendingIntent = requestNotifService(false);
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000*3600, pendingIntent);

            start.setVisibility(View.GONE);
            int elapsed = (int) SystemClock.elapsedRealtime();
            chronometer.setBase(elapsed - pauseOffset);
            editor.putLong("elapsed" + esame.getNome(), elapsed);
            editor.apply();
            chronometer.start();
            running = true;
        }
    }

    public void pauseChronometer() {
        if (running) {
            chronometer.stop();

            start.setVisibility(View.VISIBLE);

            PendingIntent pendingIntent = requestNotifService(false);
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.cancel(pendingIntent);
            try {
                if(SERVICE_RUNNING) {
                    requestNotifService(true).send();
                }
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }

            editor.putBoolean("isRunning" + esame.getNome(), false).apply();
            oldOffset = pauseOffset;
            pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();

            esame.setStudyTime(pauseOffset - oldOffset + esame.getStudyTime());
            db.esameDAO().update(esame);

            double hours  = (double) esame.getStudyTime() / (1000*60*60);
            setHours.setText("" + df.format(hours));

            running = false;

        } else {
            try {
                if(SERVICE_RUNNING) {
                    requestNotifService(true).send();
                }
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }
    }

    public void resetChronometer() {
        chronometer.setBase(SystemClock.elapsedRealtime());
        pauseOffset = 0;
    }

    public PendingIntent requestNotifService(boolean stop) {
        Intent notifyIntent = new Intent(this, NotificationReceiver.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        if(stop) {
            notifyIntent.setAction("ReceiverStop");
            notifyIntent.putExtra(NOTIF, true);
        } else {
            notifyIntent.setAction("ReceiverStart");
            notifyIntent.putExtra(NOTIF, false);
        }
        return PendingIntent.getBroadcast(getApplicationContext(), 1, notifyIntent, PendingIntent.FLAG_IMMUTABLE);
    }

    public void composeEmail() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.exam_mail) + " ");
        String lode = "";
        if(esame.isLode()) {
            lode = "L";
        }
        double hours  = (double) esame.getStudyTime() / (1000*60*60);
        String message = getResources().getString(R.string.exam) + ": " + esame.getNome() + " \n" +
                getResources().getString(R.string.date) + " " + DateFormatter.formatDate(esame.getData()) + " \n" +
                getResources().getString(R.string.study_time) + " " + df.format(hours) + " \n" +
                getResources().getString(R.string.grade) + " " + esame.getVoto() + lode + " \n\n" +
                getResources().getString(R.string.retake);
        intent.putExtra(Intent.EXTRA_TEXT, message);
        startActivity(intent);
    }


}

