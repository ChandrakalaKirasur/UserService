package com.bridgelabz.fundoonotesms.utility;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.stereotype.Service;

import com.bridgelabz.fundoonotesms.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
@Service
public class JWTUtil {
	private String secretKey="cvk1234";
	public JWTUtil() {
		
	}
	public String encodePassword(User user) {
		Map<String,Object> claims=new HashMap<>();
		return createTokenForPassword(claims,user.getPassword());
	}
	private String createTokenForPassword(Map<String,Object> claims,String subject) {
		return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis())).signWith(SignatureAlgorithm.HS256, secretKey).compact();
	}
	public String decodePassword(User user) {
		return extractClaim(user.getPassword(),Claims::getSubject);
	}
	public String generateToken(User user) {
		Map<String,Object> claims=new HashMap<>();
		return createToken(claims,user.getUserID()+"");
	}
	private String createToken(Map<String,Object> claims,String subject) {
		return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis())).setExpiration(new Date(System.currentTimeMillis()+1000*60*60*240)).signWith(SignatureAlgorithm.HS256, secretKey).compact();
	}
	public Boolean validateToken(String token,User user) {
		final Long userID=extractUserID(token);
		return (userID==user.getUserID() && !isTokenExpired(token));
	}
	public Boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}
	public Date extractExpiration(String token) {
		return extractClaim(token,Claims::getExpiration);
	}
	private <T>T extractClaim(String token,Function<Claims,T>claimsResolver){
		final Claims claims=extractAllClaims(token);
		return claimsResolver.apply(claims);
	}
	private Claims extractAllClaims(String token) {
		return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
	}
	public Long extractUserID(String token) {
		return Long.parseLong(extractClaim(token,Claims::getSubject));
	}
	
}
