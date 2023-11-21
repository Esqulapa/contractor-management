package pl.jarekzegzula.contractorBilling;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.jarekzegzula.contractorBilling.Dto.ContractorBillingDTO;

import java.time.YearMonth;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractorBillingReportByMonth {

    private  YearMonth yearMonth;

    private  Double workingHours;

    private  List<ContractorBillingDTO> contractorBillings;





}
