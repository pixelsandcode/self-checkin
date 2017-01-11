package me.tipi.self_check_in.ui.fragments;


import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Bus;

import java.io.File;

import javax.inject.Inject;

import me.tipi.self_check_in.R;
import me.tipi.self_check_in.SelfCheckInApp;
import me.tipi.self_check_in.ui.events.BackShouldShowEvent;
import me.tipi.self_check_in.ui.events.RefreshShouldShowEvent;
import me.tipi.self_check_in.ui.events.SettingShouldShowEvent;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {

  @Inject Bus bus;

  public MainFragment() {
    // Required empty public constructor
  }

  public static MainFragment newInstance(Context context) {
    MainFragment fragment = new MainFragment();
    SelfCheckInApp.get(context).inject(fragment);

    return fragment;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    deleteMadKioskContent();
    return inflater.inflate(R.layout.fragment_main, container, false);
  }

  @Override public void onResume() {
    super.onResume();
    if (getActivity() != null && bus != null) {
      bus.register(this);
      bus.post(new SettingShouldShowEvent(true));
      bus.post(new BackShouldShowEvent(false));
      bus.post(new RefreshShouldShowEvent(false));
    }
  }

  @Override public void onPause() {
    super.onPause();
    bus.unregister(this);
  }

  private void deleteMadKioskContent() {
    File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
        getString(R.string.app_name));
    if (dir.isDirectory()) {
      String[] content = dir.list();
      for (int i = 0; i < content.length; i++) {
        new File(dir, content[i]).delete();
      }
    }
  }

}
