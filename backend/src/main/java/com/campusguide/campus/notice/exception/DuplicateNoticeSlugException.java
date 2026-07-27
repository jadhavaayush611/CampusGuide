package com.campusguide.campus.notice.exception;

import com.campusguide.common.exception.ConflictException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateNoticeSlugException extends ConflictException {

    public DuplicateNoticeSlugException(String message) {
        super(message);
    }
}
