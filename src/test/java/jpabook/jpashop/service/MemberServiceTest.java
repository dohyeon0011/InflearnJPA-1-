package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class) // 스프링과 엮어서 쓸 때
@SpringBootTest // 스프링 부트를 띄운 상태로 테스트를 할 때(없으면 @Autowired 못 함)
@Transactional
public class MemberServiceTest {

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    // insert 쿼리 보고 싶으면 rollback = true로 두던가 엔티티 매니저 주입 받아서 flush() 날리기
    @Test
    public void 회원가입() throws Exception {
        //given
        Member member = new Member();
        member.setUsername("LEE");

        //when
        Long savedId = memberService.join(member);

        //then
        Assertions.assertEquals(member, memberRepository.findById(savedId));
    }

    @Test(expected = IllegalArgumentException.class)
    public void 중복_회원_예외() throws Exception {
        //given
        Member member1 = new Member();
        member1.setUsername("lee");

        Member member2 = new Member();
        member2.setUsername("lee");

        //when
        memberService.join(member1);
        memberService.join(member2);
        /*try {
            memberService.join(member2); // 예외가 발생해야 함.
        } catch (IllegalArgumentException e) {
            return;
        }*/

        //then
        fail("중복 가입 예외가 발생해야 함.");
    }


}