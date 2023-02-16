package com.example.finance7.cart.service.impl;

import com.example.finance7.cart.dto.*;
import com.example.finance7.cart.entity.Cart;
import com.example.finance7.cart.repository.CartRepository;
import com.example.finance7.cart.service.CartService;
import com.example.finance7.cart.vo.CartVO;
import com.example.finance7.cart.vo.SimpleVO;
import com.example.finance7.member.entity.Member;
import com.example.finance7.member.service.MemberService;
import com.example.finance7.product.entity.*;
import com.example.finance7.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final MemberService memberService;
    private final ProductService productService;
    private final CartRepository cartRepository;

    /**
     * 장바구니 상품 추가
     * @param productId
     * @return
     */
    @Override
    public SimpleVO addCart(Long productId) {
        try {
            Member member = memberService.findMemberByMemberId(1L);
            Product product = productService.findProductByProductId(productId);
            if(!cartRepository.existsByMemberAndProduct(member, product)) {
                Cart item = Cart.builder()
                        .member(member)
                        .product(product)
                        .build();
                cartRepository.save(item);
            } else {
                throw new Exception();
            }
        } catch (Exception e) {
            return SimpleVO.builder()
                    .status("failed:장바구니 추가에 실패했습니다.")
                    .build();
        }
        return SimpleVO.builder()
                .status("success")
                .build();
    }

    /**
     * 회원이 가진 장바구니 상품 모두 보기
     * @return
     */
    @Override
    public CartVO selectAllCartProducts() {
        Member member = memberService.findMemberByMemberId(1L);
        List<Cart> items = cartRepository.findCartsByMember(member);
        List<ProductResponseDTO> resultData = makeResultData(items);
        return CartVO.builder()
                .dataNum(items.size())
                .status("success")
                .resultData(resultData)
                .build();
    }

    /**
     * 엔티티에 맞게 DTO 작성하는 메서드
     * @param items
     * @return
     */
    private List<ProductResponseDTO> makeResultData(List<Cart> items) {
        List<ProductResponseDTO> resultData = new ArrayList<>();
        for (Cart item : items) {
            if (item.getProduct() instanceof Card) {
                resultData.add(new CardResponseDTO().toDTO((Card)item.getProduct()));
            } else if (item.getProduct() instanceof Loan) {
                resultData.add(new LoanResponseDTO().toDTO((Loan)item.getProduct()));
            } else if (item.getProduct() instanceof Savings) {
                resultData.add(new SavingResponseDTO().toDTO((Savings)item.getProduct()));
            } else if (item.getProduct() instanceof Subscription) {
                resultData.add(new SubscriptionResponseDTO().toDTO((Subscription)item.getProduct()));
            }
        }
        return resultData;
    }

    @Override
    @Transactional
    public SimpleVO deleteItem(Long productId) {
        try {
            Member member = memberService.findMemberByMemberId(1L);
            Product product = productService.findProductByProductId(productId);
            if(cartRepository.existsByMemberAndProduct(member, product)) {
                cartRepository.deleteCartByMemberAndProduct(member, product);
            } else {
                throw new Exception();
            }
        } catch (Exception e) {
            return SimpleVO.builder()
                    .status("failed:장바구니 삭제에 실패했습니다.")
                    .build();
        }
        return SimpleVO.builder()
                .status("success")
                .build();
    }
}
