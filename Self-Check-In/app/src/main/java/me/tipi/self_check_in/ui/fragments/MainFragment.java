package me.tipi.self_check_in.ui.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.tipi.self_check_in.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {


  public MainFragment() {
    // Required empty public constructor
  }


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_main, container, false);
    view.setKeepScreenOn(true);

    return view;
  }

}
