package me.tipi.self_check_in.ui.fragments;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.otto.Bus;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.tipi.self_check_in.R;
import me.tipi.self_check_in.SelfCheckInApp;
import me.tipi.self_check_in.data.api.ApiConstants;
import me.tipi.self_check_in.data.api.AuthenticationService;
import me.tipi.self_check_in.data.api.models.ApiResponse;
import me.tipi.self_check_in.data.api.models.Guest;
import me.tipi.self_check_in.data.api.models.NoteRequest;
import me.tipi.self_check_in.ui.FindUserActivity;
import me.tipi.self_check_in.ui.SignUpActivity;
import me.tipi.self_check_in.ui.events.BackShouldShowEvent;
import me.tipi.self_check_in.ui.events.PagerChangeEvent;
import me.tipi.self_check_in.ui.events.RefreshShouldShowEvent;
import me.tipi.self_check_in.ui.events.SettingShouldShowEvent;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class QuestionFragment extends Fragment {

  public static final String TAG = QuestionFragment.class.getSimpleName();

  @Inject Bus bus;
  @Inject Tracker tracker;
  @Inject AuthenticationService authenticationService;
  @Inject Guest guest;

  @Bind(R.id.first_question) EditText firstQuestionView;
  @Bind(R.id.second_question) EditText secondQuestionView;
  @Bind(R.id.third_question) EditText thirdQuestionView;
  @Bind(R.id.forth_question) EditText forthQuestionView;
  @Bind(R.id.country_question) EditText countryQuestion;

  private String guestKey;
  private MaterialDialog thirdDialog;
  private MaterialDialog fourthDialog;
  private MaterialDialog loading;
  private Handler handler = new Handler();
  private Runnable runnable = new Runnable() {
    @Override public void run() {
      startOver();
    }
  };

  public QuestionFragment() {
    // Required empty public constructor
  }

  public static QuestionFragment newInstance(Context context) {
    QuestionFragment fragment = new QuestionFragment();
    SelfCheckInApp.get(context).inject(fragment);
    return fragment;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View rootView = inflater.inflate(R.layout.fragment_question, container, false);
    ButterKnife.bind(this, rootView);

    thirdQuestionView.setInputType(InputType.TYPE_NULL);
    forthQuestionView.setInputType(InputType.TYPE_NULL);

    thirdDialog = new MaterialDialog.Builder(getActivity())
        .positiveText(R.string.yes)
        .negativeText(R.string.no)
        .content(R.string.visit_farser)
        .onPositive(new MaterialDialog.SingleButtonCallback() {
          @Override
          public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
            thirdQuestionView.setText(R.string.yes);
          }
        })
        .onNegative(new MaterialDialog.SingleButtonCallback() {
          @Override
          public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
            thirdQuestionView.setText(R.string.no);
          }
        }).build();

    fourthDialog = new MaterialDialog.Builder(getActivity())
        .positiveText(R.string.yes)
        .negativeText(R.string.no)
        .content(R.string.need_job)
        .onPositive(new MaterialDialog.SingleButtonCallback() {
          @Override
          public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
            forthQuestionView.setText(R.string.yes);
          }
        })
        .onNegative(new MaterialDialog.SingleButtonCallback() {
          @Override
          public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
            forthQuestionView.setText(R.string.no);
          }
        }).build();

    thirdQuestionView.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        thirdDialog.show();
      }
    });

    forthQuestionView.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        fourthDialog.show();
      }
    });

    thirdQuestionView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
      @Override public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
          thirdDialog.show();
        }
      }
    });

    forthQuestionView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
      @Override public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
          fourthDialog.show();
        }
      }
    });

    loading = new MaterialDialog.Builder(getActivity())
        .content("Loading")
        .cancelable(false)
        .progress(true, 0)
        .build();

    return rootView;
  }

  @Override public void onResume() {
    super.onResume();
    bus.register(this);
    if (getActivity() != null) {
      if (guest != null) {
        guestKey = guest.guest_key;
      }

      bus.post(new BackShouldShowEvent(false));
      bus.post(new RefreshShouldShowEvent(false));
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

  @Override public void setUserVisibleHint(boolean isVisibleToUser) {
    super.setUserVisibleHint(isVisibleToUser);
    if (getActivity() != null && isVisibleToUser) {
      if (guest != null) {
        guestKey = guest.guest_key;
      }

      bus.post(new BackShouldShowEvent(false));
      bus.post(new RefreshShouldShowEvent(false));
      bus.post(new SettingShouldShowEvent(false));
    }
  }

  @OnClick(R.id.continue_btn)
  public void sendNote() {
    String note = "";
    String enteredFirst = firstQuestionView.getText().toString().trim();
    String enteredSecond = secondQuestionView.getText().toString().trim();
    String enteredThird = thirdQuestionView.getText().toString().trim();
    String enteredFourth = forthQuestionView.getText().toString().trim();
    String enteredCountry = countryQuestion.getText().toString().trim();

    if (!TextUtils.isEmpty(enteredCountry)) {
      note = note + String.format("Coming from %s. ", enteredCountry);
    }

    if (!TextUtils.isEmpty(enteredFirst)) {
      note = note + String.format("Travelling to %s. ", enteredFirst);
    }

    if (!TextUtils.isEmpty(enteredSecond)) {
      note = note + String.format("Staying for %s in country. ", enteredSecond);
    }

    if (!TextUtils.isEmpty(enteredThird)) {
      if (enteredThird.equals("Yes")) {
        note = note + "Visiting fraser island. ";
      }
    }

    if (!TextUtils.isEmpty(enteredFourth)) {
      if (enteredFourth.equals("Yes")) {
        note = note + "Looking for a job.";
      }
    }

    Timber.d("guest_key is: %s", guestKey);
    if (TextUtils.isEmpty(note) || note.equals("")) {
      navigateToSuccessPage();
    } else {
      loading.show();
      authenticationService.sendNote(guestKey, new NoteRequest(note), new Callback<ApiResponse>() {
        @Override public void success(ApiResponse apiResponse, Response response) {
          Timber.d("note sent");
          loading.dismiss();
          navigateToSuccessPage();
        }

        @Override public void failure(RetrofitError error) {
          loading.dismiss();
          Timber.e(error.getMessage());
        }
      });
    }
  }

  private void navigateToSuccessPage() {
    if (guest.guest_key != null && !TextUtils.isEmpty(guest.guest_key)) {
      if (getActivity() instanceof SignUpActivity) {
        ((SignUpActivity)getActivity()).showSuccessFragment();
      } else {
        bus.post(new PagerChangeEvent(4));
      }
    } else {
      ((SignUpActivity)getActivity()).showSuccessFragment();
    }
  }

  private void startOver() {
    if (getActivity() != null && getActivity() instanceof SignUpActivity) {
      ((SignUpActivity)getActivity()).reset();
    } else if (getActivity() != null){
      ((FindUserActivity)getActivity()).reset();
    }
  }
}
