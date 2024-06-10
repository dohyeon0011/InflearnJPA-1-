package jpabook.jpashop;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class) // JUNIT에게 스프링과 관련된 것으로 테스트를 한다고 알려줌.
@SpringBootTest
public class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Test
    @Transactional // 테스트에 있으면 트랙잭션 단위로 커밋하고 지워버림.
    @Rollback(value = false) // 그래서 롤백 안 시키게 false로 넣어줌.
    public void testMember() throws Exception {
        //given
        Member member = new Member();
        member.setUsername("memberA");

        Member member2 = new Member();
        member2.setUsername("memberB");

        //when
        Long saveId = memberRepository.save(member);
        memberRepository.save(member2);
        Member findMember = memberRepository.find(saveId);

        //then
        Assertions.assertThat(findMember.getId()).isEqualTo(saveId);
        Assertions.assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        Assertions.assertThat(findMember).isEqualTo(member);
    }

}