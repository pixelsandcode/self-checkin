package me.tipi.self_check_in.ui.fragments;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Bus;

import java.util.Locale;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnClick;
import me.tipi.self_check_in.R;
import me.tipi.self_check_in.SelfCheckInApp;
import me.tipi.self_check_in.data.LanguagePreference;
import me.tipi.self_check_in.ui.SignUpActivity;
import me.tipi.self_check_in.ui.events.BackShouldShowEvent;
import me.tipi.self_check_in.ui.events.SettingShouldShowEvent;


public class LanguageFragment extends Fragment {

  public static final String TAG = LanguageFragment.class.getSimpleName();

  @Inject Bus bus;
  @Inject LanguagePreference languagePreference;

  public static LanguageFragment newInstance(Context context) {
    LanguageFragment fragment = new LanguageFragment();
    SelfCheckInApp.get(context).inject(fragment);
    return fragment;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_language, container, false);
    ButterKnife.bind(this, view);
    return view;
  }

  @Override public void onResume() {
    super.onResume();
    bus.register(this);
    if (getActivity() != null) {
      bus.post(new SettingShouldShowEvent(false));
      bus.post(new BackShouldShowEvent(true));
    }
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    ButterKnife.unbind(this);
  }

  @OnClick({R.id.english_btn, R.id.french_btn, R.id.german_btn, R.id.spanish_btn, R.id.japanese_btn, R.id.korean_btn})
  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.english_btn:
        setLanguage("en");
        break;
      case R.id.french_btn:
        setLanguage("fr");
        break;
      case R.id.german_btn:
        setLanguage("de");
        break;
      case R.id.spanish_btn:
        setLanguage("es");
        break;
      case R.id.japanese_btn:
        setLanguage("mdr");
        break;
      case R.id.korean_btn:
        setLanguage("ko");
        break;
    }
  }
  public void setLanguage(String language) {
    languagePreference.set(language);
    Locale locale = new Locale(languagePreference.get());
    Resources res = getResources();
    DisplayMetrics displayMetrics = res.getDisplayMetrics();
    Configuration configuration = res.getConfiguration();
    configuration.locale = locale;
    res.updateConfiguration(configuration, displayMetrics);
    ((SignUpActivity)getActivity()).showLandingFragment();
  }

}
