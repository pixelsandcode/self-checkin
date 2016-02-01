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
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.doomonafireball.betterpickers.datepicker.DatePickerBuilder;
import com.doomonafireball.betterpickers.datepicker.DatePickerDialogFragment;
import com.drivemode.android.typeface.TypefaceHelper;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.tipi.self_check_in.R;
import me.tipi.self_check_in.SelfCheckInApp;
import me.tipi.self_check_in.data.api.AuthenticationService;
import me.tipi.self_check_in.data.api.models.FindResponse;
import me.tipi.self_check_in.data.api.models.Guest;
import me.tipi.self_check_in.data.api.models.User;
import me.tipi.self_check_in.ui.adapters.HomeTownAutoCompleteAdapter;
import me.tipi.self_check_in.ui.events.BackShouldShowEvent;
import me.tipi.self_check_in.ui.events.EmailConflictEvent;
import me.tipi.self_check_in.ui.events.PagerChangeEvent;
import me.tipi.self_check_in.ui.events.RefreshShouldShowEvent;
import me.tipi.self_check_in.ui.events.SettingShouldShowEvent;
import me.tipi.self_check_in.ui.transform.CircleStrokeTransformation;
import me.tipi.self_check_in.util.Strings;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;

public class IdentityFragment extends Fragment implements DatePickerDialogFragment.DatePickerDialogHandler {

  @Inject Picasso picasso;
  @Inject Bus bus;
  @Inject Guest guest;
  @Inject AuthenticationService authenticationService;
  @Inject Tracker tracker;
  @Inject TypefaceHelper typeface;

  @Bind(R.id.full_name) EditText fullNameTextView;
  @Bind(R.id.email) EditText emailTextView;
  @Bind(R.id.home_town) AutoCompleteTextView homeTownACView;
  @Bind(R.id.birthday) EditText birthDayPickerView;
  @Bind(R.id.name_input_layout) TextInputLayout nameLayout;
  @Bind(R.id.email_input_layout) TextInputLayout emailLayout;
  @Bind(R.id.birthday_input_layout) TextInputLayout birthdayLayout;


  public Date dob = null;
  public String enteredEmail;
  public String enteredFullName;
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
    typeface.setTypeface(container, getResources().getString(R.string.font_regular));

    final DatePickerBuilder dpb = new DatePickerBuilder()
        .setFragmentManager(getChildFragmentManager())
        .setStyleResId(R.style.BetterPickersDialogFragment_Light)
        .setTargetFragment(IdentityFragment.this);

    birthDayPickerView.setInputType(InputType.TYPE_NULL);

    birthDayPickerView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
      @Override public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
          if (birthDayPickerView.getText().toString().equals("")) {
            dpb.show();
          }
        }
      }
    });

    birthDayPickerView.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
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
          @Override
          public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
            tracker.send(new HitBuilders.EventBuilder("Matched User", "Choose me").build());
            bus.post(new PagerChangeEvent(2));
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

    emailTextView.addTextChangedListener(new TextWatcher() {
      @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {

      }

      @Override public void onTextChanged(CharSequence s, int start, int before, int count) {

      }

      @Override public void afterTextChanged(Editable s) {
        emailLayout.setError(null);

        // Check for a valid email address.
        if (!TextUtils.isEmpty(s.toString())) {
          if (Strings.isValidEmail(s.toString())) {
            findUserWithEmail(s.toString());
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
      bus.post(new BackShouldShowEvent(true));
      bus.post(new RefreshShouldShowEvent(true));
      bus.post(new SettingShouldShowEvent(false));
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

      if (!TextUtils.isEmpty(enteredHomeTown)) {
        guest.city = Strings.getPreStringSplit(enteredHomeTown, "-");
        guest.country = Strings.getPostStringSplit(enteredHomeTown, "-");
      }

      if (dob != null && !TextUtils.isEmpty(birthDayPickerView.getText().toString().trim())) {
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

    nameLayout.setErrorEnabled(false);
    emailLayout.setErrorEnabled(false);
    birthdayLayout.setErrorEnabled(false);

    boolean cancel = false;
    View focusView = null;

    enteredEmail = emailTextView.getText().toString();
    enteredFullName = fullNameTextView.getText().toString().trim();
    enteredHomeTown = homeTownACView.getText().toString().trim();

    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.YEAR, 1900);


    // Check for validation
    if (TextUtils.isEmpty(enteredFullName)) {
      nameLayout.setError(getString(R.string.error_field_required));
      focusView = fullNameTextView;
      cancel = true;
    } else if (!validateFullName(enteredFullName)) {
      nameLayout.setError(getString(R.string.error_invalid_full_name));
      focusView = fullNameTextView;
      cancel = true;
    } else if (TextUtils.isEmpty(enteredEmail)) {
      emailLayout.setError(getString(R.string.error_field_required));
      focusView = emailTextView;
      cancel = true;
    } else if (!Strings.isValidEmail(enteredEmail)) {
      emailLayout.setError(getString(R.string.error_invalid_email));
      focusView = emailTextView;
      cancel = true;
    } else if (dob != null) {
      if (dob.after(new Date())) {
        birthdayLayout.setError(getString(R.string.birthday_error));
        focusView = birthDayPickerView;
        cancel = true;
        birthDayPickerView.setText("");
        dob = null;
      }

      if (dob.before(cal.getTime())) {
        birthdayLayout.setError(getString(R.string.birthday_error_old));
        focusView = birthDayPickerView;
        cancel = true;
        birthDayPickerView.setText("");
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

  public static boolean validateFullName(String fullName) {

    String regex = "^[\\p{L} .'-]+$";
    Pattern pattern = Pattern.compile(regex,Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(fullName);
    return matcher.find();

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
    emailLayout.setError(getString(R.string.error_conflict_email));
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
        guest.user_key = matchedUser.doc_key;
        guest.email = enteredEmail;
        Timber.d("Found user: %s", matchedUser.toString());

        if (getActivity() != null) {
          RelativeLayout dialogView = (RelativeLayout) matchUserDialog.getCustomView();
          if (dialogView != null) {
            ImageView avatar = (ImageView) dialogView.findViewById(R.id.avatar);
            TextView name = (TextView) dialogView.findViewById(R.id.user_name);
            picasso.load(Strings.makeAvatarUrl(matchedUser.doc_key))
                .resize(200, 200).centerCrop()
                .transform(new CircleStrokeTransformation(getActivity(), 0, 0))
                .placeholder(R.drawable.avatar_placeholder)
                .error(R.drawable.fail_photo).into(avatar);
            name.setText(matchedUser.name);
            matchUserDialog.show();
          }
        }
      }

      @Override public void failure(RetrofitError error) {

        if (error.getResponse() != null && error.getResponse().getStatus() == 401) {
          Timber.d("authentication failed");
        } else {
          Timber.d("Error finding: %s", error.toString());
        }
      }
    });
  }
}
