import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { SharedModule } from "../shared/shared.module";
import {
    Component,
    ElementRef,
    OnInit,
    TemplateRef,
    ViewChild,
} from "@angular/core";
import { HeaderComponent } from "../header/header.component";
import { BsModalRef, BsModalService } from "ngx-bootstrap/modal";
import { UserResponse } from "../interfaces/user/user-response";
import { Constants } from "../../environments/constants";
import { ChatGroup } from "../interfaces/group/chat-group";
import { DomSanitizer, SafeUrl } from "@angular/platform-browser";
import { UserApiService } from "../services/user-api.service";
import { catchError, of, tap } from "rxjs";
import { PageResponse } from "../interfaces/page-response";
import { Role } from "../interfaces/enum/role";
import { PageRequest } from "../interfaces/page-request";
import { MessagePageRequest } from "../interfaces/message/message-page-request";
import { MessageResponse } from "../interfaces/message/message-response";
import { MessageApiService } from "../services/message-api.service";
import { MessageRequest } from "../interfaces/message/message-request";
import { CreateGroupComponent } from "./create-group/create-group.component";
import { GroupApiService } from "../services/group-api.service";
import { GroupPageRequest } from "../interfaces/group/group-page-request";
import { animate, style, transition, trigger } from "@angular/animations";
import { UserSearch } from "../interfaces/user/user-search";
import { ViewUserComponent } from "./view-user/view-user.component";
import { ViewGroupComponent } from "./view-group/view-group.component";
import { EncryptionService } from "../services/encryption.service";
import { SharedKeyRequest } from "../interfaces/shared-key-request";

@Component({
    selector: "app-messaging",
    imports: [
        ReactiveFormsModule,
        SharedModule,
        FormsModule,
        HeaderComponent,
        CreateGroupComponent,
        ViewUserComponent,
        ViewGroupComponent,
    ],
    providers: [BsModalService],
    templateUrl: "./messaging.component.html",
    styleUrl: "./messaging.component.scss",
    standalone: true,
    animations: [
        trigger("pageChange", [
            transition(":enter", [
                style({ opacity: 0 }),
                animate("1000ms", style({ opacity: 1 })),
            ]),
        ]),
    ],
})
export class MessagingComponent implements OnInit {
    totalUsers = 0; // Total number of items.
    totalGroups = 0; // Total number of items.
    totalCurrentMessages = 0; // Total number of items.
    itemsPerPage = 10; // Number of items per page.
    maxPageDisplay = 5; // Number of page numbers to display at a time.
    // Current active page.
    currentUserPage = 1;
    currentUserSearchPage = 1;
    currentGroupPage = 1;
    currentAllGroupPage = 1;
    currentMessagePage = 1;

    modalRef?: BsModalRef;

    userPageRequest: PageRequest;
    userSearch: UserSearch;
    groupPageRequest: PageRequest;
    allGroupPageRequest: GroupPageRequest;
    messagePageRequest: MessagePageRequest;
    userPageResponse!: PageResponse<UserResponse>;
    searchUserPageResponse!: PageResponse<UserResponse>;
    groupPageResponse!: PageResponse<ChatGroup>;
    allGroupPageResponse!: PageResponse<ChatGroup>;

    isUserLoading: boolean = false;
    isMessageLoading: boolean = false;
    isUserListLoading: boolean = false;
    isGroupListLoading: boolean = false;
    isAllGroupListLoading: boolean = false;
    errorLoadingData: boolean = false;

    user: UserResponse;
    userList: UserResponse[];
    groupList: ChatGroup[];
    allGroupList: ChatGroup[] = []; // All groups for the user.

    filteredChats: UserResponse[] = [];
    selectedUserChat: UserResponse | null = null;
    selectedGroupChat: ChatGroup | null = null;
    isGroupView: boolean = false;

    searchTerm: string = "";

    messages: MessageResponse[];
    newMessage: string = "";
    attachedFile: File | null = null;

    // Get messages on schedule
    private userListInterval: any;

    // storeChats = "stored-chats";
    // storeUsers = "stored-users";
    // storeGroups = "stored-groups";

    @ViewChild("messageContainer") messageContainer!: ElementRef;

