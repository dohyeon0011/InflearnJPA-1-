package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class Delivery {

    @Id @GeneratedValue
    @Column(name = "delivery_id")
    private Long id;

    @JsonIgnore // 양방향 관계에서 엔티티를 직접적으로 노출해서 조회할 때 무한루프 피하려고
    @OneToOne(mappedBy = "delivery")
    private Order order;

    @Embedded
    private Address address;

    @Enumerated(EnumType.STRING) // ORDINAL은 필드 순서에 맞춰 순차적으로 1, 2, 3.. 증가(근데 이건 중간에 필드 끼어 들어오면 번호가 뒤죽박죽 돼서 절대 X)
    private DeliveryStatus status; // READY, COMP
}
