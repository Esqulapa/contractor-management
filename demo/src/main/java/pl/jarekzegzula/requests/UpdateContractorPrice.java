package pl.jarekzegzula.requests;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

public record UpdateContractorPrice(
        @RequestParam() @NotNull Double price
        ) {
}
