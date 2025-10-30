package ac.in.iiitd.fcs29.service.impl;

import ac.in.iiitd.fcs29.constant.Constants;
import ac.in.iiitd.fcs29.dto.ChatGroupDto;
import ac.in.iiitd.fcs29.dto.PageResponseDto;
import ac.in.iiitd.fcs29.dto.enums.Role;
import ac.in.iiitd.fcs29.dto.user.UserResponseDto;
import ac.in.iiitd.fcs29.entity.ChatGroup;
import ac.in.iiitd.fcs29.entity.User;
import ac.in.iiitd.fcs29.repository.ChatGroupRepository;
import ac.in.iiitd.fcs29.repository.UserRepository;
import ac.in.iiitd.fcs29.service.ChatGroupService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author sharadjain
 */
@Service
public class ChatGroupServiceImpl implements ChatGroupService {

    private final ChatGroupRepository groupRepo;

    private final UserRepository userRepo;

    private final ModelMapper modelMapper;

    public ChatGroupServiceImpl(ChatGroupRepository groupRepo, UserRepository userRepo, ModelMapper modelMapper) {
        this.groupRepo = groupRepo;
        this.userRepo = userRepo;
        this.modelMapper = modelMapper;
    }

    /**
     * @return - list of all groups
     */
    @Override
    public List<ChatGroupDto> getAllGroups() {
        return groupRepo.findAll().stream().map(group -> modelMapper.map(group, ChatGroupDto.class))
                .collect(Collectors.toList());
    }

    /**
     * See in interface.
     */
    @Override
    public ChatGroupDto getGroupById(String id) {
        Optional<ChatGroup> group = groupRepo.findById(UUID.fromString(id));
        if (group.isPresent()) {
            return modelMapper.map(group.get(), ChatGroupDto.class);
        }
        throw new EntityNotFoundException("Group does not exist with id = " + id);
    }

    /**
     * @param admin - admin of the group
     * @return - group with given admin
     */
    @Override
    public ChatGroupDto getGroupByAdmin(UserResponseDto admin) {
        Optional<User> opt_user = userRepo.findById(admin.getEmail());
        if (opt_user.isEmpty()) {
            throw new EntityNotFoundException("User does not exist with email-id = " + admin.getEmail());
        }
        User auth = opt_user.get();
        Optional<ChatGroup> group = groupRepo.findByAdmin(auth);

        if (group.isEmpty()) {
            throw new EntityNotFoundException("Group does not exist with admin email-id = " + admin.getEmail());
        }
        return modelMapper.map(group.get(), ChatGroupDto.class);
    }

    /**
     * @param name     - name of the group
     * @param page     - page number
     * @param pageSize - size of the page
     * @return - list of groups with given name
     */
    @Override
    public PageResponseDto<ChatGroupDto> getGroupsByName(String name, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);

