package me.tipi.self_check_in.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.tipi.self_check_in.R;
import me.tipi.self_check_in.SelfCheckInApp;
import me.tipi.self_check_in.data.api.models.Guest;
import me.tipi.self_check_in.ui.adapters.SignUpAdapter;
import me.tipi.self_check_in.ui.events.BackShouldShowEvent;
import me.tipi.self_check_in.ui.events.PagerChangeEvent;
import me.tipi.self_check_in.ui.events.SubmitEvent;
import me.tipi.self_check_in.ui.misc.ChangeSwipeViewPager;
import timber.log.Timber;

public class SignUpActivity extends AppCompatActivity {

  @Inject Bus bus;
  @Inject Guest guest;

  @Bind(R.id.pager) ChangeSwipeViewPager viewPager;
  @Bind(R.id.backBtn) TextView backButtonView;

  public SignUpAdapter adapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_sign_up);
    SelfCheckInApp.get(this).inject(this);
    ButterKnife.bind(this);

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

  @OnClick(R.id.resetBtn)
  public void reset() {
  }

  @OnClick(R.id.backBtn)
  public void backClicked() {
    if (viewPager.getCurrentItem() != 0) {
      viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
    }
  }

  @Subscribe
  public void onPagerChange(PagerChangeEvent event) {
    viewPager.setCurrentItem(event.page, true);
  }

  @Subscribe
  public void onBackShown(BackShouldShowEvent event) {
    backButtonView.setVisibility(event.show ? View.VISIBLE : View.GONE);
  }

  @Subscribe
  public void onSubmit(SubmitEvent event) {
    Timber.d("Submit", guest);
  }
}
