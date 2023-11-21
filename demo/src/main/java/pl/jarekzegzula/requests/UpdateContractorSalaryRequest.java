package pl.jarekzegzula.requests;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.RequestParam;

public record UpdateContractorSalaryRequest(
        @RequestParam(required = true)@NotNull Double salary
) {
}
