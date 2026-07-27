package com.campusguide.campus.notice.exception;

import com.campusguide.common.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NoticeNotFoundException extends ResourceNotFoundException {

    public NoticeNotFoundException(String message) {
        super(message);
    }
}
