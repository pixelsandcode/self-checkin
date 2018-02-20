/*
 * *
 *  * Copyright (c) 2015-2016 www.Tipi.me.
 *  * Created by Ashkan Hesaraki.
 *  * Ashkan.Hesaraki@gmail.com
 *
 */

package me.tipi.self_check_in.data.api.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public final class ApiResponse extends BaseResponse {
  @SerializedName("data") @Expose private Data data;

  public Data getData() {
    return data;
  }

  public void setData(Data data) {
    this.data = data;
  }

  public class Data {

    @SerializedName("name") @Expose private String name;
    @SerializedName("gender") @Expose private Integer gender;
    @SerializedName("city") @Expose private String city;
    @SerializedName("country") @Expose private String country;
    @SerializedName("state") @Expose private String state;
    @SerializedName("invisible") @Expose private Boolean invisible;
    @SerializedName("guest_key") @Expose private String guestKey;
    @SerializedName("check_in_code") @Expose private String checkInCode;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public Integer getGender() {
      return gender;
    }

    public void setGender(Integer gender) {
      this.gender = gender;
    }

    public String getCity() {
      return city;
    }

    public void setCity(String city) {
      this.city = city;
    }

    public String getCountry() {
      return country;
    }

    public void setCountry(String country) {
      this.country = country;
    }

    public String getState() {
      return state;
    }

    public void setState(String state) {
      this.state = state;
    }

    public Boolean getInvisible() {
      return invisible;
    }

    public void setInvisible(Boolean invisible) {
      this.invisible = invisible;
    }

    public String getGuestKey() {
      return guestKey;
    }

    public void setGuestKey(String guestKey) {
      this.guestKey = guestKey;
    }

    public String getCheckInCode() {
      return checkInCode;
    }

    public void setCheckInCode(String checkInCode) {
      this.checkInCode = checkInCode;
    }
  }
}
