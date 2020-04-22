package com.bridgelabz.fundoonotesms.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.bridgelabz.fundoonotesms.dto.EmailDTO;
import com.bridgelabz.fundoonotesms.dto.LoginDTO;
import com.bridgelabz.fundoonotesms.dto.ResetDTO;
import com.bridgelabz.fundoonotesms.dto.UserDTO;
import com.bridgelabz.fundoonotesms.entity.User;
import com.bridgelabz.fundoonotesms.exception.UserException;
import com.bridgelabz.fundoonotesms.repository.UserRepository;
import com.bridgelabz.fundoonotesms.service.UserService;
import com.bridgelabz.fundoonotesms.utility.JWTUtil;

/**
 * @author Chandrakala Kirasur
 */
@Service
public class UserServiceImpl implements UserService {
	@Autowired
	UserRepository userRepo;
	@Autowired
	private JWTUtil util;
	@Autowired
	private BCryptPasswordEncoder encoder;
	
	private static final String TABLE_NAME = "user";
	@Autowired
	private RestTemplate restTemplate;
	
	private RedisTemplate<String, Object> redisTemplate;

	private HashOperations<String, Long, User> hashOperations;

	@Autowired
	public UserServiceImpl(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	@PostConstruct
	private void intializeHashOperations() {
		hashOperations = redisTemplate.opsForHash();
	}

	/*
	 * This method will takes user DTO object as a input from user. it will checks
	 * whether user name is already present in data base. If it is present already
	 * it will return false statement. if user name is new to database it will
	 * verifies the email id whether given mail id is valid or not. then it will
	 * stores the information in database. and thats how registration done.
	 */
	@Override
	public boolean register(UserDTO userDto) {
		User user = new User();
		BeanUtils.copyProperties(userDto, user);
		if (userRepo.existsByFirstName(user.getFirstName())) {
			return false;
		}
		user.setPassword(encoder.encode(user.getPassword()));
		if (sendNotification(user))
			userRepo.save(user);
		return true;
	}

	/*
	 * This method is for sending verification link to user using their email
	 * id.Here it will take email id from environment variables to send mail. JMS is
	 * used to send simple mail to user.
	 */
	private boolean sendNotification(User user) {
		System.out.println(user.getEmailId());
		boolean status=restTemplate.exchange("http://notification-service/send/verification/"+user.getEmailId(),HttpMethod.GET,null, Boolean.class).getBody();
		return status;
//		"http://student-service/getStudentDetailsForSchool/{schoolname}"
	}

	/*
	 * Verify Mail method will called by user controller when user clicks the link
	 * by his mail . it will be executes and stores user mail ID is valid
	 */
	@Override
	public boolean verifyEmail(String emailId) {
		User user = userRepo.findByEmailId(emailId).orElseThrow(UserException::new);
		if (!user.isVerified()) {
			user.setVerified(true);
			userRepo.save(user);
			return true;
		}
		return false;
	}

	/*
	 * login method is for give access to the application. It will takes two
	 * parameters from user, those are user name and password. It will verifies the
	 * user name and password by fetching user name and password from database. If
	 * those are matches here it will generates token for user id and sends back to
	 * user. Here this token is used for accessing other methods in the application
	 * and it will be valid for 24 days after that they should login again to the
	 * application.
	 */
	@Override
	public User login(LoginDTO loginDto) {
		User user = userRepo.findByEmailId(loginDto.getEmailId()).orElseThrow(() -> new UserException("User not found", 400));
		if(encoder.matches(loginDto.getPassword(), user.getPassword())) {
			hashOperations.put(TABLE_NAME, user.getUserID(), user);
			return user;
		}
		return null;
	}

	/*
	 * This method takes user email ID to send link to create new password. If email
	 * is valid it will sends link and by that method they can change their
	 * password.
	 */
	@Override
	public boolean sendLinkForForgetPassword(EmailDTO email) {
		User user = userRepo.findByEmailId(email.getEmail()).orElseThrow(() -> new UserException("User not found", 400));
		System.out.println(user.getEmailId());
		String token=util.generateToken(user);
		ResponseEntity<Boolean> status=restTemplate.exchange("http://notification-service/send/forgetpassword/link/"+user.getEmailId()+"/"+token,HttpMethod.GET,null, Boolean.class);
		return status.getBody();
	}

	/*
	 * This method is to create new password. It will takes to input from new user
	 * one is user ID this is already send by developer in the link, and another two
	 * inputs are new password and confirmed password. It will encrypts the new
	 * password if both new password and confirmed password are matched and updates
	 * the user information in database by adding new password, returns boolean true
	 * as response.
	 */
	@Override
	public boolean resetUserPassword(ResetDTO resetDto) {
		System.out.println(resetDto.getToken());
		User user = userRepo.findByUserID(util.extractUserID(resetDto.getToken())).orElseThrow(() -> new UserException("User not found", 400));
		user.setPassword(encoder.encode(resetDto.getPassword()));
		userRepo.save(user);
		return true;
	}

	/*
	 * This method gives user object which has its all information. It will takes
	 * token as string and fetches the user. if user is exists it will sends user
	 * object as a response or else it will throws user exception with message
	 * "user not found".
	 */
	@Override
	public User getUser(String token) {
		Long userID = util.extractUserID(token);
		//Long userID=getRadisTokenId(token);
		Optional<User> userOp = userRepo.findByUserID(userID);
		User user = null;
		if (userOp.isPresent()) {
			user = userOp.get();
			return user;
		}
		return user;
	}

	/*
	 * This method returns updated user object as response. It will takes two inputs
	 * one is token to fetch user and another one is DTO object which has to be
	 * updated. If user exists it will updates the user by copying properties of
	 * user DTO to user entity. If user doesn't exists it will throws user
	 * exception.
	 */
	@Override
	public User updateUser(String token, UserDTO userDto) {
		User user = fetchAndGetUser(token);
		if (util.validateToken(token, user)) {
			BeanUtils.copyProperties(userDto, user);
			userRepo.save(user);
			return user;
		}
		return user;
	}

	/*
	 * Here user can deletes only his information or his account from database. So
	 * this method will deletes the user and response back by boolean true or false.
	 * It will takes user ID and token as a input. It will fetches the user by token
	 * and if it is valid it will compares the fetched user ID with given user ID if
	 * both matches it will deletes the user information from database, sends true
	 * as a response or else sends boolean false as a response.
	 */
	@Override
	public boolean deleteUser(Long userID, String token) {
		Long id = util.extractUserID(token);
		if (id.equals(userID)) {
			userRepo.deleteByUserID(userID);
			return true;
		}
		return false;
	}

	/*
	 * This method is used for get,update and delete user. It will takes token as a
	 * string and by validating it will extracts and user ID. If token is valid
	 * repository will find the user by user ID, if user exist it will returns the
	 * user to the required method if not it will sends user exception. If token is
	 * not valid it will ask user to login again
	 */
	private User fetchAndGetUser(String token) {
		try {
			Long userID = util.extractUserID(token);
			return userRepo.findByUserID(userID).orElseThrow(() -> new UserException("User not found", 400));
		} catch (Exception e) {
			throw new UserException("token is not valid so please login again", 400);
		}
	}

	/*
	 * @PostConstruct private void intializeHashOperations() { hashOperations =
	 * redisTemplate.opsForHash(); }
	 */
	/*
	 * private Long getRadisTokenId(String token) { String[]
	 * tokenParts=token.split("\\."); String tokenKey=tokenParts[1]+tokenParts[2];
	 * if(redisTemplate.opsForValue().get(tokenKey)==null) { Long
	 * idForRedis=util.extractUserID(token);
	 * redisTemplate.opsForValue().set(tokenKey, idForRedis,3*60,TimeUnit.SECONDS);
	 * } return (Long) redisTemplate.opsForValue().get(tokenKey); }
	 */
	@Override
	public List<User> findAllUser() {
		List<User> fetchedUsers=new ArrayList<User>();
		userRepo.findAll().forEach(fetchedUsers::add);
		return fetchedUsers;
	}

	@Override
	public User getUserByRadis(Long userID) {
		return hashOperations.get(TABLE_NAME, userID);
	}
}
