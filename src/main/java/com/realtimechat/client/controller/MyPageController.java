package com.realtimechat.client.controller;

import com.realtimechat.client.config.security.SecurityUser;
import com.realtimechat.client.dto.request.MyPageRequestDto;
import com.realtimechat.client.dto.response.MyPageResponseDto;
import com.realtimechat.client.service.MyPageService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/mypage")
public class MyPageController {

    private final MyPageService myPageService;
    
    @GetMapping
    public ResponseEntity<MyPageResponseDto> get(@AuthenticationPrincipal SecurityUser principal) {
        if (principal != null) {
            MyPageResponseDto myPageResponseDto = new MyPageResponseDto();
            myPageResponseDto.setNickname(principal.getMember().getNickname());
            myPageResponseDto.setProfilePath(principal.getMember().getProfilePath());

            return ResponseEntity.ok(myPageResponseDto);
        } else {
            return ResponseEntity.ok(null);
        }
        
    }

    // 닉네임 변경
    @PostMapping("/nickname")
    public ResponseEntity<String> updateNickname(@RequestBody MyPageRequestDto myPageRequestDto, @AuthenticationPrincipal SecurityUser principal) {
        String response = myPageService.updateNickname(principal.getMember(), myPageRequestDto.getNickname());
        return ResponseEntity.ok(response);
    }
}
