package pl.jarekzegzula.user;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.jarekzegzula.converter.AppUserToAppUserDtoConverter;
import pl.jarekzegzula.requests.addNewRequest.NewAppUserRequest;
import pl.jarekzegzula.system.exception.ObjectNotFoundException;
import pl.jarekzegzula.system.exception.UserAlreadyExistException;
import pl.jarekzegzula.user.dto.AppUserDto;

import java.util.List;

@Service
public class AppUserService implements UserDetailsService {

  private final AppUserRepository appUserRepository;

  private final PasswordEncoder passwordEncoder;

  private final AppUserToAppUserDtoConverter appUserToAppUserDtoConverter;

  public AppUserService(
      AppUserRepository appUserRepository,
      PasswordEncoder passwordEncoder,
      AppUserToAppUserDtoConverter appUserToAppUserDtoConverter) {
    this.appUserRepository = appUserRepository;
    this.passwordEncoder = passwordEncoder;

    this.appUserToAppUserDtoConverter = appUserToAppUserDtoConverter;
  }

  public AppUser addNewUser(NewAppUserRequest request) throws UserAlreadyExistException {

    if (appUserRepository.existsByUsername(request.username())) {
      throw new UserAlreadyExistException("Requested username is already taken.");
    }

    AppUser appUser = new AppUser();
    appUser.setUsername(request.username());
    appUser.setPassword(this.passwordEncoder.encode(request.password()));
    appUser.setRoles("admin");
    appUser.setEnabled(true);
    return appUserRepository.save(appUser);
  }

  public AppUser findById(Integer id) {
    return appUserRepository
        .findById(id)
        .orElseThrow(() -> new ObjectNotFoundException("user", id));
  }

  public List<AppUserDto> getAllUsersDto() {
    List<AppUser> all = appUserRepository.findAll();
    return appUserToAppUserDtoConverter.convertList(all);
  }

  public List<AppUser> getAllUsers() {
    return appUserRepository.findAll();
  }

  public void deleteUserById(Integer id) {
    this.appUserRepository.findById(id).orElseThrow(() -> new ObjectNotFoundException("user", id));
    this.appUserRepository.deleteById(id);
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return this.appUserRepository
        .findByUsername(username)
        .orElseThrow(
            (() -> new UsernameNotFoundException("username " + username + " is not found")));
  }
}
