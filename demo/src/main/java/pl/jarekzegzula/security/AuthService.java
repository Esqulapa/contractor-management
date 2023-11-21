package pl.jarekzegzula.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import pl.jarekzegzula.converter.AppUserToAppUserDtoConverter;
import pl.jarekzegzula.user.AppUser;
import pl.jarekzegzula.user.dto.AppUserDto;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private final JwtProvider jwtProvider;
    private final AppUserToAppUserDtoConverter appUserToAppUserDtoConverter;

    public AuthService(JwtProvider jwtProvider, AppUserToAppUserDtoConverter appUserToAppUserDtoConverter) {
        this.jwtProvider = jwtProvider;
        this.appUserToAppUserDtoConverter = appUserToAppUserDtoConverter;
    }

    public Map<String, Object> createLoginInfo(Authentication authentication) {

        AppUser appUser = (AppUser) authentication.getPrincipal();
        AppUserDto convertedUser = this.appUserToAppUserDtoConverter.convert(appUser);

        String token = this.jwtProvider.createToken(authentication);

        Map<String,Object> loginResultMap = new HashMap<>();
        loginResultMap.put("userInfo", convertedUser);
        loginResultMap.put("token", token);


        return loginResultMap;
    }
}
