package me.tipi.self_check_in;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import me.tipi.self_check_in.data.DataModule;
import me.tipi.self_check_in.ui.UiModule;

@Module(
    includes = {
        UiModule.class,
        DataModule.class
    },
    injects = {
        SelfCheckInApp.class
    }
)

public final class SelfCheckInModule {
  private final SelfCheckInApp app;

  public SelfCheckInModule(SelfCheckInApp app) {
    this.app = app;
  }

  @Provides @Singleton Application provideApplication() {
    return app;
  }
}
