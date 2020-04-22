package com.bridgelabz.fundoonotesms.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Response {
	
	private String message;
	private int status;
	private Object resultStatus;

}
