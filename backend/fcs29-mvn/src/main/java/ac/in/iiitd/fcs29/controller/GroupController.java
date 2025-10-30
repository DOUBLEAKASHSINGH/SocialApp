package ac.in.iiitd.fcs29.controller;

import ac.in.iiitd.fcs29.dto.ChatGroupDto;
import ac.in.iiitd.fcs29.dto.GroupPageRequestDto;
import ac.in.iiitd.fcs29.dto.PageResponseDto;
import ac.in.iiitd.fcs29.dto.user.UserResponseDto;
import ac.in.iiitd.fcs29.service.ChatGroupService;
import ac.in.iiitd.fcs29.service.JwtService;
import ac.in.iiitd.fcs29.service.UserService;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author sharadjain
 */
@CrossOrigin
@RestController
@RequestMapping("/group")
public class GroupController {

    private final ChatGroupService chatGroupService;

    private final UserService userService;

    private final JwtService jwtService;

    private static final Logger logger = LoggerFactory.getLogger("ac.in.iiitd.fcs29");

    public GroupController(ChatGroupService chatGroupService, UserService userService, JwtService jwtService) {
        this.chatGroupService = chatGroupService;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    /**
     * Get list of groups.
     *
     * @return - list of groups.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<ChatGroupDto>> getAllGroups() {
//        System.out.println("get all groups");
        logger.info("get all groups");
        try {
            return ResponseEntity.ok(chatGroupService.getAllGroups());
        } catch (Exception e) {
//            System.out.println("get all groups Error = " + e.getMessage());
            logger.error("get all groups Error = {}", e.getMessage());
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).build();
        }
    }

    /**
     * Method to get group with id given in url.
     *
     * @param id - group ID
     * @return - group details
     */
    @GetMapping("/{id}")
    public ResponseEntity<ChatGroupDto> getGroupById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(chatGroupService.getGroupById(id));
        } catch (EntityNotFoundException | NoSuchElementException | IllegalArgumentException e) {
            System.out.println(e.getMessage());
            HttpStatus status = HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).build();
        } catch (Exception e) {
//            System.out.println("get by id group Error = " + e.getMessage());
            logger.error("get by id group Error = {}", e.getMessage());
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).build();
        }
    }

    /**
     * Method to search group.
     *
     * @param groupRequest - group request
     * @return - request details
     */
    @PostMapping("/search")
    public ResponseEntity<PageResponseDto<ChatGroupDto>> getGroupsByName(@RequestBody GroupPageRequestDto groupRequest) {
        try {
//            System.out.println("Group search = " + groupRequest.getPage());
            logger.info("Group search = {}", groupRequest.getPage());

            // page count start with 0.
            return ResponseEntity.ok(chatGroupService.getGroupsByName(groupRequest.getName(),
                    groupRequest.getPage() - 1, groupRequest.getPageSize()));
        } catch (EntityExistsException | IllegalArgumentException e) {
//            System.out.println(e.getMessage());
            logger.error("getGroupsByName Error = {}", e.getMessage());
            HttpStatus status = HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).build();
        } catch (Exception e) {
//            System.out.println("search Group Error = " + e.getMessage());
            logger.error("getGroupsByName Group Error = {}", e.getMessage());
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).build();
        }
    }

    /**
     * Method to search group.
     *
     * @param groupRequest - group request
     * @return - request details
     */
    @PostMapping("/my-groups")
    public ResponseEntity<PageResponseDto<ChatGroupDto>> getGroupsByUser(@RequestBody GroupPageRequestDto groupRequest
            , HttpServletRequest request) {
        try {
//            System.out.println("Group search = " + groupRequest.getPage());
            logger.info("getGroupsByUser search = {}", groupRequest.getPage());
            // will throw IllegalArgumentException if request is invalid
            userService.verifyUserWithJWT(groupRequest.getName(), jwtService.extractJwtFromHeader(request));
            UserResponseDto user = userService.getUserById(groupRequest.getName());

            // page count start with 0.
            return ResponseEntity.ok(chatGroupService.getGroupsByUser(user,
                    groupRequest.getPage() - 1, groupRequest.getPageSize()));
        } catch (EntityExistsException | IllegalArgumentException e) {
//            System.out.println(e.getMessage());
            logger.error("getGroupsByUser Error = {}", e.getMessage());
            HttpStatus status = HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).build();
        } catch (Exception e) {
//            System.out.println("search Group Error = " + e.getMessage());
            logger.error("getGroupsByUser Group Error = {}", e.getMessage());
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).build();
        }
    }

    /**
     * Method to add group to database.
     *
     * @param group - ChatGroupDto
     * @return - added group.
     */
    @PostMapping()
    public ResponseEntity<ChatGroupDto> addGroup(@RequestBody ChatGroupDto group, HttpServletRequest request) {
        try {
            String userEmail = jwtService.extractUsername(jwtService.extractJwtFromHeader(request));
            UserResponseDto user = userService.getUserById(userEmail);
            // Make sure current user is admin and is member of group
            group.setAdmin(user);
            group.getMembers().add(user);
            group.setMembers(group.getMembers());
            return ResponseEntity.ok(chatGroupService.addGroup(group));
        } catch (EntityNotFoundException | EntityExistsException | IllegalArgumentException e) {
//            System.out.println("add group Error = " + e.getMessage());
            logger.error("add group Error = {}", e.getMessage());
            HttpStatus status = HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).build();
        } catch (Exception e) {
//            System.out.println("add group Error = " + e.getMessage());
            logger.error("add group unknown Error = {}", e.getMessage());
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).build();
        }
    }

    /**
     * Method to add member to group to database.
     *
     * @param group - ChatGroupDto
     * @return - new group.
     */
    @PostMapping("/add")
    public ResponseEntity<ChatGroupDto> addUserToGroup(@RequestBody ChatGroupDto group, HttpServletRequest request) {
        try {
            String userEmail = jwtService.extractUsername(jwtService.extractJwtFromHeader(request));
            UserResponseDto user = userService.getUserById(userEmail);
            return ResponseEntity.ok(chatGroupService.addMembersToGroup(group, group.getMembers(), user));
        } catch (EntityNotFoundException | EntityExistsException | IllegalArgumentException e) {
//            System.out.println("addUserToGroup Error = " + e.getMessage());
            logger.error("addUserToGroup Error = {}", e.getMessage());
            HttpStatus status = HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).build();
        } catch (Exception e) {
//            System.out.println("addUserToGroup Error = " + e.getMessage());
            logger.error("addUserToGroup unknown Error = {}", e.getMessage());
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).build();
        }
    }

    /**
     * Method to remove member from group.
     *
     * @param group - ChatGroupDto
     * @return - new group.
     */
    @PostMapping("/remove")
    public ResponseEntity<ChatGroupDto> removeUserFromGroup(@RequestBody ChatGroupDto group,
                                                            HttpServletRequest request) {
        try {
            String userEmail = jwtService.extractUsername(jwtService.extractJwtFromHeader(request));
            UserResponseDto user = userService.getUserById(userEmail);
            return ResponseEntity.ok(chatGroupService.removeMembersFromGroup(group, group.getMembers(), user));
        } catch (NoSuchElementException | EntityNotFoundException | EntityExistsException |
                 IllegalArgumentException e) {
//            System.out.println("removeUserFromGroup Error = " + e.getMessage());
            logger.error("removeUserFromGroup Error = {}", e.getMessage());
            HttpStatus status = HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).build();
        } catch (Exception e) {
//            System.out.println("removeUserFromGroup Error = " + e.getMessage());
            logger.error("removeUserFromGroup unknown Error = {}", e.getMessage());
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).build();
        }
    }

    /**
     * Method to delete group.
     *
     * @param id - group id
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Boolean> deleteGroup(@PathVariable String id, HttpServletRequest request) {
        try {
            ChatGroupDto group = chatGroupService.getGroupById(id);
            // will throw IllegalArgumentException if request is invalid
            userService.verifyUserWithJWT(group.getAdmin().getEmail(), jwtService.extractJwtFromHeader(request));
            chatGroupService.deleteGroup(group.getId().toString());
            return ResponseEntity.ok().body(true);
        } catch (EntityNotFoundException | IllegalArgumentException e) {
//            System.out.println(e.getMessage());
            logger.error("deleteGroup Error = {}", e.getMessage());
            HttpStatus status = HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(false);
        } catch (Exception e) {
//            System.out.println("delete review Error = " + e.getMessage());
            logger.error("deleteGroup unknown Error = {}", e.getMessage());
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(false);
        }
    }
}
