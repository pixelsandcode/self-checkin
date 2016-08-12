package me.tipi.self_check_in.data;

import android.content.SharedPreferences;

public class AvatarPreference extends StringPreference {
  public AvatarPreference(SharedPreferences preferences, String key) {
    super(preferences, key);
  }

  public AvatarPreference(SharedPreferences preferences, String key, String defaultValue) {
    super(preferences, key, defaultValue);
  }
}
