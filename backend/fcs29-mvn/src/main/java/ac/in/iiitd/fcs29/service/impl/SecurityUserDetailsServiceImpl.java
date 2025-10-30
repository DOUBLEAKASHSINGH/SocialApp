/*
 * Class name
 *	SecurityUserDetailsServiceImpl
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
 * 	01-06-2023
 *
 * Last updated By
 * 	Sharad Jain
 *
 * Last updated Date
 * 	02-06-2023
 *
 * Description
 * 	This is service class for user details security.
 */

package ac.in.iiitd.fcs29.service.impl;

import ac.in.iiitd.fcs29.entity.User;
import ac.in.iiitd.fcs29.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SecurityUserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepo;

    public SecurityUserDetailsServiceImpl(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepo.findById(username);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException("User Not Found");
        } else if (user.get().getIsAccountLocked()) {
            throw new IllegalArgumentException("Account Locked");
        }
        return user.get();
    }

}
