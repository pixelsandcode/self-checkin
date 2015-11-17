/*
 * *
 *  * Copyright (c) 2015-2016 www.Tipi.me.
 *  * Created by Ashkan Hesaraki.
 *  * Ashkan.Hesaraki@gmail.com
 *
 */

package me.tipi.self_check_in.ui.events;

public class PagerChangeEvent {
  public final int page;

  /**
   * Instantiates a new Pager change event.
   *
   * @param page the page
   */
  public PagerChangeEvent(int page) {
    this.page = page;
  }
}
