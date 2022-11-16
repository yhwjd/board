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
import toyproject.board.service.PostService;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Controller
public class MemberController {

    private final MemberService memberService;
    private final PostService postService;

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
        if (findMember.isEmpty()) {
            memberService.signup(member);
        } else {
            redirectAttributes.addAttribute("statusSignup", true);
            return "redirect:/signup";
        }
        redirectAttributes.addAttribute("status", true);
        return "redirect:/";
    }

    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("memberLoginDto", new MemberLoginDto());
        return "/member/login";
    }

    @PostMapping("/login")
    public String login(@Validated @ModelAttribute MemberLoginDto memberLoginDto, BindingResult bindingResult,
                        RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "/member/login";
        }
        Member findMember = memberService.findByUsername(memberLoginDto.getUsername()).get();

        if (findMember.getPassword().equals(memberLoginDto.getPassword())) {
            Long id = findMember.getId();
            redirectAttributes.addAttribute("id", id);
            return "redirect:/home/{id}";
        } else {
            redirectAttributes.addAttribute("statusLoginFail", true);
            return "redirect:/";
        }
    }

    @GetMapping("/home/{id}")
    public String home(@PathVariable Long id, Model model) {

        Member member = memberService.findById(id);
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