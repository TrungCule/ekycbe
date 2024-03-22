package vnpay.com.vn.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vnpay.com.vn.domain.RefreshToken;
import vnpay.com.vn.domain.User;
import vnpay.com.vn.repository.RefreshTokenRepository;
import vnpay.com.vn.repository.UserRepository;
import vnpay.com.vn.security.AuthoritiesConstants;
import vnpay.com.vn.web.rest.errors.TokenRefreshException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

  @Autowired
  private RefreshTokenRepository refreshTokenRepository;

  @Autowired
  private UserRepository userRepository;

  public Optional<RefreshToken> findByToken(String token) {
    return refreshTokenRepository.findByToken(token);
  }

  public RefreshToken createRefreshToken(User user) {
    RefreshToken refreshToken = new RefreshToken();

    refreshToken.setUser(user);
    refreshToken.setExpiryDate(Instant.now().plusMillis(AuthoritiesConstants.tokenValidityInMillisecondsForRefresh));
    refreshToken.setToken(UUID.randomUUID().toString());

    refreshToken = refreshTokenRepository.save(refreshToken);
    return refreshToken;
  }

  public RefreshToken verifyExpiration(RefreshToken token) {
    if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
      refreshTokenRepository.delete(token);
      throw new TokenRefreshException(token.getToken(), "Refresh token was expired. Please make a new signin request");
    }

    return token;
  }

  @Transactional
  public int deleteByUserId(Long userId) {
    return refreshTokenRepository.deleteByUser(userRepository.findById(userId).get());
  }
}
