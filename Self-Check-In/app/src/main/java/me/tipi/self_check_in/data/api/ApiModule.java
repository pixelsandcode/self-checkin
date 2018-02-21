/*
 * *
 *  * Copyright (c) 2015-2016 www.Tipi.me.
 *  * Created by Ashkan Hesaraki.
 *  * Ashkan.Hesaraki@gmail.com
 *
 */

package me.tipi.self_check_in.data.api;

import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;
import me.tipi.self_check_in.data.api.models.Guest;

@Module public final class ApiModule {

  @Provides @Singleton AuthenticationService provideAuthenticationService() {
    return provideServiceGenerator().createService(AuthenticationService.class);
  }

  @Provides @Singleton ServiceGenerator provideServiceGenerator() {
    return new ServiceGenerator();
  }

  @Provides @Singleton NetworkRequestManager provideNetworkRequestManager() {
    return new NetworkRequestManager();
  }

  /**
   * Provide guest guest.
   *
   * @return the guest
   */
  @Provides @Singleton Guest provideGuest() {
    return new Guest();
  }
}
