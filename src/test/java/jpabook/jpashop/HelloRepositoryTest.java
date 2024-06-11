package jpabook.jpashop;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class) // JUNIT에게 스프링과 관련된 것으로 테스트를 한다고 알려줌.
@SpringBootTest
public class HelloRepositoryTest {

    @Autowired
    HelloRepository helloRepository;

    @Test
    @Transactional // 테스트에 있으면 트랙잭션 단위로 커밋하고 지워버림.
    @Rollback(value = false) // 그래서 롤백 안 시키게 false로 넣어줌.
    public void testMember() throws Exception {
        //given
        Hello hello = new Hello();
        hello.setUsername("memberA");

        Hello hello2 = new Hello();
        hello2.setUsername("memberB");

        //when
        Long saveId = helloRepository.save(hello);
        helloRepository.save(hello2);
        Hello findHello = helloRepository.find(saveId);

        //then
        Assertions.assertThat(findHello.getId()).isEqualTo(saveId);
        Assertions.assertThat(findHello.getUsername()).isEqualTo(hello.getUsername());
        Assertions.assertThat(findHello).isEqualTo(hello);
    }

}