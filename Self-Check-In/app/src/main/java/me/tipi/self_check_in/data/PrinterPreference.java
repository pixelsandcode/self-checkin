package me.tipi.self_check_in.data;


import android.content.SharedPreferences;

public class PrinterPreference extends BooleanPreference {
  PrinterPreference(SharedPreferences preferences, String key) {
    super(preferences, key);
  }

  PrinterPreference(SharedPreferences preferences, String key, boolean defaultValue) {
    super(preferences, key, defaultValue);
  }
}
