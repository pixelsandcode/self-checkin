/*
 * *
 *  * Copyright (c) 2015-2016 www.Tipi.me.
 *  * Created by Ashkan Hesaraki.
 *  * Ashkan.Hesaraki@gmail.com
 *
 */

package me.tipi.self_check_in.ui;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
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

@Module(
    injects = {
        MainActivity.class,
        SignUpActivity.class,
        FindUserActivity.class,
        SettingActivity.class,
        LoginFragment.class,
        LandingFragment.class,
        AvatarFragment.class,
        PassportFragment.class,
        IdentityFragment.class,
        FindUserFragment.class,
        DateFragment.class,
        SuccessSignUpFragment.class,
        HomeTownAutoCompleteAdapter.class,
        HostelTermsFragment.class,
        QuestionFragment.class,
        MainFragment.class,
        OCRFragment.class,
        EmailFragment.class,
        ScanIDFragment.class,
        LanguageFragment.class
    },

    complete = false,
    library = true
)
public final class UiModule {
  /**
   * Provide app container app container.
   *
   * @return the app container
   */
  @Provides @Singleton AppContainer provideAppContainer() {
    return AppContainer.DEFAULT;
  }

  /**
   * Provide activity hierarchy server activity hierarchy server.
   *
   * @return the activity hierarchy server
   */
  @Provides @Singleton ActivityHierarchyServer provideActivityHierarchyServer() {
    return ActivityHierarchyServer.NONE;
  }
}
