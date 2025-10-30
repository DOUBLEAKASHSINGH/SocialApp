/*
 * Class name
 *	ReviewServiceImpl
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
 * 	11-06-2023
 *
 * Description
 * 	This is a service class for review services.
 */

package ac.in.iiitd.fcs29.service.impl;

import ac.in.iiitd.fcs29.dto.PageResponseDto;
import ac.in.iiitd.fcs29.dto.enums.RelationStatus;
import ac.in.iiitd.fcs29.dto.user.UserRelationsDto;
import ac.in.iiitd.fcs29.dto.user.UserResponseDto;
import ac.in.iiitd.fcs29.entity.User;
import ac.in.iiitd.fcs29.entity.UserRelations;
import ac.in.iiitd.fcs29.repository.UserRelationsRepository;
import ac.in.iiitd.fcs29.service.UserRelationsService;
import ac.in.iiitd.fcs29.service.UserService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author sharadjain
 */
@Service
public class UserRelationsServiceImpl implements UserRelationsService {

    private final UserRelationsRepository userRelationsRepo;

    private final UserService userService;

    private final ModelMapper modelMapper;

    public UserRelationsServiceImpl(UserRelationsRepository userRelationsRepo, UserService userService,
                                    ModelMapper modelMapper) {
        this.userRelationsRepo = userRelationsRepo;
        this.userService = userService;
        this.modelMapper = modelMapper;

    }

