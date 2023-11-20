package com.lastdance.ziip.diary.repository.entity;

import com.lastdance.ziip.global.entity.BaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class DiaryPhoto extends BaseEntity {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diary_id")
    private Diary diary;

    private String imgUrl;

    private String imgName;

    public void updateDiaryPhoto(DiaryPhoto diaryPhoto){
        this.imgUrl = diaryPhoto.getImgUrl();
        this.imgName = diaryPhoto.getImgName();
    }
}
