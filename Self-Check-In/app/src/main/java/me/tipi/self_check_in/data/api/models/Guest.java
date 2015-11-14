package me.tipi.self_check_in.data.api.models;

import java.util.Date;

import javax.inject.Singleton;

@Singleton
public final class Guest {
  public String name;
  public String email;
  public String city;
  public String country;
  public Date dob;
  public String referenceCode;
  public String passportNumber;
  public Date checkInDate;
  public Date checkOutDate;

  public Guest() {

  }
}
