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
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.doomonafireball.betterpickers.datepicker.DatePickerBuilder;
import com.doomonafireball.betterpickers.datepicker.DatePickerDialogFragment;
import com.f2prateek.rx.preferences.Preference;
import com.squareup.otto.Bus;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.Calendar;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.tipi.self_check_in.R;
import me.tipi.self_check_in.SelfCheckInApp;
import me.tipi.self_check_in.data.api.ApiConstants;
import me.tipi.self_check_in.data.api.models.Guest;
import me.tipi.self_check_in.ui.events.BackShouldShowEvent;
import me.tipi.self_check_in.ui.events.ClaimEvent;
import me.tipi.self_check_in.ui.events.SubmitEvent;
import me.tipi.self_check_in.ui.transform.CircleStrokeTransformation;
import me.tipi.self_check_in.util.Strings;

public class DateFragment extends Fragment implements DatePickerDialogFragment.DatePickerDialogHandler {

  @Inject Picasso picasso;
  @Inject Bus bus;
  @Inject @Named(ApiConstants.AVATAR) Preference<String> avatarPath;
  @Inject Guest guest;

  @Bind(R.id.taken_avatar) ImageView avatarTakenView;
  @Bind(R.id.check_in_date) EditText checkInDateView;
  @Bind(R.id.nights_number) EditText nightsNumberView;
  @Bind(R.id.reference) EditText referenceTextView;

  public Calendar checkInDate = null;
  public String dateString;
  public String enteredReference;
  public int enteredNights = 0;

  /**
   * Instantiates a new Date fragment.
   */
  public DateFragment() {
    // Required empty public constructor
  }

  /**
   * New instance date fragment.
   *
   * @param context the context
   * @return the date fragment
   */
  public static DateFragment newInstance(Context context) {
    DateFragment fragment = new DateFragment();
    SelfCheckInApp.get(context).inject(fragment);
    return fragment;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View rootView = inflater.inflate(R.layout.fragment_date, container, false);
    ButterKnife.bind(this, rootView);

    // Fix avatar according to process
    setAvatar();

    // Check-in date date picker
    checkInDateView.setInputType(InputType.TYPE_NULL);
    Calendar today = Calendar.getInstance();
    checkInDate = today;
    dateString = String.format("%d - %d - %d", today.get(Calendar.DAY_OF_MONTH), today.get(Calendar.MONTH) + 1, today.get(Calendar.YEAR));
    checkInDateView.setText(dateString);

    checkInDateView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        DatePickerBuilder fromDb = new DatePickerBuilder()
            .setFragmentManager(getChildFragmentManager())
            .setStyleResId(R.style.BetterPickersDialogFragment)
            .setTargetFragment(DateFragment.this)
            .setReference(10);
        fromDb.show();
      }
    });

    nightsNumberView.requestFocus();
    return rootView;
  }

  @Override public void onResume() {
    super.onResume();
    bus.register(this);
  }

  @Override public void onPause() {
    super.onPause();
    bus.unregister(this);
  }

  /**
   * Date submit clicked.
   */
  @OnClick(R.id.submit_btn)
  public void dateSubmitClicked() {
    if (!isError()) {
      guest.checkInDate = checkInDate.getTime();
      checkInDate.add(Calendar.DAY_OF_MONTH, enteredNights);
      guest.checkOutDate = checkInDate.getTime();
      guest.referenceCode = enteredReference;
      enteredNights = Integer.parseInt(nightsNumberView.getText().toString());

      if (guest.user_key != null && !TextUtils.isEmpty(guest.user_key)) {
        bus.post(new ClaimEvent());
      } else {
        bus.post(new SubmitEvent());
      }
    }
  }

  /**
   * Is error boolean.
   *
   * @return the boolean
   */
  private boolean isError() {

    checkInDateView.setError(null);
    nightsNumberView.setError(null);
    referenceTextView.setError(null);
    enteredReference = referenceTextView.getText().toString();

    boolean cancel = false;
    View focusView = null;
    Calendar compare = Calendar.getInstance();
    compare.add(Calendar.HOUR, -1);

    if (checkInDate == null) {
      checkInDateView.setError(getString(R.string.error_field_required));
      cancel = true;
      checkInDate = null;
    } else if (nightsNumberView.getText() == null || TextUtils.isEmpty(nightsNumberView.getText().toString())) {
      nightsNumberView.setError(getString(R.string.error_field_required));
      cancel = true;
      enteredNights = 0;
    } else if (checkInDate.before(compare)) {
      checkInDateView.setError(getString(R.string.error_less_than_today));
      cancel = true;
      checkInDate = null;
    } else if (Integer.parseInt(nightsNumberView.getText().toString()) <= 0) {
      nightsNumberView.setError(getString(R.string.error_check_out_before_check_in));
      cancel = true;
      focusView = nightsNumberView;
      enteredNights = 0;
    } else if (TextUtils.isEmpty(enteredReference)) {
      referenceTextView.setError(getString(R.string.error_field_required));
      focusView = referenceTextView;
      cancel = true;
    }

    if (cancel) {
      if (focusView != null) {
        focusView.requestFocus();
      }
    }



    return cancel;
  }

  @Override public void setUserVisibleHint(boolean isVisibleToUser) {
    super.setUserVisibleHint(isVisibleToUser);
    if (getActivity() != null && isVisibleToUser) {
      if (guest.user_key != null && !TextUtils.isEmpty(guest.user_key)) {
        bus.post(new BackShouldShowEvent(false));
      } else {
        bus.post(new BackShouldShowEvent(true));
      }

      if (nightsNumberView != null) {
        nightsNumberView.requestFocus();
      }

      setAvatar();
    }
  }

  @Override public void onDialogDateSet(int reference, int year, int monthOfYear, int dayOfMonth) {
    Calendar calendar = Calendar.getInstance();
    calendar.set(year, monthOfYear, dayOfMonth);
    dateString = String.format("%d - %d - %d", calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR));
    if (reference == 10) {
      checkInDateView.setText(dateString);
      checkInDate = calendar;
    }
  }

  /**
   * Sets avatar.
   */
  private void setAvatar() {

    if (avatarTakenView != null) {
      if (avatarPath != null && avatarPath.isSet() && avatarPath.get() != null) {
        picasso.invalidate(avatarPath.get());
        picasso.load(new File(avatarPath.get())).resize(200, 200).centerCrop()
            .transform(new CircleStrokeTransformation(getActivity(), 0, 0))
            .placeholder(R.drawable.avatar_placeholder).into(avatarTakenView);
      } else if (guest.user_key != null && !TextUtils.isEmpty(guest.user_key)) {
        picasso.load(Strings.makeAvatarUrl(guest.user_key)).resize(200, 200).centerCrop()
            .transform(new CircleStrokeTransformation(getActivity(), 0, 0))
            .placeholder(R.drawable.avatar_placeholder).into(avatarTakenView);
      }
    }
  }
}
