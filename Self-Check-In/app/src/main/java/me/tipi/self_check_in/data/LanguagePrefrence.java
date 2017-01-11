package me.tipi.self_check_in.data;

import android.content.SharedPreferences;


public class LanguagePrefrence extends StringPreference {

  public LanguagePrefrence(SharedPreferences preferences, String key, String defaultValue) {
    super(preferences, key, defaultValue);
  }

}