package me.tipi.self_check_in.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import me.tipi.self_check_in.R;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Timber.d("Created");
  }

  @Override protected void onPause() {
    super.onPause();
    Timber.d("Paused");
  }

  @Override protected void onResume() {
    super.onResume();
    Timber.d("Resumed");
  }

  @Override protected void onStop() {
    super.onStop();
    Timber.d("Stopped");
  }
}
