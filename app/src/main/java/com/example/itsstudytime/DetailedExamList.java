package com.example.itsstudytime;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import com.example.itsstudytime.database.Esame;
import com.google.android.material.snackbar.Snackbar;
import com.example.itsstudytime.EsameAdapter;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class DetailedExamList extends AppCompatActivity {
    public static String SERIA = "EXTRA_SERIALIZZATO";
    public static String POS = "EXTRA_POSITION";
    static CheckBox superato;
    static ConstraintLayout cL;
    static TextView printVoto;
    static TextView printTime;
    static CheckBox checkSuperato;
    static int position;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.esame_detailed_list);

        cL = findViewById(R.id.details_cl);
        printVoto = findViewById(R.id.print_voto);
        printTime = findViewById(R.id.print_tempostudio);
        checkSuperato = findViewById(R.id.superato_check);

        Intent intent = getIntent();
        Esame esame = (Esame) intent.getSerializableExtra(SERIA);
        position = intent.getIntExtra(POS, 0);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        TextView printNomeEsame = findViewById(R.id.print_nome);
        TextView printData = findViewById(R.id.print_data);

        printNomeEsame.setText(esame.getNome());
        printData.setText(getResources().getString(R.string.date) + " " + esame.getData());


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

        if(esame.isPrevious()) {
            superato.setChecked(true);
            MainActivity.adapter.getEsami().get(position).setPrevious(false);
            MainActivity.db.esameDAO().update(MainActivity.adapter.getEsami().get(position));
            ExamDetailsFragment detailsFragment = new ExamDetailsFragment();
            detailsFragment.show(getSupportFragmentManager(), "details");
        }

        Button letsStudy = findViewById(R.id.lets_study);
        letsStudy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                 }
        });


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




}

