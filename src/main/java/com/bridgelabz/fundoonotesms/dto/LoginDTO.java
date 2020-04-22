package com.bridgelabz.fundoonotesms.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginDTO {
	@Email
	private String emailId;
	@Size(min=8,max = 16,message = "password must be equal or greater than 8 characters and less than 16 characters")
	private String password;
}
