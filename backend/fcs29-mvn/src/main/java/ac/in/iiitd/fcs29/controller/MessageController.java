package ac.in.iiitd.fcs29.controller;

import ac.in.iiitd.fcs29.dto.*;
import ac.in.iiitd.fcs29.dto.user.UserResponseDto;
import ac.in.iiitd.fcs29.service.ChatGroupService;
import ac.in.iiitd.fcs29.service.JwtService;
import ac.in.iiitd.fcs29.service.MessageService;
import ac.in.iiitd.fcs29.service.UserService;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author sharadjain
 */
@CrossOrigin
@RestController
@RequestMapping("/message")
public class MessageController {

    private final MessageService messageService;
    private final UserService userService;
    private final ChatGroupService chatGroupService;
    private final JwtService jwtService;

    public MessageController(MessageService messageService, UserService userService,
                             ChatGroupService chatGroupService, JwtService jwtService) {
        this.messageService = messageService;
        this.userService = userService;
        this.chatGroupService = chatGroupService;
        this.jwtService = jwtService;
    }

    /**
     * Method to recent chats of user.
     *
     * @return - request details
     */
    @PostMapping({"/user-list", "/user-list/{id}"})
    public ResponseEntity<PageResponseDto<UserResponseDto>> getRecentUsers(@PathVariable(required = false) String id,
                                                                           @RequestBody PageRequestDto pageRequestDto,
                                                                           HttpServletRequest request) {
        try {
            System.out.println("getRecentUsers");
            String email;
            if (id == null || id.isEmpty()) {
                // get user email from JWT token
                email = jwtService.extractUsername(jwtService.extractJwtFromHeader(request));
                System.out.println("getRecentUsers request by id = " + email);
            } else {
                System.out.println("getRecentUsers request by id = " + id);
                // Verify received user with JWT's user.
                userService.verifyUserWithJWT(id, jwtService.extractJwtFromHeader(request));
                email = id;
            }

            // page count start with 0.
            return ResponseEntity.ok(messageService.getRecentUsers(email,
                    pageRequestDto.getPage() - 1, pageRequestDto.getPageSize()));
        } catch (EntityExistsException | IllegalArgumentException e) {
            System.out.println("getRecentUsers Error = " + e.getMessage());
            HttpStatus status = HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).build();
        } catch (Exception e) {
            System.out.println("getRecentUsers Error = " + e.getMessage());
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).build();
        }
    }

    /**
     * Method to recent chats of user.
     *
     * @return - request details
     */
    @PostMapping({"/group-list", "/group-list/{id}"})
    public ResponseEntity<PageResponseDto<ChatGroupDto>> getRecentGroups(@PathVariable(required = false) String id,
                                                                           @RequestBody PageRequestDto pageRequestDto,
                                                                           HttpServletRequest request) {
        try {
            System.out.println("getRecentGroups");
            String email;
            if (id == null || id.isEmpty()) {
                // get user email from JWT token
                email = jwtService.extractUsername(jwtService.extractJwtFromHeader(request));
            } else {
                // Verify received user with JWT's user.
                userService.verifyUserWithJWT(id, jwtService.extractJwtFromHeader(request));
                email = id;
            }
            System.out.println("getRecentGroups request by id = " + email);

            // page count start with 0.
            return ResponseEntity.ok(messageService.getRecentGroups(email,
                    pageRequestDto.getPage() - 1, pageRequestDto.getPageSize()));
        } catch (EntityExistsException | IllegalArgumentException e) {
            System.out.println("getRecentGroups Error = " + e.getMessage());
            HttpStatus status = HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).build();
        } catch (Exception e) {
            System.out.println("getRecentGroups Error = " + e.getMessage());
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).build();
        }
    }

    /**
     * Get list of messages.
     *
     * @return - list of messages.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<MessageResponseDto>> getAllMessages() {
        System.out.println("get all messages");
        try {
            return ResponseEntity.ok(messageService.getAllMessages());
        } catch (Exception e) {
            System.out.println("get all messages Error = " + e.getMessage());
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).build();
        }
    }

    /**
     * Method to get messages with id given in url.
     *
     * @param id - message ID
     * @return - messages details
     */
    @GetMapping("/{id}")
    public ResponseEntity<MessageResponseDto> getMessageById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(messageService.getMessageById(id));
        } catch (EntityNotFoundException | NoSuchElementException | IllegalArgumentException e) {
            System.out.println("get by id message Error = " + e.getMessage());
            HttpStatus status = HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).build();
        } catch (Exception e) {
            System.out.println("get by id message Error = " + e.getMessage());
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).build();
        }
    }

    /**
     * Method to get messages between 2 users.
     *
     * @param messageRequest - p2p chat request
     * @return - request details
     */
    @PostMapping("/chat")
    public ResponseEntity<PageResponseDto<MessageResponseDto>> getChatMessages(
            @RequestBody MessagePageRequestDto messageRequest, HttpServletRequest request) {
        try {
            System.out.println("getChatMessages");
            // will throw IllegalArgumentException if request is invalid
            userService.verifyUserWithJWT(messageRequest.getUser(), jwtService.extractJwtFromHeader(request));

            // page count start with 0.
            return ResponseEntity.ok(messageService.getMessageByUsers(messageRequest.getUser(),
                    messageRequest.getTarget(),
                    messageRequest.getPage() - 1, messageRequest.getPageSize()));
        } catch (EntityExistsException | IllegalArgumentException e) {
            System.out.println("getChatMessages Error = " + e.getMessage());
            HttpStatus status = HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).build();
        } catch (Exception e) {
            System.out.println("getChatMessages Error = " + e.getMessage());
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).build();
        }
    }

    /**
     * Method to get group messages.
     *
     * @param messageRequest - group request
     * @return - request details
     */
    @PostMapping("/group")
    public ResponseEntity<PageResponseDto<MessageResponseDto>> getGroupMessages(
            @RequestBody MessagePageRequestDto messageRequest, HttpServletRequest request) {
        try {
            System.out.println("Group chat");
            // will throw IllegalArgumentException if request is invalid
            userService.verifyUserWithJWT(messageRequest.getUser(), jwtService.extractJwtFromHeader(request));
            ChatGroupDto groupDto = chatGroupService.getGroupById(messageRequest.getTarget());

            // page count start with 0.
            return ResponseEntity.ok(messageService.getMessageByGroup(groupDto, messageRequest.getPage() - 1,
                    messageRequest.getPageSize()));
        } catch (EntityExistsException | IllegalArgumentException e) {
            System.out.println("getGroupMessages Error = " + e.getMessage());
            HttpStatus status = HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).build();
        } catch (Exception e) {
            System.out.println("getGroupMessages Error = " + e.getMessage());
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).build();
        }
    }

    /**
     * Method to add message to database.
     *
     * @param message - MessageRequestDto
     * @return - added message.
     */
    @PostMapping(consumes = {MediaType.ALL_VALUE})
    public ResponseEntity<MessageResponseDto> addMessage(
            @RequestPart("message") MessageRequestDto message,
            @RequestPart(value = "file", required = false) MultipartFile file,
            HttpServletRequest request) {
        try {
            if (file != null) {
                message.setFile(file);
            }
            // will throw IllegalArgumentException if request is invalid
            userService.verifyUserWithJWT(message.getSender().getEmail(), jwtService.extractJwtFromHeader(request));
            return ResponseEntity.ok(messageService.addMessage(message));
        } catch (EntityNotFoundException | IllegalArgumentException e) {
            System.out.println("add message Error = " + e.getMessage());
            HttpStatus status = HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).build();
        } catch (Exception e) {
            System.out.println("add message Error = " + e.getMessage());
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).build();
        }
    }

    /**
     * Method to edit message in database based on id.
     *
     * @param message - MessageRequestDto
     * @return - edited message.
     */
    @PutMapping(value = "/edit-message", consumes = {MediaType.ALL_VALUE})
