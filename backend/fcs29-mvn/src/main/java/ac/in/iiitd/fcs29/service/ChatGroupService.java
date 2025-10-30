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

import ac.in.iiitd.fcs29.dto.ChatGroupDto;
import ac.in.iiitd.fcs29.dto.PageResponseDto;
import ac.in.iiitd.fcs29.dto.user.UserResponseDto;

import java.util.List;
import java.util.Set;

/**
 * @author sharadjain
 */
public interface ChatGroupService {

    /**
     * Method to get list of all groups in database.
     *
     * @return - list of groups
     */
    List<ChatGroupDto> getAllGroups();

    /**
     * Method to get group by id.
     *
     * @param id
     * @return - group data
     */
    ChatGroupDto getGroupById(String id);

    /**
     * Method to get group by id.
     *
     * @param admin
     * @return - group data
     */
    ChatGroupDto getGroupByAdmin(UserResponseDto admin);

    /**
     * Method to get groups by name.
     *
     * @param name
     * @return - groups data
     */
    PageResponseDto<ChatGroupDto> getGroupsByName(String name, int page, int pageSize);

    /**
     * Method to get groups by user.
     *
     * @param user
     * @return - groups data
     */
    PageResponseDto<ChatGroupDto> getGroupsByUser(UserResponseDto user, int page, int pageSize);

    /**
     * Method to add new group to database.
     *
     * @param group
     * @return - added group
     */
    ChatGroupDto addGroup(ChatGroupDto group);

    boolean groupKeyExists(String groupId);

    String getGroupKey(String groupId);

    boolean setGroupKey(String groupId, String key);

    boolean isUserInGroup(String groupId, String userId);

    /**
     * Method to add new group to database.
     *
     * @param group
     * @param members
     * @param adder   - user who is adding new members
     * @return - if group is added
     */
    ChatGroupDto addMembersToGroup(ChatGroupDto group, Set<UserResponseDto> members, UserResponseDto adder);

    ChatGroupDto removeMembersFromGroup(ChatGroupDto group, Set<UserResponseDto> members,
                                        UserResponseDto remover);

    /**
     * Method to update group in database by given group having same id.
     *
     * @param group
     * @return - updated group
     */
    ChatGroupDto updateGroup(ChatGroupDto group);

    /**
     * Method to delete group with given id from database.
     *
     * @param id
     */
    void deleteGroup(String id);

    /**
     * Method to count number of groups.
     */
    long count();
}
