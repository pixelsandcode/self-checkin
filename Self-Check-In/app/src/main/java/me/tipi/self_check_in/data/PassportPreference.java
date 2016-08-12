package me.tipi.self_check_in.data;

import android.content.SharedPreferences;

public class PassportPreference extends StringPreference {
  public PassportPreference(SharedPreferences preferences, String key) {
    super(preferences, key);
  }

  public PassportPreference(SharedPreferences preferences, String key, String defaultValue) {
    super(preferences, key, defaultValue);
  }
}
