package pl.jarekzegzula.contractorBilling.Dto;

import lombok.Data;
import pl.jarekzegzula.contractorBilling.ContractorBilling;

import java.math.BigDecimal;
import java.time.Month;
import java.time.Year;

@Data
public class ContractorBillingDTO {
    private Integer id;
    private Integer contractorId;// Include contractor id in the DTO
    private Double workedHours;
    private Year year;
    private Month month;
    private BigDecimal payment;

    public ContractorBillingDTO(ContractorBilling contractorBilling) {
        this.id = contractorBilling.getId();
        this.contractorId = contractorBilling.getContractor().getId();
        this.workedHours = contractorBilling.getWorkedHours();
        this.year = contractorBilling.getYear();
        this.month = contractorBilling.getMonth();
        this.payment = contractorBilling.getPayment();
    }
}
