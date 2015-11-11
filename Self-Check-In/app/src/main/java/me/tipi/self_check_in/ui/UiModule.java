package me.tipi.self_check_in.ui;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import me.tipi.self_check_in.ui.adapters.HomeTownAutoCompleteAdapter;
import me.tipi.self_check_in.ui.fragments.AvatarFragment;
import me.tipi.self_check_in.ui.fragments.IdentityFragment;
import me.tipi.self_check_in.ui.fragments.LoginFragment;

@Module(
    injects = {
        MainActivity.class,
        SignUpActivity.class,
        LoginFragment.class,
        AvatarFragment.class,
        IdentityFragment.class,
        HomeTownAutoCompleteAdapter.class
    },

    complete = false,
    library = true
)
public final class UiModule {
  @Provides @Singleton AppContainer provideAppContainer() {
    return AppContainer.DEFAULT;
  }

  @Provides @Singleton ActivityHierarchyServer provideActivityHierarchyServer() {
    return ActivityHierarchyServer.NONE;
  }
}
