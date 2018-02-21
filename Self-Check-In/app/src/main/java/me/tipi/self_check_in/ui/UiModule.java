/*
 * *
 *  * Copyright (c) 2015-2016 www.Tipi.me.
 *  * Created by Ashkan Hesaraki.
 *  * Ashkan.Hesaraki@gmail.com
 *
 */

package me.tipi.self_check_in.ui;

import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

@Module
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
