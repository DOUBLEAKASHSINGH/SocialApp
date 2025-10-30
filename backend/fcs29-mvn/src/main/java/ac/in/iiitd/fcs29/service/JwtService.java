/*
 * Interface name
 *	JwtService
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
 * 	This interface defines working of jwt services.
 */

package ac.in.iiitd.fcs29.service;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;
import java.util.Map;
import java.util.function.Function;

/**
 * @author sharadjain
 */
public interface JwtService {

    /**
     * Method to extract username from jwt token.
     *
     * @param jwt
     * @return
     */
    String extractUsername(String jwt);

    /**
     * Method to extract claim from token using provided function.
     *
     * @param <T>            - return type of given function.
     * @param token          - token
     * @param claimsResolver - given function.
     * @return - extracted claim - username/email.
     */
    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);

    String extractJwtFromHeader(HttpServletRequest request);

    /**
     * Method to generate token from extra claims and UserDetails
     *
     * @param extraClaims - Any extra claims for token.
     * @param userDetails
     * @return
     */
    String generateToken(Map<String, Object> extraClaims, UserDetails userDetails);

    /**
     * Method to generate token from UserDetails
     *
     * @param userDetails
     * @return
     */
    String generateToken(UserDetails userDetails);

    /**
     * Method to generate token from UserDetails
     *
     * @param userDetails
     * @param otpToken
     * @return
     */
    String generateOtpJwtToken(UserDetails userDetails, String otpToken);

    Boolean isTokenValid(String token, UserDetails userDetails);

    /**
     * Method to check if a token is still valid.
     *
     * @param token
     * @param userDetails
     * @return
     */
    Boolean isAuthTokenValid(String token, UserDetails userDetails);

    Boolean isOtpTokenValid(String token, UserDetails userDetails);

    /**
     * Method to extract expiration date from token.
     *
     * @param token
     * @return
     */
    Date extractExpiration(String token);

    String extractOtpClaim(String token);
}
