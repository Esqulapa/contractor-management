package pl.jarekzegzula.contractor;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.jarekzegzula.requests.NewContractorRequest;
import pl.jarekzegzula.requests.UpdateContractorOvertimeMultiplier;
import pl.jarekzegzula.requests.UpdateContractorPrice;
import pl.jarekzegzula.requests.UpdateContractorSalaryRequest;
import pl.jarekzegzula.system.exception.ContractorAlreadyExistInGivenTimeException;
import pl.jarekzegzula.system.exception.ObjectNotFoundException;
import pl.jarekzegzula.system.exception.ValueUnchangedException;
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

    public List<Contractor> getContractors(){
        return contractorRepository.findAll();
    }

    public Contractor getContractorById(Integer id){
        return this.contractorRepository.findById(id).orElseThrow(()-> new ObjectNotFoundException("contractor", id));
    }
    //todo przedyskutować overtimemultipliera czy dodawać defaultowo 1.5
    // i za pomocą update requestu zmieniać, czy dać możliwość dodawania go od razu z requesta.

    public Contractor addNewContractor(NewContractorRequest request) {

        if(!contractorRepository.existsByFirstNameAndLastName(request.firstName(),request.lastName())){

            Contractor contractor = new Contractor();
            contractor.setFirstName(request.firstName());
            contractor.setLastName(request.lastName());
            contractor.setSalary(request.salary());
            contractor.setOvertimeMultiplier(request.overtimeMultiplier());
            contractor.setContractorPrice(request.contractorPrice());
            return contractorRepository.save(contractor);

        }else {
            throw new ContractorAlreadyExistInGivenTimeException("Contractor already exist");
        }

    }

    public void deleteContractorById(Integer id){

        this.contractorRepository.findById(id).orElseThrow(()-> new ObjectNotFoundException("contractor", id));

       this.contractorRepository.deleteById(id);
    }

    //todo Zapytać Pera czy zmienić void na contractor i zapisywać w repo poprawionego contractora.

    public void updateContractorSalary(UpdateContractorSalaryRequest updateRequest, Integer id){

        Contractor contractor = contractorRepository.findById(id)
                .orElseThrow(
                        () -> new ObjectNotFoundException("contractor", id ));

        if (!Objects.equals(contractor.getSalary(),updateRequest.salary()) && updateRequest.salary() >= ZERO){
            contractor.setSalary(updateRequest.salary());

        } else {
            throw new ValueUnchangedException("Salary remains the same or the value is incorrect");
        }

    }

    public void updateContractorPrice(UpdateContractorPrice updateRequest, Integer id){

        Contractor contractor = contractorRepository.findById(id)
                .orElseThrow(
                        () -> new ObjectNotFoundException("contractor", id ));

        if (!Objects.equals(contractor.getContractorPrice(),updateRequest.price())
                && updateRequest.price() >= ZERO) {
            contractor.setContractorPrice(updateRequest.price());

        } else {
            throw new ValueUnchangedException("Contractor price remains the same or the value is incorrect");
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



}
