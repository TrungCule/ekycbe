package vnpay.com.vn.web.rest.vm;

import javax.validation.constraints.NotBlank;

public class TokenVM {
  @NotBlank
  private String refreshToken;

  public String getRefreshToken() {
    return refreshToken;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }
}
