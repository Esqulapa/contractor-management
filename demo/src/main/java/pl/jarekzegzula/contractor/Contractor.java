package pl.jarekzegzula.contractor;

import static pl.jarekzegzula.calc.Calculator.calculateMonthlyEarningsForContractor;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.util.List;
import lombok.*;
import pl.jarekzegzula.contract.ContractType;
import pl.jarekzegzula.contractorBilling.ContractorBilling;
import pl.jarekzegzula.requests.addNewRequest.NewContractorRequest;

@Getter
@Setter
@EqualsAndHashCode
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
    @Column(name = "id")
    private Integer id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(name = "contract_type")
    private ContractType contractType;

    @Column(name = "monthly_earnings")
    private Double monthlyEarnings;

    @Column(name = "hourly_rate")
    private Double hourlyRate;

    @Column(name = "monthly_hour_limit")
    private Integer monthlyHourLimit;

    @Column(name = "is_overtime_paid")
    private Boolean isOvertimePaid;

    @Column(name = "overtime_multiplier")
    private Double overtimeMultiplier;

    @Column(name = "contractor_hour_price")
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
        this.monthlyEarnings = calculateMonthlyEarningsForContractor(request.hourlyRate(), request.monthlyHourLimit());
        this.contractorHourPrice = request.contractorHourPrice();
    }
    
}
