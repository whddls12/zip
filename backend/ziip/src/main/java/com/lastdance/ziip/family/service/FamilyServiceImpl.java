package com.lastdance.ziip.family.service;

import com.lastdance.ziip.family.dto.request.*;
import com.lastdance.ziip.family.dto.response.*;
import com.lastdance.ziip.family.exception.MemberAlreadyRegisteredInFamilyException;
import com.lastdance.ziip.family.repository.FamilyMemberRepository;
import com.lastdance.ziip.family.repository.FamilyRepository;
import com.lastdance.ziip.family.repository.entity.Family;
import com.lastdance.ziip.family.repository.entity.FamilyMember;
import com.lastdance.ziip.global.awsS3.AwsS3Uploader;
import com.lastdance.ziip.member.dto.FileDto;
import com.lastdance.ziip.member.dto.response.MemberProfileImgUrlResponseDto;
import com.lastdance.ziip.member.repository.MemberRepository;
import com.lastdance.ziip.member.repository.entity.Member;

import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.exception.NurigoMessageNotReceivedException;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class FamilyServiceImpl implements FamilyService {


    @Value("${coolsms.api.key}")
    private String smsApiKey;

    @Value("${coolsms.api.secret}")
    private String smsApiSecretKey;

    @Value("${coolsms.api.phone}")
    private String smsSendPhoneNumber;

    private final FamilyRepository familyRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final AwsS3Uploader s3Uploader;
    private final MemberRepository memberRepository;


    @Override
    public FamilyRegisterResponseDto registFamily(Member findMember, FamilyRegisterRequestDto familyRegisterRequest, MultipartFile file) throws IOException {

        String code = String.valueOf(UUID.randomUUID());

        // 이미지 등록 안했을 때
        if(file == null){
            Family family = Family.builder()
                    .name(familyRegisterRequest.getName())
                    .content(familyRegisterRequest.getContent())
                    .code(code)
                    .build();

            Family saveFamily = familyRepository.save(family);

            FamilyMember familyMember = FamilyMember.builder()
                    .member(findMember)
                    .family(saveFamily)
                    .nickname(familyRegisterRequest.getNickname())
                    .build();

            FamilyMember saveFamilyMember = familyMemberRepository.save(familyMember);

            FamilyRegisterResponseDto familyRegisterResponseDto = FamilyRegisterResponseDto.builder()
                    .id(saveFamily.getId())
                    .build();

            return familyRegisterResponseDto;
        }
        // 이미지 등록 했을 때
        else{
            String fileUrl = s3Uploader.upload(file, "family");
            String originalName = file.getOriginalFilename();

            FileDto newfileDto = FileDto.builder()
                    .fileOriginalName(originalName)
                    .filePath(fileUrl)
                    .build();

            Family family = Family.builder()
                    .name(familyRegisterRequest.getName())
                    .content(familyRegisterRequest.getContent())
                    .code(code)
                    .profileImgName(newfileDto.getFileOriginalName())
                    .profileImgUrl(newfileDto.getFilePath())
                    .build();

            Family saveFamily = familyRepository.save(family);

            FamilyMember familyMember = FamilyMember.builder()
                    .member(findMember)
                    .family(saveFamily)
                    .nickname(familyRegisterRequest.getNickname())
                    .build();

            FamilyMember saveFamilyMember = familyMemberRepository.save(familyMember);

            FamilyRegisterResponseDto familyRegisterResponseDto = FamilyRegisterResponseDto.builder()
                    .id(saveFamily.getId())
                    .build();

            return familyRegisterResponseDto;
        }

    }

    @Override
    public FamilyRegisterAcceptResponseDto acceptFamily(Member findMember, FamilyRegisterAcceptRequestDto familyRegisterAcceptRequest) {
        Family family = familyRepository.findByCode(familyRegisterAcceptRequest.getFamilyCode());

        FamilyMember existingFamilyMember = familyMemberRepository.findByMemberAndFamily(findMember, family);
        if (existingFamilyMember != null) {
            throw new MemberAlreadyRegisteredInFamilyException("해당 멤버는 이미 이 가족에 등록되어 있습니다.");
        }


        FamilyMember familyMember = FamilyMember.builder()
                .member(findMember)
                .family(family)
                .nickname(familyRegisterAcceptRequest.getNickname())
                .build();

        FamilyMember saveFamilyMember = familyMemberRepository.save(familyMember);

        FamilyRegisterAcceptResponseDto familyRegisterAcceptResponse = FamilyRegisterAcceptResponseDto.builder()
                .familyId(saveFamilyMember.getFamily().getId())
                .build();

        return familyRegisterAcceptResponse;
    }

    @Override
    public FamilyListResponseDto listFamily(Member findMember) {

        List<FamilyMember> familyMembers = familyMemberRepository.findAllByMember(findMember);

        List<FamilyListDetailResponseDto> familyListDetailResponseDtoList = familyMembers.stream()
                .map(familyMember -> FamilyListDetailResponseDto.builder()
                        .id(familyMember.getFamily().getId())
                        .name(familyMember.getFamily().getName())
                        .profileImgUrl(familyMember.getFamily().getProfileImgUrl())
                        .build())
                .collect(Collectors.toList());

        return FamilyListResponseDto.builder()
                .familyListDetailResponseDtoList(familyListDetailResponseDtoList)
                .build();
    }


    @Override
    public FamilyNickNameResponseDto modifyNickname(Member findMember,
            FamilyNickNameRequestDto familyNickNameRequest) {

        FamilyMember familyMember = familyMemberRepository.findByMemberAndFamilyId(findMember, familyNickNameRequest.getFamilyId());

        System.out.println(familyNickNameRequest);

        familyMember.updateNickname(familyNickNameRequest.getNickname());

        FamilyNickNameResponseDto familyNickNameResponse = FamilyNickNameResponseDto.builder()
                .familyId(familyMember.getFamily().getId())
                .build();

        return familyNickNameResponse;
    }

    @Override
    public FamilyChoiceResponseDto choiceFamily(Member findMember, long familyId) {

        Optional<Family> family = familyRepository.findById(familyId);

        FamilyChoiceResponseDto familyChoiceResponseDto = FamilyChoiceResponseDto.builder()
                .familyId(family.get().getId())
                .familyName(family.get().getName())
                .familyContent(family.get().getContent())
                .familyProfileImgUrl(family.get().getProfileImgUrl())
                .memberProfileImgUrl(findMember.getProfileImgUrl())
                .build();

        return familyChoiceResponseDto;
    }

    @Override
    public FamilyModifyResponseDto modifyFamily(Member findMember, FamilyModifyReqeustDto familyModifyReqeustDto,
        MultipartFile file) throws IOException {

        String fileUrl = s3Uploader.upload(file, "family");
        String originalName = file.getOriginalFilename();

        FileDto newfileDto = FileDto.builder()
            .fileOriginalName(originalName)
            .filePath(fileUrl)
            .build();

        Optional<Family> family = familyRepository.findById(familyModifyReqeustDto.getId());

        Family modifiedFamily = Family.builder()
            .id(family.get().getId())
            .name(familyModifyReqeustDto.getName())
            .content(familyModifyReqeustDto.getContent())
            .code(family.get().getCode())
            .profileImgName(newfileDto.getFileOriginalName())
            .profileImgUrl(newfileDto.getFilePath())
            .build();

        familyRepository.save(modifiedFamily);

        return new FamilyModifyResponseDto(modifiedFamily.getId());
    }

    @Override
    public FamilyMemberResponseDto getFamilyMember(Member findMember, long familyId) {

        Optional<Family> family = familyRepository.findById(familyId);
        List<FamilyMember> familyMemberList = familyMemberRepository.findByFamily(family.get());

        List<FamilyMemberDetailResponseDto> familyMemberDetailResponseDtoList = familyMemberList.stream()
                .map(familyMember -> FamilyMemberDetailResponseDto.builder()
                        .memberId(familyMember.getMember().getId())
                        .nickname(familyMember.getNickname())
                        .profileImgUrl(familyMember.getMember().getProfileImgUrl())
                        .build())
                .collect(Collectors.toList());

        return FamilyMemberResponseDto.builder()
                .familyMemberDetailResponseDtoList(familyMemberDetailResponseDtoList)
                .build();
    }

    @Override
    public FamilyCheckCodeResponseDto checkFamilyCode(Member findMember, FamilyCheckCodeRequestDto familyCheckCodeRequestDto) {

        Family family = familyRepository.findByCode(familyCheckCodeRequestDto.getFamilyCode());

        boolean result = true;
        if (family == null) {
            result = false;
        }

        FamilyMember existingFamilyMember = familyMemberRepository.findByMemberAndFamily(findMember, family);
        if (existingFamilyMember != null) {
            result = false;
        }

        FamilyCheckCodeResponseDto familyCheckCodeResponseDto = FamilyCheckCodeResponseDto.builder()
                .result(result)
                .build();
        return familyCheckCodeResponseDto;
    }

    @Override
    public FamilyInviteResponseDto inviteFamily(Member findMember, FamilyInviteRequestDto familyInviteRequestDto) {
        Optional<Family> family = familyRepository.findById(familyInviteRequestDto.getFamilyId());

        List<String> sendPhoneNumberList = familyInviteRequestDto.getPhoneNumber();

        // 메시지로 보내야할 가족 코드
        DefaultMessageService messageService =  NurigoApp.INSTANCE.initialize(smsApiKey, smsApiSecretKey, "https://api.coolsms.co.kr");

        ArrayList<Message> messageList = new ArrayList<>();

        for (int i = 0 ; i < sendPhoneNumberList.size() ; i++) {
            Message message = new Message();
            message.setFrom(smsSendPhoneNumber);
            message.setTo(sendPhoneNumberList.get(i));
            message.setText("집으로 초대합니다. 가족코드 : " + family.get().getCode());
            messageList.add(message);
        }

        try {
            // send 메소드로 ArrayList<Message> 객체를 넣어도 동작합니다!
            messageService.send(messageList);
        } catch (NurigoMessageNotReceivedException exception) {
            // 발송에 실패한 메시지 목록을 확인할 수 있습니다!
            System.out.println(exception.getFailedMessageList());
            System.out.println(exception.getMessage());
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        }

        FamilyInviteResponseDto familyInviteResponseDto = FamilyInviteResponseDto.builder()
                .familyId(family.get().getId())
                .build();

        return familyInviteResponseDto;
    }

    @Override
    public FamilyMemberDetailResponseDto getMemberNickname(Member findMember, Long familyId) {
        Optional<Family> family = familyRepository.findById(familyId);

        FamilyMember familyMember = familyMemberRepository.findByMemberAndFamily(findMember, family.get());

        FamilyMemberDetailResponseDto familyMemberDetailResponseDto = FamilyMemberDetailResponseDto.builder()
            .memberId(findMember.getId())
            .nickname(familyMember.getNickname())
            .build();

        return familyMemberDetailResponseDto;
    }
}
