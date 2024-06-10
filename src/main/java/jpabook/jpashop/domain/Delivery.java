package jpabook.jpashop.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class Delivery {

    @Id @GeneratedValue
    @Column(name = "delivery_id")
    private Long id;

    @OneToOne(mappedBy = "delivery")
    private Order order;

    @Embedded
    private Address address;

    @Enumerated(EnumType.STRING) // ORDINAL은 필드 순서에 맞춰 순차적으로 1, 2, 3.. 증가(근데 이건 중간에 필드 끼어 들어오면 번호가 뒤죽박죽 돼서 절대 X)
    private DeliveryStatus status; // READY, COMP
}
