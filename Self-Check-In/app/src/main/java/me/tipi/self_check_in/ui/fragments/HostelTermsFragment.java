package me.tipi.self_check_in.ui.fragments;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.f2prateek.rx.preferences.Preference;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.otto.Bus;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.tipi.self_check_in.R;
import me.tipi.self_check_in.SelfCheckInApp;
import me.tipi.self_check_in.data.api.ApiConstants;
import me.tipi.self_check_in.data.api.AuthenticationService;
import me.tipi.self_check_in.data.api.models.LoginResponse;
import me.tipi.self_check_in.ui.FindUserActivity;
import me.tipi.self_check_in.ui.SignUpActivity;
import me.tipi.self_check_in.ui.events.BackShouldShowEvent;
import me.tipi.self_check_in.ui.events.SettingShouldShowEvent;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class HostelTermsFragment extends Fragment {

  public static final String TAG = HostelTermsFragment.class.getSimpleName();

  @Inject Bus bus;
  @Inject Tracker tracker;
  @Inject AuthenticationService authenticationService;
  @Inject @Named(ApiConstants.HOSTEL_KEY)
  Preference<String> hostelKey;

  @Bind(R.id.hostel_terms) TextView termsTextView;

  private Handler handler = new Handler();
  private Runnable runnable = new Runnable() {
    @Override public void run() {
      startOver();
    }
  };

  public HostelTermsFragment() {
    // Required empty public constructor
  }

  public static HostelTermsFragment newInstance(Context context) {
    HostelTermsFragment fragment = new HostelTermsFragment();
    SelfCheckInApp.get(context).inject(fragment);
    return fragment;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View rootView = inflater.inflate(R.layout.fragment_hostel_terms, container, false);
    ButterKnife.bind(this, rootView);
    loadTerms();
    return rootView;
  }

  private void loadTerms() {
    authenticationService.getTerms(hostelKey.get(), new Callback<LoginResponse>() {
      @Override public void success(LoginResponse loginResponse, Response response) {
        termsTextView.setText(loginResponse.data.terms);
      }

      @Override public void failure(RetrofitError error) {
        loadTerms();
      }
    });
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

  @Override public void onStop() {
    super.onStop();
    handler.removeCallbacks(runnable);
  }

  @Override public void setUserVisibleHint(boolean isVisibleToUser) {
    super.setUserVisibleHint(isVisibleToUser);
    if (getActivity() != null && isVisibleToUser) {
      bus.post(new BackShouldShowEvent(false));
      //bus.post(new RefreshShouldShowEvent(true));
      bus.post(new SettingShouldShowEvent(false));

      handler.postDelayed(runnable, ApiConstants.START_OVER_TIME);
    }
  }

  @OnClick(R.id.agree_btn)
  public void agreeClicked() {
    getActivity().onBackPressed();
  }

  private void startOver() {
    if (getActivity() != null && getActivity() instanceof SignUpActivity) {
      ((SignUpActivity)getActivity()).reset();
    } else if (getActivity() != null){
      ((FindUserActivity)getActivity()).reset();
    }
  }
}
