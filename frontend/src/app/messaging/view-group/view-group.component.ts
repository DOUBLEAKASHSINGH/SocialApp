import { animate, style, transition, trigger } from "@angular/animations";
import { Component, Input, OnChanges, SimpleChanges } from "@angular/core";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { BsModalRef, BsModalService } from "ngx-bootstrap/modal";
import { SharedModule } from "../../shared/shared.module";
import { UserApiService } from "../../services/user-api.service";
import { GroupApiService } from "../../services/group-api.service";
import { UserResponse } from "../../interfaces/user/user-response";
import { ChatGroup } from "../../interfaces/group/chat-group";
import { Role } from "../../interfaces/enum/role";
import { Constants } from "../../../environments/constants";
import { catchError, of, tap } from "rxjs";
import { DomSanitizer, SafeUrl } from "@angular/platform-browser";
import { AddGroupMemberComponent } from "../add-group-member/add-group-member.component";

@Component({
    selector: "app-view-group",
    imports: [ReactiveFormsModule, SharedModule, FormsModule, AddGroupMemberComponent],
    templateUrl: "./view-group.component.html",
    providers: [BsModalService],
    styleUrl: "./view-group.component.scss",
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
export class ViewGroupComponent implements OnChanges {
    @Input()
    user: UserResponse | null = null;
    @Input()
    target: ChatGroup | null = null;
    @Input()
    displayMembers: boolean = false;
    @Input()
    modalRef?: BsModalRef;

    sortedMembers: UserResponse[] | undefined; // Sorted members list.

    totalItems = 0; // Total number of members.

    maxGroupSize = Constants.maxGroupSize; // Maximum number of group members.
    showAddMember: boolean = false; // Flag to toggle between user list and add member view

    errorLoadingData: boolean = false;
    isLoading: boolean = false;

    constructor(
        private userApiService: UserApiService,
        private groupApiService: GroupApiService,
        private sanitizer: DomSanitizer,
        private modalService: BsModalService
    ) {}

    ngOnChanges(changes: SimpleChanges): void {
        if (
            this.displayMembers &&
            this.user &&
            this.target &&
            (changes["target"] || changes["user"])
        ) {
            this.totalItems = this.target.members?.length || 0;
            this.sortMembers();

            this.target.members?.forEach((target) => {
                this.fetchUserProfilePicture(target);
            });
        }
    }

    sortMembers() {
        this.sortedMembers = this.target?.members?.sort((a, b) => (a.firstName ?? "").localeCompare(b.firstName ?? ""));
    }

    toggleAddMember(): void {
        this.showAddMember = !this.showAddMember;
    }

    get isPageLoading(): boolean {
        // return this.isLoading;
        return false;
    }

    get isGroupAdmin(): boolean {
        return (
            this.target?.admin?.email === this.user?.email ||
            this.user?.role === Role.ADMIN
        );
    }

    get isGroupMember(): boolean {
        return (
            this.target?.members?.some(
                (member) => member.email === this.user?.email
            ) || false
        );
    }

    deleteGroup() {
        if (
            !this.isGroupAdmin ||
            !this.target ||
            !confirm(
                "Are you sure you want to delete this group? This action cannot be undone."
            )
        ) {
            return;
        }
        this.isLoading = true;
        const groupId = this.target?.id || "";
        this.groupApiService
            .deleteGroup(groupId)
            .pipe(
                tap(() => {
                    console.log("Delete group success");
                    this.isLoading = false;
                    this.closeModal();
                    window.location.reload();
                }),
                catchError((error) => {
                    this.isLoading = false;
                    this.errorLoadingData = true;
                    console.log("Delete group error");
                    return of(false);
                })
            )
            .subscribe();
    }

    closeModal() {
        if (this.modalRef) {
            this.modalRef.hide(); // Close the modal
        }
    }

    leaveGroup() {
        if (
            !confirm(
                "Are you sure you want to leave this group?"
            )
        ) {
            return;
        }
        this.removeMember(this.user as UserResponse);
    }

    removeMember(member: UserResponse) {
        this.isLoading = true;
        const group: ChatGroup = this.target as ChatGroup;
        group.members = [member];
        this.groupApiService
            .removeUsersFromGroup(group)
            .pipe(
                tap((data) => {
                    console.log("Remove member success");
                    this.target = data;
                    this.sortMembers();
                    this.isLoading = false;
                }),
                catchError((error) => {
                    this.isLoading = false;
                    this.errorLoadingData = true;
                    console.log("Remove member error");
                    return of(false);
                })
            )
            .subscribe();
    }
    
    onGroupUpdated(updatedGroup: ChatGroup): void {
        console.log("Group updated in ViewGroupComponent:", updatedGroup);
        this.target = updatedGroup; // Update the target group
        this.sortMembers();
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
            // const blob = new Blob([imageBytes], {type: imageType});
            const blob = new Blob([bytes], { type: imageType });
            const objectURL = URL.createObjectURL(blob);
            return this.sanitizer.bypassSecurityTrustUrl(objectURL);
        }
        return undefined;
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
