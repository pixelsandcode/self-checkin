/*
 * *
 *  * Copyright (c) 2015-2016 www.Tipi.me.
 *  * Created by Ashkan Hesaraki.
 *  * Ashkan.Hesaraki@gmail.com
 *
 */

package me.tipi.self_check_in.ui.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;
import com.drivemode.android.typeface.TypefaceHelper;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.otto.Bus;

import java.util.Calendar;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.tipi.self_check_in.R;
import me.tipi.self_check_in.SelfCheckInApp;
import me.tipi.self_check_in.data.api.ApiConstants;
import me.tipi.self_check_in.data.api.models.Guest;
import me.tipi.self_check_in.ui.FindUserActivity;
import me.tipi.self_check_in.ui.SignUpActivity;
import me.tipi.self_check_in.ui.events.BackShouldShowEvent;
import me.tipi.self_check_in.ui.events.ClaimEvent;
import me.tipi.self_check_in.ui.events.PagerChangeEvent;
import me.tipi.self_check_in.ui.events.SettingShouldShowEvent;
import timber.log.Timber;

public class DateFragment extends Fragment implements CalendarDatePickerDialogFragment.OnDateSetListener {

  public static final String TAG = DateFragment.class.getSimpleName();

  @Inject Bus bus;
  @Inject Guest guest;
  @Inject Tracker tracker;
  @Inject TypefaceHelper typeface;

  @Bind(R.id.check_in_date) EditText checkInDateView;
  @Bind(R.id.nights_number) EditText nightsNumberView;
  @Bind(R.id.reference) EditText referenceTextView;
  @Bind(R.id.check_in_input_layout) TextInputLayout checkInLayout;
  @Bind(R.id.nights_number_input_layout) TextInputLayout nightNumberLayout;
  @Bind(R.id.terms) TextView termsTextView;

  private Calendar checkInDate = null;
  private String dateString;
  private String enteredReference = null;
  private int enteredNights = 0;
  private boolean isLogin;

  private Handler handler = new Handler();
  private Runnable runnable = new Runnable() {
    @Override public void run() {
      startOver();
    }
  };

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
    if (typeface != null) {
      typeface.setTypeface(container, getResources().getString(R.string.font_regular));
    }

    // Check-in date date picker
    checkInDateView.setInputType(InputType.TYPE_NULL);
    Calendar today = Calendar.getInstance();
    checkInDate = today;
    dateString = String.format(Locale.US, "%d - %d - %d", today.get(Calendar.DAY_OF_MONTH), today.get(Calendar.MONTH) + 1, today.get(Calendar.YEAR));
    checkInDateView.setText(dateString);

