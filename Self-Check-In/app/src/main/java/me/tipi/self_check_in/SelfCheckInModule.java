package me.tipi.self_check_in;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
    injects = {
        SelfCheckInApp.class
    },

    library = true
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
