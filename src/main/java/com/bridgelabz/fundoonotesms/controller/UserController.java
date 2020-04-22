package com.bridgelabz.fundoonotesms.controller;

import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bridgelabz.fundoonotesms.dto.EmailDTO;
import com.bridgelabz.fundoonotesms.dto.LoginDTO;
import com.bridgelabz.fundoonotesms.dto.ResetDTO;
import com.bridgelabz.fundoonotesms.dto.UserDTO;
import com.bridgelabz.fundoonotesms.entity.User;
import com.bridgelabz.fundoonotesms.response.Response;
import com.bridgelabz.fundoonotesms.response.UserResponse;
import com.bridgelabz.fundoonotesms.service.AmazonS3ClientService;
import com.bridgelabz.fundoonotesms.service.UserService;
import com.bridgelabz.fundoonotesms.utility.JWTUtil;


/**
 * @author hp
 *
 */
@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {
	@Autowired
	private UserService service;
	@Autowired
	private JWTUtil util;

	/*
	 * welcome page
	 */
	@GetMapping("/welcome")
	public String welcome() {
		return "welcome ";
	}

	/* new user registration method and send link to mail verification */
	@PostMapping("/register")
	public ResponseEntity<Response> register(@Valid @RequestBody UserDTO userDto) {
		boolean resultStatus = service.register(userDto);
		if (resultStatus) {
			return ResponseEntity.status(HttpStatus.OK)
					.body(new Response("registered successfully", 200, resultStatus));
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new Response("user already exist please login", 208, resultStatus));
	}

	/* It will executes when user clicks link by their mail */
	@RequestMapping("/{emailId}")
	public ResponseEntity<Response> verifyMail(@PathVariable String emailId) {
		boolean resultStatus = service.verifyEmail(emailId);
		if (resultStatus) {
			return ResponseEntity.status(HttpStatus.OK).body(new Response("verified ", 200, resultStatus));
		}
		return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT)
				.body(new Response("your mail is already verified", 208, resultStatus));
	}

	/*
	 * login if user name and password is correct and gives token to get access to
	 * other options in application
	 */
	@PostMapping("/login")
	public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginDTO loginDto) {
		User user = service.login(loginDto);
		if (user != null) {
			String token=util.generateToken(user);
			return ResponseEntity.status(HttpStatus.OK).body(new UserResponse("login successful", 200, token,user.getFirstName(),user.getEmailId()));
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new UserResponse("user name or password is invalid ", 208,"","",""));
	}

	/* This method is to send link to create new password */
	@PostMapping("/forgotpassword")
	public ResponseEntity<Response> sendLinkToResetPassword(@RequestBody EmailDTO email) {
		boolean resultStatus = service.sendLinkForForgetPassword(email);
		if (resultStatus) {
			return ResponseEntity.status(HttpStatus.OK).body(new Response("kindly check your mail", 200, resultStatus));
		}
		return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT)
				.body(new Response("please enter valid mail ID", 208, resultStatus));
	}

	/*
	 * this will executes when user clicks the link by their mail and resets the new
	 * password
	 */
	@PostMapping("/reset")
	public ResponseEntity<Response> resetUserPassword(@RequestBody ResetDTO resetDto){
		boolean resultStatus = service.resetUserPassword(resetDto);
		if (resultStatus) {
			return ResponseEntity.status(HttpStatus.OK)
					.body(new Response("reset password successful", 200, resultStatus));
		}
		return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT)
				.body(new Response("user not found kindly try again", 208, resultStatus));
	}

	/* to read user information */
	@GetMapping("/user")
	public ResponseEntity<Response> getUser(@RequestHeader String token) {
		User fetchedUser = service.getUser(token);
		if (fetchedUser != null) {
			return ResponseEntity.status(HttpStatus.OK)
					.body(new Response(fetchedUser.getFirstName(), 200, fetchedUser));
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response("user doesn't exist", 208, fetchedUser));
	}
	
	/* method to update user */
	@PutMapping("/user/{token}")
	public ResponseEntity<Response> updateUser(@PathVariable String token, @RequestBody UserDTO userDto){
		User updatedUser = service.updateUser(token, userDto);
		if (updatedUser != null) {
			return ResponseEntity.status(HttpStatus.OK)
					.body(new Response(userDto.getFirstName() + " updated successfully", 200, updatedUser));
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new Response("error occured while updating kindly try again", 208, updatedUser));
	}

	/* delete user by their user ID permanently */
	@DeleteMapping("/user/{userID}/{token}")
	public ResponseEntity<Response> deleteUser(@PathVariable Long userID, @PathVariable String token) {
		boolean resultStatus = service.deleteUser(userID, token);
		if (resultStatus) {
			return ResponseEntity.status(HttpStatus.OK)
					.body(new Response(userID + " deleted successfully", 200, resultStatus));
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new Response("error occured while deleting", 208, resultStatus));
	}

	@Autowired
	private AmazonS3ClientService amazonS3ClientService;
	@PostMapping("/uploadProfile/{token}")
	public ResponseEntity<Response> uploadFile(@RequestPart(value = "file") MultipartFile file,@PathVariable("token") String token) {
		String result=this.amazonS3ClientService.uploadFileToS3Bucket(file, true,token);
		if(result!=null) {
			return ResponseEntity.status(HttpStatus.OK)
					.body(new Response("got url from s3", 200, result));
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response("file [" + file.getOriginalFilename() + "] uploading request unsuccessfully.", 400, result));

	}
	@DeleteMapping("/deletephono/{token}")
	public Map<String, String> deleteFile(@RequestParam("file_name") String fileName,@PathVariable("token") String token) {
		this.amazonS3ClientService.deleteFileFromS3Bucket(fileName);
		Map<String, String> response = new HashMap<>();
		response.put("message", "file [" + fileName + "] removing request submitted successfully.");
		return response;
	}

	/*
	 * This method is taking user id as path variable and fetching user by redis
	 * caching
	 */ 
	@Cacheable(key="#userID", value="user")
	@GetMapping("/{userID}")
	public ResponseEntity<User> getUserByRadis(@PathVariable Long userID) {
		User fetchedUser = service.getUserByRadis(userID);
		if (fetchedUser != null) {
			return ResponseEntity.status(HttpStatus.OK)
					.body(fetchedUser);
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(fetchedUser);
	}
    
}
