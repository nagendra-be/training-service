package com.training.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.List;

@Component
public class JwtUtils {

	@Value("${auth.secretKey}")
	private String SECRET_KEY;

	@Value("${auth.tokenValidityMinutes}")
	private long TOKEN_VALIDITY_MINUTES;

	public String generateToken(String username) {
		return createToken(username);
	}

	public String createToken(String subject) {
		Date now = new Date();
		Date expirationDate = new Date(now.getTime() + TOKEN_VALIDITY_MINUTES * 60 * 1000);

		Key key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET_KEY));

		return Jwts.builder().setSubject(subject).setIssuedAt(now).setExpiration(expirationDate)
				.signWith(SignatureAlgorithm.HS256, key).compact();
	}

	public Boolean validateToken(String token, String username) {
		final String extractedUsername = extractUsername(token);
		return (username.equals(extractedUsername) && !isTokenExpired(token));
	}

	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	public Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parser().setSigningKey(Base64.getDecoder().decode(SECRET_KEY)).parseClaimsJws(token).getBody();
	}

	public Boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}
	
	public Authentication getAuthenticationFromToken(String token) {
	    // Extract claims from the token
	    Claims claims = extractAllClaims(token);

	    // Extract the username from the claims
	    String username = claims.getSubject();

	    // Extract any additional user information you may have stored in the token
	    // For example, roles or authorities
	    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
	    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

	    // Create an instance of UserDetails if needed
	    UserDetails userDetails = new org.springframework.security.core.userdetails.User(username, "", authorities);

	    // Return an authentication token with the UserDetails and authorities
	    return new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
	}
}
