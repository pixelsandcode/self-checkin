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
import me.tipi.self_check_in.ui.fragments.IdentityFragment;
import me.tipi.self_check_in.ui.fragments.LoginFragment;
import me.tipi.self_check_in.ui.fragments.SuccessSignUpFragment;

@Module(
    injects = {
        MainActivity.class,
        SignUpActivity.class,
        LoginFragment.class,
        AvatarFragment.class,
        IdentityFragment.class,
        DateFragment.class,
        SuccessSignUpFragment.class,
        HomeTownAutoCompleteAdapter.class
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
