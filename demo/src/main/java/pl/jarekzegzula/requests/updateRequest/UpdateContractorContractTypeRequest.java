package pl.jarekzegzula.requests.updateRequest;

import jakarta.validation.constraints.NotNull;

public record UpdateContractorContractTypeRequest(
        @NotNull Integer contractType
) {

}
