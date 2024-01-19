package pl.jarekzegzula.contractor;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pl.jarekzegzula.requests.addNewRequest.NewContractorRequest;
import pl.jarekzegzula.requests.updateRequest.*;
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
        return new Result(true, StatusCode.SUCCESS, "Success", contractorService.getContractors());
    }

    @GetMapping("{contractorId}")
    public Result getContractorById(@PathVariable("contractorId") Integer id) {
        return new Result(true, StatusCode.SUCCESS, "Success", contractorService.getContractorById(id));
    }

    @PostMapping
    public Result addContractor(@Valid @RequestBody NewContractorRequest request) throws ContractorAlreadyExistInGivenTimeException {
        contractorService.addNewContractor(request);
        return new Result(true, StatusCode.SUCCESS, "Contractor added successfully", request);
    }

    @PutMapping("hourly-rate/{contractorId}")
    public Result updateContractorHourlyRate(@PathVariable("contractorId") Integer id,
                                             @RequestBody @Valid UpdateContractorHourlyRateRequest updateRequest) {

        contractorService.updateContractorHourlyRateAndMonthlyEarnings(updateRequest, id);

        return new Result(true, StatusCode.SUCCESS, "Update success");

    }
    @PutMapping("hour-limit/{contractorId}")
    public Result updateContractorHourlyRate(@PathVariable("contractorId") Integer id,
                                             @RequestBody @Valid UpdateMonthlyHourLimitRequest updateRequest) {
        contractorService.updateMonthlyHourLimitAndMonthlyEarnings(updateRequest, id);

        return new Result(true, StatusCode.SUCCESS, "Update success");
    }

    @PutMapping("multiplier/{contractorId}")
    public Result updateContractorOvertimeMultiplier(@PathVariable("contractorId") Integer id,
                                                     @RequestBody @Valid UpdateContractorOvertimeMultiplier updateRequest) {

        contractorService.updateContractorOvertimeMultiplier(updateRequest, id);

        return new Result(true, StatusCode.SUCCESS, "Update success");
    }

    @PutMapping("hour-price/{contractorId}")
    public Result updateContractorPrice(@PathVariable("contractorId") Integer id,
                                        @RequestBody @Valid UpdateContractorHourPriceRequest updateRequest) {

        contractorService.updateContractorHourPrice(updateRequest, id);

        return new Result(true, StatusCode.SUCCESS, "Update success");
    }

    @PutMapping("overtime/{contractorId}")
    public Result updateContractorOvertime(@PathVariable("contractorId") Integer id,
                                           @RequestBody @Valid UpdateIsContractorOvertimePaid updateRequest) {

        contractorService.updateIsOvertimePaid(updateRequest, id);

        return new Result(true, StatusCode.SUCCESS, "Update success");
    }
    
    @PutMapping("contract-type/{contractorId}")
    public Result updateContractorContractType(@PathVariable("contractorId") Integer id,
                                           @RequestBody @Valid UpdateContractorContractTypeRequest updateRequest) {

        contractorService.updateContractType(updateRequest, id);

        return new Result(true, StatusCode.SUCCESS, "Update success");
    }

    @DeleteMapping("{contractorId}")
    public Result deleteContractor(@PathVariable("contractorId") Integer id) {
        contractorService.deleteContractorById(id);
        return new Result(true, StatusCode.SUCCESS, "Delete Success");
    }


}


