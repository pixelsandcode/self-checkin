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
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.doomonafireball.betterpickers.datepicker.DatePickerBuilder;
import com.doomonafireball.betterpickers.datepicker.DatePickerDialogFragment;
import com.f2prateek.rx.preferences.Preference;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Named;

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
import me.tipi.self_check_in.ui.adapters.HomeTownAutoCompleteAdapter;
import me.tipi.self_check_in.ui.events.BackShouldShowEvent;
import me.tipi.self_check_in.ui.events.EmailConflictEvent;
import me.tipi.self_check_in.ui.events.PagerChangeEvent;
import me.tipi.self_check_in.ui.transform.CircleStrokeTransformation;
import me.tipi.self_check_in.util.Strings;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;

public class IdentityFragment extends Fragment implements DatePickerDialogFragment.DatePickerDialogHandler {

  @Inject Picasso picasso;
  @Inject Bus bus;
  @Inject
  @Named(ApiConstants.AVATAR) Preference<String> avatarPath;
  @Inject Guest guest;
  @Inject AuthenticationService authenticationService;

  @Bind(R.id.taken_avatar) ImageView avatarTakenView;
  @Bind(R.id.full_name) EditText fullNameTextView;
  @Bind(R.id.email) EditText emailTextView;
  @Bind(R.id.home_town) AutoCompleteTextView homeTownACView;
  @Bind(R.id.birthday) EditText birthDayPickerView;
  @Bind(R.id.passport) EditText passportTextView;

  public Date dob = null;
  public String enteredEmail;
  public String enteredFullName;
  public String enteredPassportNumber;
  public String enteredHomeTown;
  MaterialDialog matchUserDialog;

  /**
   * Instantiates a new Identity fragment.
   */
  public IdentityFragment() {
    // Required empty public constructor
  }

