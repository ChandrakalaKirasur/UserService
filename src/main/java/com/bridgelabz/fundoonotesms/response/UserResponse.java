package com.bridgelabz.fundoonotesms.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserResponse {
	private String message;
	private int status;
	private String token;
	private String firstName;
	private String email;

}
