package com.realtimechat.client.service;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import com.realtimechat.client.domain.ChatRoom;
import com.realtimechat.client.domain.Comment;
import com.realtimechat.client.domain.Member;
import com.realtimechat.client.domain.Payment;
import com.realtimechat.client.domain.Role;
import com.realtimechat.client.dto.request.MyPageRequestDto;
import com.realtimechat.client.dto.response.MyPageCommentResponseDto;
import com.realtimechat.client.dto.response.MyPagePaymentResponseDto;
import com.realtimechat.client.repository.ChatRoomRepository;
import com.realtimechat.client.repository.CommentRepository;
import com.realtimechat.client.repository.MemberRepository;
import com.realtimechat.client.repository.PaymentRepository;
import com.realtimechat.client.util.CreateFileName;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class MyPageService {

    private final MemberRepository memberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final PaymentRepository paymentRepository;
    private final CommentRepository commentRepository;
    private final PasswordEncoder passwordEncoder;

    // 닉네임 변경
    @Transactional
    public String updateNickname(Member member, String nickname) {
        String message = "success";
        Member m = memberRepository.findByNickname(nickname);
        if (m != null) {
            message = "fail";
        } else {
            Member updateMember = memberRepository.findByNickname(member.getNickname());

            // role type - star 일 경우 channel명 변경
            if (updateMember.getRole().equals(Role.ROLE_STAR)) {
                ChatRoom chatRoom = chatRoomRepository.findByChannel(member.getNickname());
                chatRoom.update(nickname);
                chatRoomRepository.save(chatRoom);
            }
            updateMember.updateNickname(nickname);
            memberRepository.save(updateMember);
        }
        
        return message;
    }

    // 프로필 변경
    @Transactional
    public String updateProfile(Member member, MultipartFile profile) {
        String message;
        try {
            // MultipartFile profile = myPageRequestDto.getProfile();
            String originalFileName = profile.getOriginalFilename();
            String filename = new CreateFileName(originalFileName).toString();
            String folder = "/files/profile";
            String savePath = System.getProperty("user.dir") + folder;
            if (!new File(savePath).exists()) {
                try {
                    new File(savePath).mkdir();
                } catch(Exception e) {
                    e.getStackTrace();
                }
            }
            String profilePath = folder + "/" + filename;
            profile.transferTo(new File(savePath + "/" + filename));
    
            member.updateProfile(profilePath);
            memberRepository.save(member);
            message = "success";
        } catch (Exception e) {
            message = "error";
            e.printStackTrace();
        }

        return message;
        
    } 

    // 비밀번호 변경
    @Transactional
    public String updatePassword(Member member, MyPageRequestDto myPageRequestDto) {
        String message = "success";
        // 비밀번호 맞는지 확인
        if (!passwordEncoder.matches(myPageRequestDto.getPassword(), member.getPassword())) {
            message = "fail";
        } else {
            // 비밀번호 변경
            member.updatePassword(passwordEncoder.encode(myPageRequestDto.getNewPassword()));
            memberRepository.save(member);
        }
        return message;
    }

    // 결제 내역
    public Map<String, Object> getPaymentList(Member member, Pageable pageable) {
        // resposne
        Page<Payment> payments = paymentRepository.findByMember(member, pageable);

        List<MyPagePaymentResponseDto> paymentList = payments.stream().map(MyPagePaymentResponseDto::new).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("paymentList", paymentList);

        Map<String, Object> pageList = new HashMap<>();
        pageList.put("page", payments.getNumber());
        pageList.put("totalPages", payments.getTotalPages());
        pageList.put("nextPage", pageable.next().getPageNumber());
        pageList.put("size", pageable.getPageSize());

        result.put("pageList", pageList);

        return result;

    }

    // 작성한 댓글+답글
    @Transactional
    public Map<String, Object> getCommentList(Member member, Pageable pageable) {
        Page<Comment> comments = commentRepository.findByMember(member, pageable);

        List<MyPageCommentResponseDto> commentList = comments.stream().map(MyPageCommentResponseDto::new).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("commentList", commentList);

        Map<String, Integer> pageList = new HashMap<>();
        pageList.put("page", comments.getNumber());
        pageList.put("totalPages", comments.getTotalPages());
        pageList.put("nextPage", pageable.next().getPageNumber());
        pageList.put("size", pageable.getPageSize());

        result.put("pageList", pageList);
        
        return result;
    }
}
