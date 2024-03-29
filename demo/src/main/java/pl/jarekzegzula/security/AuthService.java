package pl.jarekzegzula.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import pl.jarekzegzula.converter.AppUserToAppUserDtoConverter;
import pl.jarekzegzula.user.AppUser;
import pl.jarekzegzula.user.dto.AppUserDto;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AuthService {

  private final JwtProvider jwtProvider;
  private final AppUserToAppUserDtoConverter appUserToAppUserDtoConverter;

  public AuthService(
      JwtProvider jwtProvider, AppUserToAppUserDtoConverter appUserToAppUserDtoConverter) {
    this.jwtProvider = jwtProvider;
    this.appUserToAppUserDtoConverter = appUserToAppUserDtoConverter;
  }

  public Map<String, Object> createLoginInfo(Authentication authentication) {

    String authorities =
            authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(" "));

    AppUser appUser = (AppUser) authentication.getPrincipal();
    AppUserDto convertedUser = this.appUserToAppUserDtoConverter.convert(appUser);
    String username = convertedUser.username();

    String accessToken = this.jwtProvider.createAccessToken(username,authorities);
    String refreshToken = this.jwtProvider.createRefreshToken(username);

    Map<String, Object> loginResultMap = new HashMap<>();
    loginResultMap.put("userInfo", convertedUser);
    loginResultMap.put("access_token", accessToken);
    loginResultMap.put("refresh_token", refreshToken);

    return loginResultMap;
  }
}
