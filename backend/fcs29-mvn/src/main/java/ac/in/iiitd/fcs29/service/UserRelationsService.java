/*
 * Interface name
 *	ReviewService
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
 * 	31-05-2023
 *
 * Last updated By
 * 	Sharad Jain
 *
 * Last updated Date
 * 	10-06-2023
 *
 * Description
 * 	This interface defines working of product related services.
 */

package ac.in.iiitd.fcs29.service;

import ac.in.iiitd.fcs29.dto.PageResponseDto;
import ac.in.iiitd.fcs29.dto.enums.RelationStatus;
import ac.in.iiitd.fcs29.dto.user.UserRelationsDto;

import java.util.List;
import java.util.UUID;

/**
 * @author sharadjain
 */
public interface UserRelationsService {

    /**
     * Method to get list of all relations in database.
     *
     * @return - list of relations
     */
    List<UserRelationsDto> getAllRelations();

    /**
     * Method to get relations by product id.
     *
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    PageResponseDto<UserRelationsDto> getRelationsByUserId(String userId, int page, int pageSize);

    PageResponseDto<UserRelationsDto> getFriendsByUserId(String userId, int page, int pageSize);

    PageResponseDto<UserRelationsDto> getReceivedRelationsByUserId(String userId, int page, int pageSize);

    PageResponseDto<UserRelationsDto> getSentRelationsByUserId(String userId, int page, int pageSize);

    PageResponseDto<UserRelationsDto> getBlockedRelationsByUserId(String userId, int page, int pageSize);

    /**
     * Method to get relation by id.
     *
     * @param id
     * @return - relation data
     */
    UserRelationsDto getRelationsById(UUID id);

    /**
     * Method to add new relation to database.
     *
     * @param relation
     * @return - added relation
     */
    UserRelationsDto addRelations(UserRelationsDto relation);

    /**
     * Method to approve relation in database by given relation having same id.
     *
     * @param userId
     * @param friendId
     * @param status
     * @return - approve status
     */
    Boolean changeRelationStatus(String userId, String friendId, RelationStatus status);

    RelationStatus getRelationStatus(String userId, String friendId);

    UserRelationsDto getRelation(String userId, String friendId);

    /**
     * Method to delete relation with given id from database.
     *
     * @param userId
     * @param friendId
     */
    void deleteRelation(String userId, String friendId);

    /**
     * Method to count number of relations.
     */
    long count();
}
