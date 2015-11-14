package me.tipi.self_check_in.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import me.tipi.self_check_in.ui.events.BackShouldShowEvent;
import me.tipi.self_check_in.ui.events.SubmitEvent;
import me.tipi.self_check_in.ui.transform.CircleStrokeTransformation;

public class DateFragment extends Fragment implements DatePickerDialogFragment.DatePickerDialogHandler {

  @Inject Picasso picasso;
  @Inject Bus bus;
  @Inject @Named(ApiConstants.AVATAR) Preference<String> avatarPath;
  @Inject Guest guest;

  @Bind(R.id.taken_avatar) ImageView avatarTakenView;
  @Bind(R.id.check_in_date) EditText checkInDateView;
  @Bind(R.id.check_out_date) EditText checkOutDateView;

  public Date checkInDate = null;
  public Date checkOutDate = null;

  public DateFragment() {
    // Required empty public constructor
  }

  public static DateFragment newInstance(Context context) {
    DateFragment fragment = new DateFragment();
    SelfCheckInApp.get(context).inject(fragment);
    return fragment;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View rootView = inflater.inflate(R.layout.fragment_date, container, false);
    ButterKnife.bind(this, rootView);

    if (avatarPath.isSet() && avatarPath.get() != null) {
      picasso.invalidate(avatarPath.get());
    }

    checkInDateView.setInputType(InputType.TYPE_NULL);
    checkOutDateView.setInputType(InputType.TYPE_NULL);

    checkInDateView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        DatePickerBuilder dpb = new DatePickerBuilder()
            .setFragmentManager(getChildFragmentManager())
            .setStyleResId(R.style.BetterPickersDialogFragment)
            .setTargetFragment(DateFragment.this)
            .setReference(10);
        dpb.show();
      }
    });

    checkOutDateView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        DatePickerBuilder dpb = new DatePickerBuilder()
            .setFragmentManager(getChildFragmentManager())
            .setStyleResId(R.style.BetterPickersDialogFragment)
            .setTargetFragment(DateFragment.this)
            .setReference(20);
        dpb.show();
      }
    });

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

  @OnClick(R.id.submit_btn)
  public void dateSubmitClicked() {
    if (!isError()) {
      guest.checkInDate = checkInDate;
      guest.checkOutDate = checkOutDate;

      bus.post(new SubmitEvent());
    }
  }

  private boolean isError() {

    checkInDateView.setError(null);
    checkOutDateView.setError(null);

    boolean cancel = false;
    View focusView = null;

    if (checkInDate == null) {
      checkInDateView.setError(getString(R.string.error_field_required));
      focusView = checkInDateView;
      cancel = true;
      checkInDate = null;
    } else if (checkOutDate == null) {
      checkOutDateView.setError(getString(R.string.error_field_required));
      focusView = checkOutDateView;
      cancel = true;
      checkOutDate = null;
    } else if (checkInDate.before(new Date())) {
      checkInDateView.setError(getString(R.string.error_less_than_today));
      focusView = checkInDateView;
      cancel = true;
      checkInDate = null;
    } else if (checkOutDate.equals(checkInDate) || checkOutDate.before(checkInDate)) {
      checkOutDateView.setError(getString(R.string.error_check_out_before_check_in));
      focusView = checkOutDateView;
      cancel = true;
      checkOutDate = null;
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

      bus.post(new BackShouldShowEvent(true));
      if (avatarPath.isSet() && avatarPath.get() != null) {
        picasso.load(avatarPath.get()).resize(200, 200).centerCrop()
            .transform(new CircleStrokeTransformation(getActivity(), 0, 0))
            .placeholder(R.drawable.avatar_placeholder).into(avatarTakenView);
      }
    }
  }

  @Override public void onDialogDateSet(int reference, int year, int monthOfYear, int dayOfMonth) {
    Calendar calendar = Calendar.getInstance();
    String dateString = String.format("%d - %d - %d", dayOfMonth, monthOfYear, year);
    calendar.set(year, monthOfYear, dayOfMonth);
    if (reference == 10) {
      checkInDateView.setText(dateString);
      checkInDate = calendar.getTime();
    } else if (reference == 20) {
      checkOutDateView.setText(dateString);
      checkOutDate = calendar.getTime();
    }
  }
}
