package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Member {

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    @NotEmpty
    private String username;

    @Embedded
    private Address address;

    // 양방향 관계에서 엔티티를 직접적으로 노출해서 조회할 때 무한루프 피하려고
    @JsonIgnore // api 조회시 주문 정보는 빠지고 조회됨. 근데 어디선 필요하고 어디선 안 필요하고 그래서 엔티티에 녹이기엔 안 좋음.
    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>();

}
