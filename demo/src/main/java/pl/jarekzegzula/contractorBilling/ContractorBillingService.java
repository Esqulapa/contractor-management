package pl.jarekzegzula.contractorBilling;


import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.jarekzegzula.contractor.Contractor;
import pl.jarekzegzula.contractor.ContractorRepository;
import pl.jarekzegzula.contractorBilling.dto.ContractorBillingDTO;
import pl.jarekzegzula.requests.addNewRequest.NewContractorBillingRequest;
import pl.jarekzegzula.requests.updateRequest.UpdateContractorBillingHoursRequest;
import pl.jarekzegzula.system.exception.ContractorAlreadyExistInGivenTimeException;
import pl.jarekzegzula.system.exception.ObjectNotFoundException;
import pl.jarekzegzula.system.exception.SameHoursOrLessThanZeroException;

import static pl.jarekzegzula.system.Constants.*;


import java.time.*;
import java.util.List;
import java.util.Objects;


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

    public ContractorBilling getContractorBillingById(Integer id) {
        return this.contractorBillingRepository.findById(id).orElseThrow(() -> new ObjectNotFoundException("contractor billing", id));
    }

    public ContractorBilling addNewContractorBilling(NewContractorBillingRequest request) {

        Contractor contractor = contractorRepository.findById(request.id())
                .orElseThrow(
                        () -> new ObjectNotFoundException("contractor", request.id()));

        if (!(request.workedHours() >= 0) ||
                contractorBillingRepository
                        .existsByContractorIdAndYearAndMonth(request.id(), request.year(), request.month())) {

            if (!(request.workedHours() >= 0)) {
                throw new SameHoursOrLessThanZeroException("Given data is less or equal to zero");

            } else {
                throw new ContractorAlreadyExistInGivenTimeException("Contractor billing at given date already exists");
            }
        } else return contractorBillingRepository.save(new ContractorBilling(request, contractor));

    }


    public void updateContractorBillingWorkedHours(UpdateContractorBillingHoursRequest updateRequest, Integer id) {

        ContractorBilling contractor = contractorBillingRepository.findById(id).orElseThrow(
                () -> new ObjectNotFoundException("contractor billing", id));

        if (!Objects.equals(updateRequest.workedHours(), contractor.getWorkedHours()) && updateRequest.workedHours() >= ZERO) {
            contractor.setWorkedHours(updateRequest.workedHours());
        } else {
            throw new SameHoursOrLessThanZeroException("The given data to be changed is the same or less than zero");
        }

    }

    public ContractorBillingReportByMonth getContractorBillingsMonthlyReport(Year year, Month month) {


        List<ContractorBilling> contractorsByYearAndMonth = this.contractorBillingRepository
                .findByYearAndMonth(year, month).orElseThrow(
                        () -> new ObjectNotFoundException("contractor billings", year.toString(), month.name()));

        List<ContractorBillingDTO> contractorBillingDTOS = getContractorBillingDTOS(contractorsByYearAndMonth);

        if (!contractorsByYearAndMonth.isEmpty()) {

            return new ContractorBillingReportByMonth(contractorBillingDTOS,year,month);

        } else {
            throw new ObjectNotFoundException("contractor billings", year.toString(), month.name());
        }
    }

    public static List<ContractorBillingDTO> getContractorBillingDTOS(List<ContractorBilling> contractorsByYearAndMonth) {
        return contractorsByYearAndMonth.stream().map(ContractorBillingDTO::new).toList();
    }

    public void deleteContractorBillingById(Integer id) {
        this.contractorBillingRepository.findById(id).orElseThrow(() -> new ObjectNotFoundException("Contractor Billing", id));

        this.contractorBillingRepository.deleteById(id);
    }

}
