package com.lastdance.ziip.diary.service;

import com.lastdance.ziip.diary.dto.request.DiaryCommentDeleteRequestDto;
import com.lastdance.ziip.diary.dto.request.DiaryCommentModifyRequestDto;
import com.lastdance.ziip.diary.dto.request.DiaryCommentWriteRequestDto;
import com.lastdance.ziip.diary.dto.response.DiaryCommentDeleteResponseDto;
import com.lastdance.ziip.diary.dto.response.DiaryCommentModifyResponseDto;
import com.lastdance.ziip.diary.dto.response.DiaryCommentWriteResponseDto;
import com.lastdance.ziip.diary.exception.validator.DiaryValidator;
import com.lastdance.ziip.diary.repository.DiaryCommentRepository;
import com.lastdance.ziip.diary.repository.DiaryRepository;
import com.lastdance.ziip.diary.repository.entity.Diary;
import com.lastdance.ziip.diary.repository.entity.DiaryComment;
import com.lastdance.ziip.family.repository.FamilyRepository;
import com.lastdance.ziip.member.repository.MemberRepository;
import com.lastdance.ziip.member.repository.entity.Member;
import com.lastdance.ziip.schedule.repository.ScheduleRepository;
import com.lastdance.ziip.schedule.repository.entity.Schedule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.stream.events.Comment;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class DiaryCommentServiceImpl implements DiaryCommentService{

    private final DiaryRepository diaryRepository;
    private final DiaryCommentRepository diaryCommentRepository;
    private final DiaryValidator diaryValidator;

    @Override
    public DiaryCommentWriteResponseDto writeDiaryComment(Member findMember,
                                                          DiaryCommentWriteRequestDto diaryCommentWriteRequestDto){

        Optional<Diary> diary = diaryRepository.findById(diaryCommentWriteRequestDto.getDiaryId());

        DiaryComment diaryComment = DiaryComment.builder()
                .member(findMember)
                .diary(diary.get())
                .content(diaryCommentWriteRequestDto.getContent())
                .build();

        DiaryComment saveDiaryComment = diaryCommentRepository.save(diaryComment);


        DiaryCommentWriteResponseDto diaryCommentWriteResponseDto = DiaryCommentWriteResponseDto.builder()
                .commentId(saveDiaryComment.getId())
                .build();

        return diaryCommentWriteResponseDto;
    }

    @Override
    public DiaryCommentModifyResponseDto modifyDiaryComment(Member findMember,
                                                            DiaryCommentModifyRequestDto diaryCommentModifyRequestDto) {

        Optional<DiaryComment> diaryComment = diaryCommentRepository.findById(diaryCommentModifyRequestDto.getCommentId());

        diaryComment.get().updateDiaryComment(diaryCommentModifyRequestDto);

        diaryCommentRepository.save(diaryComment.get());

        DiaryCommentModifyResponseDto diaryCommentModifyResponseDto = DiaryCommentModifyResponseDto.builder()
                .commentId(diaryCommentModifyRequestDto.getCommentId())
                .build();

        return  diaryCommentModifyResponseDto;
    }

    @Override
    public DiaryCommentDeleteResponseDto deleteDiaryComment(Member findMember, DiaryCommentDeleteRequestDto diaryCommentDeleteRequestDto) {

        Optional<DiaryComment> tmpDiaryComment = diaryCommentRepository.findById(diaryCommentDeleteRequestDto.getCommentId());
        diaryValidator.checkDiaryCommentExist(tmpDiaryComment);
        DiaryComment diaryComment = tmpDiaryComment.get();

        diaryValidator.checkDiaryCommentManager(diaryComment, findMember.getId());

        diaryCommentRepository.delete(diaryComment);

        DiaryCommentDeleteResponseDto diaryCommentDeleteResponseDto = DiaryCommentDeleteResponseDto.builder()
                .commentId(diaryCommentDeleteRequestDto.getCommentId())
                .build();

        return diaryCommentDeleteResponseDto;
    }
}
