package com.example.itsstudytime;


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
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import com.example.itsstudytime.database.Esame;
import com.example.itsstudytime.database.EsameDB;
import com.example.itsstudytime.notifications.NotificationReceiver;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.itsstudytime.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createNotificationChannel();



//        AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
//        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,  System.currentTimeMillis(),
//                1000 * 60 * 60 * 24, pendingIntent);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        setSupportActionBar(binding.toolbar);

        cL = findViewById(R.id.main_layout);

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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

         int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this.getApplicationContext(), Settings.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    /*
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

     */


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

                                db.esameDAO().insertAll(nuovoEsame);
                                adapter.addEsame(nuovoEsame);
                                Snackbar.make(cL, cL.getResources().getString(R.string.exam_snackbar) + " " + date, Snackbar.LENGTH_LONG).show();


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
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "StudyTime", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("This is a test");
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


}