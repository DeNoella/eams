package com.eams.exception;
import com.eams.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@Slf4j @RestControllerAdvice
public class GlobalExceptionHandler {
 @ExceptionHandler(ResourceNotFoundException.class)
 public ResponseEntity<ApiResponse<Void>> nf(ResourceNotFoundException e){return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));}
 @ExceptionHandler(MagicLinkExpiredException.class)
 public ResponseEntity<ApiResponse<Void>> ml(MagicLinkExpiredException e){return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(e.getMessage()));}
 @ExceptionHandler(EmailAlreadyInUseException.class)
 public ResponseEntity<ApiResponse<Void>> du(EmailAlreadyInUseException e){return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(e.getMessage()));}
 @ExceptionHandler(MethodArgumentNotValidException.class)
 public ResponseEntity<ApiResponse<Map<String,String>>> v(MethodArgumentNotValidException ex){
  Map<String,String> m=new HashMap<>();
  ex.getBindingResult().getAllErrors().forEach(er->m.put(((FieldError)er).getField(),er.getDefaultMessage()));
  return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.<Map<String,String>>builder().success(false).message("Validation failed").data(m).build());
 }
 @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
 public ResponseEntity<ApiResponse<Void>> di(org.springframework.dao.DataIntegrityViolationException ex){
  log.error("Data integrity violation: ",ex);
  Throwable r=ex.getMostSpecificCause();
  String msg=r!=null?r.getMessage():ex.getMessage();
  if(msg!=null&&msg.length()>300) msg=msg.substring(0,300)+"…";
  return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("Could not save record: "+(msg==null?"data constraint violated":msg)));
 }
 @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
 public ResponseEntity<ApiResponse<Void>> ur(org.springframework.http.converter.HttpMessageNotReadableException ex){
  log.warn("Malformed request body: {}",ex.getMessage());
  return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("Malformed request body"));
 }
 @ExceptionHandler(Exception.class)
 public ResponseEntity<ApiResponse<Void>> g(Exception ex){
  log.error("Unhandled exception: ",ex);
  String d=ex.getClass().getSimpleName();
  if(ex.getMessage()!=null&&!ex.getMessage().isBlank()){
   String m=ex.getMessage();
   if(m.length()>300) m=m.substring(0,300)+"…";
   d=d+": "+m;
  }
  return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("An unexpected error occurred — "+d));
 }
}