    isMobileView: boolean = false; // Tracks whether the screen is in mobile view
    showChatWindow: boolean = false; // Tracks whether to show the chat window in mobile view

    constructor(
        private userApiService: UserApiService,
        private messageApiService: MessageApiService,
        private groupApiService: GroupApiService,
        private encryptionService: EncryptionService,
        private sanitizer: DomSanitizer,
        private modalService: BsModalService
    ) {
        this.userList = [];
        this.groupList = [];
        this.messages = [];
        this.user = {
            email: "email@domain.com",
            firstName: "First Name",
            lastName: "Last Name",
            bio: "",
            role: Role.USER,
            isVerified: false,
            isCredentialsExpired: false,
            isAccountLocked: false,
        };
        this.userPageRequest = {
            page: this.currentUserPage,
            pageSize: this.itemsPerPage,
        };
        this.groupPageRequest = {
            page: this.currentGroupPage,
            pageSize: this.itemsPerPage,
        };
        this.allGroupPageRequest = {
            name: "",
            page: this.currentAllGroupPage,
            pageSize: this.itemsPerPage,
        };
        this.messagePageRequest = {
            user: "user",
            target: "user",
            page: this.currentMessagePage,
            pageSize: this.itemsPerPage,
        };
        this.userSearch = {
            page: this.currentUserSearchPage,
            pageSize: this.itemsPerPage,
        };
    }

    ngOnInit(): void {
        this.fetchUser();
        this.checkScreenWidth();
        window.addEventListener("resize", this.checkScreenWidth.bind(this));

        // Schedule fetchUserList to run every 5 seconds
        this.userListInterval = setInterval(() => {
            if (this.isGroupView) {
                this.fetchGroupList(true);
                if (this.selectedGroupChat) {
                    this.fetchGroupMessages(this.selectedGroupChat, true);
                }
            } else {
                this.fetchUserList(true);
                if (this.selectedUserChat) {
                    this.fetchUserMessages(this.selectedUserChat, true);
                }
            }
        }, 10 * 1000); // 5 seconds in milliseconds
    }

    ngOnDestroy(): void {
        window.removeEventListener("resize", this.checkScreenWidth.bind(this));

        // Clear the interval when the component is destroyed
        if (this.userListInterval) {
            clearInterval(this.userListInterval);
        }
    }

    checkScreenWidth(): void {
        this.isMobileView = window.innerWidth <= 768; // Adjust breakpoint as needed
        if (!this.isMobileView) {
            this.showChatWindow = false; // Reset to show both views on larger screens
        }
    }

    toggleChatWindow(): void {
        this.showChatWindow = !this.showChatWindow;
    }

    get isPageLoading(): boolean {
        return this.isUserLoading; // ||
        // this.isUserListLoading ||
        // this.isGroupListLoading ||
        // this.isAllGroupListLoading ||
        // this.isMessageLoading
    }

    get selectedChat(): string | null {
        if (this.isGroupView) {
            return this.selectedGroupChat ? this.selectedGroupChat.id : null;
        }
        return this.selectedUserChat ? this.selectedUserChat.email : null;
    }

    /**
     * Method to fetch user from backend.
     */
    fetchUser(): void {
        // this.isUserAdmin = true;
        // this.fetchUserList();
        this.isUserLoading = true;
        console.log("Fetching User");
        this.userApiService
            .getUserById()
            .pipe(
                tap((data) => {
                    this.user = data;
                    // this.fetchUserProfilePicture(this.user);
                    this.fetchUserList();
                    console.log("User =", this.user);
                    this.isUserLoading = false;
                }),
                catchError(() => {
                    this.errorLoadingData = true;
                    this.isUserLoading = false;
                    console.log("User Fetch error");
                    return of(false);
                })
            )
            .subscribe();
    }

