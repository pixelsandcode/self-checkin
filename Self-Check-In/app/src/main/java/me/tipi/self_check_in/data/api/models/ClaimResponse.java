package me.tipi.self_check_in.data.api.models;

public class ClaimResponse extends BaseResponse{
  public final Guest data;

  public ClaimResponse(Guest data) {
    this.data = data;
  }
}
