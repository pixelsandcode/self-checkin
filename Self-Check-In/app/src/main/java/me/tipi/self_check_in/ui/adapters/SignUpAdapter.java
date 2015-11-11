package me.tipi.self_check_in.ui.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import me.tipi.self_check_in.ui.fragments.AvatarFragment;

public class SignUpAdapter extends FragmentStatePagerAdapter {

  private Context context;

  public SignUpAdapter(FragmentManager fm, Context context) {
    super(fm);
    this.context = context;
  }

  @Override public Fragment getItem(int position) {
    switch (position) {
      case 0:
      case 1:
      case 2:
        return AvatarFragment.newInstance(context);
      default:
        return null;
    }
  }

  @Override public int getCount() {
    return 3;
  }
}
