package com.lastdance.ziip.member.controller;

import com.lastdance.ziip.global.auth.jwt.JwtTokenProvider;
import com.lastdance.ziip.global.auth.oauth2.Messaging;
import com.lastdance.ziip.global.util.ResponseTemplate;
import com.lastdance.ziip.global.util.property.RedirectUrlProperties;
import com.lastdance.ziip.member.dto.LoginDto;
import com.lastdance.ziip.member.dto.TokenDto;
import com.lastdance.ziip.member.dto.request.LoginRequestDto;
import com.lastdance.ziip.member.dto.request.MemberInfoUpdateRequestDto;
import com.lastdance.ziip.member.dto.request.NicknameRequestDto;
import com.lastdance.ziip.member.dto.response.*;
import com.lastdance.ziip.member.enums.MemberResponseMessage;
import com.lastdance.ziip.member.repository.entity.Member;
import com.lastdance.ziip.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Tag(name = "Members", description = "멤버 관련 API")
@RestController
@RequiredArgsConstructor
@EnableWebMvc
@Slf4j
@RequestMapping("/api/members")
public class MemberController {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberService memberService;
    private final Messaging messaging;


    @Value("${oauth2.naver.state}")
    private String naverState;


    // 회원가입 또는 로그인
    @Operation(summary = "카카오로 로그인 및 회원가입", description = "카카오로 로그인 및 회원가입 하는 API")
    @PostMapping("/kakao/login")
    public ResponseEntity<LoginResponseDto> loginKakao(@RequestBody LoginRequestDto codeRequest) throws IOException {
        LoginDto member = memberService.findKakaoMemberByAuthorizedCode(codeRequest.getCode(), RedirectUrlProperties.KAKAO_REDIRECT_URL, codeRequest.getFcmToken());

        String accessToken = jwtTokenProvider.createAccessToken(member.getId(), member.getSocialId(), member.getSocialType());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getId());
        jwtTokenProvider.storeRefreshToken(member.getId(), refreshToken);

