package com.lastdance.ziip.member.repository.entity;

import com.lastdance.ziip.diary.repository.entity.Diary;
import com.lastdance.ziip.diary.repository.entity.DiaryAlert;
import com.lastdance.ziip.diary.repository.entity.DiaryComment;
import com.lastdance.ziip.diary.repository.entity.DiaryCommentAlert;
import com.lastdance.ziip.member.dto.FileDto;
import com.lastdance.ziip.member.dto.request.MemberInfoUpdateRequestDto;
import com.lastdance.ziip.member.enums.Gender;
import com.lastdance.ziip.family.repository.entity.FamilyMember;
import com.lastdance.ziip.global.entity.BaseEntity;
import com.lastdance.ziip.member.enums.Role;
import com.lastdance.ziip.member.enums.SocialType;
import com.lastdance.ziip.plan.repository.entity.Plan;

import com.lastdance.ziip.schedule.repository.entity.Schedule;
import java.util.List;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

import com.lastdance.ziip.schedule.repository.entity.SchedulePhoto;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Member extends BaseEntity{

    @Id @GeneratedValue
    private Long id;

    private String email;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String name;
    private String profileImgUrl;
    private String profileImgName;
    private String socialId;

    @Enumerated(EnumType.STRING)
    private SocialType socialType;

    private String fcmToken;

    @Enumerated(EnumType.STRING)
    private Role role;


    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private List<FamilyMember> familyMembers;

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private List<Diary> diaries;

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private List<DiaryComment> diaryComments;

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private List<Plan> plans;

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private List<Schedule> schedules;

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private List<DiaryAlert> diaryAlerts;

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private List<DiaryCommentAlert> diaryCommentAlerts;

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<SchedulePhoto> schedulePhotos;

    public void updateName(String name){
        this.name = name;
    }

    //회원정보 수정
    public void updateMemberInfo(MemberInfoUpdateRequestDto mypageUpdateRequestDto, FileDto fileDto) {
        this.name = mypageUpdateRequestDto.getName();
        this.profileImgName = fileDto.getFileOriginalName();
        this.profileImgUrl = fileDto.getFilePath();
    }
    public void updateMemberInfo(MemberInfoUpdateRequestDto mypageUpdateRequestDto) {
        this.name = mypageUpdateRequestDto.getName();
    }
    public void updateMemberInfo(FileDto fileDto) {
        this.profileImgName = fileDto.getFileOriginalName();
        this.profileImgUrl = fileDto.getFilePath();
    }

}
