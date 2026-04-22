package kg.kut.os.mentorhub.common.dto;

import java.time.LocalDateTime;
import java.util.Map;

public class ErrorResponse {

    private String code;
    private String errorCode;
    private String message;
    private LocalDateTime timestamp;
    private Map<String, String> fieldErrors;

    public ErrorResponse() {
    }

    public ErrorResponse(String errorCode, String message, LocalDateTime timestamp) {
        this.code = errorCode;
        this.errorCode = errorCode;
        this.message = message;
        this.timestamp = timestamp;
    }

    public ErrorResponse(String errorCode, String message, LocalDateTime timestamp, Map<String, String> fieldErrors) {
        this.code = errorCode;
        this.errorCode = errorCode;
        this.message = message;
        this.timestamp = timestamp;
        this.fieldErrors = fieldErrors;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(Map<String, String> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }
}
