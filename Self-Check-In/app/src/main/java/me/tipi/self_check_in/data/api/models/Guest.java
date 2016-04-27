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
}
