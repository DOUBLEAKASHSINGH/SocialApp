/*
 * Class name
 *	ReviewController
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
 * 	09-06-2023
 *
 * Description
 * 	This is a controller for operations on reviews.
 */

package ac.in.iiitd.fcs29.controller;

import ac.in.iiitd.fcs29.dto.*;
import ac.in.iiitd.fcs29.dto.enums.RelationStatus;
import ac.in.iiitd.fcs29.dto.enums.Role;
import ac.in.iiitd.fcs29.dto.user.UserRelationsDto;
import ac.in.iiitd.fcs29.dto.user.UserResponseDto;
import ac.in.iiitd.fcs29.service.JwtService;
import ac.in.iiitd.fcs29.service.UserRelationsService;
import ac.in.iiitd.fcs29.service.UserService;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * @author sharadjain
 */
@CrossOrigin
@RestController
@RequestMapping("/relations")
public class RelationsController {

    private final UserRelationsService userRelationsService;

    private final UserService userService;

    private final JwtService jwtService;

    public RelationsController(UserRelationsService userRelationsService, UserService userService,
                               JwtService jwtService) {
        this.userRelationsService = userRelationsService;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    /**
     * Get list of all relations of a user.
     *
     * @return - list of relations.
     */
    @PostMapping("/all")
    public ResponseEntity<PageResponseDto<UserRelationsDto>> getUserRelations(
            @RequestBody PageRequestDto relationsRequest, HttpServletRequest request) {
        try {
            System.out.println("all relations page request = " + relationsRequest.getPage());
            String userId = jwtService.extractUsername(jwtService.extractJwtFromHeader(request));
            UserResponseDto user = userService.getUserById(userId);
            if (!user.getRole().equals(Role.ADMIN)) {
                System.out.println("getUserRelations Error user's role is not admin = " + user.getRole());
                HttpStatus status = HttpStatus.FORBIDDEN;
                return ResponseEntity.status(status).build();
            }
            // page count start with 0.
            return ResponseEntity
                    .ok(userRelationsService.getRelationsByUserId(userId, relationsRequest.getPage() - 1,
                            relationsRequest.getPageSize()));
        } catch (Exception e) {
            System.out.println("getUserRelations Error = " + e.getMessage());
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).build();
        }
    }

