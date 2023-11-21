package pl.jarekzegzula.contractor;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.jarekzegzula.requests.NewContractorRequest;
import pl.jarekzegzula.requests.UpdateContractorSalaryRequest;
import pl.jarekzegzula.system.exception.ContractorAlreadyExistInGivenTimeException;
import pl.jarekzegzula.system.exception.ObjectNotFoundException;
import pl.jarekzegzula.system.exception.SalaryUnchangedException;
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

    public Contractor addNewContractor(NewContractorRequest request) {

        if(!contractorRepository.existsByFirstNameAndLastName(request.firstName(),request.lastName())){

            Contractor contractor = new Contractor();
            contractor.setFirstName(request.firstName());
            contractor.setLastName(request.lastName());
            contractor.setSalary(request.salary());
            return contractorRepository.save(contractor);

        }else {
            throw new ContractorAlreadyExistInGivenTimeException("Contractor already exist");
        }

    }

    public void deleteContractorById(Integer id){

        this.contractorRepository.findById(id).orElseThrow(()-> new ObjectNotFoundException("contractor", id));

       this.contractorRepository.deleteById(id);
    }

    public void updateContractorSalary(UpdateContractorSalaryRequest updateRequest, Integer id){

        Contractor contractor = contractorRepository.findById(id)
                .orElseThrow(
                        () -> new ObjectNotFoundException("contractor", id ));

        if (!Objects.equals(contractor.getSalary(),updateRequest.salary()) && updateRequest.salary() >= ZERO){
            contractor.setSalary(updateRequest.salary());

        } else {
            throw new SalaryUnchangedException("Salary remains the same or the value is incorrect");
        }

    }



}
