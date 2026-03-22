package com.onlinestories.story_service.Exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    USER_NOT_FOUND(1001, "Author of this story not found", HttpStatus.NOT_FOUND),
    STORY_NOT_FOUND(2001, "Story not found", HttpStatus.NOT_FOUND),
    CHAPTER_NOT_FOUND(2002, "Chapter not found", HttpStatus.NOT_FOUND),
    COMMENT_NOT_FOUND(2003, "Comment not found", HttpStatus.NOT_FOUND),
    DRAFT_NOT_FOUND(2004, "Draft not found", HttpStatus.NOT_FOUND),
    DRAFT_ALREADY_EXISTS(2005, "Draft already exists for this story", HttpStatus.BAD_REQUEST),
    VERSION_NOT_FOUND(2006, "Version not found", HttpStatus.NOT_FOUND),
    CHAPTER_ALREADY_PUBLISHED(2007, "Chapter is already published", HttpStatus.BAD_REQUEST),
    NOT_AUTHOR_OF_STORY(2008, "User is not the author of the story", HttpStatus.FORBIDDEN),
    GENRE_NOT_FOUND(2009, "Genre not found", HttpStatus.NOT_FOUND),
    GENRE_ALREADY_EXISTS(2010, "Genre already exists", HttpStatus.BAD_REQUEST),
    PARENT_COMMENT_NOT_FOUND(2011, "Parent comment not found", HttpStatus.NOT_FOUND),
    STORY_IS_NOT_PUBLISHED(2012, "Story is not published", HttpStatus.BAD_REQUEST),
    CHAPTER_IS_NOT_PUBLISHED(2013, "Chapter is not published", HttpStatus.BAD_REQUEST),
    RATING_ALREADY_EXISTS(2014, "User has already rated this story", HttpStatus.BAD_REQUEST),
    STORY_IS_COMPLETED(2015, "Story is already completed", HttpStatus.BAD_REQUEST),
    INVALID_FILE(2016, "File is empty or missing", HttpStatus.BAD_REQUEST),
    UNSUPPORTED_FILE_TYPE(2017, "Only .docx files are supported", HttpStatus.BAD_REQUEST),
    EMPTY_IMPORT_CONTENT(2018, "Imported content is empty", HttpStatus.BAD_REQUEST),
    FILE_IMPORT_FAILED(2019, "Failed to import Word file", HttpStatus.INTERNAL_SERVER_ERROR),
    ACCESS_DENIED(2900, "Access denied", HttpStatus.FORBIDDEN)

    ;
    private int code;
    private String message;
    private HttpStatus httpStatusCode;

    ErrorCode(int code, String message, HttpStatusCode httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = (HttpStatus) httpStatusCode;
    }
}