    /**
     * Get list of approved relations.
     *
     * @return - list of relations.
     */
    @PostMapping("/friends")
    public ResponseEntity<PageResponseDto<UserRelationsDto>> getUserFriends(
            @RequestBody PageRequestDto relationsRequest, HttpServletRequest request) {
        try {
            System.out.println("Friends relations page request = " + relationsRequest.getPage());
            String userId = jwtService.extractUsername(jwtService.extractJwtFromHeader(request));
            // page count start with 0.
            return ResponseEntity
                    .ok(userRelationsService.getFriendsByUserId(userId, relationsRequest.getPage() - 1,
                            relationsRequest.getPageSize()));
        } catch (Exception e) {
            System.out.println("getUserFriends Error = " + e.getMessage());
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).build();
        }
    }

    /**
     * Get list of pending relations request received.
     *
     * @return - list of relations.
     */
    @PostMapping("/received")
    public ResponseEntity<PageResponseDto<UserRelationsDto>> getReceivedRelations(
            @RequestBody PageRequestDto relationsRequest, HttpServletRequest request) {
        try {
            System.out.println("Received relations page request = " + relationsRequest.getPage());
            String userId = jwtService.extractUsername(jwtService.extractJwtFromHeader(request));
            // page count start with 0.
            return ResponseEntity
                    .ok(userRelationsService.getReceivedRelationsByUserId(userId, relationsRequest.getPage() - 1,
                            relationsRequest.getPageSize()));
        } catch (Exception e) {
            System.out.println("getReceivedRelations Error = " + e.getMessage());
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).build();
        }
    }

    /**
     * Get list of pending relations request sent.
     *
     * @return - list of relations.
     */
    @PostMapping("/sent")
    public ResponseEntity<PageResponseDto<UserRelationsDto>> getSentRelations(
            @RequestBody PageRequestDto relationsRequest, HttpServletRequest request) {
        try {
            System.out.println("Sent relations page request = " + relationsRequest.getPage());
            String userId = jwtService.extractUsername(jwtService.extractJwtFromHeader(request));
            // page count start with 0.
            return ResponseEntity
                    .ok(userRelationsService.getSentRelationsByUserId(userId, relationsRequest.getPage() - 1,
                            relationsRequest.getPageSize()));
        } catch (Exception e) {
            System.out.println("getSentRelations Error = " + e.getMessage());
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).build();
        }
    }

    /**
     * Get list of blocked relations by user.
     *
     * @return - list of relations.
     */
    @PostMapping("/blocked")
    public ResponseEntity<PageResponseDto<UserRelationsDto>> getBlockedRelations(
            @RequestBody PageRequestDto relationsRequest, HttpServletRequest request) {
        try {
            System.out.println("Sent relations page request = " + relationsRequest.getPage());
            String userId = jwtService.extractUsername(jwtService.extractJwtFromHeader(request));
            // page count start with 0.
            return ResponseEntity
                    .ok(userRelationsService.getBlockedRelationsByUserId(userId, relationsRequest.getPage() - 1,
                            relationsRequest.getPageSize()));
        } catch (Exception e) {
            System.out.println("getSentRelations Error = " + e.getMessage());
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).build();
        }
    }

    /**
     * Method to get relation by id.
     *
     * @param id - UserRelations ID
     * @return - UserRelationsDto
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserRelationsDto> getRelationsById(@PathVariable String id) {
        try {
            System.out.println("relation request by id = " + id);
            // page count start with 0.
            return ResponseEntity.ok(userRelationsService.getRelationsById(UUID.fromString(id)));
        } catch (Exception e) {
            System.out.println("getRelationsById Error = " + e.getMessage());
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).build();
        }
    }

    /**
     * Method to add relation to database.
     *
     * @param relation - UserRelationsDto
     * @return - added relation.
     */
    @PostMapping("/add")
    public ResponseEntity<UserRelationsDto> addRelation(@RequestBody UserRelationsDto relation,
                                                        HttpServletRequest request) {
        System.out.println("add relation approval = " + relation.getStatus());
        try {
            // will throw IllegalArgumentException if request is invalid
            userService.verifyUserWithJWT(relation.getUser().getEmail(), jwtService.extractJwtFromHeader(request));
            UserResponseDto user = userService.getUserById(relation.getUser().getEmail());
            relation.setUser(user);
            return ResponseEntity.ok(userRelationsService.addRelations(relation));
        } catch (EntityExistsException | IllegalArgumentException e) {
            System.out.println(e.getMessage());
            HttpStatus status = HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).build();
        } catch (Exception e) {
            System.out.println("add relation Error = " + e.getMessage());
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).build();
        }
    }

    /**
     * Method to get the status of a relation by id.
     *
     * @param id - UserRelations ID
     * @return - status of the relation.
     */
    @GetMapping("/status/{id}")
    public ResponseEntity<UserRelationsDto> getRelationStatus(@PathVariable String id,
                                                    HttpServletRequest request) {
        try {
            System.out.println("relation status request by id = " + id);
            String userId = jwtService.extractUsername(jwtService.extractJwtFromHeader(request));
            return ResponseEntity.ok(userRelationsService.getRelation(userId, id));
        } catch(NoSuchElementException e) {
            System.out.println("getRelationStatus Error = " + e.getMessage());
            HttpStatus status = HttpStatus.NOT_FOUND;
            return ResponseEntity.status(status).build();
        } catch (IllegalArgumentException e) {
            System.out.println("getRelationStatus Error = " + e.getMessage());
            HttpStatus status = HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).build();
        } catch(Exception e) {
            System.out.println("getRelationStatus Error = " + e.getMessage());
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).build();
        }
    }

    /**
     * Method to approve review in database based on id.
     *
     * @param relation - UserRelationsDto
     * @return - operation status.
     */
    @PutMapping("/change-status")
    public ResponseEntity<Boolean> changeRelation(@RequestBody UserRelationsDto relation, HttpServletRequest request) {
        System.out.println("change relation - " + relation.getUser().getEmail() + ", " + relation.getFriend().getEmail());
        try {
            // will throw IllegalArgumentException if request is invalid
            userService.verifyUserWithJWT(relation.getFriend().getEmail(), jwtService.extractJwtFromHeader(request));
//            String jwt = jwtService.extractJwtFromHeader(request);
//            try {
//                userService.verifyUserWithJWT(relation.getFriend().getEmail(), jwt);
//            } catch (IllegalStateException e) {
//                userService.verifyUserWithJWT(relation.getUser().getEmail(), jwt);
//            }

            return ResponseEntity.ok(userRelationsService.changeRelationStatus(relation.getUser().getEmail(),
                    relation.getFriend().getEmail(), relation.getStatus()));
        } catch (EntityNotFoundException | IllegalArgumentException | IllegalStateException e) {
            System.out.println(e.getMessage());
            HttpStatus status = HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).build();
        } catch (Exception e) {
            System.out.println("approve relation Error = " + e.getMessage());
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).build();
        }
    }

    /**
     * Method to delete relation based on given relation DTO.
     *
     * @param id - relation id
     * @return - operation status.
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Boolean> deleteRelation(@PathVariable String id, HttpServletRequest request) {
        try {
            UserRelationsDto relation = userRelationsService.getRelationsById(UUID.fromString(id));
            // Check with both user and friend in DB
            // will throw IllegalArgumentException if request is invalid
            String jwt = jwtService.extractJwtFromHeader(request);
            try {
                userService.verifyUserWithJWT(relation.getFriend().getEmail(), jwt);
            } catch (IllegalStateException e) {
                userService.verifyUserWithJWT(relation.getUser().getEmail(), jwt);
            }
            userRelationsService.deleteRelation(relation.getUser().getEmail(), relation.getFriend().getEmail());
            return ResponseEntity.ok().body(true);
        } catch (EntityNotFoundException | IllegalArgumentException e) {
            System.out.println(e.getMessage());
            HttpStatus status = HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(false);
        } catch (Exception e) {
            System.out.println("delete review Error = " + e.getMessage());
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(false);
        }
    }
}
