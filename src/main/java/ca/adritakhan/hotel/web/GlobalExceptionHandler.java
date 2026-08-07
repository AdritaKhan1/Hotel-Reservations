package ca.adritakhan.hotel.web;

import ca.adritakhan.hotel.service.EmailAlreadyRegisteredException;
import ca.adritakhan.hotel.service.HotelNotFoundException;
import ca.adritakhan.hotel.service.InvalidCardException;
import ca.adritakhan.hotel.service.InvalidReservationRequestException;
import ca.adritakhan.hotel.service.InvalidStayException;
import ca.adritakhan.hotel.service.ReservationConflictException;
import ca.adritakhan.hotel.service.RoomTypeNotFoundException;
import ca.adritakhan.hotel.service.RoomsSoldOutException;
import ca.adritakhan.hotel.web.dto.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RoomTypeNotFoundException.class)
    public ResponseEntity<ApiError> notFound(RoomTypeNotFoundException e) {
        return build(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(HotelNotFoundException.class)
    public ResponseEntity<ApiError> hotelNotFound(HotelNotFoundException e) {
        return build(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(RoomsSoldOutException.class)
    public ResponseEntity<ApiError> soldOut(RoomsSoldOutException e) {
        return build(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(InvalidStayException.class)
    public ResponseEntity<ApiError> invalidStay(InvalidStayException e) {
        return build(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(InvalidReservationRequestException.class)
    public ResponseEntity<ApiError> invalidReservationRequest(InvalidReservationRequestException e) {
        return build(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(ReservationConflictException.class)
    public ResponseEntity<ApiError> conflict(ReservationConflictException e) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
    }

    @ExceptionHandler(InvalidCardException.class)
    public ResponseEntity<ApiError> invalidCard(InvalidCardException e) {
        return build(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    public ResponseEntity<ApiError> emailTaken(EmailAlreadyRegisteredException e) {
        return build(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> authFailed(AuthenticationException e) {
        return build(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(ApiError.of(status.value(), status.getReasonPhrase(), message));
    }
}
