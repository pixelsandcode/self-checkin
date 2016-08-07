/*
 * *
 *  * Copyright (c) 2015-2016 www.Tipi.me.
 *  * Created by Ashkan Hesaraki.
 *  * Ashkan.Hesaraki@gmail.com
 *
 */

package me.tipi.self_check_in.data.api.models;

import java.util.Date;

import javax.inject.Singleton;

@Singleton
public final class Guest {
  public String user_key = "";
  public String name;
  public String email;
  public String city;
  public String country;
  public int gender;
  public Date dob;
  public String referenceCode;
  public String passportNumber;
  public Date checkInDate;
  public Date checkOutDate;
  public long time;
  public String guest_key;

  /**
   * Instantiates a new Guest.
   */
  public Guest() {

  }

  @Override public String toString() {
    return "User{" +
        "user_key='" + user_key + '\'' +
        ", name='" + name + '\'' +
        ", email=" + email +
        ", city='" + city + '\'' +
        ", country='" + country + '\'' +
        ", gender='" + gender + '\'' +
        ", dob='" + dob.toString() + '\'' +
        ", passport number='" + passportNumber + '\'' +
        ", check in date='" + checkInDate.toString() + '\'' +
        ", check out date='" + checkOutDate.toString() + '\'' +
        ", guest key='" + guest_key + '\'' +
        '}';
  }

  public String toStringNoDate() {
    return "User{" +
        "user_key='" + user_key + '\'' +
        ", name='" + name + '\'' +
        ", email=" + email +
        ", city='" + city + '\'' +
        ", country='" + country + '\'' +
        ", gender='" + gender + '\'' +
        ", passport number='" + passportNumber + '\'' +
        ", guest key='" + guest_key + '\'' +
        '}';
  }
}
