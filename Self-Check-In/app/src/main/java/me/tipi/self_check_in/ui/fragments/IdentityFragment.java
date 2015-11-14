package me.tipi.self_check_in.ui.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;

import com.codetroopers.betterpickers.datepicker.DatePickerBuilder;
import com.codetroopers.betterpickers.datepicker.DatePickerDialogFragment;
import com.f2prateek.rx.preferences.Preference;
import com.squareup.otto.Bus;
import com.squareup.picasso.Picasso;

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
import me.tipi.self_check_in.data.api.models.Guest;
import me.tipi.self_check_in.ui.adapters.HomeTownAutoCompleteAdapter;
import me.tipi.self_check_in.ui.events.BackShouldShowEvent;
import me.tipi.self_check_in.ui.events.PagerChangeEvent;
import me.tipi.self_check_in.ui.transform.CircleStrokeTransformation;
import me.tipi.self_check_in.util.Strings;

public class IdentityFragment extends Fragment implements DatePickerDialogFragment.DatePickerDialogHandler {

  @Inject Picasso picasso;
  @Inject Bus bus;
  @Inject @Named(ApiConstants.AVATAR) Preference<String> avatarPath;
  @Inject Guest guest;

  @Bind(R.id.taken_avatar) ImageView avatarTakenView;
  @Bind(R.id.full_name) EditText fullNameTextView;
  @Bind(R.id.email) EditText emailTextView;
  @Bind(R.id.reference) EditText referenceTextView;
  @Bind(R.id.home_town) AutoCompleteTextView homeTownACView;
  @Bind(R.id.birthday) EditText birthDayPickerView;
  @Bind(R.id.passport) EditText passportTextView;

  public Date dob = null;
  public String enteredEmail;
  public String enteredFullName;
  public String enteredReference;
  public String enteredPassportNumber;
  public String enteredHomeTown;


  public IdentityFragment() {
    // Required empty public constructor
  }

  public static IdentityFragment newInstance(Context context) {
    IdentityFragment fragment = new IdentityFragment();
    SelfCheckInApp.get(context).inject(fragment);
    return fragment;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View rootView = inflater.inflate(R.layout.fragment_identity, container, false);
    ButterKnife.bind(this, rootView);


    if (avatarPath.isSet() && avatarPath.get() != null) {
      picasso.invalidate(avatarPath.get());
    }

    birthDayPickerView.setInputType(InputType.TYPE_NULL);
    birthDayPickerView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        DatePickerBuilder dpb = new DatePickerBuilder()
            .setFragmentManager(getChildFragmentManager())
            .setStyleResId(R.style.BetterPickersDialogFragment)
            .setTargetFragment(IdentityFragment.this);
        dpb.show();
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
      if (avatarPath.isSet() && avatarPath.get() != null) {
        picasso.load(avatarPath.get()).resize(200, 200).centerCrop()
            .transform(new CircleStrokeTransformation(getActivity(), 0, 0))
            .placeholder(R.drawable.avatar_placeholder).into(avatarTakenView);
      }
    }
  }

  @OnClick(R.id.continue_btn)
  public void continueToDate() {
    if (!isError()) {
      guest.name = enteredFullName;
      guest.email = enteredEmail;
      guest.referenceCode = enteredReference;
      guest.passportNumber = enteredPassportNumber;

      if (TextUtils.isEmpty(enteredHomeTown)) {
        guest.city = Strings.getPreStringSplit(enteredHomeTown, "-");
        guest.country = Strings.getPostStringSplit(enteredHomeTown, "-");
      }

      if (dob != null) {
        guest.dob= dob;
      }

      bus.post(new PagerChangeEvent(2));
    }
  }

  private boolean isError() {

    fullNameTextView.setError(null);
    emailTextView.setError(null);
    passportTextView.setError(null);
    referenceTextView.setError(null);
    birthDayPickerView.setError(null);

    boolean cancel = false;
    View focusView = null;

    enteredEmail = emailTextView.getText().toString();
    enteredFullName = fullNameTextView.getText().toString();
    enteredReference = referenceTextView.getText().toString();
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
    } else if (TextUtils.isEmpty(enteredReference)) {
       referenceTextView.setError(getString(R.string.error_field_required));
       focusView = referenceTextView;
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
    birthDayPickerView.setText(String.format("%d - %d - %d", dayOfMonth, monthOfYear, year));
    calendar.set(year, monthOfYear, dayOfMonth);
    dob = calendar.getTime();
  }
}
