package pl.jarekzegzula.contractor;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ContractorRepository extends JpaRepository<Contractor, Integer> {

  boolean existsByFirstNameAndLastName(String firstName, String lastName);
}
