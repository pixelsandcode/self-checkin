

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
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.drivemode.android.typeface.TypefaceHelper;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.microblink.recognizers.RecognitionResults;
import com.microblink.recognizers.blinkid.mrtd.MRTDRecognitionResult;
import com.squareup.otto.Bus;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import me.tipi.self_check_in.data.api.models.Guest;
import me.tipi.self_check_in.ui.SignUpActivity;
import me.tipi.self_check_in.ui.adapters.HomeTownAutoCompleteAdapter;
import me.tipi.self_check_in.ui.events.BackShouldShowEvent;
import me.tipi.self_check_in.ui.events.SettingShouldShowEvent;
import me.tipi.self_check_in.util.Strings;
import timber.log.Timber;

public class IdentityFragment extends Fragment {

  public static final String TAG = IdentityFragment.class.getSimpleName();
  public static final String OCR_RESULTS = "orc_results";

  @Inject
  Bus bus;
  @Inject
  Guest guest;
  @Inject
  Tracker tracker;
  @Inject
  TypefaceHelper typeface;

  @Bind(R.id.title)
  TextView titleTextView;
  @Bind(R.id.your_country)
  TextView yourCountryLabel;
  @Bind(R.id.full_name)
  EditText fullNameTextView;
  @Bind(R.id.home_town)
  AutoCompleteTextView homeTownACView;
  @Bind(R.id.birthday)
  EditText birthDayPickerView;
  @Bind(R.id.name_input_layout)
  TextInputLayout nameLayout;
  @Bind(R.id.birthday_input_layout)
  TextInputLayout birthdayLayout;
  @Bind(R.id.hometown_input_layout)
  TextInputLayout homeTownLayout;
  @Bind(R.id.radioSex)
  RadioGroup radioGroup;
  @Bind(R.id.passport)
  EditText passportEditText;
  @Bind(R.id.passport_input_layout)
  TextInputLayout passportLayout;
  @Bind(R.id.passport_label)
  TextView passportLabel;

  private Date dob = null;
  private String enteredFullName;
  private String enteredHomeTown;
  private String enteredPassport;
  private boolean hasSelectedHometown;

  private Handler handler = new Handler();
  private Runnable runnable = new Runnable() {
    @Override
    public void run() {
      startOver();
    }
  };

  private RecognitionResults results = null;

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
  public static IdentityFragment newInstance(Context context, RecognitionResults results) {
    IdentityFragment fragment = new IdentityFragment();
    if (results != null) {
      Bundle args = new Bundle();
      args.putParcelable(OCR_RESULTS, results);
      fragment.setArguments(args);
    }

    SelfCheckInApp.get(context).inject(fragment);
    return fragment;
  }