    @Override
    public List<UserRelationsDto> getAllRelations() {
        return userRelationsRepo.findAll().stream().map(userRelations -> modelMapper.map(userRelations,
                        UserRelationsDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public PageResponseDto<UserRelationsDto> getRelationsByUserId(String userId, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        User user = modelMapper.map(userService.getUserById(userId), User.class);
        Page<UserRelations> relationsPage = userRelationsRepo.findRelationsOfUser(user, pageable);
        return createPageResponseDto(pageSize, relationsPage);
    }

    @Override
    public PageResponseDto<UserRelationsDto> getFriendsByUserId(String userId, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        User user = modelMapper.map(userService.getUserById(userId), User.class);
        Page<UserRelations> relationsPage = userRelationsRepo.findFriendsOfUser(user, RelationStatus.ACCEPTED,
                pageable);
        return createPageResponseDto(pageSize, relationsPage);
    }

    @Override
    public PageResponseDto<UserRelationsDto> getReceivedRelationsByUserId(String userId, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        User user = modelMapper.map(userService.getUserById(userId), User.class);
        // find where user is in "friend" (receiver) column and relations status is pending
        Page<UserRelations> relationsPage = userRelationsRepo.findByFriendAndStatusOrderByCreatedAt(user,
                RelationStatus.PENDING, pageable);
        return createPageResponseDto(pageSize, relationsPage);
    }

    @Override
    public PageResponseDto<UserRelationsDto> getSentRelationsByUserId(String userId, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        User user = modelMapper.map(userService.getUserById(userId), User.class);
        // find where relations status is pending
        Page<UserRelations> relationsPage = userRelationsRepo.findByUserAndStatusOrderByCreatedAt(user,
                RelationStatus.PENDING, pageable);
        return createPageResponseDto(pageSize, relationsPage);
    }

    @Override
    public PageResponseDto<UserRelationsDto> getBlockedRelationsByUserId(String userId, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        User user = modelMapper.map(userService.getUserById(userId), User.class);
        // find where relations status is blocked
        Page<UserRelations> relationsPage = userRelationsRepo.findByUserAndStatusOrderByCreatedAt(user,
                RelationStatus.BLOCKED, pageable);
        return createPageResponseDto(pageSize, relationsPage);
    }

    private PageResponseDto<UserRelationsDto> createPageResponseDto(int pageSize, Page<UserRelations> relationsPage) {
        List<UserRelationsDto> relations = relationsPage.getContent().stream()
                .map(relation -> modelMapper.map(relation, UserRelationsDto.class))
                .collect(Collectors.toList());

        PageResponseDto<UserRelationsDto> relationsResponse = new PageResponseDto<>();
        relationsResponse.setItems(relations);
        relationsResponse.setPageSize(pageSize);
        relationsResponse.setTotalSize(relationsPage.getTotalElements());
        return relationsResponse;
    }

    @Override
    public UserRelationsDto getRelationsById(UUID id) {
        Optional<UserRelations> relations = userRelationsRepo.findById(id);
        if (relations.isPresent())
            return modelMapper.map(relations.get(), UserRelationsDto.class);
        throw new NoSuchElementException("Relation does not exist with email-id = " + id);
    }

    @Override
    @Transactional
    public UserRelationsDto addRelations(UserRelationsDto relation) {
        System.out.println("add relation service");
        if (relation.getUser().getEmail().equalsIgnoreCase(relation.getFriend().getEmail())) {
            throw new IllegalArgumentException("Cannot add relation with same user");
        }
        if (relation.getStatus() == RelationStatus.ACCEPTED || relation.getStatus() == RelationStatus.REJECTED) {
            throw new IllegalArgumentException("Cannot add relation with status = " + relation.getStatus());
        }
        User user = modelMapper.map(relation.getUser(), User.class);
        User friend = modelMapper.map(relation.getFriend(), User.class);
        if (userRelationsRepo.countByUserAndFriend(user, friend) > 0) {
//            changeRelationStatus(relation.getUser().getEmail(), relation.getFriend().getEmail(), relation.getStatus
//            ());
            RelationStatus status = getRelationStatus(relation.getUser().getEmail(), relation.getFriend().getEmail());
            if (status == RelationStatus.BLOCKED)
                throw new IllegalArgumentException("Cannot change relation with blocked status");
//            else
//                throw new EntityExistsException("Relation already exists with id = " + relation.getId());
        }
        return modelMapper.map(userRelationsRepo.saveAndFlush(modelMapper.map(relation, UserRelations.class)),
                UserRelationsDto.class);
    }

    @Override
    @Transactional
    public Boolean changeRelationStatus(String userId, String friendId, RelationStatus status) {
        System.out.println("change relation service");
        User user1 = modelMapper.map(userService.getUserById(userId), User.class);
        User user2 = modelMapper.map(userService.getUserById(friendId), User.class);
        // user2 sent relation request to user1. user1 is approving/rejecting it
        Optional<UserRelations> relation = userRelationsRepo.findByUserAndFriend(user1, user2);
        if (relation.isEmpty()) {
            UserRelationsDto relationDto = new UserRelationsDto();
            relationDto.setStatus(status);
            UserResponseDto u1 = new UserResponseDto();
            UserResponseDto u2 = new UserResponseDto();
            u1.setEmail(userId);
            u2.setEmail(friendId);
            relationDto.setUser(u1);
            relationDto.setFriend(u2);
            addRelations(relationDto);
            return true;
        }
        System.out.println("Relation exists");
        if (relation.get().getStatus() == RelationStatus.BLOCKED) {
            throw new IllegalStateException("Cannot change status of blocked relation");
        }
        relation.get().setStatus(status);

        userRelationsRepo.save(relation.get());

        return true;
    }

    @Override
    public RelationStatus getRelationStatus(String userId, String friendId) {
        User user1 = modelMapper.map(userService.getUserById(userId), User.class);
        User user2 = modelMapper.map(userService.getUserById(friendId), User.class);
        // user2 sent relation request to user1. user1 is approving/rejecting it
        Optional<UserRelations> relation = userRelationsRepo.findByUserAndFriend(user2, user1);
        if (relation.isEmpty() || relation.get().getStatus() == null) {
            relation = userRelationsRepo.findByUserAndFriend(user1, user2);
            if (relation.isEmpty() || relation.get().getStatus() == null)
                return RelationStatus.UNKNOWN;
        }
        return relation.get().getStatus();
    }

    @Override
    public UserRelationsDto getRelation(String userId, String friendId) {
        User user1 = modelMapper.map(userService.getUserById(userId), User.class);
        User user2 = modelMapper.map(userService.getUserById(friendId), User.class);
        // user2 sent relation request to user1. user1 is approving/rejecting it
        Optional<UserRelations> relation = userRelationsRepo.findByUserAndFriend(user2, user1);
        if (relation.isEmpty()) {
            relation = userRelationsRepo.findByUserAndFriend(user1, user2);
            if (relation.isEmpty())
                throw new NoSuchElementException("Relation does not exist between users = " + userId + " and " + friendId);
        }
//        System.out.println("Relation details: " + relation.get().getUser().getEmail() + " " +
//                relation.get().getFriend().getEmail() + " " + relation.get().getStatus());
        return modelMapper.map(relation.get(), UserRelationsDto.class);
    }

    @Override
    public void deleteRelation(String userId, String friendId) {
        User user1 = modelMapper.map(userService.getUserById(userId), User.class);
        User user2 = modelMapper.map(userService.getUserById(userId), User.class);
        // user2 sent relation request to user1. user1 is approving/rejecting it
        Optional<UserRelations> relation = userRelationsRepo.findByUserAndFriend(user2, user1);
        if (relation.isEmpty()) {
            relation = userRelationsRepo.findByUserAndFriend(user1, user2);
        }
        if (relation.isPresent()) {
            userRelationsRepo.deleteById(relation.get().getId());
        }
//        else
//            throw new NoSuchElementException("Relation does not exist between users = " + userId + " and " +
//            friendId);
    }

    @Override
    public long count() {
        return userRelationsRepo.count();
    }

}
