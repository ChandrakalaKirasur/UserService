package com.bridgelabz.fundoonotesms.dto;

import java.io.Serializable;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

import com.sun.istack.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@NotNull
	private String firstName;
	private String lastName;
	@Size(min=8,max = 16,message = "password must be equal or greater than 8 characters and less than 16 characters")
	private String password;
	@NotNull
	private String phoneNumber;
	@Email
	private String emailId;

}
