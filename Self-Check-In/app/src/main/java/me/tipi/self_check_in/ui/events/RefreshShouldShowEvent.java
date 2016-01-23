/*
 * *
 *  * Copyright (c) 2015-2016 www.Tipi.me.
 *  * Created by Ashkan Hesaraki.
 *  * Ashkan.Hesaraki@gmail.com
 *
 */

package me.tipi.self_check_in.ui.events;

public class RefreshShouldShowEvent {
  public final boolean show;

  public RefreshShouldShowEvent(boolean show) {
    this.show = show;
  }
}
