import { Component, OnInit, TemplateRef } from "@angular/core";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { SharedModule } from "../shared/shared.module";
import { catchError, Observable, of, tap } from "rxjs";
import { PageResponse } from "../interfaces/page-response";
import { BsModalRef, BsModalService } from "ngx-bootstrap/modal";
import { animate, style, transition, trigger } from "@angular/animations";
import { Role } from "../interfaces/enum/role";
import { HeaderComponent } from "../header/header.component";
import { UserResponse } from "../interfaces/user/user-response";
import { PageRequest } from "../interfaces/page-request";
import { ApiService } from "../services/api.service";
import { UserApiService } from "../services/user-api.service";
import { ViewUsersComponent } from "./view-users/view-users.component";
import { EditUserComponent } from "./edit-user/edit-user.component";
import { DomSanitizer, SafeUrl } from "@angular/platform-browser";
import { ProfilePictureRequest } from "../interfaces/user/profile-picture-request";
import { Constants } from "../../environments/constants";
import { ViewDocumentComponent } from "./view-document/view-document.component";
import { VerificationPurpose } from "../interfaces/enum/verification-purpose";
import { OtpComponent } from "../auth/otp/otp.component";
import { Otp } from "../interfaces/login/otp";
import { EmailOtp } from "../interfaces/login/emailOtp";

@Component({
    selector: "app-dashboard",
    standalone: true,
    imports: [
        ReactiveFormsModule,
        SharedModule,
        FormsModule,
        HeaderComponent,
        ViewUsersComponent,
        EditUserComponent,
        ViewDocumentComponent,
        OtpComponent,
    ],
    templateUrl: "./dashboard.component.html",
    styleUrl: "./dashboard.component.scss",
    providers: [BsModalService],
    animations: [
        trigger("pageChange", [
            transition(":enter", [
                style({ opacity: 0 }),
                animate("1000ms", style({ opacity: 1 })),
            ]),
        ]),
    ],
})
export class DashboardComponent implements OnInit {
    totalItems = 0; // Total number of items.
    currentPage = 1; // Current active page.
    itemsPerPage = 10; // Number of items per page.
    maxPageDisplay = 5; // Number of page numbers to display at a time.

    currentView: string | null = null;
    listCategory: string | null = null;

    // User Details.
    user: UserResponse;
    targetUser: UserResponse;
    isUserAdmin: boolean = false;

    // Users
    userPageResponse!: PageResponse<UserResponse>;
    userPageRequest!: PageRequest;
    userList: UserResponse[];

    // Web page status.
    errorLoadingData: boolean = false;
    // isPageLoading: boolean = false;
    isUserLoading: boolean = false;
    isUserListLoading: boolean = false;

    modalRef?: BsModalRef;
    otpPurpose: VerificationPurpose = VerificationPurpose.ACCOUNT_VERIFICATION;
    handleOtp = (otp: Otp) => {};

    constructor(
        private apiService: ApiService,
        private userApiService: UserApiService,
        private modalService: BsModalService,
        private sanitizer: DomSanitizer
    ) {
        this.userList = [];
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
        this.targetUser = this.user;
    }

    ngOnInit(): void {
        this.userPageRequest = {
            page: this.currentPage,
            pageSize: this.itemsPerPage,
            sortBy: ["firstName"],
            sortDesc: false,
        };
        this.fetchUser();
    }

    get isPageLoading(): boolean {
        return this.isUserLoading || this.isUserListLoading;
    }

