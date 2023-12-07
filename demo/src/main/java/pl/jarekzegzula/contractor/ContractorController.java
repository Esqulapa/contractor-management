package pl.jarekzegzula.contractor;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pl.jarekzegzula.requests.NewContractorRequest;
import pl.jarekzegzula.requests.UpdateContractorOvertimeMultiplier;
import pl.jarekzegzula.requests.UpdateContractorPrice;
import pl.jarekzegzula.requests.UpdateContractorSalaryRequest;
import pl.jarekzegzula.system.Result;
import pl.jarekzegzula.system.StatusCode;
import pl.jarekzegzula.system.exception.ContractorAlreadyExistInGivenTimeException;

@RestController
@RequestMapping("${api.endpoint.base-url}/contractor")
public class ContractorController {

    private final ContractorService contractorService;

    @Autowired
    public ContractorController(ContractorService contractorService) {
        this.contractorService = contractorService;
    }

    @GetMapping()
    public Result getAllContractors() {
        return new Result(true, StatusCode.SUCCESS,"Success",contractorService.getContractors());

    }


    @GetMapping("{contractorId}")
    public Result getContractorById(@PathVariable("contractorId") Integer id){
        return new Result(true,StatusCode.SUCCESS,"Success",contractorService.getContractorById(id));
    }

    @PostMapping
    public Result addContractor(@Valid @RequestBody NewContractorRequest request) throws ContractorAlreadyExistInGivenTimeException {
        contractorService.addNewContractor(request);
    return new Result(true,StatusCode.SUCCESS,"Contractor added successfully",request);
    }

    @PutMapping("salary/{contractorId}")
    public Result updateContractorSalary(@PathVariable("contractorId") Integer id,
                                  @RequestBody @Valid UpdateContractorSalaryRequest updateRequest) {

        contractorService.updateContractorSalary(updateRequest,id);

        return new Result(true,StatusCode.SUCCESS,"Update success");
    }

    @PutMapping("multiplier/{contractorId}")
    public Result updateContractorOvertimeMultiplier(@PathVariable("contractorId") Integer id,
                                         @RequestBody @Valid UpdateContractorOvertimeMultiplier updateRequest) {

        contractorService.updateContractorOvertimeMultiplier(updateRequest,id);

        return new Result(true,StatusCode.SUCCESS,"Update success");
    }

    @PutMapping("price/{contractorId}")
    public Result updateContractorPrice(@PathVariable("contractorId") Integer id,
                                                     @RequestBody @Valid UpdateContractorPrice updateRequest) {

        contractorService.updateContractorPrice(updateRequest,id);

        return new Result(true,StatusCode.SUCCESS,"Update success");
    }

    @DeleteMapping("{contractorId}")
    public Result deleteContractor(@PathVariable("contractorId") Integer id) {
        contractorService.deleteContractorById(id);
        return new Result(true,StatusCode.SUCCESS,"Delete Success");
    }


}


