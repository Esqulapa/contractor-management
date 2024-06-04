package pl.jarekzegzula.requests.addNewRequest;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.RequestParam;

public record NewAppUserRequest(@RequestParam(required = true)@NotNull @NotEmpty(message = "username is required.") String username,
                                @RequestParam(required = true)@NotNull @NotEmpty(message = "password is required.") String password) {

}