    /**
     * Method to fetch user from backend.
     */
    fetchUser(): void {
        this.isUserLoading = true;
        console.log("Fetching User");
        this.userApiService
            .getUserById()
            .pipe(
                tap((data) => {
                    this.user = data;
                    this.isUserAdmin = this.user.role === Role.ADMIN;
                    this.fetchUserProfilePicture(this.user);
                    if (this.isUserAdmin) {
                        this.fetchUserList();
                    }
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

    /**
     * Method to fetch User list from backend.
     */
    fetchUserList(): void {
        this.isUserListLoading = true;
        this.userPageRequest.page = this.currentPage;
        console.log("Fetching user page");

        this.userApiService
            .getAllUsers(this.userPageRequest)
            .pipe(
                tap((data) => {
                    console.log("Users data =", data);
                    this.userPageResponse = data;
                    this.userList = this.userPageResponse.items;
                    this.totalItems = this.userPageResponse.totalSize;
                    this.userList.forEach((target) => {
                        this.fetchUserProfilePicture(target);
                    });
                    setTimeout(() => {
                        this.isUserListLoading = false;
                    }, 300);
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
     * Page change event handler.
     *
     * @param event
     */
    pageChanged(event: any) {
        console.log("page changed", event);
        this.currentPage = event.page;
        this.fetchUserList();
    }

    editUser(template: TemplateRef<any>, user: UserResponse) {
        console.log("editUser clicked");
        this.currentView = "editUser";
        this.targetUser = user;
        this.modalRef = this.modalService.show(template, {
            backdrop: "static",
            class: "modal-xl",
        });
    }

    viewFriends(template: TemplateRef<any>) {
        console.log("viewFriends clicked");
        this.currentView = "userList";
        this.listCategory = "friends";
        this.modalRef = this.modalService.show(template, {
            backdrop: "static",
            class: "modal-xl",
        });
    }

    viewBlockedUsers(template: TemplateRef<any>) {
        console.log("View Blocked users clicked");
        this.currentView = "userList";
        this.listCategory = "blocked";
        this.modalRef = this.modalService.show(template, {
            backdrop: "static",
            class: "modal-xl",
        });
    }

    viewReceivedRequest(template: TemplateRef<any>) {
        console.log("viewReceivedRequest clicked");
        this.currentView = "userList";
        this.listCategory = "received";
        this.modalRef = this.modalService.show(template, {
            backdrop: "static",
            class: "modal-xl",
        });
    }

    viewSentRequest(template: TemplateRef<any>) {
        console.log("viewSentRequest clicked");
        this.currentView = "userList";
        this.listCategory = "sent";
        this.modalRef = this.modalService.show(template, {
            backdrop: "static",
            class: "modal-xl",
        });
    }

    /**
     * Method to handle add review event.
     */
    viewUserDocument(template: TemplateRef<any>, user: UserResponse) {
        console.log("viewUserDocument clicked");
        this.currentView = "viewUserDocument";
        this.targetUser = user;
        this.modalRef = this.modalService.show(template, {
            backdrop: "static",
            class: "modal-xl",
        });
    }

    updateVerification(template: TemplateRef<any>, target: UserResponse, isVerified: boolean) {
        this.otpPurpose = VerificationPurpose.ACCOUNT_VERIFICATION;
        console.log("Mark User Verified clicked");
        const otp: Otp = {
            otp: "",
            purpose: this.otpPurpose,
        };
        this.handleOtp = (otp: Otp) => {
            otp.purpose = this.otpPurpose;
            target.otp = otp;
            this.isUserLoading = true;
            target.isVerified = isVerified;
            this.handleRequest(this.apiService.verify(target));
        }
        this.sendOtpEmail(template, otp);
    }

    updateLock(template: TemplateRef<any>, target: UserResponse, isSuspended: boolean) {
        this.otpPurpose = VerificationPurpose.ACCOUNT_ACTIVATION;
        console.log("Suspend User clicked");
        const otp: Otp = {
            otp: "",
            purpose: this.otpPurpose,
        };
        this.handleOtp = (otp: Otp) => {
            otp.purpose = this.otpPurpose;
            target.otp = otp;
            this.isUserLoading = true;
            target.isAccountLocked = isSuspended;
            this.handleRequest(this.apiService.suspend(target));
        }
        this.sendOtpEmail(template, otp);
    }

    deleteUser(template: TemplateRef<any>, target: UserResponse) {
        this.otpPurpose = VerificationPurpose.USER_DELETION;
        console.log("Delete User clicked");
        const otp: Otp = {
            otp: "",
            purpose: this.otpPurpose,
        };

        this.handleOtp = (otp: Otp) => {
            otp.purpose = this.otpPurpose;
            target.otp = otp;
            this.isUserLoading = true;
            this.apiService
                .delete(target)
                .pipe(
                    tap(() => {
                        console.log("Delete success");
                        this.isUserLoading = false;
                        this.fetchUserList();
                        this.closeModal();
                    }),
                    catchError(() => {
                        this.isUserLoading = false;
                        this.errorLoadingData = true;
                        console.log("Delete error");
                        return of(false);
                    })
                )
                .subscribe();
        }
        this.sendOtpEmail(template, otp);
    }

    sendOtpEmail(template: TemplateRef<any>, otp: Otp) {
        this.isUserLoading = true;
        this.apiService
            .sendOtp(otp)
            .pipe(
                tap((data) => {
                    this.isUserLoading = false;
                    console.log("otp sent");
                    this.showOtp(template);
                }),
                catchError((error) => {
                    this.isUserLoading = false;
                    console.log(error);
                    console.log(error.status);
                    if (error.mesage != undefined) {
                        alert(error.mesage);
                    } else {
                        alert("Some internal error");
                    }
                    return [];
                })
            )
            .subscribe();
    }

    showOtp(template: TemplateRef<any>) {
        console.log("User login OTP");
        this.modalRef = this.modalService.show(template, {
            backdrop: "static",
            class: "modal-xl",
        });
    }

    closeModal() {
        if (this.modalRef) {
            this.modalRef.hide(); // Close the modal
        }
    }

    downloadLogs() {
        this.apiService.downloadLogsZip().subscribe({
            next: (zipBlob) => {
                const blob = new Blob([zipBlob], { type: "application/zip" });
                const url = window.URL.createObjectURL(blob);

                const a = document.createElement("a");
                a.href = url;
                a.download = "logs.zip";
                a.click();

                window.URL.revokeObjectURL(url);
            },
            error: (err) => {
                console.error("Failed to download logs:", err);
            },
        });
    }

    handleRequest(req: Observable<UserResponse>) {
        req.pipe(
            tap(() => {
                console.log("Change relation success");
                this.isUserLoading = false;
                this.fetchUserList();
                this.closeModal();
            }),
            catchError(() => {
                this.isUserLoading = false;
                this.errorLoadingData = true;
                console.log("Relation change error");
                return of(false);
            })
        ).subscribe();
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

    onFileSelected = ($event: Event, target: UserResponse) => {
        const file = ($event.target as HTMLInputElement).files?.[0];
        if (!file) return;

        console.log("User for file: ", target.email);
        console.log("File selected: ", file.size / (1024 * 1024), file.type);

        // Validate file size (Max 10MB)
        const maxSize = Constants.maxFileSize;
        if (file.size > maxSize) {
            alert("File size exceeds 5MB. Please select a smaller image.");
            return;
        }

        if (!Constants.allowedImageTypes.includes(file.type)) {
            alert("Invalid file type. Allowed types: PNG, JPG.");
            return;
        }

        // Confirm before upload
        if (!confirm("Do you want to upload this image?")) {
            return;
        }
        this.uploadUserImage(file, target);
    };

    uploadUserImage(file: File, target: UserResponse) {
        console.log("Upload User Image clicked");
        this.isUserLoading = true;
        const imageRequest: ProfilePictureRequest = {
            email: target.email,
            profilePicture: file,
        };
        this.userApiService
            .setUserImage(imageRequest)
            .pipe(
                tap(() => {
                    console.log("Upload success");
                    this.isUserLoading = false;
                    this.fetchUserProfilePicture(target);
                }),
                catchError(() => {
                    this.isUserLoading = false;
                    this.errorLoadingData = true;
                    console.log("Upload error");
                    return of(false);
                })
            )
            .subscribe();
    }
}
