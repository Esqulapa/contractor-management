package pl.jarekzegzula.contractorBilling;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import pl.jarekzegzula.contractor.Contractor;

import java.math.BigDecimal;
import java.time.Month;
import java.time.Year;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Table
@Entity
public class ContractorBilling {
    @Id
    @SequenceGenerator(
            name = "contractorBilling_id_sequence",
            sequenceName = "contractorBilling_id_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "contractorBilling_id_sequence"
    )
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "contractor_id")
    @JsonBackReference
    private Contractor contractor;

    private Double workedHours;

    private Year year;

    private Month month;

    private BigDecimal payment;

}