    /**
     * Method to fetch User list from backend.
     */
    fetchUserList(silently = false): void {
        if (!silently) {
            this.isUserListLoading = true;
        }
        this.userPageRequest.page = this.currentUserPage;
        console.log("Fetching recent user list");

        this.messageApiService
            .getRecentUsers(this.userPageRequest)
            .pipe(
                tap((data) => {
                    if (this.isGroupView) {
                        this.isUserListLoading = false;
                        return;
                    }
                    console.log("Users data =", data);
                    this.userPageResponse = data;
                    this.totalUsers = this.userPageResponse.totalSize;
                    if (
                        this.userList.map((user) => user.email).join(",") !==
                        data.items.map((item) => item.email).join(",")
                    ) {
                        this.userList = this.userPageResponse.items;
                        this.userList.forEach((target) => {
                            this.fetchUserProfilePicture(target);
                        });
                        this.updateFilteredChats();
                    }
                    if (this.userList.length === 0) {
                        this.searchTerm = "";
                        this.filterChats(true);
                    }
                    this.isUserListLoading = false;
                }),
                catchError(() => {
                    this.isUserListLoading = false;
                    this.errorLoadingData = true;
                    console.log("User list Fetch error");
                    return of(false);
                })
            )
            .subscribe();
    }

    /**
     * Method to fetch group list from backend.
     */
    fetchGroupList(silently = false): void {
        this.fetchAllGroupsByUser(silently);
        // if (!silently) {
        //     this.isGroupListLoading = true;
        // }
        // this.groupPageRequest.page = this.currentGroupPage;
        // console.log("Fetching recent group list");

        // this.messageApiService
        //     .getRecentGroups(this.userPageRequest)
        //     .pipe(
        //         tap((data) => {
        //             console.log("Group list data =", data);
        //             if (data.items.length === 0) {
        //                 this.fetchAllGroupsByUser(silently);
        //             }
        //             this.groupPageResponse = data;
        //             this.totalGroups = this.groupPageResponse.totalSize;
        //             if (
        //                 data.items.length > 0 &&
        //                 this.groupList.map((group) => group.id).join(",") !==
        //                     data.items.map((item) => item.id).join(",")
        //             ) {
        //                 this.groupList = this.groupPageResponse.items;
        //                 // this.groupList.forEach(target => {
        //                 //     this.fetchUserProfilePicture(target);
        //                 // });
        //             }
        //             this.isGroupListLoading = false;
        //         }),
        //         catchError(() => {
        //             this.isGroupListLoading = false;
        //             this.errorLoadingData = true;
        //             console.log("Group list Fetch error");
        //             return of(false);
        //         })
        //     )
        //     .subscribe();
    }

    /**
     * Method to fetch all groups by user from backend.
     */
    fetchAllGroupsByUser(silently = false): void {
        if (!silently) {
            this.isAllGroupListLoading = true;
        }
        this.allGroupPageRequest.page = this.currentAllGroupPage;
        this.allGroupPageRequest.name = this.user.email;
        console.log("Fetching all groups by user");

        this.groupApiService
            .getGroupByUser(this.allGroupPageRequest)
            .pipe(
                tap((data) => {
                    if (!this.isGroupView) {
                        this.isAllGroupListLoading = false;
                        return;
                    }
                    console.log("All groups by user data =", data);
                    this.totalGroups = data.totalSize;
                    if (
                        data.items.length > 0 &&
                        this.groupList.map((group) => group.id).join(",") !==
                            data.items.map((item) => item.id).join(",")
                    ) {
                        this.allGroupPageResponse = data;
                        this.groupList = this.allGroupPageResponse.items;
                        // this.groupList.forEach(target => {
                        //     this.fetchUserProfilePicture(target);
                        // });
                    }
                    setTimeout(() => {
                        this.isAllGroupListLoading = false;
                    }, 300);
                }),
                catchError((error) => {
                    this.isAllGroupListLoading = false;
                    this.errorLoadingData = true;
                    console.log("Error fetching all groups by user", error);
                    return of(false);
                })
            )
            .subscribe();
    }