        // Fetch groups with given name.
        Page<ChatGroup> groupsPage = groupRepo.findByNameContainsIgnoreCase(name, pageable);
        return createPageResponseDto(pageSize, groupsPage);
    }

    /**
     * @param user     - user who is a member of the group
     * @param page     - page number
     * @param pageSize - size of the page
     * @return - list of groups the user is a member of
     */
    @Override
    public PageResponseDto<ChatGroupDto> getGroupsByUser(UserResponseDto user, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        Optional<User> optionalUser = userRepo.findById(user.getEmail());
        if (optionalUser.isEmpty()) {
            throw new EntityNotFoundException("User does not exist with email-id = " + user.getEmail());
        }
        User foundUser = optionalUser.get();

        // Fetch groups the user is a member of.
        Page<ChatGroup> groupsPage = groupRepo.findByUser(foundUser, pageable);
        return createPageResponseDto(pageSize, groupsPage);
    }

    private PageResponseDto<ChatGroupDto> createPageResponseDto(int pageSize, Page<ChatGroup> groupPage) {
        List<ChatGroupDto> messagePages = groupPage.getContent().stream()
                .map(group -> modelMapper.map(group, ChatGroupDto.class))
                .collect(Collectors.toList());

        PageResponseDto<ChatGroupDto> relationsResponse = new PageResponseDto<>();
        relationsResponse.setItems(messagePages);
        relationsResponse.setPageSize(pageSize);
        relationsResponse.setTotalSize(groupPage.getTotalElements());
        return relationsResponse;
    }

    /**
     * @param group - group to be added
     * @return - added group
     */
    @Override
    @Transactional
    public ChatGroupDto addGroup(ChatGroupDto group) {
        // Validate admin
        User admin = userRepo.findById(group.getAdmin().getEmail()).orElseThrow(() ->
                new EntityNotFoundException("Admin does not exist with id = " + group.getAdmin().getEmail()));
        List<String> userIds = group.getMembers().stream().map(UserResponseDto::getEmail).toList();
        Set<User> members = new HashSet<>(userRepo.findAllById(userIds));
        members.add(admin);
        ChatGroup chatGroup = modelMapper.map(group, ChatGroup.class);
        chatGroup.setAdmin(admin);
        chatGroup.setMembers(members);
//        String key = serverKeyService.generateSharedKey(chatGroup);
//        chatGroup.setSharedKey(key);
        return modelMapper.map(groupRepo.saveAndFlush(chatGroup), ChatGroupDto.class);
    }

    @Override
    public boolean groupKeyExists(String groupId) {
        Optional<ChatGroup> group = groupRepo.findById(UUID.fromString(groupId));
        if (group.isPresent()) {
            return group.get().getSharedKey() != null;
        }
        throw new EntityNotFoundException("Group does not exist with id = " + groupId);
    }

    @Override
    public String getGroupKey(String groupId) {
        Optional<ChatGroup> group = groupRepo.findById(UUID.fromString(groupId));
        if (group.isPresent()) {
            return group.get().getSharedKey();
        }
        throw new EntityNotFoundException("Group does not exist with id = " + groupId);
    }

    @Override
    public boolean setGroupKey(String groupId, String key) {
        Optional<ChatGroup> group = groupRepo.findById(UUID.fromString(groupId));
        if (group.isPresent()) {
            group.get().setSharedKey(key);
            groupRepo.save(group.get());
            return true;
        }
        throw new EntityNotFoundException("Group does not exist with id = " + groupId);
    }

    @Override
    public boolean isUserInGroup(String groupId, String userId) {
        Optional<ChatGroup> group = groupRepo.findById(UUID.fromString(groupId));
        if (group.isEmpty()) {
            throw new EntityNotFoundException("Group does not exist with id = " + groupId);
        }
        boolean isMember = group.get().getMembers().stream()
                .anyMatch(user -> user.getEmail().equalsIgnoreCase(userId));
        if (!isMember) {
            return group.get().getAdmin().getEmail().equalsIgnoreCase(userId);
        }
        return true;
    }

    /**
     * @param group   - group to which members are to be added
     * @param members - members to be added
     * @param adder   - user who is adding new members
     * @return - added group
     */
    @Override
    @Transactional
    public ChatGroupDto addMembersToGroup(ChatGroupDto group, Set<UserResponseDto> members, UserResponseDto adder) {
        Optional<ChatGroup> optGroup = groupRepo.findById(group.getId());
        if (optGroup.isEmpty()) {
            throw new EntityNotFoundException("Group does not exist with id = " + group.getId());
        }
        ChatGroup chatGroup = optGroup.get();
        List<String> newUserIds = members.stream().map(UserResponseDto::getEmail).toList();
        if (chatGroup.getMembers().stream().noneMatch(u -> u.getEmail().equalsIgnoreCase(adder.getEmail()))) {
            throw new IllegalArgumentException("Non Member cannot add User/s to group");
        }
        Set<User> newMembers = new HashSet<>(userRepo.findAllById(newUserIds));
        Set<User> currentMembers = chatGroup.getMembers();
        currentMembers.addAll(newMembers);
        if (currentMembers.size() > Constants.MAX_GROUP_SIZE) {
            throw new IllegalArgumentException("Group size cannot exceed " + Constants.MAX_GROUP_SIZE);
        }
        chatGroup.setMembers(currentMembers);

        return modelMapper.map(groupRepo.saveAndFlush(chatGroup), ChatGroupDto.class);
    }

    /**
     * @param group   - group from which members are to be removed
     * @param members - members to be removed
     * @param remover - user who is removing members
     * @return - removed group
     */
    @Override
    @Transactional
    public ChatGroupDto removeMembersFromGroup(ChatGroupDto group, Set<UserResponseDto> members,
                                               UserResponseDto remover) {
        Optional<ChatGroup> optGroup = groupRepo.findById(group.getId());
        if (optGroup.isEmpty()) {
            throw new EntityNotFoundException("Group does not exist with id = " + group.getId());
        }
        ChatGroup chatGroup = optGroup.get();
        // remover is not admin AND remover is not group admin AND remover is not removing himself
        if (remover.getRole() != Role.ADMIN
                && !chatGroup.getAdmin().getEmail().equalsIgnoreCase(remover.getEmail())
                && members.stream().allMatch(
                member -> member.getEmail().equalsIgnoreCase(remover.getEmail()))
        ) {
            throw new IllegalArgumentException("Only Admin can remove User/s from group " +
                    "AND Non-Admin cannot remove other User/s from group");
        }
        List<String> userIds = members.stream().map(UserResponseDto::getEmail).toList();
        Set<User> targets = new HashSet<>(userRepo.findAllById(userIds));
        Set<User> currentMembers = chatGroup.getMembers();

        currentMembers.removeAll(targets);

        // If the group becomes empty, delete it.
        if (currentMembers.isEmpty()) {
            groupRepo.delete(chatGroup);
            return null;
        }

        // Update admin if the current admin is being removed.
        if (userIds.contains(chatGroup.getAdmin().getEmail())) {
            User newAdmin = currentMembers.iterator().next(); // Select a new admin from remaining members.
            chatGroup.setAdmin(newAdmin);
        }

        chatGroup.setMembers(currentMembers);
        return modelMapper.map(groupRepo.saveAndFlush(chatGroup), ChatGroupDto.class);
    }

    /**
     * @param group - group to be updated
     * @return - updated group
     */
    @Override
    public ChatGroupDto updateGroup(ChatGroupDto group) {
        if (groupRepo.existsById(group.getId()))
            return modelMapper.map(groupRepo.save(modelMapper.map(group, ChatGroup.class)), ChatGroupDto.class);
        throw new EntityNotFoundException("Group does not exist with id = " + group.getId());
    }

    /**
     * @param id - id of the group to be deleted
     */
    @Override
    public void deleteGroup(String id) {
        if (groupRepo.existsById(UUID.fromString(id)))
            groupRepo.deleteById(UUID.fromString(id));
        else throw new EntityNotFoundException("Group does not exist with id = " + id);
    }

    @Override
    public long count() {
        return groupRepo.count();
    }

}
