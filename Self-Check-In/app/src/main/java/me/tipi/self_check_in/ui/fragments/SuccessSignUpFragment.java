/*
 * *
 *  * Copyright (c) 2015-2016 www.Tipi.me.
 *  * Created by Ashkan Hesaraki.
 *  * Ashkan.Hesaraki@gmail.com
 *
 */

package me.tipi.self_check_in.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.f2prateek.rx.preferences.Preference;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.otto.Bus;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.ButterKnife;
import butterknife.OnClick;
import me.tipi.self_check_in.R;
import me.tipi.self_check_in.SelfCheckInApp;
import me.tipi.self_check_in.data.api.ApiConstants;
import me.tipi.self_check_in.ui.FindUserActivity;
import me.tipi.self_check_in.ui.SignUpActivity;
import me.tipi.self_check_in.ui.events.BackShouldShowEvent;
import me.tipi.self_check_in.ui.events.RefreshShouldShowEvent;
import me.tipi.self_check_in.ui.events.SettingShouldShowEvent;

public class SuccessSignUpFragment extends Fragment {

  @Inject Bus bus;
  @Inject Tracker tracker;
  @Inject @Named(ApiConstants.HOSTEL_NAME)
  Preference<String> hostelName;

  /**
   * Instantiates a new Success sign up fragment.
   */
  public SuccessSignUpFragment() {
    // Required empty public constructor
  }

  /**
   * New instance success sign up fragment.
   *
   * @param context the context
   * @return the success sign up fragment
   */
  public static SuccessSignUpFragment newInstance(Context context) {
    SuccessSignUpFragment fragment = new SuccessSignUpFragment();
    SelfCheckInApp.get(context).inject(fragment);
    return fragment;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View rootView = inflater.inflate(R.layout.fragment_success_sign_up, container, false);
    ButterKnife.bind(this, rootView);
    return rootView;
  }

  @Override public void onResume() {
    super.onResume();
    bus.register(this);
    tracker.setScreenName("Success");
    tracker.send(new HitBuilders.ScreenViewBuilder().build());
  }

  @Override public void onPause() {
    super.onPause();
    bus.unregister(this);
  }

  @Override public void setUserVisibleHint(boolean isVisibleToUser) {
    super.setUserVisibleHint(isVisibleToUser);
    if (getActivity() != null && isVisibleToUser) {
      bus.post(new BackShouldShowEvent(false));
      bus.post(new RefreshShouldShowEvent(true));
      bus.post(new SettingShouldShowEvent(false));

      new Handler().postDelayed(new Runnable() {
        @Override public void run() {
          startOver();
        }
      }, 15000);
    }
  }

  @OnClick(R.id.continue_btn)
  public void finishTapped() {
    startOver();
  }

  private void startOver() {
    if (getActivity() != null && getActivity() instanceof SignUpActivity) {
      ((SignUpActivity)getActivity()).reset();
    } else if (getActivity() != null){
      ((FindUserActivity)getActivity()).reset();
    }
  }
}
