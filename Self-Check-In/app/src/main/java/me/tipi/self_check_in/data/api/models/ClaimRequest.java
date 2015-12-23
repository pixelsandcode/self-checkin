/*
 * *
 *  * Copyright (c) 2015-2016 www.Tipi.me.
 *  * Created by Ashkan Hesaraki.
 *  * Ashkan.Hesaraki@gmail.com
 *
 */

package me.tipi.self_check_in.data.api.models;

public class ClaimRequest {
  public final String email;
  public final Booking booking;

  /**
   * Instantiates a new Claim request.
   *
   * @param email   the email
   * @param booking the booking
   */
  public ClaimRequest(String email, Booking booking) {
    this.email = email;
    this.booking = booking;
  }
}
