package com.lastdance.ziip.album.service;

import com.lastdance.ziip.album.dto.request.AlbumMonthRequestDto;
import com.lastdance.ziip.album.dto.response.AlbumImageResponseDto;
import com.lastdance.ziip.album.dto.response.AlbumListResponseDto;
import com.lastdance.ziip.album.dto.response.AlbumMonthResponseDto;
import com.lastdance.ziip.album.enums.ImageCategory;
import com.lastdance.ziip.diary.repository.DiaryPhotoRepository;
import com.lastdance.ziip.diary.repository.entity.DiaryPhoto;
import com.lastdance.ziip.diary.repository.entity.QDiaryPhoto;
import com.lastdance.ziip.family.repository.entity.FamilyMember;
import com.lastdance.ziip.family.repository.entity.QFamilyMember;
import com.lastdance.ziip.member.repository.entity.Member;
import com.lastdance.ziip.schedule.repository.SchedulePhotoRepository;
import com.lastdance.ziip.schedule.repository.entity.QSchedulePhoto;
import com.lastdance.ziip.schedule.repository.entity.SchedulePhoto;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AlbumServiceImpl implements AlbumService{

    private final JPAQueryFactory jpaQueryFactory;
    private final DiaryPhotoRepository diaryPhotoRepository;
    private final SchedulePhotoRepository schedulePhotoRepository;

    @Override
    public AlbumListResponseDto listAlbum(Member findMember, Long familyId) {

        QSchedulePhoto qSchedulePhoto = QSchedulePhoto.schedulePhoto;

        List<SchedulePhoto> schedulePhotos = jpaQueryFactory
                .selectFrom(qSchedulePhoto)
                .where(qSchedulePhoto.schedule.family.id.eq(familyId))
                .fetch();


        QDiaryPhoto qDiaryPhoto = QDiaryPhoto.diaryPhoto;

        List<DiaryPhoto> diaryPhotos = jpaQueryFactory
                .selectFrom(qDiaryPhoto)
                .where(qDiaryPhoto.diary.family.id.eq(familyId))
                .fetch();

        List<AlbumImageResponseDto> resultList = new ArrayList<>();

        List<AlbumImageResponseDto> schedulePhotoList = schedulePhotos.stream()
                .map(schedulePhoto -> schedulePhotoToDto(schedulePhoto))
                .collect(Collectors.toList());

        List<AlbumImageResponseDto> diaryPhotoList = diaryPhotos.stream()
                .map(diaryPhoto -> diaryPhotoToDto(diaryPhoto))
                .collect(Collectors.toList());

        resultList.addAll(schedulePhotoList);
        resultList.addAll(diaryPhotoList);
        Collections.sort(resultList, Comparator.comparing(AlbumImageResponseDto::getStartDate));

        AlbumListResponseDto albumListResponseDto = AlbumListResponseDto.builder()
                .images(resultList)
                .build();

        return albumListResponseDto;
    }

    @Override
    public AlbumMonthResponseDto monthAlbum(Member fineMember, int year, int month, Long familyId) {

        // 해당 월의 시작날짜와 마지막 날짜 계산
        LocalDateTime startOfMonth = LocalDateTime.of(year,month,1,0,0,0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);

        LocalDate startOfMonthDate = LocalDate.of(year,month,1);
        LocalDate endOfMonthDate = startOfMonthDate.plusMonths(1).minusDays(1);

        // 해당 월의 일기, 스케줄 사진 조회
        QSchedulePhoto qSchedulePhoto = QSchedulePhoto.schedulePhoto;

        List<SchedulePhoto> schedulePhotos = jpaQueryFactory
                .selectFrom(qSchedulePhoto)
                .where(qSchedulePhoto.schedule.family.id.eq(familyId))
                .where(qSchedulePhoto.schedule.startDate.between(startOfMonthDate,endOfMonthDate))
                .where(qSchedulePhoto.schedule.endDate.between(startOfMonthDate,endOfMonthDate))
                .fetch();


        QDiaryPhoto qDiaryPhoto = QDiaryPhoto.diaryPhoto;

        List<DiaryPhoto> diaryPhotos = jpaQueryFactory
                .selectFrom(qDiaryPhoto)
                .where(qDiaryPhoto.diary.family.id.eq(familyId))
                .where(qDiaryPhoto.createdAt.between(startOfMonth,endOfMonth))
                .fetch();

        List<AlbumImageResponseDto> resultList = new ArrayList<>();

        List<AlbumImageResponseDto> schedulePhotoList = schedulePhotos.stream()
                .map(schedulePhoto -> schedulePhotoToDto(schedulePhoto))
                .collect(Collectors.toList());

        List<AlbumImageResponseDto> diaryPhotoList = diaryPhotos.stream()
                .map(diaryPhoto -> diaryPhotoToDto(diaryPhoto))
                .collect(Collectors.toList());

        resultList.addAll(schedulePhotoList);
        resultList.addAll(diaryPhotoList);
        Collections.sort(resultList, Comparator.comparing(AlbumImageResponseDto::getStartDate));

        AlbumMonthResponseDto albumMonthResponseDto = AlbumMonthResponseDto.builder()
                .images(resultList)
                .build();

        return albumMonthResponseDto;
    }


    // 스케줄 사진 dto화
    private AlbumImageResponseDto schedulePhotoToDto(SchedulePhoto schedulePhoto){
        return AlbumImageResponseDto.builder()
                .category(ImageCategory.SCHEDULE)
                .detail(schedulePhoto.getSchedule().getTitle())
                .imgUrl(schedulePhoto.getImgUrl())
                .startDate(schedulePhoto.getSchedule().getStartDate())
                .endDate(schedulePhoto.getSchedule().getEndDate())
                .build();
    }


    // 일정 사진 dto화
    private AlbumImageResponseDto diaryPhotoToDto(DiaryPhoto diaryPhoto){

        QFamilyMember qFamilyMember = QFamilyMember.familyMember;

        List<FamilyMember> familyMemberData = jpaQueryFactory
                .selectFrom(qFamilyMember)
                .where(qFamilyMember.family.id.eq(diaryPhoto.getDiary().getFamily().getId())
                        .and (qFamilyMember.member.id.eq(diaryPhoto.getDiary().getMember().getId())))
                .fetch();

        return AlbumImageResponseDto.builder()
                .category(ImageCategory.DIARY)
                .detail(familyMemberData.get(0).getNickname() + "의 일기")
                .imgUrl(diaryPhoto.getImgUrl())
                .startDate(diaryPhoto.getCreatedAt().toLocalDate())
                .endDate(null)
                .build();
    }

}