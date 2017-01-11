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
import android.widget.Button;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.Locale;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.tipi.self_check_in.R;
import me.tipi.self_check_in.SelfCheckInApp;
import me.tipi.self_check_in.data.LanguagePrefrence;
import me.tipi.self_check_in.ui.SignUpActivity;


public class LanguageFragmnet extends Fragment {

  public static final String TAG = LanguageFragmnet.class.getSimpleName();

  @Bind(R.id.english_btn) Button englishBtn;
  @Bind(R.id.french_btn) Button frenchBtn;
  @Bind(R.id.german_btn) Button germanBtn;
  @Bind(R.id.spanish_btn) Button spanishBtn;
  @Bind(R.id.japanese_btn) Button japaneseBtn;
  @Bind(R.id.korean_btn) Button koreanBtn;

  @Inject LanguagePrefrence languagePrefrence;

  private MaterialDialog loading;

  public static LanguageFragmnet newInstance(Context context) {
    LanguageFragmnet fragment = new LanguageFragmnet();
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
        setLanguage("ja");
        break;
      case R.id.korean_btn:
        setLanguage("ko");
        break;
    }
  }
  public void setLanguage(String language) {
    languagePrefrence.set(language);
    Locale locale = new Locale(languagePrefrence.get());
    Resources res = getResources();
    DisplayMetrics displayMetrics = res.getDisplayMetrics();
    Configuration configuration = res.getConfiguration();
    configuration.locale = locale;
    res.updateConfiguration(configuration, displayMetrics);
    ((SignUpActivity)getActivity()).showLandingFragment();
  }
}
