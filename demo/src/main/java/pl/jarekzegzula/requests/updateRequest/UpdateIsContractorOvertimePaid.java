package pl.jarekzegzula.requests.updateRequest;


import jakarta.validation.constraints.NotNull;

public record UpdateIsContractorOvertimePaid(@NotNull Boolean value) {
}
