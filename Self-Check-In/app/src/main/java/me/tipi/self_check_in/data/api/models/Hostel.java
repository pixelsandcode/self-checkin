/*
 * *
 *  * Copyright (c) 2015-2016 www.Tipi.me.
 *  * Created by Ashkan Hesaraki.
 *  * Ashkan.Hesaraki@gmail.com
 *
 */

package me.tipi.self_check_in.data.api.models;

public class Hostel {
  public final String name;
  public final String doc_key;
  public final String terms;

  public Hostel(String name, String doc_key, String terms) {
    this.name = name;
    this.doc_key = doc_key;
    this.terms = terms;
  }
}