    /**
     * Method to fetch group messages from backend.
     */
    fetchGroupMessages(target: ChatGroup, silently = false): void {
        if (!silently) {
            this.isMessageLoading = true;
        }
        this.messagePageRequest.user = this.user.email;
        this.messagePageRequest.target = target.id;
        this.messagePageRequest.page = this.currentMessagePage;
        console.log("Fetching group messages list");

        this.messageApiService
            .getGroupMessages(this.messagePageRequest)
            .pipe(
                tap((data) => {
                    if (
                        data.items.length > 0 &&
                        data.items[0].group?.id !== target.id
                    ) {
                        this.isMessageLoading = false;
                        return;
                    }
                    console.log("group message data =", data);
                    data.items = data.items.reverse(); // Reverse the order of messages
                    // this.messages = data.items;
                    // this.totalCurrentMessages = data.totalSize;
                    if (
                        this.messages.map((msg) => msg.id).join(",") !==
                        data.items.map((item) => item.id).join(",")
                    ) {
                        this.messages = data.items;
                        this.totalCurrentMessages = data.totalSize;
                    }
                    this.isMessageLoading = false;
                    setTimeout(() => {
                        this.scrollToBottom();
                    }, 100);
                }),
                catchError(() => {
                    this.isMessageLoading = false;
                    this.errorLoadingData = true;
                    console.log("Messages Fetch error");
                    return of(false);
                })
            )
            .subscribe();
    }

    /**
     * Method to fetch group messages from backend.
     */
    fetchUserMessages(target: UserResponse, silently = false): void {
        if (!silently) {
            this.isMessageLoading = true;
        }
        this.messagePageRequest.user = this.user.email;
        this.messagePageRequest.target = target.email;
        this.messagePageRequest.page = this.currentMessagePage;
        console.log("Fetching user messages list");

        this.messageApiService
            .getChatMessages(this.messagePageRequest)
            .pipe(
                tap((data) => {
                    if (
                        data.items.length > 0 &&
                        data.items[0].sender.email !== target.email &&
                        data.items[0].receiver?.email !== target.email
                    ) {
                        this.isMessageLoading = false;
                        return;
                    }
                    console.log("message data =", data);
                    data.items = data.items.reverse(); // Reverse the order of messages
                    // this.messages = data.items;
                    // this.totalCurrentMessages = data.totalSize;
                    if (
                        this.messages.map((msg) => msg.id).join(",") !==
                        data.items.map((item) => item.id).join(",")
                    ) {
                        this.messages = data.items;
                        this.totalCurrentMessages = data.totalSize;
                    }
                    // this.groupList.forEach(target => {
                    //     this.fetchUserProfilePicture(target);
                    // });
                    this.isMessageLoading = false;
                    setTimeout(() => {
                        this.scrollToBottom();
                    }, 100);
                }),
                catchError(() => {
                    this.isMessageLoading = false;
                    this.errorLoadingData = true;
                    console.log("Messages Fetch error");
                    return of(false);
                })
            )
            .subscribe();
    }

    scrollToBottom(): void {
        if (this.messageContainer) {
            try {
                this.messageContainer.nativeElement.scrollTop =
                    this.messageContainer.nativeElement.scrollHeight;
            } catch (err) {
                console.error("Error scrolling to bottom:", err);
            }
        }
    }

    // Filter chats based on search term and current view (messages or groups)
    filterChats(searchOnline = false): void {
        const term = this.searchTerm.toLowerCase();
        if (this.isGroupView) {
            if (this.groupList.length > 0) {
                this.groupList = this.groupList.filter((chat) => {
                    return chat.name?.toLowerCase().includes(term);
                });
            } else {
                this.allGroupList = this.allGroupList.filter((chat) => {
                    return chat.name?.toLowerCase().includes(term);
                });
            }
        } else if (searchOnline) {
            this.userSearch = {
                firstName: term,
                lastName: term,
                email: term,
                page: 1,
                pageSize: this.itemsPerPage,
            };
            this.isUserListLoading = true;
            this.userApiService
                .searchUsers(this.userSearch)
                .pipe(
                    tap((data) => {
                        console.log("User search data =", data);
                        this.searchUserPageResponse = data;
                        this.userList = this.searchUserPageResponse.items;
                        this.userList.forEach((target) => {
                            this.fetchUserProfilePicture(target);
                        });
                        // this.filteredChats = this.userList;
                        this.updateFilteredChats();
                        this.isUserListLoading = false;
                    }),
                    catchError(() => {
                        this.isUserListLoading = false;
                        this.errorLoadingData = true;
                        console.log("Filter Messages Fetch error");
                        return of(false);
                    })
                )
                .subscribe();
        } else {
            this.filteredChats = this.userList.filter((chat) => {
                return (
                    chat.firstName?.toLowerCase().includes(term) ||
                    chat.lastName?.toLowerCase().includes(term) ||
                    chat.email?.toLowerCase().includes(term)
                );
            });
        }
    }

