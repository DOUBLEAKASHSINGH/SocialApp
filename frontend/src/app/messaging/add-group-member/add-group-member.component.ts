import { Component, EventEmitter, Input, Output } from "@angular/core";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { SharedModule } from "../../shared/shared.module";
import { animate, style, transition, trigger } from "@angular/animations";
import { BsModalService } from "ngx-bootstrap/modal";
import { ChatGroup } from "../../interfaces/group/chat-group";
import { UserResponse } from "../../interfaces/user/user-response";
import { UserApiService } from "../../services/user-api.service";
import { GroupApiService } from "../../services/group-api.service";
import { DomSanitizer, SafeUrl } from "@angular/platform-browser";
import { catchError, of, tap } from "rxjs";
import { UserSearch } from "../../interfaces/user/user-search";
import { Constants } from "../../../environments/constants";

@Component({
    selector: "app-add-group-member",
    imports: [ReactiveFormsModule, SharedModule, FormsModule],
    templateUrl: "./add-group-member.component.html",
    styleUrl: "./add-group-member.component.scss",
    providers: [BsModalService],
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
export class AddGroupMemberComponent {
    @Input()
    user: UserResponse | null = null;
    @Input()
    group: ChatGroup | null = null;
    @Output()
    groupUpdated = new EventEmitter<ChatGroup>();

    userSearch!: UserSearch;

    totalItems = 0; // Total number of search results.
    currentPage = 1; // Current active page.
    itemsPerPage = 10; // Number of items per page.
    maxPageDisplay = 5; // Number of page numbers to display at a time.

    errorLoadingData: boolean = false;

    searchTerm: string = "";
    searchResults: UserResponse[] = [];
    isSearching: boolean = false;
    isLoading: boolean = false;

    constructor(
        private userApiService: UserApiService,
        private groupApiService: GroupApiService,
        private sanitizer: DomSanitizer
    ) {}

    isGroupMember(user: UserResponse): boolean {
        if (!this.group || !this.group.members) {
            return false;
        }
        return this.group.members.some((member) => member.email === user.email);
    }

    isMaxLengthReached(): boolean {
        if (!this.group || !this.group.members) {
            return false;
        }
        return this.group.members.length >= Constants.maxGroupSize;
    }

    addUserToGroup(user: UserResponse): void {
        if (!this.group) {
            return;
        }
        this.isLoading = true;
        console.log(`Adding user ${user.email} to group ${this.group.name}`);
        let groupRequest: ChatGroup = {
            ...this.group,
        };
        groupRequest.members = [user];
        this.groupApiService
            .addUsersToGroup(groupRequest)
            .pipe(
                tap((data) => {
                    console.log("add member data =", data);
                    this.totalItems = data.members?.length || 0;
                    this.group = data;
                    this.groupUpdated.emit(this.group);
                    this.isLoading = false;
                }),
                catchError(() => {
                    this.isLoading = false;
                    this.errorLoadingData = true;
                    console.log("Add member error");
                    return of(false);
                })
            )
            .subscribe();
    }

    searchUsers(): void {
        this.userSearch = {
            firstName: this.searchTerm,
            lastName: this.searchTerm,
            email: this.searchTerm,
            page: this.currentPage, // Use the current page for pagination
            pageSize: this.itemsPerPage,
        };
        this.isSearching = true;

        this.userApiService
            .searchUsers(this.userSearch)
            .pipe(
                tap((data) => {
                    console.log("User search data =", data);
                    this.searchResults = data.items;
                    this.totalItems = data.totalSize; // Update total items for pagination

                    this.isSearching = false;
                    this.searchResults.forEach((target) => {
                        this.fetchUserProfilePicture(target);
                    });
                }),
                catchError(() => {
                    this.isSearching = false;
                    this.errorLoadingData = true;
                    console.log("User search error");
                    return of(false);
                })
            )
            .subscribe();
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
                    this.errorLoadingData = true;
                    console.log("Profile Picture Fetch error");
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
            const blob = new Blob([bytes], { type: imageType });
            const objectURL = URL.createObjectURL(blob);
            return this.sanitizer.bypassSecurityTrustUrl(objectURL);
        }
        return undefined;
    }

    /**
     * Handle page change event for pagination.
     *
     * @param event
     */
    pageChanged(event: any): void {
        console.log("Page changed:", event);
        this.currentPage = event.page;
        this.searchUsers(); // Fetch users for the new page
    }

    /**
     * Method to find min of two numbers.
     *
     * @param a
     * @param b
     * @returns
     */
    min(a: number, b: number) {
        return a < b ? a : b;
    }
}
