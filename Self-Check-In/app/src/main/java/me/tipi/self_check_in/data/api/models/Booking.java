/*
 * *
 *  * Copyright (c) 2015-2016 www.Tipi.me.
 *  * Created by Ashkan Hesaraki.
 *  * Ashkan.Hesaraki@gmail.com
 *
 */

package me.tipi.self_check_in.data.api.models;

public class Booking {
  public final String reference_number;
  public final String from;
  public final String to;

  public Booking(String reference_number, String from, String to) {
    this.reference_number = reference_number;
    this.from = from;
    this.to = to;
  }
}