//    public ResponseEntity<MessageResponseDto> editMessage(@ModelAttribute MessageRequestDto message,
//                                                          HttpServletRequest request) {
    public ResponseEntity<MessageResponseDto> editMessage(
            @RequestPart("message") MessageRequestDto message,
            @RequestPart(value = "file", required = false) MultipartFile file,
            HttpServletRequest request) {
        System.out.println("edit message - " + message.getSender().getEmail());
        try {
            if (file != null) {
                message.setFile(file);
            }
            // will throw IllegalArgumentException if request is invalid
            userService.verifyUserWithJWT(message.getSender().getEmail(), jwtService.extractJwtFromHeader(request));
            return ResponseEntity.ok(messageService.updateMessage(message));
        } catch (EntityNotFoundException | IllegalArgumentException | IllegalStateException e) {
            System.out.println("editMessage Error = " + e.getMessage());
            HttpStatus status = HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).build();
        } catch (Exception e) {
            System.out.println("editMessage Error = " + e.getMessage());
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).build();
        }
    }

    /**
     * Method to delete message.
     *
     * @param id - Message ID
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Boolean> deleteMessage(@PathVariable String id, HttpServletRequest request) {
        try {
            MessageResponseDto message = messageService.getMessageById(id);
            // will throw IllegalArgumentException if request is invalid
            userService.verifyUserWithJWT(message.getSender().getEmail(), jwtService.extractJwtFromHeader(request));
            messageService.deleteMessage(message.getId().toString());
            return ResponseEntity.ok().body(true);
        } catch (EntityNotFoundException | IllegalArgumentException e) {
            System.out.println("delete message Error = " + e.getMessage());
            HttpStatus status = HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(false);
        } catch (Exception e) {
            System.out.println("delete message Error = " + e.getMessage());
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(false);
        }
    }
}
