package me.tipi.self_check_in.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;
import me.tipi.self_check_in.R;
import me.tipi.self_check_in.SelfCheckInApp;

public class SignUpActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    SelfCheckInApp.get(this).inject(this);
    ButterKnife.bind(this);
    setContentView(R.layout.activity_sign_up);
  }

  @OnClick(R.id.resetBtn)
  public void reset() {

  }
}
