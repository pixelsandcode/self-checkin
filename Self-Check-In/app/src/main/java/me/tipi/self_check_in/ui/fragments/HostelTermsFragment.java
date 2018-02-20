package me.tipi.self_check_in.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.f2prateek.rx.preferences2.Preference;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.otto.Bus;
import javax.inject.Inject;
import javax.inject.Named;
import me.tipi.self_check_in.R;
import me.tipi.self_check_in.SelfCheckInApp;
import me.tipi.self_check_in.data.api.ApiConstants;
import me.tipi.self_check_in.data.api.AppCallback;
import me.tipi.self_check_in.data.api.NetworkRequestManager;
import me.tipi.self_check_in.data.api.models.BaseResponse;
import me.tipi.self_check_in.data.api.models.LoginResponse;
import me.tipi.self_check_in.ui.AppContainer;
import me.tipi.self_check_in.ui.FindUserActivity;
import me.tipi.self_check_in.ui.SignUpActivity;
import me.tipi.self_check_in.ui.events.BackShouldShowEvent;
import me.tipi.self_check_in.ui.events.SettingShouldShowEvent;
import retrofit2.Call;
import retrofit2.Response;
import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class HostelTermsFragment extends Fragment {

  public static final String TAG = HostelTermsFragment.class.getSimpleName();

  @Inject Bus bus;
  @Inject Tracker tracker;
  @Inject
  @Named(ApiConstants.HOSTEL_KEY) Preference<String> hostelKey;
  @Inject AppContainer appContainer;

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

    NetworkRequestManager.getInstance().callGetTermsApi(hostelKey.get(), new AppCallback() {
      @Override public void onRequestSuccess(Call call, Response response) {

        LoginResponse loginResponse = (LoginResponse) response.body();
        termsTextView.setText(loginResponse.data.terms);
      }

      @Override public void onRequestFail(Call call, BaseResponse response) {
        loadTerms();
      }

      @Override public void onApiNotFound(Call call, BaseResponse response) {
        termsTextView.setText(R.string.no_terms);
        Timber.w("Sorry this hostel's Terms & Conditions isn't available! - hostel_key : %s", hostelKey.get());
      }

      @Override public void onBadRequest(Call call, BaseResponse response) {

      }

      @Override public void onAuthError(Call call, BaseResponse response) {

      }

      @Override public void onServerError(Call call, BaseResponse response) {
        Snackbar.make(appContainer.bind(getActivity()), R.string.no_connection, Snackbar.LENGTH_LONG).show();
      }

      @Override public void onRequestTimeOut(Call call, Throwable t) {

      }

      @Override public void onNullResponse(Call call) {

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
      ((SignUpActivity) getActivity()).reset();
    } else if (getActivity() != null) {
      ((FindUserActivity) getActivity()).reset();
    }
  }
}
