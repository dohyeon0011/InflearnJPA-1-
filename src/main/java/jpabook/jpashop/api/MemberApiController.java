package jpabook.jpashop.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    /**
     * 1번과 비교했을 때의 장단점.
     * 진짜 찐 엔티티의 필드명을 바꿔도 setter만 바꿔주면 됨.
     * API 스펙이 바뀌지 않음.
     */
    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) { // json으로 온 body를 member에 json으로 매핑해서 넣어줌.
        Member member = new Member();
        member.setUsername(request.getName());

        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    /**
     * 등록 V1: 요청 값으로 Member 엔티티를 직접 받는다.
     * 문제점
     * - 엔티티에 프레젠테이션 계층을 위한 로직이 추가된다.
     *   - 엔티티에 API 검증을 위한 로직이 들어간다. (@NotEmpty 등등)
     *   - 실무에서는 회원 엔티티를 위한 API가 다양하게 만들어지는데, 한 엔티티에 각각의 API를 위한 모든 요청 요구사항을 담기는 어렵다.
     * - 엔티티가 변경되면 API 스펙이 변한다.
     * 결론
     * - API 요청 스펙에 맞추어 별도의 DTO를 파라미터로 받는다.
     */
    /*@PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) { // json으로 온 body를 member에 json으로 매핑해서 넣어줌.
        Long id = memberService.join(member);

        return new CreateMemberResponse(id);
    }*/

    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(@PathVariable("id") Long id,
                                               @RequestBody @Valid UpdateMemberRequest requst) {
        memberService.update(id, requst.getName());
        Member findMember = memberService.findMember(id);

        return new UpdateMemberResponse(findMember.getId(), findMember.getUsername());
    }

    @GetMapping("/api/v2/members")
    public Result membersV2() {
        List<Member> findMembers = memberService.findMembers();
        List<MemberListDto> collect = findMembers.stream()
                .map(m -> new MemberListDto(m.getUsername()))
                .collect(Collectors.toList());

        return new Result(collect.size(), collect);
    }

    /**
     * 조회 V1: 응답 값으로 엔티티를 직접 외부에 노출한다.
     * 문제점
     * - 엔티티에 프레젠테이션 계층을 위한 로직이 추가된다.
     * - 기본적으로 엔티티의 모든 값이 노출된다.
     * - 응답 스펙을 맞추기 위해 로직이 추가된다. (@JsonIgnore, 별도의 뷰 로직 등등)
     * - 실무에서는 같은 엔티티에 대해 API가 용도에 따라 다양하게 만들어지는데, 한 엔티티에 각각의 API를 위한 프레젠테이션 응답 로직을 담기는 어렵다.
     * - 엔티티가 변경되면 API 스펙이 변한다.
     * - 추가로 컬렉션을 직접 반환하면 항후 API 스펙을 변경하기 어렵다.(별도의 Result 클래스 생성으로 해결)
     * 결론
     * - API 응답 스펙에 맞추어 별도의 DTO를 반환한다.
     */
    // 조회 V1: 안 좋은 버전, 모든 엔티티가 노출, @JsonIgnore -> 이건 정말 최악, api가 이거 하나 인가! 화면에 종속적이지 마라!
    // 조회시 Address 같은 엔티티가 껴있는 상태에서 조회하면 안됨.
    /*@GetMapping("/api/v1/members")
    public List<Member> membersV1() {
        return memberService.findMembers();
    }*/

    @Data
    @AllArgsConstructor
    static class Result<T> { // 리스트를 바로 컬렉션이랑 내보내면 json 배열 타입으로 나가서 유연성이 떨어져서 Result(임의)로 감싸줘야함.
        private int count;
        private T data;
    }

    @Data
    @AllArgsConstructor
    static class MemberListDto {
        private String name;
    }


    @Data
    static class UpdateMemberRequest {
        private String name;
    }

    @Data
    @AllArgsConstructor // 엔티티에는 자제, DTO에는 ㄱㅊ(성향 차이지만 데이터만 왔다 갔다 하는 거라)
    static class UpdateMemberResponse {
        private Long id;
        private String name;
    }

    @Data
    static class CreateMemberRequest {

        @NotEmpty // 어디서는 null 이면 안되고 어디선 null 이어도 되는 경우가 발생할 때 이런 DTO를 만들면 좋음.
        private String name;
    }

    @Data
    static class CreateMemberResponse {
        private Long id;

        public CreateMemberResponse(Long id) {
            this.id = id;
        }
    }

}
