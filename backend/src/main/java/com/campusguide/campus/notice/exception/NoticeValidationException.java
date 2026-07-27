package com.campusguide.campus.notice.exception;

import com.campusguide.common.exception.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NoticeValidationException extends BadRequestException {

    public NoticeValidationException(String message) {
        super(message);
    }
}
