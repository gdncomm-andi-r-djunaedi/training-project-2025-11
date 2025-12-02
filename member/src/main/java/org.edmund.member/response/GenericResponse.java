package org.edmund.member.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL) // omit null message/data in JSON
public class GenericResponse<T> {
    private Integer code;
    private String status;
    private String message;
    private T data;

    private static <T> GenericResponse<T> of(HttpStatus http, String message, T data) {
        GenericResponse<T> r = new GenericResponse<>();
        r.setCode(http.value());
        r.setStatus(http.getReasonPhrase());
        r.setMessage(message);
        r.setData(data);
        return r;
    }

    public static <T> GenericResponse<T> ok(T data) {
        return of(HttpStatus.OK, null, data);
    }

    public static <T> GenericResponse<T> ok(String message, T data) {
        return of(HttpStatus.OK, message, data);
    }

    public static <T> GenericResponse<T> okMessage(String message) {
        return ok(message, null);
    }

    public static <T> GenericResponse<T> okData(T data) {
        return ok(data);
    }

    public static <T> GenericResponse<T> badRequest(String message) {
        return of(HttpStatus.BAD_REQUEST, message, null);
    }

    public static <T> GenericResponse<T> badRequest(String message, T data) {
        return of(HttpStatus.BAD_REQUEST, message, data);
    }

    public static <T> GenericResponse<T> badRequestWithData(String message, T data) {
        return badRequest(message, data);
    }

    public static <T> GenericResponse<T> notFound(String message) {
        return of(HttpStatus.NOT_FOUND, message, null);
    }

    public static <T> GenericResponse<T> notFound(String message, T data) {
        return of(HttpStatus.NOT_FOUND, message, data);
    }

    public static <T> GenericResponse<T> notFoundWithData(String message, T data) {
        return notFound(message, data);
    }

    public static <T> GenericResponse<T> status(HttpStatus status, String message, T data) {
        return of(status, message, data);
    }

    public static <T> GenericResponse<T> status(HttpStatus status, String message) {
        return of(status, message, null);
    }
}