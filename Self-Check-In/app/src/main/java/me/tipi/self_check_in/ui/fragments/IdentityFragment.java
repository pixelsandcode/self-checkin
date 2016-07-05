/*
 * *
 *  * Copyright (c) 2015-2016 www.Tipi.me.
 *  * Created by Ashkan Hesaraki.
 *  * Ashkan.Hesaraki@gmail.com
 *
 */

package me.tipi.self_check_in.ui.fragments;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.drivemode.android.typeface.TypefaceHelper;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import me.tipi.self_check_in.ui.FindUserActivity;
import me.tipi.self_check_in.ui.SignUpActivity;
import me.tipi.self_check_in.ui.adapters.HomeTownAutoCompleteAdapter;
import me.tipi.self_check_in.ui.events.BackShouldShowEvent;
import me.tipi.self_check_in.ui.events.EmailConflictEvent;
import me.tipi.self_check_in.ui.events.PagerChangeEvent;
import me.tipi.self_check_in.ui.events.SettingShouldShowEvent;
import me.tipi.self_check_in.ui.transform.CircleStrokeTransformation;
import me.tipi.self_check_in.util.Strings;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;

public class IdentityFragment extends Fragment {

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
  @Bind(R.id.hometown_input_layout) TextInputLayout homeTownLayout;
  @Bind(R.id.radioSex) RadioGroup radioGroup;


  public Date dob = null;
  public String enteredEmail;
  public String enteredFullName;
  public String enteredHomeTown;
  MaterialDialog matchUserDialog;
  private boolean hasSelectedHometown;

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

    birthDayPickerView.setInputType(InputType.TYPE_NULL);

    birthDayPickerView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
      @Override public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
          if (birthDayPickerView.getText().toString().equals("")) {
            showDobDialog();
          }
        }
      }
    });

    birthDayPickerView.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        showDobDialog();
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
            guest.guest_key = "0";
            bus.post(new PagerChangeEvent(4));
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

    homeTownACView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        hasSelectedHometown = true;
        homeTownLayout.setError(null);
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
      //bus.post(new RefreshShouldShowEvent(true));
      bus.post(new SettingShouldShowEvent(false));

      new Handler().postDelayed(new Runnable() {
        @Override public void run() {
          startOver();
        }
      }, ApiConstants.START_OVER_TIME);
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

      if (!TextUtils.isEmpty(enteredHomeTown) && hasSelectedHometown && enteredHomeTown.contains("-")) {
        guest.city = Strings.getPreStringSplit(enteredHomeTown, "-").trim();
        guest.country = Strings.getPostStringSplit(enteredHomeTown, "-").trim();
      }

      if (dob != null && !TextUtils.isEmpty(birthDayPickerView.getText().toString().trim())) {
        guest.dob = dob;
      }

      if (radioGroup.getCheckedRadioButtonId() == R.id.radioFemale) {
        guest.gender = 0;
      } else {
        guest.gender = 1;
      }

      bus.post(new PagerChangeEvent(3));
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
    nameLayout.setError(null);
    emailLayout.setError(null);
    birthdayLayout.setError(null);

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
    } else if (TextUtils.isEmpty(enteredHomeTown)) {
      homeTownACView.setError(getString(R.string.error_field_required));
      focusView = homeTownACView;
      cancel = true;
    } else if (!hasSelectedHometown) {
      homeTownLayout.setError(getString(R.string.home_town_error));
      focusView = homeTownACView;
      cancel = true;
      homeTownACView.setText("");
    } else if (dob == null || TextUtils.isEmpty(birthDayPickerView.getText().toString().trim())) {
      birthdayLayout.setError(getString(R.string.error_field_required));
      focusView = birthDayPickerView;
      cancel = true;
    } else if (dob != null && !TextUtils.isEmpty(birthDayPickerView.getText().toString().trim())) {
      if (dob.after(new Date())) {
        birthdayLayout.setError(getString(R.string.birthday_error));
        focusView = birthDayPickerView;
        cancel = true;
        birthDayPickerView.setText("");
        dob = null;
      }

      if (dob != null && dob.before(cal.getTime())) {
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

  private void showDobDialog() {
    DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), android.R.style.Theme_Holo_Light_Dialog,
        new DatePickerDialog.OnDateSetListener() {
          @Override
          public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            birthdayLayout.setError(null);
            Calendar calendar = Calendar.getInstance();
            birthDayPickerView.setText(String.format(Locale.US, "%d - %d - %d", dayOfMonth, monthOfYear + 1, year));
            calendar.set(year, monthOfYear, dayOfMonth);
            dob = calendar.getTime();
          }
        }, 1985, 6, 15);
    DatePicker datePicker = datePickerDialog.getDatePicker();
    datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    styleDatePicker(datePicker);
    datePickerDialog.show();
  }

  private void styleDatePicker(DatePicker datePicker) {
    datePicker.setCalendarViewShown(false);
    datePicker.setSpinnersShown(true);
    LinearLayout llFirst = (LinearLayout) datePicker.getChildAt(0);
    LinearLayout llSecond = (LinearLayout) llFirst.getChildAt(0);
    for (int i = 0; i < llSecond.getChildCount(); i++) {
      NumberPicker picker = (NumberPicker) llSecond.getChildAt(i); // Numberpickers in llSecond
      Field[] pickerFields = NumberPicker.class.getDeclaredFields();
      for (Field pf : pickerFields) {
        if (pf.getName().equals("mSelectionDivider")) {
          pf.setAccessible(true);
          try {
            pf.set(picker, getResources().getDrawable(R.drawable.picker_divider));
          } catch (IllegalArgumentException e) {
            e.printStackTrace();
          } catch (Resources.NotFoundException e) {
            e.printStackTrace();
          } catch (IllegalAccessException e) {
            e.printStackTrace();
          }
          break;
        }
      }
    }
  }

  public static boolean validateFullName(String fullName) {

    String regex = "^[\\p{L} .'-]+$";
    Pattern pattern = Pattern.compile(regex,Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(fullName);
    return matcher.find();

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

  private void startOver() {
    if (getActivity() != null && getActivity() instanceof SignUpActivity) {
      ((SignUpActivity)getActivity()).reset();
    } else if (getActivity() != null){
      ((FindUserActivity)getActivity()).reset();
    }
  }
}
