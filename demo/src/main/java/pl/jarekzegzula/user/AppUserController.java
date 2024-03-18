package pl.jarekzegzula.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import pl.jarekzegzula.converter.AppUserToAppUserDtoConverter;
import pl.jarekzegzula.requests.addNewRequest.NewAppUserRequest;
import pl.jarekzegzula.security.JwtProvider;
import pl.jarekzegzula.system.Result;
import pl.jarekzegzula.system.StatusCode;
import pl.jarekzegzula.system.exception.UserAlreadyExistException;
import pl.jarekzegzula.user.dto.AppUserDto;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("${api.endpoint.base-url}/users")
public class AppUserController {

  private final AppUserService appUserService;

  private final AppUserToAppUserDtoConverter appUserToAppUserDtoConverter;
  private final JwtProvider jwtProvider;

  @Autowired
  public AppUserController(
      AppUserService appUserService,
      AppUserToAppUserDtoConverter appUserToAppUserDtoConverter,
      JwtProvider jwtProvider) {
    this.appUserService = appUserService;
    this.appUserToAppUserDtoConverter = appUserToAppUserDtoConverter;
    this.jwtProvider = jwtProvider;
  }

  @GetMapping
  public Result getUsers() {
    return new Result(true, StatusCode.SUCCESS, "Success", appUserService.getAllUsers());
  }

  @GetMapping("/{id}")
  public Result getUserById(@PathVariable Integer id) {
    return new Result(true, StatusCode.SUCCESS, "Success", appUserService.findById(id));
  }

  @GetMapping("/dto")
  public Result getUsersDto() {
    return new Result(true, StatusCode.SUCCESS, "Success", appUserService.getAllUsersDto());
  }

  @PostMapping("/token/refresh")
  public Result refreshToken(HttpServletRequest request) {
    String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    String username = jwtProvider.verifyToken(header);

    AppUser user = appUserService.findByUsername(username);
    String roles = user.getRoles();

    String accessToken = this.jwtProvider.createAccessToken(username, roles);
    String refreshToken = this.jwtProvider.createRefreshToken(username);

    Map<String, Object> refreshedTokens = new HashMap<>();
    refreshedTokens.put("access_token", accessToken);
    refreshedTokens.put("refresh_token", refreshToken);

    return new Result(true, StatusCode.SUCCESS, "Success", refreshedTokens);
  }

  @PostMapping("/register")
  public Result createUser(@Valid @RequestBody NewAppUserRequest user)
      throws UserAlreadyExistException {

    AppUser appUser = appUserService.addNewUser(user);
    AppUserDto userDto = this.appUserToAppUserDtoConverter.convert(appUser);

    return new Result(true, StatusCode.SUCCESS, "Registered successfully", userDto);
  }

  @DeleteMapping("/{userId}")
  public Result deleteUser(@PathVariable Integer userId) {
    this.appUserService.deleteUserById(userId);
    return new Result(true, StatusCode.SUCCESS, "Delete Success");
  }
}
