package com.lastdance.ziip.diary.enums;

public enum DiaryResponseMessage {

    DIARY_WRITE_SUCCESS("일기 작성 성공"),
    DIARY_LIST_SUCCESS("일기 리스트 조회 성공"),
    DIARY_DETAIL_SUCCESS("일기 상세 조회 성공"),
    DIARY_DELETE_SUCCESS("일기 삭제 성공"),
    DIARY_MODIFY_SUCCESS("일기 수정 성공"),
    DIARY_COMMENT_WRITE_SUCCESS("일기 댓글 작성 성공"),
    DIARY_COMMENT_MODIFY_SUCCESS("일기 댓글 수정 성공"),
    DIARY_COMMENT_DELETE_SUCCESS("일기 댓글 삭제 성공");
    private final String message;

    DiaryResponseMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

}