  /**
   * New instance identity fragment.
   *
   * @param context the context
   * @return the identity fragment
   */
  public static IdentityFragment newInstance(Context context) {
    IdentityFragment fragment = new IdentityFragment();
    SelfCheckInApp.get(context).inject(fragment);
    return fragment;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View rootView = inflater.inflate(R.layout.fragment_identity, container, false);
    ButterKnife.bind(this, rootView);

    final DatePickerBuilder dpb = new DatePickerBuilder()
        .setFragmentManager(getChildFragmentManager())
        .setStyleResId(R.style.BetterPickersDialogFragment_Light)
        .setTargetFragment(IdentityFragment.this);


    birthDayPickerView.setInputType(InputType.TYPE_NULL);
    birthDayPickerView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        dpb.show();
      }
    });

    matchUserDialog = new MaterialDialog.Builder(getActivity())
        .customView(R.layout.found_user_dialog, false)
        .positiveText(R.string.dialog_me_text)
        .negativeText(R.string.dialog_not_me_text)
        .positiveColorRes(R.color.secondaryAccent)
        .cancelable(false)
        .onPositive(new MaterialDialog.SingleButtonCallback() {
          @Override public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
            avatarPath.delete();
            bus.post(new PagerChangeEvent(3));
          }
        })
        .onNegative(new MaterialDialog.SingleButtonCallback() {
          @Override public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
            emailTextView.setText("");
            guest.email = null;
            guest.user_key = null;
          }
        }).build();

    setAvatar();

    homeTownACView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == R.id.next || actionId == EditorInfo.IME_ACTION_NEXT) {
          birthDayPickerView.requestFocus();
          getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
              new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                  dpb.show();
                }
              }, 200);
            }
          });
          return true;
        }
        return false;
      }
    });

    emailTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
      @Override public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
          emailTextView.setError(null);

          enteredEmail = emailTextView.getText().toString();

          // Check for a valid email address.
          if (!TextUtils.isEmpty(enteredEmail)) {
            if (Strings.isValidEmail(enteredEmail)) {
              findUserWithEmail(enteredEmail);
            }
          }
        }
      }
    });

    return rootView;
  }

  @Override public void onResume() {
    super.onResume();
    bus.register(this);
    homeTownACView.setAdapter(new HomeTownAutoCompleteAdapter(getActivity()));
  }

  @Override public void onPause() {
    super.onPause();
    bus.unregister(this);
  }

  @Override public void setUserVisibleHint(boolean isVisibleToUser) {
    super.setUserVisibleHint(isVisibleToUser);
    if (getActivity() != null && isVisibleToUser) {
      bus.post(new BackShouldShowEvent(true));
      setAvatar();
    }
  }

  /**
   * Continue to date.
   */
  @OnClick(R.id.continue_btn)
  public void continueToPassport() {
    if (!isError()) {
      guest.name = enteredFullName;
      guest.email = enteredEmail;
      guest.passportNumber = enteredPassportNumber;

      if (!TextUtils.isEmpty(enteredHomeTown)) {
        guest.city = Strings.getPreStringSplit(enteredHomeTown, "-");
        guest.country = Strings.getPostStringSplit(enteredHomeTown, "-");
      }

      if (dob != null) {
        guest.dob = dob;
      }

      bus.post(new PagerChangeEvent(2));
    }
  }

  /**
   * Is error boolean.
   *
   * @return the boolean
   */
  private boolean isError() {

    fullNameTextView.setError(null);
    emailTextView.setError(null);
    passportTextView.setError(null);
    birthDayPickerView.setError(null);

    boolean cancel = false;
    View focusView = null;

    enteredEmail = emailTextView.getText().toString();
    enteredFullName = fullNameTextView.getText().toString();
    enteredPassportNumber = passportTextView.getText().toString();
    enteredHomeTown = homeTownACView.getText().toString();

    // Check for validation
    if (TextUtils.isEmpty(enteredFullName)) {
      fullNameTextView.setError(getString(R.string.error_field_required));
      focusView = fullNameTextView;
      cancel = true;
    } else if (TextUtils.isEmpty(enteredEmail)) {
      emailTextView.setError(getString(R.string.error_field_required));
      focusView = emailTextView;
      cancel = true;
    } else if (!Strings.isValidEmail(enteredEmail)) {
      emailTextView.setError(getString(R.string.error_invalid_email));
      focusView = emailTextView;
      cancel = true;
    } else if (TextUtils.isEmpty(enteredPassportNumber)) {
      passportTextView.setError(getString(R.string.error_field_required));
      focusView = passportTextView;
      cancel = true;
    } else if (dob != null) {
      if (dob.after(new Date())) {
        birthDayPickerView.setError(getString(R.string.birthday_error));
        focusView = birthDayPickerView;
        cancel = true;
        dob = null;
      }
    }

    if (cancel) {
      if (focusView != null) {
        focusView.requestFocus();
      }
    }

    return cancel;
  }

  @Override public void onDialogDateSet(int reference, int year, int monthOfYear, int dayOfMonth) {
    Calendar calendar = Calendar.getInstance();
    birthDayPickerView.setText(String.format("%d - %d - %d", dayOfMonth, monthOfYear + 1, year));
    calendar.set(year, monthOfYear, dayOfMonth);
    dob = calendar.getTime();
  }

  /**
   * On email conflict.
   *
   * @param event the event
   */
  @Subscribe
  public void onEmailConflict(EmailConflictEvent event) {
    emailTextView.requestFocus();
    emailTextView.setError(getString(R.string.error_conflict_email));
  }

  /**
   * Find user with email.
   *
   * @param enteredEmail the entered email
   */
  private void findUserWithEmail(final String enteredEmail) {
    authenticationService.findUser(enteredEmail, new Callback<FindResponse>() {
      @Override public void success(FindResponse findResponse, Response response) {
        User matchedUser = findResponse.data;
        Timber.d("Found user: %s", matchedUser.toString());

        RelativeLayout dialogView = (RelativeLayout) matchUserDialog.getCustomView();
        if (dialogView != null) {
          ImageView avatar = (ImageView) dialogView.findViewById(R.id.avatar);
          TextView name = (TextView) dialogView.findViewById(R.id.user_name);
          picasso.load(Strings.makeAvatarUrl(matchedUser.doc_key))
              .resize(200, 200).centerCrop()
              .transform(new CircleStrokeTransformation(getActivity(), 0, 0))
              .placeholder(R.drawable.avatar_placeholder).into(avatar);
          name.setText(matchedUser.name);
          matchUserDialog.show();
        }

        guest.user_key = matchedUser.doc_key;
        guest.email = enteredEmail;
      }

      @Override public void failure(RetrofitError error) {

        if (error.getResponse().getStatus() == 401) {
          Timber.d("authentication failed");
        } else {
          Timber.d("Error finding: %s", error.toString());
        }
      }
    });
  }

  /**
   * Sets avatar.
   */
  private void setAvatar() {

    if (avatarPath != null && avatarPath.isSet() && avatarPath.get() != null && avatarTakenView != null) {
      picasso.invalidate(avatarPath.get());
      picasso.load(new File(avatarPath.get())).resize(200, 200).centerCrop()
          .transform(new CircleStrokeTransformation(getActivity(), 0, 0))
          .placeholder(R.drawable.avatar_placeholder).into(avatarTakenView);
    }
  }
}
