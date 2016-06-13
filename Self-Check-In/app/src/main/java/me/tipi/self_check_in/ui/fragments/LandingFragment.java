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
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.drivemode.android.typeface.TypefaceHelper;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.otto.Bus;

import javax.inject.Inject;

import butterknife.ButterKnife;
import me.tipi.self_check_in.R;
import me.tipi.self_check_in.SelfCheckInApp;
import me.tipi.self_check_in.ui.AppContainer;
import me.tipi.self_check_in.ui.events.BackShouldShowEvent;
import me.tipi.self_check_in.ui.events.RefreshShouldShowEvent;
import me.tipi.self_check_in.ui.events.SettingShouldShowEvent;
import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class LandingFragment extends Fragment {

  @Inject Bus bus;
  @Inject Tracker tracker;
  @Inject AppContainer appContainer;
  @Inject TypefaceHelper typeface;

  public LandingFragment() {
    // Required empty public constructor
  }

  public static LandingFragment newInstance(Context context) {
    LandingFragment fragment = new LandingFragment();
    SelfCheckInApp.get(context).inject(fragment);
    return fragment;
  }


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View rootView =  inflater.inflate(R.layout.fragment_landing, container, false);
    ButterKnife.bind(this, rootView);

    Timber.d("OnCreateView");
    typeface.setTypeface(container, getResources().getString(R.string.font_regular));
    return rootView;
  }

  @Override public void onResume() {
    super.onResume();
    bus.register(this);
    tracker.setScreenName(getClass().getSimpleName());
    tracker.send(new HitBuilders.ScreenViewBuilder().build());
  }

  @Override public void onPause() {
    super.onPause();
    bus.unregister(this);
  }

  @Override public void setUserVisibleHint(boolean isVisibleToUser) {
    super.setUserVisibleHint(isVisibleToUser);
    if (getActivity() != null && isVisibleToUser) {
      bus.post(new SettingShouldShowEvent(false));
      bus.post(new BackShouldShowEvent(true));
      bus.post(new RefreshShouldShowEvent(false));
    }
  }

}
