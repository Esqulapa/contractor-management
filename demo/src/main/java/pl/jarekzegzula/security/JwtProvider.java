package pl.jarekzegzula.security;

import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class JwtProvider {

  public static final long ACCESS_TOKEN_LIFESPAN = 15 * 60 * 1000;
  public static final long REFRESH_TOKEN_LIFESPAN = 120 * 60 * 1000;
  private final JwtEncoder jwtEncoder;
  private final JwtDecoder jwtDecoder;
  private final String BEARER = "Bearer ";

  public JwtProvider(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder) {
    this.jwtEncoder = jwtEncoder;
      this.jwtDecoder = jwtDecoder;
  }

  public String createAccessToken(String username, String authorities) {
    Instant now = Instant.now();

    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .issuer("self")
            .issuedAt(now)
            .expiresAt(now.plus(ACCESS_TOKEN_LIFESPAN, ChronoUnit.MILLIS))
            .subject(username)
            .claim("authorities", authorities)
            .build();

    return this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
  }

  public String createRefreshToken(String username){
    Instant now = Instant.now();

    JwtClaimsSet claims =
            JwtClaimsSet.builder()
                    .issuer("self")
                    .issuedAt(now)
                    .expiresAt(now.plus(REFRESH_TOKEN_LIFESPAN, ChronoUnit.MILLIS))
                    .subject(username)
                    .build();

    return this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
  }

  public String verifyToken(String authHeader){
    String token = authHeader.substring(BEARER.length());
    Jwt decodedjwt = jwtDecoder.decode(token);
    return decodedjwt.getSubject();
  }
}
