package me.tipi.self_check_in.ui;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.viewpagerindicator.CirclePageIndicator;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.tipi.self_check_in.R;
import me.tipi.self_check_in.SelfCheckInApp;
import me.tipi.self_check_in.ui.adapters.SignUpAdapter;
import me.tipi.self_check_in.ui.events.PagerChangeEvent;
import timber.log.Timber;

public class SignUpActivity extends AppCompatActivity {

  @Inject Bus bus;

  @Bind(R.id.pager) ViewPager viewPager;
  @Bind(R.id.indicator) CirclePageIndicator indicator;

  public SignUpAdapter adapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_sign_up);
    SelfCheckInApp.get(this).inject(this);
    ButterKnife.bind(this);

    adapter = new SignUpAdapter(getSupportFragmentManager(), this);
    viewPager.setAdapter(adapter);
    indicator.setViewPager(viewPager);
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

  @OnClick(R.id.resetBtn)
  public void reset() {
  }

  @Subscribe
  public void onPagerChange(PagerChangeEvent event) {
    viewPager.setCurrentItem(event.page, true);
  }
}
