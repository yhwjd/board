package toyproject.board.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import toyproject.board.domain.*;
import toyproject.board.dto.member.*;
import toyproject.board.service.MemberService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Controller
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/signup")
    public String signup(Model model) {
        model.addAttribute("memberSignupDto", new MemberSignupDto());
        return "member/signup";
    }

    // @ModelAttribute 는 @Setter 있어야 한다!!!
    // Member 가 @Id 있기 때문에 Dto 만들어서 builder
    @PostMapping("/signup")
    public String join(@Validated @ModelAttribute MemberSignupDto memberSignupDto, BindingResult bindingResult,
                       RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "member/signup";
        }
        Member member = Member.builder()
                .email(memberSignupDto.getEmail())
                .username(memberSignupDto.getUsername())
                .password(memberSignupDto.getPassword())
                .build();

        Optional<Member> findMember = memberService.findByUsername(memberSignupDto.getUsername());
        if (findMember.isPresent()) {
            bindingResult.reject("signupError", new Object[]{}, null);
            return "member/signup";
        }

        memberService.signup(member);
        redirectAttributes.addAttribute("status", true);

        return "redirect:/";
    }

    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("memberLoginDto", new MemberLoginDto());
        return "/member/login";
    }

    @PostMapping("/login")
    public String login(@Validated @ModelAttribute MemberLoginDto memberLoginDto,
                        BindingResult bindingResult,
                        HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            return "/member/login";
        }
        // validation 을 만족하지만 존재하지 않는 아이디
        Optional<Member> findMemberOptional = memberService.findByUsername(memberLoginDto.getUsername());

        if (findMemberOptional.isPresent()) {
            Member findMember = findMemberOptional.get();
            if (findMember.getPassword().equals(memberLoginDto.getPassword())) {
                HttpSession session = request.getSession();
                session.setAttribute("loginMember", findMember);
                return "redirect:/home";
            }
        }

        bindingResult.reject("loginError", new Object[]{}, null);
        return "/member/login";
    }

    @GetMapping("/home")
    public String home(
            @SessionAttribute(name = "loginMember", required = false) Member loginMember, Model model) {

        if (loginMember == null) {
            return "redirect:/";
        }

        Member member = memberService.findByUsername(loginMember.getUsername()).get();
        List<Post> posts = member.getPosts();
        model.addAttribute("member", member);
        model.addAttribute("posts", posts);
        return "member/home";
    }

    @GetMapping("/deleteMember/{id}")
    public String deleteForm(@PathVariable Long id, Model model) {
        Member member = memberService.findById(id);
        model.addAttribute("member", member);
        model.addAttribute("memberDeleteDto", new MemberDeleteDto());
        return "member/deleteMember";
    }

    @PostMapping("/deleteMember/{id}") // form method post 설정안함
    public String deleteMember(@PathVariable Long id, @ModelAttribute MemberDeleteDto memberDeleteDto,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        Member member = memberService.findById(id);

        // String == 비교 말고 equals 사용해야함. String 은 불변 객체
        if (member.getPassword().equals(memberDeleteDto.getPassword()) &&
        member.getPassword().equals(memberDeleteDto.getPasswordCheck())) {
            redirectAttributes.addAttribute("statusDeleteMember", true);
            memberService.deleteMember(member);
            return "redirect:/";
        }

        redirectAttributes.addAttribute("statusDeleteFail", true);
        return "redirect:/deleteMember/{id}";
    }

    @GetMapping("/updateMember/{id}")
    public String updateMember(@PathVariable Long id, Model model) {
        Member member = memberService.findById(id);
        model.addAttribute("member", member);
        return "member/updateMember";
    }

    @PostMapping("/updateMember/{id}")
    public String updateMember(@PathVariable Long id, @Validated @ModelAttribute MemberUpdateDto memberUpdateDto,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "member/updateMember";
        }

        Member member = memberService.findById(id);
        member.updateMember(memberUpdateDto.getUsername(), memberUpdateDto.getPassword());

        memberService.signup(member);
        redirectAttributes.addAttribute("statusUpdateMember", true);

        return "redirect:/";
    }
}