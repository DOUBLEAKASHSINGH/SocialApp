/*
 * Interface name
 *	ProductService
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
 * 	29-05-2023
 *
 * Last updated By
 * 	Sharad Jain
 *
 * Last updated Date
 * 	11-06-2023
 *
 * Description
 * 	This interface defines working of product related services.
 */

package ac.in.iiitd.fcs29.service;

import ac.in.iiitd.fcs29.dto.*;
import ac.in.iiitd.fcs29.dto.user.UserResponseDto;

import java.util.List;

/**
 * @author sharadjain
 */
public interface MessageService {

    PageResponseDto<UserResponseDto> getRecentUsers(String id, int page, int pageSize);

    PageResponseDto<ChatGroupDto> getRecentGroups(String id, int page, int pageSize);

    /**
     * Method to get list of all messages in database.
     *
     * @return - list of messages
     */
    List<MessageResponseDto> getAllMessages();

    /**
     * Method to get message by id.
     *
     * @param id
     * @return - message data
     */
    MessageResponseDto getMessageById(String id);

    /**
     * Method to get message by id.
     *
     * @param user1Id
     * @param user2Id
     * @return - message data
     */
    PageResponseDto<MessageResponseDto> getMessageByUsers(String user1Id, String user2Id, int page, int pageSize);

    /**
     * Method to get message by id.
     *
     * @param group
     * @return - message data
     */
    PageResponseDto<MessageResponseDto> getMessageByGroup(ChatGroupDto group, int page, int pageSize);

    /**
     * Method to add new message to database.
     *
     * @param message
     * @return - added message
     */
    MessageResponseDto addMessage(MessageRequestDto message);

    /**
     * Method to update message in database by given message having same id.
     *
     * @param message
     * @return - updated message
     */
    MessageResponseDto updateMessage(MessageRequestDto message);

    /**
     * Method to delete message with given id from database.
     *
     * @param id
     */
    void deleteMessage(String id);

    /**
     * Method to count number of messages.
     */
    long count();
}
