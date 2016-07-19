package me.tipi.self_check_in.ui.fragments;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
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
import butterknife.OnClick;
import me.tipi.self_check_in.R;
import me.tipi.self_check_in.SelfCheckInApp;
import me.tipi.self_check_in.data.api.ApiConstants;
import me.tipi.self_check_in.ui.SignUpActivity;
import me.tipi.self_check_in.ui.events.BackShouldShowEvent;
import me.tipi.self_check_in.ui.events.SettingShouldShowEvent;
import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class ScanIDFragment extends Fragment {
  public static final String TAG = ScanIDFragment.class.getSimpleName();

  @Inject Bus bus;
  @Inject Tracker tracker;
  @Inject TypefaceHelper typeface;

  private Handler handler = new Handler();
  private Runnable runnable = new Runnable() {
    @Override public void run() {
      startOver();
    }
  };


  public ScanIDFragment() {
    // Required empty public constructor
  }

  public static ScanIDFragment newInstance(Context context) {
    ScanIDFragment fragment = new ScanIDFragment();
    SelfCheckInApp.get(context).inject(fragment);
    return fragment;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_scan_id, container, false);
    ButterKnife.bind(this, view);

    Timber.d("OnCreateView");
    typeface.setTypeface(container, getResources().getString(R.string.font_regular));

    return view;
  }

  @Override public void onResume() {
    super.onResume();
    bus.register(this);
    if (getActivity() != null) {
      bus.post(new SettingShouldShowEvent(false));
      bus.post(new BackShouldShowEvent(true));

      handler.postDelayed(runnable, ApiConstants.START_OVER_TIME);
    }

    tracker.setScreenName(getClass().getSimpleName());
    tracker.send(new HitBuilders.ScreenViewBuilder().build());
  }

  @Override public void onPause() {
    super.onPause();
    bus.unregister(this);
  }

  @Override public void onStop() {
    super.onStop();
    handler.removeCallbacks(runnable);
  }

  @OnClick(R.id.yes_btn)
  public void yesTapped() {
    ((SignUpActivity)getActivity()).showScanFragment();
  }

  @OnClick(R.id.no_btn)
  public void noTapped() {
    ((SignUpActivity)getActivity()).showPassportFragment();
  }

  private void startOver() {
    if (getActivity() != null && getActivity() instanceof SignUpActivity) {
      ((SignUpActivity)getActivity()).reset();
    }
  }
}
