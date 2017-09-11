package com.jeesuite.admin.exception;

public class JeesuiteBaseException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	private int code;
	

	public JeesuiteBaseException() {}


	public JeesuiteBaseException(int code,String message) {
		super(message);
		this.code = code;
	}


	public int getCode() {
		return code;
	}
	
	
	public static JeesuiteBaseException NOT_EXIST_EXCEPTION = new JeesuiteBaseException(1001, "对象不存在");

}
