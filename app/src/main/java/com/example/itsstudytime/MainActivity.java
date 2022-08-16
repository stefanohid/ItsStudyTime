package com.example.itsstudytime;


import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import com.example.itsstudytime.database.Esame;
import com.example.itsstudytime.database.EsameDB;
import com.example.itsstudytime.dates.DateFormatter;
import com.example.itsstudytime.notifications.NotificationReceiver;
import com.example.itsstudytime.notifications.NotificationService;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.itsstudytime.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    protected static EsameAdapter adapter;
    LinearLayoutManager layoutManager;
    static SwipeRefreshLayout swipeRefreshLayout;
    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    static EsameDB db;
    static CoordinatorLayout cL;
    public static final String CHANNEL_ID = "CHANNEL_ID";
    private static SharedPreferences prefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createNotificationChannel();

        /*
        Recupero del tema selezionato nelle impostazioni
         */
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = prefs.getString("list_preference_1", "default");
        if(theme.equals("dark")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else if(theme.equals("default")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        } else if(theme.equals("white")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        cL = findViewById(R.id.main_layout);

        if(!prefs.getBoolean("message", false)) {
            MessageDialog md = new MessageDialog();
            md.show(getSupportFragmentManager(), "welcome_message");
        }

        db = Room.databaseBuilder(getApplicationContext(), EsameDB.class, "db_esame").allowMainThreadQueries().fallbackToDestructiveMigration().build();
        recyclerView = findViewById(R.id.id_recview);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new EsameAdapter(this, db.esameDAO().getEsami());
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));

        swipeRefreshLayout = findViewById(R.id.id_srl);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adapter.clear();
                adapter.addAll((ArrayList<Esame>) db.esameDAO().getEsami());
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                adapter.removeEsame(position);
                db.esameDAO().delete(db.esameDAO().getEsami().get(position));
                adapter.notifyDataSetChanged();
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        ItemTouchHelper.SimpleCallback simpleSecondCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                adapter.removeEsame(position);
            }
        };
        ItemTouchHelper itemTouchHelper2 = new ItemTouchHelper(simpleSecondCallback);
        itemTouchHelper2.attachToRecyclerView(recyclerView);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EsamePickerFragment example = new EsamePickerFragment();
                example.show(getSupportFragmentManager(), "nuovo_esame");

            }
        });



    }


    @Override
    protected void onRestart() {
        super.onRestart();
        adapter.clear();
        adapter.addAll((ArrayList<Esame>) db.esameDAO().getEsami());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this.getApplicationContext(), Settings.class);
            startActivity(intent);
        } else if (id == R.id.about_us) {
            Intent intent = new Intent(this.getApplicationContext(), AboutUs.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }


    public static class EsamePickerFragment extends DialogFragment  {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            final Calendar c = Calendar.getInstance();
            int anno = c.get(Calendar.YEAR);
            int mese = c.get(Calendar.MONTH);
            int giorno = c.get(Calendar.DAY_OF_MONTH);

            AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
            builder.setView(R.layout.exam_dialog_layout);

            builder.setPositiveButton(getResources().getString(R.string.positive_button), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    EditText n = getDialog().findViewById(R.id.edit_nome);

                    if(n.getText().toString().replaceAll("\\s+","").isEmpty()) {
                        dialog.cancel();
                        Snackbar.make(cL, getResources().getString(R.string.empty_string), Snackbar.LENGTH_LONG).show();
                    } else {

                        DatePickerDialog dP = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                                boolean previous = false;
                                if (year < anno || (year == anno && month < mese) || (year == anno && month == mese && dayOfMonth < giorno)) {
                                    previous = true;
                                }

                                String m = String.valueOf(month+1);
                                if (month+1<10) {
                                    m = String.format("%02d", month+1);;
                                };

                                String date = String.valueOf(year) + "-" + m + "-" + String.valueOf(dayOfMonth);

                                Esame nuovoEsame = new Esame(n.getText().toString(), date);
                                nuovoEsame.setPrevious(previous);
                                nuovoEsame.setStudyTime(0L);

                                boolean checkNome = false;
                                for(Esame e : db.esameDAO().getEsami()) {
                                    if(e.getNome().equals(nuovoEsame.getNome())) {
                                        checkNome = true;
                                        Snackbar.make(cL, cL.getResources().getString(R.string.duplicate) + " ", Snackbar.LENGTH_LONG).show();
                                        break;
                                    }
                                }

                                if(!checkNome) {
                                    db.esameDAO().insert(nuovoEsame);
                                    adapter.addEsame(nuovoEsame);
                                    adapter.clear();
                                    adapter.addAll((ArrayList<Esame>) db.esameDAO().getEsami());
                                    Snackbar.make(cL, cL.getResources().getString(R.string.exam_snackbar) + " " + DateFormatter.formatDate(date), Snackbar.LENGTH_LONG).show();
                                }

                            }

                        }, anno, mese, giorno);
                        dP.setButton(DatePickerDialog.BUTTON_POSITIVE, getResources().getString(R.string.positive_button), dP);
                        dP.setButton(DatePickerDialog.BUTTON_NEGATIVE, getResources().getString(R.string.negative_button), dP);
                        dP.show();
                    }
                }
            });
            builder.setNegativeButton(getResources().getString(R.string.negative_button), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();}
            });

            setCancelable(false);
            return builder.create();

        }


    }

    public class SimpleDividerItemDecoration extends RecyclerView.ItemDecoration {
        private Drawable mDivider;

        public SimpleDividerItemDecoration(Context context) {
            mDivider = ContextCompat.getDrawable(context, R.drawable.line_divider);
        }

        @Override
        public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
            int left = parent.getPaddingLeft();
            int right = parent.getWidth() - parent.getPaddingRight();

            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = parent.getChildAt(i);

                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

                int top = child.getBottom() + params.bottomMargin;
                int bottom = top + mDivider.getIntrinsicHeight();

                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "StudyTime", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static void nukeEsami() {
        db.esameDAO().nukeEsami();
        db.clearAllTables();
    }

    public static class MessageDialog extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
            builder.setView(R.layout.welcome_dialog_layout);
            builder.setPositiveButton(getResources().getString(R.string.positive_button), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    CheckBox ch = (CheckBox) getDialog().findViewById(R.id.message_checkbox);
                    if(ch.isChecked()) {
                        prefs.edit().putBoolean("message", true).apply();
                    }
                }
            });

            setCancelable(false);
            return builder.create();
        }
    }

}