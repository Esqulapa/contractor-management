package pl.jarekzegzula.requests.addNewRequest;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.RequestParam;

public record NewContractorRequest(
        @RequestParam() @NotEmpty String firstName,
        @RequestParam() @NotEmpty String lastName,
        @RequestParam() @NotNull Integer contractType,
        @RequestParam() @NotNull Double hourlyRate,
        @RequestParam() @NotNull Integer monthlyHourLimit,
        @RequestParam() @NotNull Boolean isOvertimePaid,
        @RequestParam() @NotNull Double overtimeMultiplier,
        @RequestParam() @NotNull Double contractorHourPrice
) {

}