package pl.jarekzegzula.requests;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.RequestParam;

public record NewContractorRequest(
        @RequestParam() @NotEmpty String firstName,
        @RequestParam() @NotEmpty String lastName,
        @RequestParam() @NotNull Double salary,
        @RequestParam() @NotNull Double overtimeMultiplier,
        @RequestParam() @NotNull Double contractorPrice
) {

}