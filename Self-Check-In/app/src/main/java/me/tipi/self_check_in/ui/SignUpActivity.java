/*
 * *
 *  * Copyright (c) 2015-2016 www.Tipi.me.
 *  * Created by Ashkan Hesaraki.
 *  * Ashkan.Hesaraki@gmail.com
 *
 */

package me.tipi.self_check_in.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.f2prateek.rx.preferences.Preference;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.tipi.self_check_in.R;
import me.tipi.self_check_in.SelfCheckInApp;
import me.tipi.self_check_in.data.api.ApiConstants;
import me.tipi.self_check_in.data.api.AuthenticationService;
import me.tipi.self_check_in.data.api.models.ApiResponse;
import me.tipi.self_check_in.data.api.models.Guest;
import me.tipi.self_check_in.ui.adapters.SignUpAdapter;
import me.tipi.self_check_in.ui.events.BackShouldShowEvent;
import me.tipi.self_check_in.ui.events.EmailConflictEvent;
import me.tipi.self_check_in.ui.events.PagerChangeEvent;
import me.tipi.self_check_in.ui.events.SubmitEvent;
import me.tipi.self_check_in.ui.misc.ChangeSwipeViewPager;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

public class SignUpActivity extends AppCompatActivity {

  @Inject Bus bus;
  @Inject Guest guest;
  @Inject AuthenticationService authenticationService;
  @Inject @Named(ApiConstants.AVATAR) Preference<String> avatarPath;

  @Bind(R.id.pager) ChangeSwipeViewPager viewPager;
  @Bind(R.id.backBtn) TextView backButtonView;

  MaterialDialog loading;

  public SignUpAdapter adapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_sign_up);
    SelfCheckInApp.get(this).inject(this);
    ButterKnife.bind(this);

    loading = new MaterialDialog.Builder(this)
        .content("Submitting")
        .cancelable(false)
        .progress(true, 0)
        .build();

    adapter = new SignUpAdapter(getSupportFragmentManager(), this);
    viewPager.setAdapter(adapter);
    viewPager.setSwipingEnabled(false);
  }

  @Override protected void onResume() {
    super.onResume();
    Timber.d("Resumed");
    bus.register(this);
  }

  @Override protected void onPause() {
    super.onPause();
    Timber.d("Paused");
    bus.unregister(this);
  }

  @Override
  public void onBackPressed() {
    if (viewPager.getCurrentItem() == 0) {
      // If the user is currently looking at the first step, allow the system to handle the
      // Back button. This calls finish() on this activity and pops the back stack.
      super.onBackPressed();
    } else {
      // Otherwise, select the previous step.
      viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
    }
  }

  /**
   * Start over.
   */
  @OnClick(R.id.resetBtn)
  public void startOver() {
    reset();
    bus.post(new PagerChangeEvent(0));
  }

  /**
   * Back clicked.
   */
  @OnClick(R.id.backBtn)
  public void backClicked() {
    if (viewPager.getCurrentItem() != 0) {
      viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
    }
  }

  /**
   * On pager change.
   *
   * @param event the event
   */
  @Subscribe
  public void onPagerChange(PagerChangeEvent event) {
    viewPager.setCurrentItem(event.page, true);
  }

  /**
   * On back shown.
   *
   * @param event the event
   */
  @Subscribe
  public void onBackShown(BackShouldShowEvent event) {
    backButtonView.setVisibility(event.show ? View.VISIBLE : View.GONE);
  }

  /**
   * On submit.
   *
   * @param event the event
   */
  @Subscribe
  public void onSubmit(SubmitEvent event) {
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    MediaType mediaType = MediaType.parse("multipart/form-data");
    if (avatarPath != null && avatarPath.get() != null) {
      File avatarFile = new File(avatarPath.get());
      RequestBody requestBody = RequestBody.create(mediaType, avatarFile);
      Call<ApiResponse> call = authenticationService.addGuest(
          RequestBody.create(mediaType, guest.email),
          RequestBody.create(mediaType, guest.name),
          TextUtils.isEmpty(guest.city) ? null : RequestBody.create(mediaType, guest.city),
          TextUtils.isEmpty(guest.country) ? null : RequestBody.create(mediaType, guest.country),
          RequestBody.create(mediaType, guest.passportNumber),
          guest.dob == null ? null : RequestBody.create(mediaType, dateFormat.format(guest.dob)),
          RequestBody.create(mediaType, guest.referenceCode),
          RequestBody.create(mediaType, dateFormat.format(guest.checkInDate)),
          RequestBody.create(mediaType, dateFormat.format(guest.checkOutDate)),
          requestBody
      );

      loading.show();
      call.enqueue(new Callback<ApiResponse>() {
        @Override public void onResponse(Response<ApiResponse> response, Retrofit retrofit) {
          loading.dismiss();
          if (response.isSuccess()) {
            Timber.d("Good");
            viewPager.setCurrentItem(3, true);
          } else {
            Timber.d(response.raw().message());
            if (response.raw().code() == 409) {
              viewPager.setCurrentItem(1, true);
              bus.post(new EmailConflictEvent());
            }
          }
        }

        @Override public void onFailure(Throwable t) {
          Timber.e(t, "failed");
          loading.dismiss();
        }
      });
    }

  }

  /**
   * Reset.
   */
  private void reset() {
    avatarPath.delete();
    if (guest != null) {
      guest = null;
    }

    Intent intent = getIntent();
    finish();
    startActivity(intent);
  }
}
