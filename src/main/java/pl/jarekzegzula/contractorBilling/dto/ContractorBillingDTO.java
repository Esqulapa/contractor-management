package pl.jarekzegzula.contractorBilling.dto;

import lombok.Data;
import pl.jarekzegzula.contractorBilling.ContractorBilling;

import java.math.BigDecimal;
import java.time.Month;
import java.time.Year;

@Data
public class ContractorBillingDTO {

  private Integer id;
  private Integer contractorId;
  private Double workedHours;
  private Year year;
  private Month month;
  private BigDecimal contractorRemuneration;
  private BigDecimal clientCharge;
  private BigDecimal profit;

  public ContractorBillingDTO(ContractorBilling contractorBilling) {
    this.id = contractorBilling.getId();
    this.contractorId = contractorBilling.getContractor().getId();
    this.workedHours = contractorBilling.getWorkedHours();
    this.year = contractorBilling.getYear();
    this.month = contractorBilling.getMonth();
    this.contractorRemuneration = contractorBilling.getContractorRemuneration();
    this.clientCharge = contractorBilling.getClientCharge();
    this.profit = contractorBilling.getProfit();
  }
}
