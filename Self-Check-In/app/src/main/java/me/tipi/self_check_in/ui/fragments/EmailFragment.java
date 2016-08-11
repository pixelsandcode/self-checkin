package me.tipi.self_check_in.ui.fragments;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
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

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.drivemode.android.typeface.TypefaceHelper;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.otto.Bus;
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
import me.tipi.self_check_in.ui.SignUpActivity;
import me.tipi.self_check_in.ui.events.BackShouldShowEvent;
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
public class EmailFragment extends Fragment {

  public static final String TAG = EmailFragment.class.getSimpleName();

  @Inject Picasso picasso;
  @Inject Bus bus;
  @Inject Guest guest;
  @Inject Tracker tracker;
  @Inject TypefaceHelper typeface;
  @Inject AppContainer appContainer;
  @Inject AuthenticationService authenticationService;

  @Bind(R.id.email) EditText emailTextView;
  @Bind(R.id.email_input_layout) TextInputLayout emailLayout;

  private String enteredEmail;
  private MaterialDialog matchUserDialog;
  private MaterialDialog loading;
  private int loginCount;

  private Handler handler = new Handler();
  private Runnable runnable = new Runnable() {
    @Override public void run() {
      startOver();
    }
  };

  public EmailFragment() {
    // Required empty public constructor
  }

  public static EmailFragment newInstance(Context context) {
    EmailFragment fragment = new EmailFragment();
    SelfCheckInApp.get(context).inject(fragment);
    return fragment;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_email, container, false);
    ButterKnife.bind(this, view);
    typeface.setTypeface(container, getResources().getString(R.string.font_regular));

    matchUserDialog = new MaterialDialog.Builder(getActivity())
        .customView(R.layout.found_user_dialog, false)
        .positiveText(R.string.dialog_me_text)
        .negativeText(R.string.dialog_not_me_text)
        .positiveColorRes(R.color.secondaryAccent)
        .cancelable(false)
        .onPositive(new MaterialDialog.SingleButtonCallback() {
          @Override
          public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
            tracker.send(new HitBuilders.EventBuilder("Matched User", "Choose me").build());
            guest.guest_key = null;
            ((SignUpActivity)getActivity()).showDateFragment();
          }
        })
        .onNegative(new MaterialDialog.SingleButtonCallback() {
          @Override
          public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
            emailTextView.setText("");
            guest.email = null;
            guest.user_key = null;
          }
        }).build();


    loading = new MaterialDialog.Builder(getActivity())
        .content("Please wait ...")
        .cancelable(false)
        .progress(true, 0)
        .build();

    return view;
  }


  @Override public void onResume() {
    super.onResume();
    bus.register(this);

    if (getActivity() != null) {
      bus.post(new BackShouldShowEvent(true));
      bus.post(new SettingShouldShowEvent(false));

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

  /**
   * Continue to date.
   */
  @OnClick(R.id.continue_btn)
  public void continueToPassport() {
    if (!isError()) {
      guest.email = enteredEmail;
      if (!TextUtils.isEmpty(enteredEmail)) {
        if (Strings.isValidEmail(enteredEmail)) {
          findUserWithEmail(enteredEmail);
        }
      }
    }
  }

  /**
   * Is error boolean.
   *
   * @return the boolean
   */
  private boolean isError() {

    emailLayout.setErrorEnabled(false);
    emailLayout.setError(null);

    boolean cancel = false;
    View focusView = null;

    enteredEmail = emailTextView.getText().toString();

    // Check for validation
    if (TextUtils.isEmpty(enteredEmail)) {
      emailLayout.setError(getString(R.string.error_field_required));
      focusView = emailTextView;
      cancel = true;
    } else if (!Strings.isValidEmail(enteredEmail)) {
      emailLayout.setError(getString(R.string.error_invalid_email));
      focusView = emailTextView;
      cancel = true;
    }

    if (cancel) {
      if (focusView != null) {
        focusView.requestFocus();
      }
    }

    return cancel;
  }

  /**
   * Find user with email.
   *
   * @param enteredEmail the entered email
   */
  private void findUserWithEmail(final String enteredEmail) {
    loading.show();
    authenticationService.findUser(enteredEmail, new Callback<FindResponse>() {
      @Override public void success(FindResponse findResponse, Response response) {
        loading.dismiss();
        User matchedUser = findResponse.data;
        guest.user_key = matchedUser.doc_key;
        guest.email = enteredEmail;
        Timber.d("Found user: %s", matchedUser.toString());

        if (getActivity() != null) {
          RelativeLayout dialogView = (RelativeLayout) matchUserDialog.getCustomView();
          if (dialogView != null) {
            ImageView avatar = (ImageView) dialogView.findViewById(R.id.avatar);
            TextView name = (TextView) dialogView.findViewById(R.id.user_name);
            picasso.load(Strings.makeAvatarUrl(matchedUser.doc_key))
                .resize(400, 400).centerCrop()
                .transform(new CircleStrokeTransformation(getActivity(), 0, 0))
                .placeholder(R.drawable.avatar_placeholder)
                .error(R.drawable.fail_photo).into(avatar);
            name.setText(matchedUser.name);
            matchUserDialog.show();
          }
        }
      }

      @Override public void failure(RetrofitError error) {
        loading.dismiss();

        if (error.getResponse() != null && error.getResponse().getStatus() == 504) {
          Snackbar.make(appContainer.bind(getActivity()), R.string.no_connection, Snackbar.LENGTH_LONG).show();
          return;
        }

        if (error.getResponse() != null && error.getResponse().getStatus() == 404) {
          ((SignUpActivity)getActivity()).showScanIDFragment();
          guest.user_key = null;
          guest.guest_key = null;
          return;
        }

        if (error.getResponse() != null && error.getResponse().getStatus() == 401) {
          Timber.e("authentication failed");
          return;
        }


        Timber.e("Error finding: %s", error.toString());
      }
    });
  }

  private void startOver() {
    if (getActivity() != null && getActivity() instanceof SignUpActivity) {
      ((SignUpActivity)getActivity()).reset();
    }
  }
}
