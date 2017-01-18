package me.tipi.self_check_in.data;

import android.content.SharedPreferences;


public class LanguagePreference extends StringPreference {

  public LanguagePreference(SharedPreferences preferences, String key, String defaultValue) {
    super(preferences, key, defaultValue);
  }

}