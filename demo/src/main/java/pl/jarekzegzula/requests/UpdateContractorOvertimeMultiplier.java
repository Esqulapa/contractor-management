package pl.jarekzegzula.requests;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.RequestParam;

public record UpdateContractorOvertimeMultiplier(
        @RequestParam @NotNull Double multiplier
) {
}