        return ResponseEntity.ok()
                .header("Authorization", accessToken)
                .header("Authorization-Refresh", refreshToken)
                .body(LoginResponseDto.builder()
                        .message("카카오 로그인을 성공하셨습니다")
                        .id(member.getId())
                        .name(member.getName())
                        .firstLogin(member.isFirstLogin())
                        .build());
    }

    @Operation(summary = "네이버로 로그인 및 회원가입", description = "네이버로 로그인 하는 API")
    @PostMapping("/naver/login")
    public ResponseEntity<LoginResponseDto> loginNaver(@RequestBody LoginRequestDto codeRequest) {
        LoginDto member = memberService.findNaverMemberByAuthorizedCode(codeRequest.getCode(), naverState);

        String accessToken = jwtTokenProvider.createAccessToken(member.getId(), member.getSocialId(), member.getSocialType());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getId());
        jwtTokenProvider.storeRefreshToken(member.getId(), refreshToken);

        return ResponseEntity.ok()
                .header("Authorization", accessToken)
                .header("Authorization-Refresh", refreshToken)
                .body(LoginResponseDto.builder()
                        .message("네이버 로그인을 성공하셨습니다")
                        .id(member.getId())
                        .name(member.getName())
                        .firstLogin(member.isFirstLogin())
                        .build());
    }


    @Operation(summary = "access&refresh 토큰 재발급", description = "access토큰 만료되면 refresh 토큰을 이용하여 재발급하는 API")
    @GetMapping("/reissue")
    public ResponseEntity<RefreshTokenResponseDto> reissue(HttpServletRequest httpServletRequest) {
        String refreshToken = httpServletRequest.getHeader("Authorization-Refresh");

        System.out.println("refreshToken = " + refreshToken);
        TokenDto tokenDto = memberService.reissue(refreshToken);


        return ResponseEntity.ok()
                .header("Authorization", tokenDto.getAccessToken())
                .header("Authorization-Refresh", tokenDto.getRefreshToken())
                .body(RefreshTokenResponseDto.builder()
                        .message("accessToken 과 refreshToken이 재발급 성공하셨습니다")
                        .id(tokenDto.getId())
                        .build());
    }

    @Operation(summary = "멤버 정보 수정", description = "멤버 정보(프로필사진,닉네임) 수정")
    @PutMapping("/{id}")
    private BaseResponseDto updateMemberInfo(@PathVariable Long id,
                                             @RequestParam(name = "name", required = false) MemberInfoUpdateRequestDto memberInfoUpdateRequestDto,
                                             @RequestParam(value = "file", required = false) MultipartFile file
            , HttpServletRequest httpServletRequest) throws IOException {

        if (memberInfoUpdateRequestDto != null) {
            memberInfoUpdateRequestDto.setFile(file);
        }

        String token = httpServletRequest.getHeader("Authorization");
        Member findMember = memberService.findMemberByJwtToken(token);

        return memberService.updateMemberInfo(id, memberInfoUpdateRequestDto, findMember, file);
    }

    @Operation(summary = "닉네임 설정", description = "닉네임 설정")
    @PutMapping("/nickname")
    private BaseResponseDto setNickname(@RequestBody NicknameRequestDto nicknameRequestDto,
                                        HttpServletRequest httpServletRequest) {
        String token = httpServletRequest.getHeader("Authorization");
        if (token == null) return null;

        Member findMember = memberService.findMemberByJwtToken(token);

        return memberService.updateNickname(nicknameRequestDto.getName(), findMember);
    }

    @Operation(summary = "닉네임 중복 검사", description = "닉네임 중복 검사")
    @GetMapping("/nickname")
    public BaseResponseDto validNickname(@RequestParam("nickname") String nickname,
                                         HttpServletRequest httpServletRequest) {
        String token = httpServletRequest.getHeader("Authorization");
        if (token == null) return null;

        Member findMember = memberService.findMemberByJwtToken(token);

        return memberService.validNickname(nickname, findMember);
    }

    // 사용자 정보 전체 조회
    @Operation(summary = "사용자 정보 전체 조회", description = "사용자 정보 전체 조회")
    @GetMapping("/allGetInfo")
    public ResponseEntity<ResponseTemplate<MemberAllInfoResponse>> getAllMemberInfo(HttpServletRequest httpServletRequest){

        String token = httpServletRequest.getHeader("Authorization");
        if (token == null) return null;

        Member findMember = memberService.findMemberByJwtToken(token);

        MemberAllInfoResponse memberAllInfoResponse = memberService.getALlMemberInfo(findMember);

        return new ResponseEntity<>(
                ResponseTemplate.<MemberAllInfoResponse>builder()
                        .result(true)
                        .msg(MemberResponseMessage.MEMBER_GETALLMEMBER_SUCCESS.getMessage())
                        .data(memberAllInfoResponse)
                        .build(),
                HttpStatus.OK);
    }

    @Operation(summary = "멤버 프로필사진만 수정", description = "프로필사진 수정")
    @PutMapping("/profile")
    private BaseResponseDto updateMemberProfileImage(@RequestParam(value = "file", required = false) MultipartFile file
        , HttpServletRequest httpServletRequest) throws IOException {

        String token = httpServletRequest.getHeader("Authorization");
        Member findMember = memberService.findMemberByJwtToken(token);

        return memberService.updateMemberProfileImage(findMember, file);
    }

    @Operation(summary = "가족별 멤버 Firebase 토큰 조회", description = "가족별 멤버 Firebase 토큰 조회")
    @GetMapping("/getFcmToken")
    private ResponseEntity<ResponseTemplate<FcmTokenResponseDto>> findFcmTokensByFamilyIdAndExcludeMemberId(@RequestParam(value = "familyId", required = false) Long familyId
        , HttpServletRequest httpServletRequest) throws IOException {

        String token = httpServletRequest.getHeader("Authorization");
        Member findMember = memberService.findMemberByJwtToken(token);

        FcmTokenResponseDto responseDto =  memberService.findFcmTokensByFamilyIdAndExcludeMemberId(findMember, familyId);

        return new ResponseEntity<>(
            ResponseTemplate.<FcmTokenResponseDto>builder()
                .result(true)
                .msg(MemberResponseMessage.MEMBER_FCMTOKEN_SUCCESS.getMessage())
                .data(responseDto)
                .build(),
            HttpStatus.OK);
    }
}
