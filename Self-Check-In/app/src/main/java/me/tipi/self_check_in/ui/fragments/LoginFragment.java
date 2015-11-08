package me.tipi.self_check_in.ui.fragments;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import me.tipi.self_check_in.R;

public class LoginFragment extends Fragment {

  private View rootView;

  public LoginFragment() {
    // Required empty public constructor
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    rootView = inflater.inflate(R.layout.fragment_login, container, false);
    ButterKnife.bind(this, rootView);
    return rootView;
  }


}
