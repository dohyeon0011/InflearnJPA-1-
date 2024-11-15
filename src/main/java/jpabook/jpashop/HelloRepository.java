package jpabook.jpashop;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class HelloRepository {

    @PersistenceContext // 이 어노테이션이 있으면 엔티티 매니저를 주입해줌.(팩토리 생성하고 그런건 부트가 다 해줌, build.gradle에서 spring data jpa 라이브러리를 추가해서)
    private EntityManager em;

    public Long save(Hello hello) {
        em.persist(hello);
        return hello.getId();
    }

    public Hello find(Long id) {
        return em.find(Hello.class, id);
    }

}
