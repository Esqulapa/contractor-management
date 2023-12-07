package pl.jarekzegzula.contractor;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.jarekzegzula.contractorBilling.ContractorBilling;

import java.math.BigDecimal;
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
            sequenceName = "contractor_id_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "contractor_id_sequence"
    )
    private Integer id;

    private String firstName;

    private String lastName;

    private Double salary;

    private Double overtimeMultiplier;

    private Double contractorPrice;

    @OneToMany(mappedBy = "contractor", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ContractorBilling> contractorBillings;


}
