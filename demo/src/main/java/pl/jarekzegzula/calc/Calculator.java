package pl.jarekzegzula.calc;

import pl.jarekzegzula.contractor.Contractor;
import pl.jarekzegzula.contractorBilling.dto.ContractorBillingDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static pl.jarekzegzula.system.Constants.WORKDAY;

public class Calculator {


    public static Double calculateMonthlyEarningsForContractor(Double hourlyRate,Integer hourLimit){
        return  hourlyRate * hourLimit;
    }

    public static BigDecimal sumBigDecimalFieldFromContractorBillingList(
            List<ContractorBillingDTO> contractorsByYearAndMonth, Function<ContractorBillingDTO, BigDecimal> fieldExtractor) {
        return contractorsByYearAndMonth
                .stream()
                .map(fieldExtractor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static BigDecimal calculateContractorPayment(Double workedHours, Contractor contractor) {

        return switch (contractor.getContractType()) {
            case CONTRACT_OF_EMPLOYMENT -> calculatePaymentForContractOfEmployment(workedHours, contractor);
            case CONTRACT_B2B -> calculatePaymentForB2BContract(workedHours, contractor);
            case CONTRACT_OF_MANDATE -> calculatePaymentForContractOfMandate(workedHours, contractor);
        };
    }

    public static BigDecimal calculateProfit(BigDecimal clientCharge, BigDecimal contractorRemuneration) {
        return clientCharge.subtract(contractorRemuneration);
    }

    public static BigDecimal calculateClientsChargeFromContractorHours(Double workedHours, Contractor contractor) {
        return BigDecimal.valueOf(workedHours * contractor.getContractorHourPrice());
    }

    public static BigDecimal calculatePaymentForContractOfMandate(Double workedHours, Contractor contractor) {
        return BigDecimal.valueOf(workedHours * contractor.getHourlyRate()).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal calculatePaymentForB2BContract(Double workedHours, Contractor contractor) {
        if (workedHours > contractor.getMonthlyHourLimit() && contractor.getIsOvertimePaid()) {
            Double overtimeHours = workedHours - contractor.getMonthlyHourLimit();
            double overtimePayment = overtimeHours * contractor.getHourlyRate() * contractor.getOvertimeMultiplier();

            return BigDecimal
                    .valueOf(contractor.getMonthlyEarnings() + overtimePayment)
                    .setScale(2, RoundingMode.HALF_UP);
        } else
            return BigDecimal
                    .valueOf(workedHours * contractor.getHourlyRate())
                    .setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal calculatePaymentForContractOfEmployment(Double workedHours, Contractor contractor) {
        if (Objects.equals(contractor.getMonthlyHourLimit().doubleValue(), workedHours)) {
            return BigDecimal.valueOf(contractor.getMonthlyEarnings());
        } else if (workedHours < contractor.getMonthlyHourLimit()) {
            return BigDecimal.valueOf(workedHours * contractor.getHourlyRate());
        } else {
            double overtimePayment =
                    (workedHours - contractor.getMonthlyHourLimit())
                            * contractor.getHourlyRate() * contractor.getOvertimeMultiplier();

            return BigDecimal
                    .valueOf(contractor.getMonthlyEarnings() + overtimePayment)
                    .setScale(2, RoundingMode.HALF_UP);
        }
    }

    public static Double countWorkingHoursWithoutWeekendsInMonth(Year year, Month month) {

        double totalWorkingHours = 0.0;

        LocalDate startDate = LocalDate.of(year.getValue(), month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        while (!startDate.isAfter(endDate)) {
            if (startDate.getDayOfWeek() != DayOfWeek.SATURDAY && startDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
                totalWorkingHours += WORKDAY;
            }
            startDate = startDate.plusDays(1);
        }

        return totalWorkingHours;
    }
}
