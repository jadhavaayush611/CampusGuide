package com.campusguide.campus.notice.exception;

import com.campusguide.common.exception.BadRequestException;
import com.campusguide.common.exception.ConflictException;
import com.campusguide.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NoticeExceptionTest {

    @Test
    void noticeNotFoundException_StoresMessageAndInheritsResourceNotFoundException() {
        String message = "Notice not found with ID: 1234";
        NoticeNotFoundException exception = new NoticeNotFoundException(message);

        assertEquals(message, exception.getMessage());
        assertInstanceOf(ResourceNotFoundException.class, exception);
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void duplicateNoticeSlugException_StoresMessageAndInheritsConflictException() {
        String message = "Notice with slug 'exam-notice' already exists";
        DuplicateNoticeSlugException exception = new DuplicateNoticeSlugException(message);

        assertEquals(message, exception.getMessage());
        assertInstanceOf(ConflictException.class, exception);
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void noticeValidationException_StoresMessageAndInheritsBadRequestException() {
        String message = "Expiration date must be after publication date";
        NoticeValidationException exception = new NoticeValidationException(message);

        assertEquals(message, exception.getMessage());
        assertInstanceOf(BadRequestException.class, exception);
        assertInstanceOf(RuntimeException.class, exception);
    }
}
