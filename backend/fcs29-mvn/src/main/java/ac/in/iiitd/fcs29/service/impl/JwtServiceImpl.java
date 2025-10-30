/*
 * Class name
 *	JwtServiceImpl
 *
 * Version info
 *	JavaSE-11
 *
 * Copyright notice
 *
 * Author info
 *	Name: Sharad Jain
 *	Email-ID: sharad.jain@nagarro.com
 *
 * Creation date
 * 	03-06-2023
 *
 * Last updated By
 * 	Sharad Jain
 *
 * Last updated Date
 * 	03-06-2023
 *
 * Description
 * 	This class defines working of jwt services.
 */

package ac.in.iiitd.fcs29.service.impl;

import ac.in.iiitd.fcs29.constant.Constants;
import ac.in.iiitd.fcs29.service.BlacklistedTokenService;
import ac.in.iiitd.fcs29.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @author sharadjain
 */
@Service
public class JwtServiceImpl implements JwtService {

    // HS256 secret key for user verification.
    private static final String SECRET_KEY = Constants.Security.JWT_SECRET;
//	private static final String SECRET_KEY = "703273357638792F423F4528482B4D6250655368566D597133743677397A2443";

    // Token expiration time of 1 Hour.
    private static final long EXPIRE_TIME = Constants.Security.TOKEN_EXPIRY_DURATION;
//	private static final long EXPIRE_TIME = (long) 1000 * 60 * 60;

    private static final String OTP_TOKEN = Constants.Security.OTP_TOKEN;  // "otp-token"
    private static final String BEARER_HEADER = Constants.Security.BEARER_HEADER;  // "Authorization"
    private static final String BEARER_PREFIX = Constants.Security.BEARER_PREFIX;  // "Bearer "

    private final BlacklistedTokenService blacklistedTokenService;

    public JwtServiceImpl(BlacklistedTokenService blacklistedTokenService) {
        this.blacklistedTokenService = blacklistedTokenService;
    }

    @Override
    public String extractJwtFromHeader(HttpServletRequest request) {
        String header = request.getHeader(BEARER_HEADER);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    @Override
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        // set extra claims and username in token.
        return Jwts.builder().setClaims(extraClaims).setSubject(userDetails.getUsername())
                // add issued-at date and expiration date
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE_TIME))
                // add secret key to token.
                .signWith(getSignInKey(), SignatureAlgorithm.HS256).compact();
    }

    @Override
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    @Override
    public String generateOtpJwtToken(UserDetails userDetails, String otpToken) {
        // Generate a short-lived JWT token for OTP verification
        return Jwts.builder()
                .setSubject(userDetails.getUsername()) // Use email as the subject
                .claim(OTP_TOKEN, otpToken) // Add custom claim to indicate OTP verification
                .setIssuedAt(new Date(System.currentTimeMillis()))
                // Set expiration time to 3.5 minutes
                .setExpiration(new Date(System.currentTimeMillis() + (Constants.OTP_DURATION_SEC * 1000)))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256) // Sign with secret key and algorithm
                .compact();
    }

    @Override
    public Boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())
                && !isTokenExpired(token)
                && !blacklistedTokenService.isTokenBlacklisted(token));
    }

    @Override
    public Boolean isAuthTokenValid(String token, UserDetails userDetails) {
        return (isTokenValid(token, userDetails)
                && extractOtpClaim(token) == null);
    }

    @Override
    public Boolean isOtpTokenValid(String token, UserDetails userDetails) {
        return (isTokenValid(token, userDetails) &&
                extractOtpClaim(token) != null &&
                !extractOtpClaim(token).isEmpty());
    }

    @Override
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Method to check if token is expired.
     *
     * @param token - JWT token
     * @return - is token expired
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Method to extract claims (payload) from jwt token.
     *
     * @param token - jwt token
     * @return - claims
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getSignInKey()).build().parseClaimsJws(token).getBody();
    }

    @Override
    public String extractUsername(String jwt) {
        // subject should have username/email or otpToken in case of otp verification.
        return extractClaim(jwt, Claims::getSubject);
    }

    @Override
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    @Override
    public String extractOtpClaim(String token) {
        final Claims claims = extractAllClaims(token);
        return claims.get(OTP_TOKEN, String.class);
    }

    /**
     * Method to get secret key.
     *
     * @return - secret key.
     */
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
