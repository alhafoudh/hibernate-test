package entities;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString(exclude = {"installments", "payments"})
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "orders")
public class Order {

    @Id
    private UUID id;

    private BigDecimal totalAmountPaid;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "order")
    private final Set<Installment> installments = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "order")
    private final Set<Payment> payments = new HashSet<>();

}
