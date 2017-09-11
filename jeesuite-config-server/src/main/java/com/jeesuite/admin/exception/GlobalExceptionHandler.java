package com.jeesuite.admin.exception;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jeesuite.admin.model.WrapperResponseEntity;

@ControllerAdvice
public class GlobalExceptionHandler {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@ExceptionHandler(Exception.class)
	@ResponseBody
	public ResponseEntity<WrapperResponseEntity> exceptionHandler(Exception e, HttpServletResponse response) {
		WrapperResponseEntity resp = new WrapperResponseEntity();
		if (e instanceof JeesuiteBaseException) {
			JeesuiteBaseException e1 = (JeesuiteBaseException) e;
			resp.setCode(e1.getCode());
			resp.setMsg(e1.getMessage());
		} else if(e instanceof org.springframework.web.HttpRequestMethodNotSupportedException){
			resp.setCode(HttpStatus.METHOD_NOT_ALLOWED.value());
			resp.setMsg(e.getMessage()); 
		}else if(e instanceof org.springframework.web.HttpMediaTypeException){
			resp.setCode(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value());
			resp.setMsg(e.getMessage()); 
		}else {
			resp.setCode(500);
			resp.setMsg("系统繁忙");
			logger.error("",e);
		}
		return new ResponseEntity<WrapperResponseEntity>(resp,HttpStatus.OK);
	}
}