package pl.jarekzegzula.contractorBilling;

import static pl.jarekzegzula.calc.Calculator.*;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Month;
import java.time.Year;
import lombok.*;
import pl.jarekzegzula.contractor.Contractor;
import pl.jarekzegzula.requests.addNewRequest.NewContractorBillingRequest;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@Table
@Entity
public class ContractorBilling {
  @Id
  @SequenceGenerator(
      name = "contractorBilling_id_sequence",
      sequenceName = "contractorBilling_id_sequence",
      allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "contractorBilling_id_sequence")
  @Column(name = "id")
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "contractor_id")
  @JsonBackReference
  private Contractor contractor;

  @Column(name = "worked_hours")
  private Double workedHours;

  @Column(name = "year")
  private Year year;

  @Enumerated(EnumType.STRING)
  @Column(name = "month")
  private Month month;

  @Column(name = "contractor_remuneration")
  private BigDecimal contractorRemuneration;

  @Column(name = "client_charge")
  private BigDecimal clientCharge;

  @Column(name = "profit")
  private BigDecimal profit;

  public ContractorBilling(NewContractorBillingRequest request, Contractor contractor) {
    this.contractor = contractor;
    this.workedHours = request.workedHours();
    this.year = request.year();
    this.month = request.month();
    this.contractorRemuneration = calculateContractorPayment(request.workedHours(), contractor);
    this.clientCharge =
        calculateClientsChargeFromContractorHours(request.workedHours(), contractor);
    this.profit = calculateProfit(clientCharge, contractorRemuneration);
  }
}
