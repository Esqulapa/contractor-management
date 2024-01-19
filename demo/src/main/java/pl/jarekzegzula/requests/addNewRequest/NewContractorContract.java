package pl.jarekzegzula.requests.addNewRequest;

public record NewContractorContract(
         Integer id,
         Integer contractType,
         Double monthlyEarnings,
         Double hourlyRate,
         Integer monthlyHourLimit,
         Double overtimeMultiplier,
         Double clientPayment
         ){
}
