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
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.drivemode.android.typeface.TypefaceHelper;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.tipi.self_check_in.R;
import me.tipi.self_check_in.SelfCheckInApp;
import me.tipi.self_check_in.data.api.ApiConstants;
import me.tipi.self_check_in.data.api.AuthenticationService;
import me.tipi.self_check_in.data.api.models.FindResponse;
import me.tipi.self_check_in.data.api.models.Guest;
import me.tipi.self_check_in.data.api.models.User;
import me.tipi.self_check_in.ui.AppContainer;
import me.tipi.self_check_in.ui.FindUserActivity;
import me.tipi.self_check_in.ui.SignUpActivity;
import me.tipi.self_check_in.ui.events.AuthenticationFailedEvent;
import me.tipi.self_check_in.ui.events.AuthenticationPassedEvent;
import me.tipi.self_check_in.ui.events.BackShouldShowEvent;
import me.tipi.self_check_in.ui.events.PagerChangeEvent;
import me.tipi.self_check_in.ui.events.SettingShouldShowEvent;
import me.tipi.self_check_in.ui.transform.CircleStrokeTransformation;
import me.tipi.self_check_in.util.Strings;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class FindUserFragment extends Fragment {

  @Inject Picasso picasso;
  @Inject Bus bus;
  @Inject AuthenticationService authenticationService;
  @Inject Guest guest;
  @Inject Tracker tracker;
  @Inject TypefaceHelper typeface;
  @Inject AppContainer appContainer;

  @Bind(R.id.email) EditText emailEditText;
  @Bind(R.id.email_input_layout) TextInputLayout emailLayout;
  @Bind(R.id.try_again) TextView tryAgainView;
  @Bind(R.id.match_user_container) RelativeLayout matchedUserContainer;
  @Bind(R.id.avatar) ImageView avatarView;
  @Bind(R.id.user_name) TextView userNameView;

  private String enteredEmail;
  private MaterialDialog loading;

  private Handler handler = new Handler();
  private Runnable runnable = new Runnable() {
    @Override public void run() {
      startOver();
    }
  };

  /**
   * Instantiates a new Find user fragment.
   */
  public FindUserFragment() {
    // Required empty public constructor
  }

  /**
   * New instance find user fragment.
   *
   * @param context the context
   * @return the find user fragment
   */
  public static FindUserFragment newInstance(Context context) {
    FindUserFragment fragment = new FindUserFragment();
    SelfCheckInApp.get(context).inject(fragment);
    return fragment;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment// Inflate the layout for this fragment
    View rootView = inflater.inflate(R.layout.fragment_find_user, container, false);
    ButterKnife.bind(this, rootView);
    Timber.d("ViewCreated");
    if (typeface != null) {
      typeface.setTypeface(container, getResources().getString(R.string.font_regular));
    }

    loading = new MaterialDialog.Builder(getActivity())
        .content("Please wait")
        .cancelable(false)
        .progress(true, 0)
        .build();

    matchedUserContainer.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        bus.post(new PagerChangeEvent(1));
      }
    });

    return  rootView;
  }

  @Override public void onResume() {
    super.onResume();
    bus.register(this);
    Timber.d("Resumed");
    tracker.setScreenName(getClass().getSimpleName());
    tracker.send(new HitBuilders.ScreenViewBuilder().build());
  }

  @Override public void onPause() {
    super.onPause();
    bus.unregister(this);
    Timber.d("Paused");
  }

  @Override public void onStop() {
    super.onStop();
    handler.removeCallbacks(runnable);
  }

  @Override public void setUserVisibleHint(boolean isVisibleToUser) {
    super.setUserVisibleHint(isVisibleToUser);
    if (getActivity() != null && isVisibleToUser) {
      bus.post(new BackShouldShowEvent(true));
      //bus.post(new RefreshShouldShowEvent(true));
      bus.post(new SettingShouldShowEvent(false));

      handler.postDelayed(runnable, ApiConstants.START_OVER_TIME);
    }
  }

  /**
   * On auth passed.
   *
   * @param event the event
   */
  @Subscribe
  public void onAuthPassed(AuthenticationPassedEvent event) {
    find();
    Timber.d("finding again after login");
  }

  /**
   * Find.
   */
  @OnClick(R.id.find_btn)
  public void find() {
    if (!isError()) {
      guest.user_key = "";
      guest.name = "";
      matchedUserContainer.setVisibility(View.GONE);
      tryAgainView.setVisibility(View.GONE);
      loading.show();
      authenticationService.findUser(enteredEmail, new Callback<FindResponse>() {
        @Override public void success(FindResponse findResponse, Response response) {
          loading.dismiss();
          User matchedUser = findResponse.data;
          Timber.d("Found user: %s", matchedUser.toString());

          // Handling show/hide views
          tryAgainView.setVisibility(View.GONE);
          matchedUserContainer.setVisibility(View.VISIBLE);

          // Filling user info
          picasso.load(Strings.makeAvatarUrl(matchedUser.doc_key))
              .resize(200, 200).centerCrop()
              .transform(new CircleStrokeTransformation(getActivity(), 0, 0))
              .placeholder(R.drawable.avatar_placeholder).into(avatarView);
          userNameView.setText(matchedUser.name);
          guest.name = matchedUser.name;
          guest.user_key = matchedUser.doc_key;
          guest.email = enteredEmail;

          tracker.send(new HitBuilders.EventBuilder("Matched User", "User found").build());
        }

        @Override public void failure(RetrofitError error) {

          if (error.getResponse() != null && error.getResponse().getStatus() == 504) {
            Snackbar.make(appContainer.bind(getActivity()), R.string.no_connection, Snackbar.LENGTH_LONG).show();
            return;
          }

          if (error.getResponse() != null && error.getResponse().getStatus() == 401) {
            Timber.d("authentication failed");
            bus.post(new AuthenticationFailedEvent());
          } else {
            // Handling show/hide views
            loading.dismiss();
            tryAgainView.setVisibility(View.VISIBLE);
            matchedUserContainer.setVisibility(View.GONE);
            Timber.d("Error finding: %s", error.toString());
          }
        }
      });
    }
  }

  /**
   * Is error boolean.
   *
   * @return the boolean
   */
  private boolean isError() {

    emailLayout.setError(null);
    emailLayout.setErrorEnabled(false);

    boolean cancel = false;
    View focusView = null;

    enteredEmail = emailEditText.getText().toString();

    // Check for validation
    if (TextUtils.isEmpty(enteredEmail)) {
      emailLayout.setErrorEnabled(true);
      emailLayout.setError(getString(R.string.error_field_required));
      focusView = emailEditText;
      cancel = true;
    } else if (!Strings.isValidEmail(enteredEmail)) {
      emailLayout.setErrorEnabled(true);
      emailLayout.setError(getString(R.string.error_invalid_email));
      focusView = emailEditText;
      cancel = true;
    }

    if (cancel) {
      if (focusView != null) {
        focusView.requestFocus();
      }
    }

    return cancel;
  }

  private void startOver() {
    if (getActivity() != null && getActivity() instanceof SignUpActivity) {
      ((SignUpActivity)getActivity()).reset();
    } else if (getActivity() != null){
      ((FindUserActivity)getActivity()).reset();
    }
  }

}
