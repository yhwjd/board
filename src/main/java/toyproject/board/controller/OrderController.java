package toyproject.board.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import toyproject.board.domain.Item;
import toyproject.board.domain.Member;
import toyproject.board.domain.Order;
import toyproject.board.dto.item.ItemDto;
import toyproject.board.service.ItemService;
import toyproject.board.service.MemberService;
import toyproject.board.service.OrderService;

@Slf4j
@Controller
@RequiredArgsConstructor
public class OrderController {

    private final MemberService memberService;
    private final OrderService orderService;
    private final ItemService itemService;

    @GetMapping("/item")
    public String itemForm() {
        return "item/itemHome";
    }

    @PostMapping("/item")
    public String addItem(@SessionAttribute(name = "loginMember", required = false) Member loginMember,
                          @ModelAttribute ItemDto itemDto) {

        Member member = memberService.findByUsername(loginMember.getUsername()).get();

        Item item = itemService.addItem(itemDto);
        Order order = Order.createOrder(member, item);
        orderService.makeOrder(order);

        return "redirect:/order";
    }

    @GetMapping("/order")
    public String orderForm(@SessionAttribute(name = "loginMember", required = false) Member loginMember,
                            Model model) {

        Member member = memberService.findByUsername(loginMember.getUsername()).get();
        Order order = member.getOrder();
        model.addAttribute("order", order);

        return "order/orderHome";
    }
}
