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
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import me.tipi.self_check_in.SelfCheckInApp;
import me.tipi.self_check_in.data.api.AuthenticationService;
import me.tipi.self_check_in.data.api.models.Country;
import me.tipi.self_check_in.data.api.models.CountryResponse;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class HomeTownAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {

  @Inject AuthenticationService authenticationService;

  private LayoutInflater mInflater;
  private List<Country> countries = Collections.emptyList();

  /**
   * Instantiates a new Home town auto complete adapter.
   *
   * @param context the context
   */
  public HomeTownAutoCompleteAdapter(final Context context) {
    super(context, -1);
    SelfCheckInApp.get(context).inject(this);
    mInflater = LayoutInflater.from(context);
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
          if (constraint.length() > 2) {
            Call<CountryResponse> call = authenticationService.getSuggestedCountries(constraint.toString());
            call.enqueue(new Callback<CountryResponse>() {
              @Override
              public void onResponse(Response<CountryResponse> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                  HomeTownAutoCompleteAdapter.this.countries = response.body().data;
                }
              }

              @Override public void onFailure(Throwable t) {

              }
            });
          }
        }

        filterResults.values = countries;
        filterResults.count = countries.size();

        return filterResults;
      }

      @SuppressWarnings("unchecked")
      @Override
      protected void publishResults(final CharSequence constraint, final FilterResults results) {
        clear();
        for (Country country : (List<Country>) results.values) {
          String homeTown;
          for (int i = 0; i < country.cities.size(); i++) {
            homeTown = String.format("%s - %s", country.cities.get(i), country.name);
            add(homeTown);
          }
        }
        if (results.count > 0) {
          notifyDataSetChanged();
        } else {
          notifyDataSetInvalidated();
        }
      }
    };
  }
}