  @Override
  public void onCreate(
      @Nullable
          Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      results = getArguments().getParcelable(OCR_RESULTS);
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View rootView = inflater.inflate(R.layout.fragment_identity, container, false);
    ButterKnife.bind(this, rootView);
    typeface.setTypeface(container, getResources().getString(R.string.font_regular));

    fillDetailsFromPassport();

    if (results == null) {
      titleTextView.setText(R.string.enter_details);
      passportLabel.setText(R.string.license_no);
    } else {
      titleTextView.setText(R.string.your_details);
      passportLabel.setText(R.string.passport_no);
      yourCountryLabel.setText(R.string.your_country);
      homeTownACView.addTextChangedListener(new MyTextWatcher());

    }

    birthDayPickerView.setInputType(InputType.TYPE_NULL);

    birthDayPickerView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
      @Override
      public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
          if (birthDayPickerView.getText().toString().equals("")) {
            showDobDialog();
          }
        }
      }
    });

    birthDayPickerView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        showDobDialog();
      }
    });

    homeTownACView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        hasSelectedHometown = true;
        homeTownLayout.setError(null);
      }
    });
    setCountry();
    return rootView;
  }

  @Override
  public void onResume() {
    super.onResume();
    bus.register(this);

    homeTownACView.setAdapter(new HomeTownAutoCompleteAdapter(getActivity()));
    homeTownACView.setThreshold(1);


    if (getActivity() != null) {
      bus.post(new BackShouldShowEvent(true));
      bus.post(new SettingShouldShowEvent(false));

      handler.postDelayed(runnable, ApiConstants.START_OVER_TIME);
    }

    tracker.setScreenName(getClass().getSimpleName());
    tracker.send(new HitBuilders.ScreenViewBuilder().build());
  }

  @Override
  public void onPause() {
    super.onPause();
    bus.unregister(this);

  }

  @Override
  public void onStop() {
    super.onStop();
    handler.removeCallbacks(runnable);
  }

  /**
   * Continue to date.
   */
  @OnClick(R.id.continue_btn)
  public void continueToPassport() {
    if (!isError()) {
      guest.name = enteredFullName;
      guest.passportNumber = enteredPassport;

      if (!TextUtils.isEmpty(enteredHomeTown) && hasSelectedHometown) {
        if (enteredHomeTown.contains("-")) {
          guest.city = Strings.getPreStringSplit(enteredHomeTown, "-").trim();
          guest.country = Strings.getPostStringSplit(enteredHomeTown, "-").trim();
        } else {
          guest.country = enteredHomeTown;
          guest.city = null;

        }
      }

      if (dob != null && !TextUtils.isEmpty(birthDayPickerView.getText().toString().trim())) {
        guest.dob = dob;
      }

      if (radioGroup.getCheckedRadioButtonId() == R.id.radioFemale) {
        guest.gender = 0;
      } else {
        guest.gender = 1;
      }

      Timber.w("We have identity info going to avatar fragment");
      ((SignUpActivity) getActivity()).showAvatarFragment();
    }
  }

  /**
   * Is error boolean.
   *
   * @return the boolean
   */

  private boolean isError() {

    nameLayout.setErrorEnabled(false);
    birthdayLayout.setErrorEnabled(false);
    nameLayout.setError(null);
    birthdayLayout.setError(null);
    passportLayout.setError(null);

    boolean cancel = false;
    View focusView = null;

    enteredFullName = fullNameTextView.getText().toString().trim();
    enteredHomeTown = homeTownACView.getText().toString().trim();
    enteredPassport = passportEditText.getText().toString().trim();

    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.YEAR, 1900);

    // Check for validation
    if (TextUtils.isEmpty(enteredFullName)) {
      nameLayout.setError(getString(R.string.error_field_required));
      focusView = fullNameTextView;
      cancel = true;
    } else if (!checkIfHasSpace(enteredFullName)) {
      nameLayout.setError(getString(R.string.error_invalid_name_count));
      focusView = fullNameTextView;
      cancel = true;
    } else if (!validateFullName(enteredFullName)) {
      nameLayout.setError(getString(R.string.error_invalid_full_name));
      focusView = fullNameTextView;
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
    } else if (TextUtils.isEmpty(enteredPassport)) {
      passportLayout.setError(getString(R.string.error_field_required));
      focusView = passportEditText;
      cancel = true;
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
      NumberPicker picker = (NumberPicker) llSecond.getChildAt(i); // NumberPickers in llSecond
      Field[] pickerFields = NumberPicker.class.getDeclaredFields();
      for (Field pf : pickerFields) {
        if (pf.getName().equals("mSelectionDivider")) {
          pf.setAccessible(true);
          try {
            pf.set(picker, ContextCompat.getDrawable(getActivity(), R.drawable.picker_divider));
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

  private boolean validateFullName(String fullName) {

    String regex = "^[\\p{L} .'-]+$";
    Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(fullName);
    return matcher.find();

  }

  private boolean checkIfHasSpace(String fullName) {
    return fullName.contains(" ");
  }

  private void fillDetailsFromPassport() {
    if (results != null && results.getRecognitionResults() != null && results.getRecognitionResults()[0] != null) {
      MRTDRecognitionResult mrtdRecognitionResult = (MRTDRecognitionResult) results.getRecognitionResults()[0];
      if (mrtdRecognitionResult != null) {
        fullNameTextView.setText(String.format("%s %s", mrtdRecognitionResult.getSecondaryId(), mrtdRecognitionResult.getPrimaryId()));

        // Date Of Birth
        Calendar calendar = Calendar.getInstance();
        String bornDate = mrtdRecognitionResult.getDateOfBirth();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMdd");
        Date date = null;
        try {
          date = simpleDateFormat.parse(bornDate);
        } catch (ParseException e) {
          e.printStackTrace();
        }
        calendar.setTime(date);
        String bDateString = String.format(Locale.US, "%d - %d - %d", calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR));
        birthDayPickerView.setText(bDateString);
        dob = calendar.getTime();

        // Gender
        if (mrtdRecognitionResult.getSex() != null && mrtdRecognitionResult.getSex().equals("M")) {
          radioGroup.check(R.id.radioMale);
        } else {
          radioGroup.check(R.id.radioFemale);
        }

        passportEditText.setText(mrtdRecognitionResult.getDocumentNumber());
      }
    }
  }

  private void startOver() {
    if (getActivity() != null && getActivity() instanceof SignUpActivity) {
      ((SignUpActivity) getActivity()).reset();
    }
  }

  private String jsonConutries() {
    String json = null;
    try {
      InputStream is = getActivity().getAssets().open("countries.json");
      int size = is.available();
      byte[] buffer = new byte[size];
      is.read(buffer);
      is.close();
      json = new String(buffer, "UTF-8");
    } catch (IOException ex) {
      ex.printStackTrace();
      return null;
    }
    return json;
  }

  private String country(String country) {
    try {
      JSONObject jsonObject = new JSONObject(jsonConutries());
      return jsonObject.getString(country);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return "";
  }

  private void setCountry() {
    if (results != null) {
      {
        hasSelectedHometown = true;
        homeTownLayout.setError(null);
        MRTDRecognitionResult mrtdRecognitionResult = (MRTDRecognitionResult) results.getRecognitionResults()[0];
        homeTownACView.setText(country(mrtdRecognitionResult.getNationality()
        ));

      }
    }
  }

  private class MyTextWatcher implements TextWatcher {

    private MyTextWatcher() {
    }

    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
      checkCountry();
    }

    public void afterTextChanged(Editable editable) {

    }
  }

  private void checkCountry() {
    if (homeTownACView.getText().toString().equals(""))
      yourCountryLabel.setText(R.string.which_city_are_you_from);
  }
}