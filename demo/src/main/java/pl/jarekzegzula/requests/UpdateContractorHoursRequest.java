package pl.jarekzegzula.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.web.bind.annotation.RequestParam;

public record UpdateContractorHoursRequest(
                                      @RequestParam(required = true) @NotNull @PositiveOrZero Double workedHours) {


}
