package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
//@AllArgsConstructor
@Transactional(readOnly = true) // 기본값이 false로 public들이 기본적으로 트랜잭션이 걸림, 한 트랙잭션 단위로 비즈니스 로직이 수행이 되어야함.
public class MemberService {

    private final MemberRepository memberRepository;

    // 생성자 주입(setter 주입은 언제 어디서 바꿔치기가 될 수 있기 때문에, 이럴 가능성은 적지만.)
    // 이러면 테스트 코드에서 mock 객체를 넣기에도 좋음.
//    public MemberService(MemberRepository memberRepository) {
//        this.memberRepository = memberRepository;
//    }

    // 회원 가입
    @Transactional // readOnly = false 걸기
    public Long join(Member member) {
        validateDuplicateMember(member); // 회원 중복 검증
        memberRepository.save(member);

        return member.getId();
    }

    private void validateDuplicateMember(Member member) {
        List<Member> findMembers = memberRepository.findByName(member.getUsername());
        if (!findMembers.isEmpty()) {
            throw new IllegalArgumentException("이미 존재하는 회원입니다.");
        }
    }

    // 회원 전체 조회
//    @Transactional(readOnly = true) // 조회 시(읽기 시) 트랙잭션 readOnly = true를 주면, 최적화가 돼서 좋음, 데이터 변경하는 곳에 하면 데이터 변경이 안 먹힘.
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

//    @Transactional(readOnly = true) 일일이 거는 방법
    public Member findMember(Long memberId) {
        return memberRepository.findById(memberId);
    }

    @Transactional // 변경 감지
    public void update(Long id, String name) { // Member로 반환하면 업데이트 하면서 셀렉도 날리게 돼서 분리하려고 void로
        Member member = memberRepository.findById(id);
        member.setUsername(name);
    }

}
