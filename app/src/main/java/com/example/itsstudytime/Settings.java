package com.example.itsstudytime;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.ThemeUtils;
import androidx.fragment.app.DialogFragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

public class Settings extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);


    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        String theme = sharedPreferences.getString("list_preference_1", "default");
        if(key.equals("list_preference_1")) {
            if(theme.equals("dark")) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else if(theme.equals("default")) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            } else if(theme.equals("white")) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        }

        boolean sw = sharedPreferences.getBoolean("switch_preference_1", false);
        Log.i("sw", sw + "");
        if(key.equals("switch_preference_1")) {
            if(sw) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setView(R.layout.confirm_dialog_layout);
                builder.setPositiveButton(getResources().getString(R.string.positive_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.nukeEsami();
                    }
                });

                builder.setNegativeButton(getResources().getString(R.string.negative_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.setCancelable(false);
                if(!(isFinishing()))
                {
                    builder.create().show();
                }

            }
        }

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
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

    public static class ConfirmFragment extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
            builder.setView(R.layout.confirm_dialog_layout);

            builder.setPositiveButton(getResources().getString(R.string.positive_button), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            builder.setNegativeButton(getResources().getString(R.string.negative_button), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });


            return null;
        }
    }


}