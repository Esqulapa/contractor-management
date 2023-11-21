package pl.jarekzegzula.contractorBilling;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Month;
import java.time.Year;
import java.util.List;
import java.util.Optional;

public interface ContractorBillingRepository extends JpaRepository<ContractorBilling,Integer> {

    Optional<ContractorBilling> findById(Integer id);

    Optional<List<ContractorBilling>> findByYearAndMonth(Year year, Month month);

    boolean existsByContractor_IdAndYearAndMonth(Integer id, Year year, Month month);
}
