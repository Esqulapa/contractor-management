package pl.jarekzegzula.contractorBilling;

import lombok.Data;
import pl.jarekzegzula.calc.Calculator;
import pl.jarekzegzula.contractorBilling.dto.ContractorBillingDTO;

import java.math.BigDecimal;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;

@Data
public class ContractorBillingReportByMonth {

  private YearMonth yearMonth;

  private Double workingHours;

  private BigDecimal expense;

  private BigDecimal income;

  private BigDecimal profit;

  private List<ContractorBillingDTO> contractorBillingDTOS;

  public ContractorBillingReportByMonth(
      List<ContractorBillingDTO> contractorsByYearAndMonth, Year year, Month month) {
    this.yearMonth = YearMonth.of(year.getValue(), month);
    this.workingHours = Calculator.countWorkingHoursWithoutWeekendsInMonth(year, month);
    this.expense =
        Calculator.sumContractorsBillingFinancial(
            contractorsByYearAndMonth, ContractorBillingDTO::getContractorRemuneration);
    this.income =
        Calculator.sumContractorsBillingFinancial(
            contractorsByYearAndMonth, ContractorBillingDTO::getClientCharge);
    this.profit = Calculator.calculateProfit(income, expense);
    this.contractorBillingDTOS = contractorsByYearAndMonth;
  }
}
