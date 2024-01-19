package pl.jarekzegzula.contractorBilling;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import pl.jarekzegzula.contractor.Contractor;
import pl.jarekzegzula.requests.addNewRequest.NewContractorBillingRequest;

import java.math.BigDecimal;
import java.time.Month;
import java.time.Year;
import java.util.Objects;

import static pl.jarekzegzula.calc.Calculator.*;


@Getter
@Setter
@ToString
@NoArgsConstructor
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

    @Enumerated(EnumType.STRING)
    private Month month;

    private BigDecimal contractorRemuneration;

    private BigDecimal clientCharge;

    private BigDecimal profit;

    public ContractorBilling(NewContractorBillingRequest request,Contractor contractor) {
        this.contractor = contractor;
        this.workedHours = request.workedHours();
        this.year = request.year();
        this.month = request.month();
        this.contractorRemuneration = calculateContractorPayment(request.workedHours(), contractor);
        this.clientCharge = calculateClientsChargeFromContractorHours(request.workedHours(), contractor);
        this.profit = calculateProfit(clientCharge, contractorRemuneration);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ContractorBilling that = (ContractorBilling) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
