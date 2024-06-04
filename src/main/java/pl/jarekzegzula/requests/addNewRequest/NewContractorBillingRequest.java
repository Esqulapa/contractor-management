package pl.jarekzegzula.requests.addNewRequest;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Month;
import java.time.Year;

public record NewContractorBillingRequest(
        @RequestParam(required = true)@NotNull  Integer id,
        @RequestParam(required = true)@NotNull  Double workedHours,
        @RequestParam(required = true)@NotNull  Year year,
        @RequestParam(required = true)@NotNull  Month month
        ) {
}