    updateFilteredChats(): void {
        if (this.filteredChats.length === 0) {
            this.filteredChats = this.userList;
        } else {
            this.filteredChats = [...this.userList, ...this.filteredChats];
            this.filteredChats = this.filteredChats.filter(
                (chat, index, self) =>
                    index === self.findIndex((c) => c.email === chat.email)
            );
            console.log("Filtered chats:", this.filteredChats);
            if (this.searchTerm) {
                this.filteredChats = this.filteredChats.filter((chat) => {
                    return (
                        chat.firstName
                            ?.toLowerCase()
                            .includes(this.searchTerm) ||
                        chat.lastName
                            ?.toLowerCase()
                            .includes(this.searchTerm) ||
                        chat.email?.toLowerCase().includes(this.searchTerm)
                    );
                });
            }
        }
    }

    // Set the current view to either messages (users) or groups
    setView(view: "users" | "groups"): void {
        this.isGroupView = view === "groups";
        if (this.isGroupView) {
            this.fetchGroupList();
            // this.fetchAllGroupsByUser();
        } else {
            this.fetchUserList();
        }
    }

    // Select a chat from the list
    selectChat(chat: UserResponse | ChatGroup): void {
        if (this.isGroupView) {
            this.selectedGroupChat = chat as ChatGroup;
            const keyReq: SharedKeyRequest = {
                groupId: this.selectedGroupChat.id,
            };
            this.encryptionService.getGroupKey(keyReq);
            this.fetchGroupMessages(chat as ChatGroup);
            this.isUserLoading = true;
            setTimeout(() => {
                this.isUserLoading = false;
            }, 1000);
        } else {
            this.selectedUserChat = chat as UserResponse;
            const keyReq: SharedKeyRequest = {
                user1: this.user.email,
                user2: this.selectedUserChat.email,
            };
            this.encryptionService.getP2PKey(keyReq);
            this.fetchUserMessages(chat as UserResponse);
            this.isUserLoading = true;
            setTimeout(() => {
                this.isUserLoading = false;
            }, 1000);
        }

        // Show the chat window in mobile view
        if (this.isMobileView) {
            this.showChatWindow = true;
        }
    }

    // Send a text or file message
    sendMessage(): void {
        if (!this.newMessage.trim() && !this.attachedFile) {
            return; // Do not send empty messages
        }
        this.isMessageLoading = true;

        const messageRequest: MessageRequest = {
            id: "", // Backend will generate the ID
            message: this.attachedFile
                ? this.attachedFile.name
                : this.newMessage.trim(),
            file: this.attachedFile || undefined,
            contentType: this.attachedFile
                ? this.attachedFile.type || "file/unknown"
                : "text/plain",
            sender: this.user,
            receiver: this.isGroupView
                ? undefined
                : this.selectedUserChat || undefined,
            group: this.isGroupView
                ? this.selectedGroupChat || undefined
                : undefined,
            createdAt: new Date(),
            lastUpdatedAt: new Date(),
        };
        console.log("Sending message:", messageRequest);
        this.messageApiService
            .sendMessage(messageRequest)
            .pipe(
                tap((response: MessageResponse) => {
                    console.log("Message sent:", response);
                    response.message = this.newMessage.trim(); // Set the message text
                    this.messages.push(response); // Add the new message to the chat
                    this.newMessage = "";
                    this.attachedFile = null;

                    // Scroll to the bottom of the chat messages after sending
                    setTimeout(() => {
                        if (this.messageContainer) {
                            this.messageContainer.nativeElement.scrollTop =
                                this.messageContainer.nativeElement.scrollHeight;
                        }
                    }, 100);
                }),
                catchError(() => {
                    console.log("Error sending message");
                    alert("Failed to send the message. Please try again.");
                    return of(false);
                }),
                tap(() => {
                    this.isMessageLoading = false;
                })
            )
            .subscribe();
    }

