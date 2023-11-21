package pl.jarekzegzula.contractorBilling;


import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.jarekzegzula.contractor.Contractor;
import pl.jarekzegzula.contractor.ContractorRepository;
import pl.jarekzegzula.contractorBilling.Dto.ContractorBillingDTO;
import pl.jarekzegzula.requests.NewContractorBillingRequest;
import pl.jarekzegzula.requests.UpdateContractorHoursRequest;
import pl.jarekzegzula.system.exception.ContractorAlreadyExistInGivenTimeException;
import pl.jarekzegzula.system.exception.ObjectNotFoundException;
import pl.jarekzegzula.system.exception.SameHoursOrLessThanZeroException;

import static pl.jarekzegzula.system.Constants.*;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
@Transactional
public class ContractorBillingService {

    private final ContractorBillingRepository contractorBillingRepository;
    private final ContractorRepository contractorRepository;

    @Autowired
    public ContractorBillingService(ContractorBillingRepository contractorBillingRepository, ContractorRepository contractorRepository) {
        this.contractorBillingRepository = contractorBillingRepository;
        this.contractorRepository = contractorRepository;
    }

    public List<ContractorBilling> findAll() {

        return contractorBillingRepository.findAll();
    }

    public ContractorBilling getContractorBillingById(Integer id){
        return this.contractorBillingRepository.findById(id).orElseThrow(()-> new ObjectNotFoundException("contractor billing", id));
    }

    public ContractorBilling addNewContractorBilling(NewContractorBillingRequest request) {

        Contractor contractor = contractorRepository.findById(request.id())
                .orElseThrow(
                        () -> new ObjectNotFoundException("contractor", request.id()));

        if (!(request.workedHours() >= 0)) {
            throw new SameHoursOrLessThanZeroException("Given data is less or equal to zero");
        }

        if(contractorBillingRepository.existsByContractor_IdAndYearAndMonth(request.id(),request.year(),request.month())){
            throw new ContractorAlreadyExistInGivenTimeException("Contractor billing at given date already exist");
        } else {
                ContractorBilling contractorBilling = new ContractorBilling();

                contractorBilling.setContractor(contractor);
                contractorBilling.setWorkedHours(request.workedHours());
                contractorBilling.setYear(request.year());
                contractorBilling.setMonth(request.month());

                contractorBilling.setPayment(
                        calculatePayment(
                                countWorkingHoursWithoutWeekendsInMonth(
                                        request.year(), request.month()),
                                request.workedHours(),
                                contractor.getSalary())
                );

            return contractorBillingRepository.save(contractorBilling);
        }


    }


    public void updateContractorBillingWorkedHours(UpdateContractorHoursRequest updateRequest, Integer id) throws SameHoursOrLessThanZeroException {

        ContractorBilling contractor = contractorBillingRepository.findById(id).orElseThrow(
                () -> new ObjectNotFoundException("contractor billing", id));

        if (!Objects.equals(updateRequest.workedHours(), contractor.getWorkedHours()) && updateRequest.workedHours() >= ZERO ){
            contractor.setWorkedHours(updateRequest.workedHours());
        } else {
            throw new SameHoursOrLessThanZeroException("the given data to be changed is the same or less than zero");
        }

    }


    public ContractorBillingReportByMonth getContractorsWorkedHoursInGivenMonth(Year year, Month month) {



        List<ContractorBilling> contractorsByYearAndMonth = this.contractorBillingRepository.
                findByYearAndMonth(year, month).orElseThrow(() -> new ObjectNotFoundException("contractor billings", year.toString(), month.name()));



        if(!contractorsByYearAndMonth.isEmpty()){

            List<ContractorBillingDTO> contractorBillingDTOS = contractorsByYearAndMonth.stream().map(ContractorBillingDTO::new).toList();
            ContractorBillingReportByMonth contractorBillingReportByMonth = new ContractorBillingReportByMonth();
            contractorBillingReportByMonth.setContractorBillings(contractorBillingDTOS);
            contractorBillingReportByMonth.setYearMonth(YearMonth.of(year.getValue(), month));
            contractorBillingReportByMonth.setWorkingHours(countWorkingHoursWithoutWeekendsInMonth(year, month));

            return contractorBillingReportByMonth;
        }else {
            throw new ObjectNotFoundException("contractor billings", year.toString(), month.name());
        }
    }

    public static BigDecimal calculatePayment(Double fullTime, Double workedHours, Double salary) {


        if (workedHours <= ZERO_HOURS) {
            return new BigDecimal(ZERO_HOURS).setScale(2, RoundingMode.HALF_UP);

        } else if (workedHours <= fullTime) {

            double result = workedHours * (salary / fullTime);
            BigDecimal bigDecimal = new BigDecimal(result);
            return bigDecimal.setScale(2, RoundingMode.HALF_UP);

        } else {

            double fullTimePayment = fullTime * (salary / fullTime);

            double overtimePayment = (workedHours - fullTime) * (OVER_TIME_MULTIPLIER * salary / fullTime);

            double result = fullTimePayment + overtimePayment;

            BigDecimal bigDecimal = new BigDecimal(result);

            return bigDecimal.setScale(2, RoundingMode.HALF_UP);
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

    public void deleteContractorBillingById(Integer id) {
        this.contractorBillingRepository.findById(id).orElseThrow(()-> new ObjectNotFoundException("Contractor Billing", id));

        this.contractorBillingRepository.deleteById(id);
    }
}
