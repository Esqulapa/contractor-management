package pl.jarekzegzula.requests.updateRequest;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.RequestParam;

public record UpdateContractorHourPriceRequest(
        @RequestParam() @NotNull Double hourPrice
        ) {
}
