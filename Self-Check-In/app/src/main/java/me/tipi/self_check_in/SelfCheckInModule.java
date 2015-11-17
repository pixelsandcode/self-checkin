/*
 * *
 *  * Copyright (c) 2015-2016 www.Tipi.me.
 *  * Created by Ashkan Hesaraki.
 *  * Ashkan.Hesaraki@gmail.com
 *
 */

package me.tipi.self_check_in;

import android.app.Application;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

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

  /**
   * Instantiates a new Self check in module.
   *
   * @param app the app
   */
  public SelfCheckInModule(SelfCheckInApp app) {
    this.app = app;
  }

  /**
   * Provide application application.
   *
   * @return the application
   */
  @Provides @Singleton Application provideApplication() {
    return app;
  }

  /**
   * Provide bus bus.
   *
   * @return the bus
   */
  @Provides @Singleton Bus provideBus() {
    return new Bus(ThreadEnforcer.ANY);
  }
}
