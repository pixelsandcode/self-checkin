package me.tipi.self_check_in.data.api.models;

import java.util.List;

public final class CountryResponse {

  public final List<Country> data;

  protected CountryResponse(List<Country> data) {
    this.data = data;
  }
}
