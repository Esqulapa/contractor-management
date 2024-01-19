package pl.jarekzegzula.contractor;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.jarekzegzula.contract.ContractType;
import pl.jarekzegzula.requests.addNewRequest.NewContractorRequest;
import pl.jarekzegzula.requests.updateRequest.*;
import pl.jarekzegzula.system.exception.ContractorAlreadyExistInGivenTimeException;
import pl.jarekzegzula.system.exception.IllegalContractTypeArgument;
import pl.jarekzegzula.system.exception.ObjectNotFoundException;
import pl.jarekzegzula.system.exception.ValueUnchangedException;

import static pl.jarekzegzula.calc.Calculator.calculateMonthlyEarningsForContractor;
import static pl.jarekzegzula.system.Constants.*;

import java.util.List;
import java.util.Objects;

@Transactional
@Service
public class ContractorService {

    private final ContractorRepository contractorRepository;

    @Autowired
    public ContractorService(ContractorRepository contractorRepository) {
        this.contractorRepository = contractorRepository;
    }

    public Contractor getContractorById(Integer id){
        return this.contractorRepository.findById(id).orElseThrow(()-> new ObjectNotFoundException("contractor", id));
    }
    public List<Contractor> getContractors(){
        return contractorRepository.findAll();
    }
    public Contractor addNewContractor(NewContractorRequest request) {

        if(!contractorRepository.existsByFirstNameAndLastName(request.firstName(),request.lastName())){

            return contractorRepository.save(new Contractor(request));

        }else {
            throw new ContractorAlreadyExistInGivenTimeException("Contractor already exist");
        }

    }
    public void deleteContractorById(Integer id){

        this.contractorRepository.findById(id).orElseThrow(()-> new ObjectNotFoundException("contractor", id));

       this.contractorRepository.deleteById(id);
    }
    public void updateContractorHourlyRateAndMonthlyEarnings(UpdateContractorHourlyRateRequest updateRequest, Integer id){

        Contractor contractor = findContractor(id);

        if (!Objects.equals(contractor.getHourlyRate(),updateRequest.hourlyRate()) && updateRequest.hourlyRate() >= ZERO){
            contractor.setHourlyRate(updateRequest.hourlyRate());
            contractor.setMonthlyEarnings(calculateMonthlyEarningsForContractor(updateRequest.hourlyRate(), contractor.getMonthlyHourLimit()));

        } else {
            throw new ValueUnchangedException("Hourly rate remains the same or the value is incorrect");
        }

    }
    public void updateContractorOvertimeMultiplier(UpdateContractorOvertimeMultiplier updateRequest, Integer id){

        Contractor contractor = contractorRepository.findById(id)
                .orElseThrow(
                        () -> new ObjectNotFoundException("contractor", id ));

        if (!Objects.equals(contractor.getOvertimeMultiplier(),updateRequest.multiplier())
                && updateRequest.multiplier() >= ZERO) {
            contractor.setOvertimeMultiplier(updateRequest.multiplier());

        } else {
            throw new ValueUnchangedException("Contractor overtime multiplier remains the same or the value is incorrect");
        }

    }
    public void updateMonthlyHourLimitAndMonthlyEarnings(UpdateMonthlyHourLimitRequest updateRequest, Integer id){

        Contractor contractor = findContractor(id);

        if (!Objects.equals(contractor.getMonthlyHourLimit(),updateRequest.hours())
                && updateRequest.hours() >= ZERO) {
            contractor.setMonthlyHourLimit(updateRequest.hours());
            contractor.setMonthlyEarnings(
                    calculateMonthlyEarningsForContractor(contractor.getHourlyRate(), updateRequest.hours()
                    ));

        } else {
            throw new ValueUnchangedException("Contractor hour limit remains the same or the value is incorrect");
        }
    }
    public void updateContractorHourPrice(UpdateContractorHourPriceRequest updateRequest, Integer id){

        Contractor contractor = findContractor(id);

        if (!Objects.equals(contractor.getContractorHourPrice(),updateRequest.hourPrice())
                && updateRequest.hourPrice() >= ZERO) {
            contractor.setContractorHourPrice(updateRequest.hourPrice());

        } else {
            throw new ValueUnchangedException("Contractor hour price remains the same or the value is incorrect");
        }

    }
    public void updateIsOvertimePaid(UpdateIsContractorOvertimePaid request, Integer id){
        Contractor contractor = findContractor(id);

        if(!contractor.getIsOvertimePaid().equals(request.value()))
        {
            contractor.setIsOvertimePaid(request.value());
        }else {
        throw new ValueUnchangedException("Given value is the same or incorrect");
        }

    }
    public void updateContractType(UpdateContractorContractTypeRequest request, Integer id){

        Contractor contractor = findContractor(id);

        if (!ContractType.isValidContractTypeValue(request.contractType())) {
            throw new IllegalContractTypeArgument("Invalid ContractType value: " + request.contractType());
        }

        if(!contractor.getContractType().equals(ContractType.fromValue(request.contractType()))
        && request.contractType() > ZERO )
        {
            contractor.setContractType(ContractType.fromValue(request.contractType()));
        }else {
            throw new ValueUnchangedException("Given Contract type is the same");
        }

    }
    private Contractor findContractor(Integer id) {
        return contractorRepository.findById(id)
                .orElseThrow(
                        () -> new ObjectNotFoundException("contractor", id));
    }





}
