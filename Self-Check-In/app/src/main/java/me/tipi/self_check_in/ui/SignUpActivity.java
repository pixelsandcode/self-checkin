package me.tipi.self_check_in.ui;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.viewpagerindicator.CirclePageIndicator;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.tipi.self_check_in.R;
import me.tipi.self_check_in.SelfCheckInApp;
import me.tipi.self_check_in.ui.adapters.SignUpAdapter;

public class SignUpActivity extends AppCompatActivity {

  @Bind(R.id.pager) ViewPager viewPager;
  @Bind(R.id.indicator) CirclePageIndicator indicator;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_sign_up);
    SelfCheckInApp.get(this).inject(this);
    ButterKnife.bind(this);

    viewPager.setAdapter(new SignUpAdapter(getSupportFragmentManager()));
    indicator.setViewPager(viewPager);
  }

  @OnClick(R.id.resetBtn)
  public void reset() {

  }
}