    // Handle file selection for attachment
    handleFileInput(event: Event): void {
        const file = event.target as HTMLInputElement;
        if (file.files && file.files[0]) {
            // Validate file size (Max 10MB)
            const maxSize = Constants.maxFileSize;
            console.log(
                "File selected: ",
                file.size / (1024 * 1024),
                file.type
            );
            if (file.size > maxSize) {
                alert("File size exceeds 5MB. Please select a smaller file.");
                return;
            }
            this.attachedFile = file.files[0];
            console.log("File attached: ", this.attachedFile);
        }
    }

    removeAttachedFile(): void {
        this.attachedFile = null;
        console.log("File removed");
    }

    fetchUserProfilePicture(target: UserResponse) {
        this.userApiService
            .getUserImage(target.email)
            .pipe(
                tap((data) => {
                    console.log("Profile Picture =", data);
                    target.profilePictureUrl = this.convertToImage(
                        data.profilePicture,
                        data.profilePictureType
                    );
                }),
                catchError(() => {
                    // this.errorLoadingData = true;
                    console.log("Profile Picture Fetch error: ", target.email);
                    return of(false);
                })
            )
            .subscribe();
    }

    convertToImage(
        imageBytes: Uint8Array<ArrayBufferLike>,
        imageType: string
    ): SafeUrl | undefined {
        if (imageBytes.length != 0) {
            // Decode Base64
            const binary = atob(imageBytes.toString());
            const len = binary.length;
            const bytes = new Uint8Array(len);
            for (let i = 0; i < len; i++) {
                bytes[i] = binary.charCodeAt(i);
            }
            // const blob = new Blob([imageBytes], {type: imageType});
            const blob = new Blob([bytes], { type: imageType });
            const objectURL = URL.createObjectURL(blob);
            return this.sanitizer.bypassSecurityTrustUrl(objectURL);
        }
        return undefined;
    }

    convertToFile(
        byteArray: Uint8Array,
        fileType: string | undefined,
        fileName: string | undefined
    ): SafeUrl | undefined {
        if (!byteArray.length || !fileType) {
            return undefined;
        }
        // Decode Base64
        const binary = atob(byteArray.toString());
        const len = binary.length;
        const bytes = new Uint8Array(len);
        for (let i = 0; i < len; i++) {
            bytes[i] = binary.charCodeAt(i);
        }
        const blob = new Blob([bytes], { type: fileType });
        const objectURL = URL.createObjectURL(blob);
        // Create a temporary anchor element to trigger the download
        const a = document.createElement("a");
        a.href = objectURL;
        a.download = fileName || "file"; // Set the file name for the download
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        return this.sanitizer.bypassSecurityTrustResourceUrl(objectURL);
    }

    createGroup(template: TemplateRef<any>) {
        console.log("createGroup clicked");
        this.modalRef = this.modalService.show(template, {
            backdrop: "static",
            class: "modal-xl",
        });
    }

    viewUser(template: TemplateRef<any>, target: UserResponse | null): void {
        if (!target) {
            return;
        }
        console.log("Viewing user:", target);
        this.modalRef = this.modalService.show(template, {
            backdrop: "static",
            class: "modal-lg",
            initialState: {
                user: this.user, // Pass the logged-in user
                target: target, // Pass the target user
            },
        });
    }

    viewGroup(template: TemplateRef<any>, target: ChatGroup | null): void {
        if (!target) {
            return;
        }
        console.log("Viewing group:", target);
        this.modalRef = this.modalService.show(template, {
            backdrop: "static",
            class: "modal-lg",
        });
    }

    // PAGINATION HANDLERS
    onUserPageChanged(event: any): void {
        console.log("User page changed:", event);
        this.currentUserPage = event.page;
        this.fetchUserList(); // Fetch the user list for the new page
    }

    onGroupPageChanged(event: any): void {
        console.log("Group page changed:", event);
        this.currentGroupPage = event.page;
        this.fetchGroupList(); // Fetch the group list for the new page
    }

    onMessagePageChanged(event: any): void {
        console.log("Message page changed:", event);
        this.currentMessagePage = event.page;
        if (this.isGroupView && this.selectedGroupChat) {
            this.fetchGroupMessages(this.selectedGroupChat); // Fetch messages for the selected group
        } else if (!this.isGroupView && this.selectedUserChat) {
            this.fetchUserMessages(this.selectedUserChat); // Fetch messages for the selected user
        }
    }
}
