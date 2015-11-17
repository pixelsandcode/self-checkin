/*
 * *
 *  * Copyright (c) 2015-2016 www.Tipi.me.
 *  * Created by Ashkan Hesaraki.
 *  * Ashkan.Hesaraki@gmail.com
 *
 */

package me.tipi.self_check_in.ui.misc;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class ChangeSwipeViewPager extends ViewPager {

  private boolean enabled;

  /**
   * Instantiates a new Change swipe view pager.
   *
   * @param context the context
   * @param attrs   the attrs
   */
  public ChangeSwipeViewPager(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    return this.enabled && super.onTouchEvent(event);

  }

  @Override
  public boolean onInterceptTouchEvent(MotionEvent event) {
    return this.enabled && super.onInterceptTouchEvent(event);

  }

  /**
   * Sets swiping enabled.
   *
   * @param enabled the enabled
   */
  public void setSwipingEnabled(boolean enabled) {
    this.enabled = enabled;
  }
}
