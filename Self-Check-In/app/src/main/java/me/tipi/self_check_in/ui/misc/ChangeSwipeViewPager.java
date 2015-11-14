package me.tipi.self_check_in.ui.misc;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class ChangeSwipeViewPager extends ViewPager {

  private boolean enabled;

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

  public void setSwipingEnabled(boolean enabled) {
    this.enabled = enabled;
  }
}
