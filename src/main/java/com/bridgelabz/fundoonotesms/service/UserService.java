package com.bridgelabz.fundoonotesms.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.bridgelabz.fundoonotesms.dto.EmailDTO;
import com.bridgelabz.fundoonotesms.dto.LoginDTO;
import com.bridgelabz.fundoonotesms.dto.ResetDTO;
import com.bridgelabz.fundoonotesms.dto.UserDTO;
import com.bridgelabz.fundoonotesms.entity.User;

@Service
public interface UserService {

	public boolean register(UserDTO userDto);

	boolean verifyEmail(String emailId);

	User login(LoginDTO loginDto);

	public boolean sendLinkForForgetPassword(EmailDTO emailID);

	public boolean resetUserPassword(ResetDTO resetDto);

	public User getUser(String token);

	public User updateUser(String token, UserDTO userDto);

	public boolean deleteUser(Long userID, String token);
	
	public List<User> findAllUser();

	public User getUserByRadis(Long userID);

}