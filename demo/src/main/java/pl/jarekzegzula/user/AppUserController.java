package pl.jarekzegzula.user;


import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pl.jarekzegzula.converter.AppUserToAppUserDtoConverter;
import pl.jarekzegzula.requests.NewAppUserRequest;
import pl.jarekzegzula.system.Result;
import pl.jarekzegzula.system.StatusCode;
import pl.jarekzegzula.system.exception.UserAlreadyExistException;
import pl.jarekzegzula.user.dto.AppUserDto;


@RestController
@RequestMapping("${api.endpoint.base-url}/users")
public class AppUserController {

    private final AppUserService appUserService;

    private final AppUserToAppUserDtoConverter appUserToAppUserDtoConverter;

    @Autowired
    public AppUserController(AppUserService appUserService, AppUserToAppUserDtoConverter appUserToAppUserDtoConverter) {
        this.appUserService = appUserService;
        this.appUserToAppUserDtoConverter = appUserToAppUserDtoConverter;
    }



    @GetMapping
    public Result getUsers(){
        return new Result(true,StatusCode.SUCCESS,"Success", appUserService.getAllUsers());

    }
    @GetMapping("/{id}")
    public Result getUserById(@PathVariable Integer id) {
        return new Result(true,StatusCode.SUCCESS,"Success",appUserService.findById(id));
    }

    @GetMapping("/dto")
    public Result getUsersDto(){
        return new Result(true,StatusCode.SUCCESS,"Success", appUserService.getAllUsersDto());

    }

    @PostMapping("/register")
    public Result createUser(@Valid @RequestBody NewAppUserRequest user) throws UserAlreadyExistException {

        AppUser appUser = appUserService.addNewUser(user);
        AppUserDto userDto = this.appUserToAppUserDtoConverter.convert(appUser);


        return new Result(true,StatusCode.SUCCESS,"Registered successfully",userDto);
    }
    @DeleteMapping("/{userId}")
    public Result deleteUser(@PathVariable Integer userId) {
        this.appUserService.deleteUserById(userId);
        return new Result(true, StatusCode.SUCCESS, "Delete Success");
    }




}
