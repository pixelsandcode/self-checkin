/*
 * *
 *  * Copyright (c) 2015-2016 www.Tipi.me.
 *  * Created by Ashkan Hesaraki.
 *  * Ashkan.Hesaraki@gmail.com
 *
 */

package me.tipi.self_check_in.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import me.tipi.self_check_in.SelfCheckInApp;
import me.tipi.self_check_in.data.api.NetworkRequestManager;
import me.tipi.self_check_in.data.api.models.Country;

public class HomeTownAutoCompleteAdapter extends BaseAdapter implements Filterable {

  private LayoutInflater mInflater;
  private List<String> countries = new ArrayList<>();
  List<Country> data = Collections.emptyList();

  /**
   * Instantiates a new Home town auto complete adapter.
   *
   * @param context the context
   */
  public HomeTownAutoCompleteAdapter(final Context context) {
    SelfCheckInApp.get(context).inject(this);
    mInflater = LayoutInflater.from(context);
  }

  @Override public int getCount() {
    return countries.size();
  }

  @Override public String getItem(int position) {
    return countries.get(position);
  }

  @Override public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(final int position, final View convertView, final ViewGroup parent) {
    final TextView tv;
    if (convertView != null) {
      tv = (TextView) convertView;
    } else {
      tv = (TextView) mInflater.inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
    }

    tv.setText(getItem(position));
    return tv;
  }

  @Override
  public Filter getFilter() {
    return new Filter() {
      @Override
      protected FilterResults performFiltering(final CharSequence constraint) {
        final FilterResults filterResults = new FilterResults();
        if (constraint != null) {
          List<Country> countries = NetworkRequestManager.getInstance().callGetCountriesApi(constraint.toString());

          filterResults.values = countries;
          filterResults.count = countries.size();
        } else {
          data.clear();
        }

        return filterResults;
      }

      @SuppressWarnings("unchecked")
      @Override
      protected void publishResults(final CharSequence constraint, final FilterResults results) {
        if (results != null && results.count > 0) {
          countries.clear();
          for (Country country : (List<Country>) results.values) {
            String homeTown;
            for (int i = 0; i < country.cities.size(); i++) {
              homeTown = String.format("%s - %s", country.cities.get(i), country.name);
              countries.add(homeTown);
            }
          }

          notifyDataSetChanged();
        } else {
          notifyDataSetInvalidated();
        }
      }
    };
  }
}
