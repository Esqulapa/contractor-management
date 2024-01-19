package pl.jarekzegzula.contractorBilling;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pl.jarekzegzula.contractorBilling.dto.ContractorBillingDTO;
import pl.jarekzegzula.requests.addNewRequest.NewContractorBillingRequest;
import pl.jarekzegzula.requests.updateRequest.UpdateContractorBillingHoursRequest;
import pl.jarekzegzula.system.Result;
import pl.jarekzegzula.system.StatusCode;
import pl.jarekzegzula.system.exception.ContractorAlreadyExistInGivenTimeException;
import pl.jarekzegzula.system.exception.SameHoursOrLessThanZeroException;

import java.time.Month;
import java.time.Year;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.endpoint.base-url}/contractor/billing")
public class ContractorBillingController {

    private final ContractorBillingService contractorBillingService;



    @Autowired
    public ContractorBillingController(ContractorBillingService contractorBillingService) {
        this.contractorBillingService = contractorBillingService;
    }

    @GetMapping()
    public Result getContractorBillings() {
    List<ContractorBillingDTO> contractorBillingDTOs = contractorBillingService.findAll().stream()
            .map(ContractorBillingDTO::new)
            .collect(Collectors.toList());

    return new Result(true, StatusCode.SUCCESS, "Success", contractorBillingDTOs);
}

    @GetMapping("{contractorId}")
    public Result getContractorBillingById(@PathVariable("contractorId") Integer id){
        return new Result(true,StatusCode.SUCCESS,"Success",contractorBillingService.getContractorBillingById(id));
    }

    @GetMapping("/report")
    public Result getContractorBillingsMonthlyReport(
            @Valid
            @RequestParam("year") Year year,
            @RequestParam("month") Month month) {
        return new Result(true, StatusCode.SUCCESS,"Success"
                ,contractorBillingService.getContractorBillingsMonthlyReport(year, month));
    }

    @PostMapping
    public Result addContractorBilling(@Valid @RequestBody NewContractorBillingRequest request) throws ContractorAlreadyExistInGivenTimeException {
        contractorBillingService.addNewContractorBilling(request);
        return new Result(true,StatusCode.SUCCESS,"Contractor billing added successfully",request);
    }


    @PutMapping("/hours/{contractorBillingId}")
    public Result updateContractorBillingWorkedHours(@PathVariable("contractorBillingId") Integer id,
                                                     @Valid @RequestBody UpdateContractorBillingHoursRequest updateRequest) throws SameHoursOrLessThanZeroException {

        contractorBillingService.updateContractorBillingWorkedHours(updateRequest,id);

        return new Result(true,StatusCode.SUCCESS,"Update success");
    }

    @DeleteMapping("{contractorBillingId}")
    public Result deleteContractorBilling(@PathVariable("contractorBillingId") Integer id){
        contractorBillingService.deleteContractorBillingById(id);
        return new Result(true,StatusCode.SUCCESS,"Delete success");
    }



}
