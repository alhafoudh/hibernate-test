package entities;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString(exclude = {"order"})
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "installments")
public class Installment {

    @Id
    private UUID id;

    private BigDecimal totalAmount;

    private BigDecimal paidAmount;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "order_id")
    private Order order;

}
