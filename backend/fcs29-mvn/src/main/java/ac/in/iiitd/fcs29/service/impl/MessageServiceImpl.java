package ac.in.iiitd.fcs29.service.impl;

import ac.in.iiitd.fcs29.constant.Constants;
import ac.in.iiitd.fcs29.dto.ChatGroupDto;
import ac.in.iiitd.fcs29.dto.MessageRequestDto;
import ac.in.iiitd.fcs29.dto.MessageResponseDto;
import ac.in.iiitd.fcs29.dto.PageResponseDto;
import ac.in.iiitd.fcs29.dto.enums.RelationStatus;
import ac.in.iiitd.fcs29.dto.user.UserResponseDto;
import ac.in.iiitd.fcs29.entity.ChatGroup;
import ac.in.iiitd.fcs29.entity.Message;
import ac.in.iiitd.fcs29.entity.User;
import ac.in.iiitd.fcs29.repository.ChatGroupRepository;
import ac.in.iiitd.fcs29.repository.MessageRepository;
import ac.in.iiitd.fcs29.service.ChatGroupService;
import ac.in.iiitd.fcs29.service.MessageService;
import ac.in.iiitd.fcs29.service.UserRelationsService;
import ac.in.iiitd.fcs29.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author sharadjain
 */
@Service
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;

    private final ChatGroupRepository chatGroupRepository;

    private final UserService userService;

    private final ModelMapper modelMapper;
    private final UserRelationsService userRelationsService;
    private final ChatGroupService chatGroupService;

    public MessageServiceImpl(MessageRepository messageRepository, ChatGroupRepository chatGroupRepository,
                              UserService userService, ModelMapper modelMapper,
                              UserRelationsService userRelationsService, ChatGroupService chatGroupService) {
        this.messageRepository = messageRepository;
        this.chatGroupRepository = chatGroupRepository;
        this.userService = userService;
        this.modelMapper = modelMapper;
        this.userRelationsService = userRelationsService;
        this.chatGroupService = chatGroupService;
    }

    /**
     * @param id -
     * @return -
     */
    @Override
    public PageResponseDto<UserResponseDto> getRecentUsers(String id, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<User> users = messageRepository.findChatUsers(id, pageable);
        List<UserResponseDto> userPages = users.getContent().stream()
                .map(user -> modelMapper.map(user, UserResponseDto.class))
                .collect(Collectors.toList());

        PageResponseDto<UserResponseDto> userResponse = new PageResponseDto<>();
        userResponse.setItems(userPages);
        userResponse.setPageSize(pageSize);
        userResponse.setTotalSize(users.getTotalElements());
        return userResponse;
    }

    /**
     * @param id -
     * @return -
     */
    @Override
    public PageResponseDto<ChatGroupDto> getRecentGroups(String id, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<ChatGroup> chatGroups = messageRepository.findChatGroups(id, pageable);
        List<ChatGroupDto> groupPages = chatGroups.getContent().stream()
                .map(group -> modelMapper.map(group, ChatGroupDto.class))
                .collect(Collectors.toList());

        PageResponseDto<ChatGroupDto> groupResponse = new PageResponseDto<>();
        groupResponse.setItems(groupPages);
        groupResponse.setPageSize(pageSize);
        groupResponse.setTotalSize(chatGroups.getTotalElements());
        return groupResponse;
    }

    @Override
    public List<MessageResponseDto> getAllMessages() {
        return messageRepository.findAll().stream().map(message -> modelMapper.map(message, MessageResponseDto.class))
                .collect(Collectors.toList());
    }

    /**
     * @param id -
     * @return -
     */
    @Override
    public MessageResponseDto getMessageById(String id) {
        Optional<Message> message = messageRepository.findById(UUID.fromString(id));
        if (message.isPresent())
            return modelMapper.map(message.get(), MessageResponseDto.class);
        throw new NoSuchElementException("Message does not exist with id = " + id);
    }

    /**
     * @param user1Id  -
     * @param user2Id  -
     * @param page     -
     * @param pageSize -
     * @return -
     */
    @Override
    public PageResponseDto<MessageResponseDto> getMessageByUsers(String user1Id, String user2Id, int page,
                                                                 int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        User u1 = modelMapper.map(userService.getUserById(user1Id), User.class);
        User u2 = modelMapper.map(userService.getUserById(user2Id), User.class);

        // Fetch messages from the database where either user is sender or receiver
        Page<Message> messagesPage = messageRepository.findMessagesBetweenUsers(u1, u2, pageable);

        return createMessagePageResponseDto(pageSize, messagesPage);
    }

    /**
     * @param group    -
     * @param page     -
     * @param pageSize -
     * @return -
     */
    @Override
    public PageResponseDto<MessageResponseDto> getMessageByGroup(ChatGroupDto group, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        Optional<ChatGroup> optGroup = chatGroupRepository.findById(group.getId());
        if (optGroup.isEmpty()) {
            throw new EntityNotFoundException("Group does not exist with email-id = " + group.getId());
        }
        ChatGroup chatGroup = optGroup.get();

        // find where relations status is blocked
        Page<Message> messagePage = messageRepository.findByGroupOrderByCreatedAtDesc(chatGroup, pageable);
        return createMessagePageResponseDto(pageSize, messagePage);
    }

    private PageResponseDto<MessageResponseDto> createMessagePageResponseDto(int pageSize, Page<Message> messagePage) {
        List<MessageResponseDto> messagePages = messagePage.getContent().stream()
                .map(message -> modelMapper.map(message, MessageResponseDto.class))
                .collect(Collectors.toList());

        PageResponseDto<MessageResponseDto> messageResponse = new PageResponseDto<>();
        messageResponse.setItems(messagePages);
        messageResponse.setPageSize(pageSize);
        messageResponse.setTotalSize(messagePage.getTotalElements());
        return messageResponse;
    }

    @Override
    public MessageResponseDto addMessage(MessageRequestDto message) {
        if (message.getReceiver() != null &&
                userRelationsService.getRelationStatus(message.getSender().getEmail(), message.getReceiver().getEmail())
                        == RelationStatus.BLOCKED)
            throw new IllegalArgumentException("Message cannot be sent to blocked users");
//        Message msg = modelMapper.map(message, Message.class);
        Message msg = MessageRequestToMessage(message);
        return modelMapper.map(messageRepository.saveAndFlush(msg), MessageResponseDto.class);
    }

    @Override
    public MessageResponseDto updateMessage(MessageRequestDto message) {
        if (messageRepository.existsById(message.getId())) {
            Message msg = MessageRequestToMessage(message);
            return modelMapper.map(messageRepository.saveAndFlush(msg), MessageResponseDto.class);
        }
        throw new NoSuchElementException("Message does not exist with id = " + message.getId());
    }

    /**
     * @param id -
     */
    @Override
    public void deleteMessage(String id) {
        if (messageRepository.existsById(UUID.fromString(id)))
            messageRepository.deleteById(UUID.fromString(id));
        else
            throw new NoSuchElementException("Message does not exist with id = " + id);
    }

    @Override
    public long count() {
        return messageRepository.count();
    }

    /**
     * Converts a MessageRequestDto object into a Message object by mapping its fields
     * and validating its content type and file data, if present.
     *
     * @param message the MessageRequestDto object containing the request data to be converted into a Message.
     * @return a Message object containing the mapped and validated data from the provided MessageRequestDto.
     * @throws IllegalArgumentException if the file content type is invalid or an error occurs while reading the file.
     */
    Message MessageRequestToMessage(MessageRequestDto message) {
        Message msg = new Message();
        msg.setSender(modelMapper.map(message.getSender(), User.class));
        if (message.getReceiver() == null) {
            System.out.println("Group message received " + message.getSender() + " " + message.getGroup().getName());
            ChatGroupDto group = chatGroupService.getGroupById(String.valueOf(message.getGroup().getId()));
            if (group.getMembers().stream().noneMatch(
                    u -> u.getEmail().equals(message.getSender().getEmail()))
            ) {
                throw new IllegalArgumentException("Sender is not a member of the group");
            }
            msg.setGroup(modelMapper.map(message.getGroup(), ChatGroup.class));
        } else {
            System.out.println("P2P message received " + message.getSender() + " " + message.getReceiver());
            msg.setReceiver(modelMapper.map(message.getReceiver(), User.class));
        }
        if (message.getContentType() == null || message.getContentType().equalsIgnoreCase(Constants.TEXT_PREFIX)) {
            System.out.println("Message content type is Text: " + message.getContentType());
            msg.setMessage(message.getMessage());
            msg.setIv(message.getIv());
            msg.setContentType(Constants.MESSAGE_CONTENT_TYPE);
            msg.setFile(new byte[0]);
        } else {
            System.out.println("Message content type is File: " + message.getContentType());
            // Validate the file if content-type is not text
            MultipartFile file = message.getFile();
            if (file.isEmpty()) {
                throw new IllegalArgumentException("File is empty " + file.getContentType());
            }
//            msg.setMessage("");
            msg.setMessage(message.getMessage());
            msg.setIv(message.getIv());
            msg.setContentType(file.getContentType());
            try {
                // Set image data in the user entity
                msg.setFile(message.getFile().getBytes()); // Store the image as binary
            } catch (IOException e) {
                throw new IllegalArgumentException("Error reading file data");
            }
        }
        return msg;
    }

}
