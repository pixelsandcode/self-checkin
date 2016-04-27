/*
 * *
 *  * Copyright (c) 2015-2016 www.Tipi.me.
 *  * Created by Ashkan Hesaraki.
 *  * Ashkan.Hesaraki@gmail.com
 *
 */

package me.tipi.self_check_in.ui.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import me.tipi.self_check_in.ui.fragments.DateFragment;
import me.tipi.self_check_in.ui.fragments.FindUserFragment;
import me.tipi.self_check_in.ui.fragments.HostelTermsFragment;
import me.tipi.self_check_in.ui.fragments.QuestionFragment;
import me.tipi.self_check_in.ui.fragments.SuccessSignUpFragment;

public class LoginAdapter extends FragmentStatePagerAdapter {

  private Context context;

  /**
   * Instantiates a new Sign up adapter.
   *
   * @param fm      the fm
   * @param context the context
   */
  public LoginAdapter(FragmentManager fm, Context context) {
    super(fm);
    this.context = context;
  }

  @Override public Fragment getItem(int position) {
    switch (position) {
      case 0:
        return FindUserFragment.newInstance(context);
      case 1:
        return DateFragment.newInstance(context);
      case 2:
        return HostelTermsFragment.newInstance(context);
      case 3:
        return QuestionFragment.newInstance(context);
      case 4:
        return SuccessSignUpFragment.newInstance(context);
      default:
        return null;
    }
  }

  @Override public int getCount() {
    return 5;
  }
}
