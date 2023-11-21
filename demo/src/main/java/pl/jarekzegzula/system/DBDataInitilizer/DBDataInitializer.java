package pl.jarekzegzula.system.DBDataInitilizer;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pl.jarekzegzula.contractor.Contractor;
import pl.jarekzegzula.contractor.ContractorRepository;
import pl.jarekzegzula.contractorBilling.ContractorBillingService;
import pl.jarekzegzula.requests.NewAppUserRequest;
import pl.jarekzegzula.requests.NewContractorBillingRequest;
import pl.jarekzegzula.user.AppUser;
import pl.jarekzegzula.user.AppUserRepository;
import pl.jarekzegzula.user.AppUserService;

import java.time.Month;
import java.time.Year;
import java.util.ArrayList;

@Component
public class DBDataInitializer implements CommandLineRunner {

    private final AppUserService appUserService;

    private final ContractorRepository contractorRepository;

    private final AppUserRepository appUserRepository;

    private final PasswordEncoder passwordEncoder;

    private final ContractorBillingService contractorBillingService;

    public DBDataInitializer(AppUserService appUserService, ContractorRepository contractorRepository, AppUserRepository appUserRepository, PasswordEncoder passwordEncoder, ContractorBillingService contractorBillingService) {
        this.appUserService = appUserService;
        this.contractorRepository = contractorRepository;
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.contractorBillingService = contractorBillingService;
    }

    @Override
    public void run(String... args) throws Exception {

        //Users

        NewAppUserRequest newAppUserRequest = new NewAppUserRequest("user","1234");
        this.appUserService.addNewUser(newAppUserRequest);

        AppUser appUser = new AppUser();
        appUser.setUsername("user2");
        appUser.setPassword(this.passwordEncoder.encode("12345"));
        appUser.setId(2);
        appUser.setRoles("user");
        appUser.setEnabled(true);

        this.appUserRepository.save(appUser);


        //Contractors


        Contractor contractor = new Contractor();
        contractor.setContractorBillings(new ArrayList<>());
        contractor.setId(1);
        contractor.setFirstName("Marian");
        contractor.setLastName("Paździoch");
        contractor.setSalary(1600.);



        Contractor contractor2 = new Contractor();
        contractor2.setContractorBillings(new ArrayList<>());
        contractor2.setId(2);
        contractor2.setFirstName("Ryszard");
        contractor2.setLastName("Peja");
        contractor2.setSalary(3000.0);


        Contractor contractor3 = new Contractor();
        contractor3.setContractorBillings(new ArrayList<>());
        contractor3.setId(3);
        contractor3.setFirstName("Ferdynand");
        contractor3.setLastName("Kiepski");
        contractor3.setSalary(1500.0);

        contractorRepository.save(contractor);
        contractorRepository.save(contractor2);
        contractorRepository.save(contractor3);

        //ContractorBillings

        NewContractorBillingRequest newContractorBillingRequest = new NewContractorBillingRequest(
                1,
                160.0,
                Year.of(2023),
                Month.MARCH);


        NewContractorBillingRequest newContractorBillingRequest1 = new NewContractorBillingRequest(
                2,
                160.0,
                Year.of(2023),
                Month.MARCH);




        NewContractorBillingRequest newContractorBillingRequest2 = new NewContractorBillingRequest(
                3,
                160.0,
                Year.of(2023),
                Month.MARCH);


        NewContractorBillingRequest newContractorBillingRequest3 = new NewContractorBillingRequest(
                3,
                160.0,
                Year.of(2023),
                Month.FEBRUARY);


        contractorBillingService.addNewContractorBilling(newContractorBillingRequest);
        contractorBillingService.addNewContractorBilling(newContractorBillingRequest1);
        contractorBillingService.addNewContractorBilling(newContractorBillingRequest2);
        contractorBillingService.addNewContractorBilling(newContractorBillingRequest3);




    }
}
