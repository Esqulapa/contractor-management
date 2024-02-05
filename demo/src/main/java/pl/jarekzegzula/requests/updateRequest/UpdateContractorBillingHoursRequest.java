package pl.jarekzegzula.requests.updateRequest;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.web.bind.annotation.RequestParam;

public record UpdateContractorBillingHoursRequest(
        @RequestParam(required = true) @NotNull @PositiveOrZero Double workedHours) {


}
