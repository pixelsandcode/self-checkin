/*
 * *
 *  * Copyright (c) 2015-2016 www.Tipi.me.
 *  * Created by Ashkan Hesaraki.
 *  * Ashkan.Hesaraki@gmail.com
 *
 */

package me.tipi.self_check_in.data.api.models;

public class User {
  public final String doc_key;
  public final String name;

  /**
   * Instantiates a new User.
   *
   * @param doc_key the doc key
   * @param name    the name
   */
  public User(String doc_key, String name) {
    this.doc_key = doc_key;
    this.name = name;
  }

  @Override public String toString() {
    return "User{" +
        "doc_key='" + doc_key + '\'' +
        ", name='" + name + '\'' +
        '}';
  }
}
