package pl.jarekzegzula.contractor;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;
import pl.jarekzegzula.contract.ContractType;
import pl.jarekzegzula.contractorBilling.ContractorBilling;
import pl.jarekzegzula.requests.addNewRequest.NewContractorRequest;

import java.util.List;
import java.util.Objects;

import static pl.jarekzegzula.calc.Calculator.calculateMonthlyEarningsForContractor;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
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

    @Enumerated(EnumType.STRING)
    @Column(name = "contract_type")
    private ContractType contractType;

    private Double monthlyEarnings;

    private Double hourlyRate;

    private Integer monthlyHourLimit;

    private Boolean isOvertimePaid;

    private Double overtimeMultiplier;

    private Double contractorHourPrice;

    @OneToMany(mappedBy = "contractor", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @ToString.Exclude
    private List<ContractorBilling> contractorBillings;

    public Contractor(NewContractorRequest request) {
        this.firstName = request.firstName();
        this.lastName = request.lastName();
        this.contractType = ContractType.fromValue(request.contractType());
        this.hourlyRate = request.hourlyRate();
        this.monthlyHourLimit = request.monthlyHourLimit();
        this.isOvertimePaid = request.isOvertimePaid();
        this.overtimeMultiplier = request.overtimeMultiplier();
        this.monthlyEarnings = calculateMonthlyEarningsForContractor(request.hourlyRate(),request.monthlyHourLimit());
        this.contractorHourPrice = request.contractorHourPrice();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Contractor that = (Contractor) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
