package pl.jarekzegzula.contractor;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.jarekzegzula.contractorBilling.ContractorBilling;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table
public class Contractor {
    @Id
    @SequenceGenerator(
            name = "contractor_id_sequence",
            sequenceName = "contractor_id_sequence"
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "contractor_id_sequence"
    )
    private Integer id;


    private String firstName;

    private String lastName;

    private Double salary;

    @OneToMany(mappedBy = "contractor", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ContractorBilling> contractorBillings;

//    public void removeAllBillings() {
//        this.contractorBillings.stream().forEach(billing -> billing.setContractor(null));
//        this.contractorBillings = new ArrayList<>();
//    }
//
//    public void removeArtifact(ContractorBilling contractorBillingToBeAssigned) {
//        // Remove artifact owner.
//        contractorBillingToBeAssigned.setContractor(null);
//        this.contractorBillings.remove(contractorBillingToBeAssigned);
//    }


}
