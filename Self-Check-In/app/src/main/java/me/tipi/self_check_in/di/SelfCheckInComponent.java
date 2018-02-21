package me.tipi.self_check_in.di;

import dagger.Component;
import javax.inject.Singleton;
import me.tipi.self_check_in.SelfCheckInModule;
import me.tipi.self_check_in.data.DataModule;
import me.tipi.self_check_in.data.api.ApiModule;
import me.tipi.self_check_in.data.api.NetworkRequestManager;
import me.tipi.self_check_in.ui.FindUserActivity;
import me.tipi.self_check_in.ui.MainActivity;
import me.tipi.self_check_in.ui.SettingActivity;
import me.tipi.self_check_in.ui.SignUpActivity;
import me.tipi.self_check_in.ui.UiModule;
import me.tipi.self_check_in.ui.adapters.HomeTownAutoCompleteAdapter;
import me.tipi.self_check_in.ui.fragments.AvatarFragment;
import me.tipi.self_check_in.ui.fragments.DateFragment;
import me.tipi.self_check_in.ui.fragments.EmailFragment;
import me.tipi.self_check_in.ui.fragments.FindUserFragment;
import me.tipi.self_check_in.ui.fragments.HostelTermsFragment;
import me.tipi.self_check_in.ui.fragments.IdentityFragment;
import me.tipi.self_check_in.ui.fragments.LandingFragment;
import me.tipi.self_check_in.ui.fragments.LanguageFragment;
import me.tipi.self_check_in.ui.fragments.LoginFragment;
import me.tipi.self_check_in.ui.fragments.MainFragment;
import me.tipi.self_check_in.ui.fragments.OCRFragment;
import me.tipi.self_check_in.ui.fragments.PassportFragment;
import me.tipi.self_check_in.ui.fragments.QuestionFragment;
import me.tipi.self_check_in.ui.fragments.ScanIDFragment;
import me.tipi.self_check_in.ui.fragments.SuccessSignUpFragment;

/**
 * Created by Khashayar on 20/02/2018.
 */
@Singleton @Component(modules = {
    ApiModule.class, DataModule.class, SelfCheckInModule.class, UiModule.class
}) public interface SelfCheckInComponent {

  void inject(MainActivity mainActivity);

  void inject(SignUpActivity signUpActivity);

  void inject(FindUserActivity findUserActivity);

  void inject(SettingActivity settingActivity);

  void inject(LoginFragment loginFragment);

  void inject(LandingFragment landingFragment);

  void inject(AvatarFragment avatarFragment);

  void inject(PassportFragment passportFragment);

  void inject(IdentityFragment identityFragment);

  void inject(FindUserFragment findUserFragment);

  void inject(DateFragment dateFragment);

  void inject(SuccessSignUpFragment successSignUpFragment);

  void inject(HomeTownAutoCompleteAdapter homeTownAutoCompleteAdapter);

  void inject(HostelTermsFragment hostelTermsFragment);

  void inject(QuestionFragment questionFragment);

  void inject(MainFragment mainFragment);

  void inject(OCRFragment ocrFragment);

  void inject(EmailFragment emailFragment);

  void inject(ScanIDFragment scanIDFragment);

  void inject(LanguageFragment languageFragment);

  void inject(NetworkRequestManager networkRequestManager);
}