    final CalendarDatePickerDialogFragment cdp = new CalendarDatePickerDialogFragment()
        .setOnDateSetListener(DateFragment.this)
        .setFirstDayOfWeek(Calendar.SUNDAY)
        .setPreselectedDate(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH))
        .setDoneText("Set")
        .setCancelText("Cancel");

    checkInDateView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        cdp.show(getActivity().getSupportFragmentManager(), DateFragment.class.getSimpleName());
      }
    });

    SpannableString ss = new SpannableString("By tapping 'Next' I agree to the Hostels Terms & Conditions");
    ClickableSpan clickableSpan = new ClickableSpan() {
      @Override
      public void onClick(View textView) {
        if (isLogin) {
          if (getActivity() instanceof SignUpActivity) {
            ((SignUpActivity)getActivity()).showTermsFragment();
          } else {
            bus.post(new PagerChangeEvent(2));
          }
        } else {
          ((SignUpActivity)getActivity()).showTermsFragment();
        }
      }
      @Override
      public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        ds.setUnderlineText(false);
      }
    };
    ss.setSpan(clickableSpan, 41, 59, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

    termsTextView.setText(ss);
    termsTextView.setMovementMethod(LinkMovementMethod.getInstance());
    termsTextView.setHighlightColor(Color.TRANSPARENT);

    return rootView;
  }

  @Override public void onResume() {
    super.onResume();
    bus.register(this);

    if (getActivity() != null) {
      bus.post(new BackShouldShowEvent(true));
      bus.post(new SettingShouldShowEvent(false));
      isLogin = guest.user_key != null && !TextUtils.isEmpty(guest.user_key);

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

  @Override public void setUserVisibleHint(boolean isVisibleToUser) {
    super.setUserVisibleHint(isVisibleToUser);
    if (getActivity() != null && isVisibleToUser) {
      bus.post(new BackShouldShowEvent(true));
      bus.post(new SettingShouldShowEvent(false));
      isLogin = guest.user_key != null && !TextUtils.isEmpty(guest.user_key);

      handler.postDelayed(runnable, ApiConstants.START_OVER_TIME);
    }
  }

  /**
   * Date submit clicked.
   */
  @OnClick(R.id.submit_btn)
  public void dateSubmitClicked() {

    if (!isError()) {
      enteredNights = Integer.parseInt(nightsNumberView.getText().toString());
      guest.checkInDate = checkInDate.getTime();
      checkInDate.add(Calendar.DAY_OF_MONTH, enteredNights);
      guest.checkOutDate = checkInDate.getTime();

      guest.referenceCode = null;

      if (isLogin) {
        bus.post(new ClaimEvent());
      } else {
        if (getActivity() instanceof SignUpActivity) {
          Timber.w("User entered %d nights and going to submit method", enteredNights);
          ((SignUpActivity)getActivity()).submit();
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

    checkInLayout.setError(null);
    nightNumberLayout.setError(null);
    //enteredReference = referenceTextView.getText().toString().trim();

    boolean cancel = false;
    View focusView = null;
    Calendar compare = Calendar.getInstance();
    compare.add(Calendar.HOUR, -1);

    if (checkInDate == null) {
      checkInLayout.setErrorEnabled(true);
      checkInLayout.setError(getString(R.string.error_field_required));
      cancel = true;
      checkInDate = null;
    } else if (nightsNumberView.getText() == null || TextUtils.isEmpty(nightsNumberView.getText().toString())) {
      nightNumberLayout.setError(getString(R.string.error_field_required));
      cancel = true;
      enteredNights = 0;
    } else if (checkInDate.before(compare)) {
      checkInLayout.setError(getString(R.string.error_less_than_today));
      cancel = true;
      checkInDate = null;
    } else if (nightsNumberView.getText().toString().trim().length() > 3) {
      nightNumberLayout.setError(getString(R.string.error_check_out_more_than_one_year));
      cancel = true;
      focusView = nightsNumberView;
      enteredNights = 0;
    } else if (Integer.parseInt(nightsNumberView.getText().toString()) <= 0) {
      nightNumberLayout.setError(getString(R.string.error_check_out_before_check_in));
      cancel = true;
      focusView = nightsNumberView;
      enteredNights = 0;
    } else if (Integer.parseInt(nightsNumberView.getText().toString()) > 365) {
      nightNumberLayout.setError(getString(R.string.error_check_out_more_than_one_year));
      cancel = true;
      focusView = nightsNumberView;
      enteredNights = 0;
    }

    if (cancel) {
      if (focusView != null) {
        focusView.requestFocus();
      }
    }

    return cancel;
  }

  @Override
  public void onDateSet(CalendarDatePickerDialogFragment dialog, int year, int monthOfYear, int dayOfMonth) {
    Calendar calendar = Calendar.getInstance();
    calendar.set(year, monthOfYear, dayOfMonth);
    dateString = String.format(Locale.US, "%d - %d - %d", calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR));
    checkInDateView.setText(dateString);
    checkInDate = calendar;
  }

  private void startOver() {
    if (getActivity() != null && getActivity() instanceof SignUpActivity) {
      ((SignUpActivity)getActivity()).reset();
    } else if (getActivity() != null){
      ((FindUserActivity)getActivity()).reset();
    }
  }
}
