package me.tipi.self_check_in.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.otto.Bus;
import javax.inject.Inject;
import me.tipi.self_check_in.R;
import me.tipi.self_check_in.SelfCheckInApp;
import me.tipi.self_check_in.data.api.ApiConstants;
import me.tipi.self_check_in.data.api.AppCallback;
import me.tipi.self_check_in.data.api.NetworkRequestManager;
import me.tipi.self_check_in.data.api.models.BaseResponse;
import me.tipi.self_check_in.data.api.models.Guest;
import me.tipi.self_check_in.data.api.models.NoteRequest;
import me.tipi.self_check_in.ui.AppContainer;
import me.tipi.self_check_in.ui.FindUserActivity;
import me.tipi.self_check_in.ui.SignUpActivity;
import me.tipi.self_check_in.ui.events.BackShouldShowEvent;
import me.tipi.self_check_in.ui.events.PagerChangeEvent;
import me.tipi.self_check_in.ui.events.RefreshShouldShowEvent;
import me.tipi.self_check_in.ui.events.SettingShouldShowEvent;
import retrofit2.Call;
import retrofit2.Response;
import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class QuestionFragment extends Fragment {

  public static final String TAG = QuestionFragment.class.getSimpleName();

  @Inject NetworkRequestManager networkRequestManager;
  @Inject Bus bus;
  @Inject Tracker tracker;
  @Inject Guest guest;
  @Inject AppContainer appContainer;

  @Bind(R.id.first_question) EditText firstQuestionView;
  @Bind(R.id.second_question) EditText secondQuestionView;
  @Bind(R.id.third_question) EditText thirdQuestionView;
  @Bind(R.id.forth_question) EditText forthQuestionView;
  @Bind(R.id.fifth_question) EditText fifthQuestionView;
  @Bind(R.id.country_question) EditText countryQuestion;

  private String guestKey;
  private MaterialDialog thirdDialog;
  private MaterialDialog fourthDialog;
  private MaterialDialog fifthDialog;
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
    SelfCheckInApp.get(context).getSelfCheckInComponent().inject(fragment);
    return fragment;
  }

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View rootView = inflater.inflate(R.layout.fragment_question, container, false);
    ButterKnife.bind(this, rootView);

    thirdQuestionView.setInputType(InputType.TYPE_NULL);
    forthQuestionView.setInputType(InputType.TYPE_NULL);
    fifthQuestionView.setInputType(InputType.TYPE_NULL);

    thirdDialog = new MaterialDialog.Builder(getActivity()).positiveText(R.string.yes)
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
        })
        .build();

    fourthDialog = new MaterialDialog.Builder(getActivity()).positiveText(R.string.yes)
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
        })
        .build();

    fifthDialog = new MaterialDialog.Builder(getActivity()).positiveText(R.string.yes)
        .negativeText(R.string.no)
        .content(R.string.do_you_want_to_learn_how_to_surf)
        .onPositive(new MaterialDialog.SingleButtonCallback() {
          @Override
          public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
            fifthQuestionView.setText(R.string.yes);
          }
        })
        .onNegative(new MaterialDialog.SingleButtonCallback() {
          @Override
          public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
            fifthQuestionView.setText(R.string.no);
          }
        })
        .build();

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

    fifthQuestionView.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        fifthDialog.show();
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

    fifthQuestionView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
      @Override public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
          fifthDialog.show();
        }
      }
    });

    loading = new MaterialDialog.Builder(getActivity()).content("Loading")
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

  @OnClick(R.id.continue_btn) public void sendNote() {
    String note = "";
    String enteredFirst = firstQuestionView.getText().toString().trim();
    String enteredSecond = secondQuestionView.getText().toString().trim();
    String enteredThird = thirdQuestionView.getText().toString().trim();
    String enteredFourth = forthQuestionView.getText().toString().trim();
    String enteredFifth = fifthQuestionView.getText().toString().trim();
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
        note = note + "Looking for a job. ";
      }
    }

    if (!TextUtils.isEmpty(enteredFifth)) {
      if (enteredFifth.equals("Yes")) {
        note = note + "Wants to learn surfing";
      }
    }

    if (TextUtils.isEmpty(note) || note.equals("")) {
      navigateToSuccessPage();
    } else {
      loading.show();
      networkRequestManager.callSendNoteApi(guestKey, new NoteRequest(note), new AppCallback() {
        @Override public void onRequestSuccess(Call call, Response response) {
          loading.dismiss();
          Timber.d("note sent");
          navigateToSuccessPage();
        }

        @Override public void onRequestFail(Call call, BaseResponse response) {
          loading.dismiss();
          Timber.w("Error note - guest_key is: %s", guestKey);
          Timber.w("ERROR: %s",
              response.getMessage() != null ? response.getMessage() : response.toString());
          navigateToSuccessPage();
        }

        @Override public void onBadRequest(Call call, BaseResponse response) {
          loading.dismiss();
        }

        @Override public void onApiNotFound(Call call, BaseResponse response) {
          loading.dismiss();
        }

        @Override public void onAuthError(Call call, BaseResponse response) {
          loading.dismiss();
        }

        @Override public void onServerError(Call call, BaseResponse response) {
          loading.dismiss();
          Snackbar.make(appContainer.bind(getActivity()), R.string.no_connection,
              Snackbar.LENGTH_LONG).show();
        }

        @Override public void onRequestTimeOut(Call call, Throwable t) {
          loading.dismiss();
        }

        @Override public void onNullResponse(Call call) {
          loading.dismiss();
        }
      });
    }
  }

  private void navigateToSuccessPage() {
    if (guest.guest_key != null && !TextUtils.isEmpty(guest.guest_key)) {
      if (getActivity() instanceof SignUpActivity) {
        ((SignUpActivity) getActivity()).showSuccessFragment();
      } else {
        bus.post(new PagerChangeEvent(4));
      }
    } else {
      ((SignUpActivity) getActivity()).showSuccessFragment();
    }
  }

  private void startOver() {
    if (getActivity() != null && getActivity() instanceof SignUpActivity) {
      ((SignUpActivity) getActivity()).reset();
    } else if (getActivity() != null) {
      ((FindUserActivity) getActivity()).reset();
    }
  }
}
