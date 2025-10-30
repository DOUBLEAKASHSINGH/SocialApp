import { Component, TemplateRef } from "@angular/core";
import { SharedModule } from "../../shared/shared.module";
import {
    FormControl,
    FormGroup,
    ReactiveFormsModule,
    Validators,
} from "@angular/forms";
import { ApiService } from "../../services/api.service";
import { Router } from "@angular/router";
import { catchError, tap } from "rxjs";
import { User } from "../../interfaces/user/user";
import { Role } from "../../interfaces/enum/role";
import { Constants } from "../../../environments/constants";
import { BsModalRef, BsModalService } from "ngx-bootstrap/modal";
import { animate, style, transition, trigger } from "@angular/animations";
import { OtpComponent } from "../otp/otp.component";
import { Otp } from "../../interfaces/login/otp";
import { UserLogin } from "../../interfaces/login/login-request";
import { VerificationPurpose } from "../../interfaces/enum/verification-purpose";
import { EmailOtp } from "../../interfaces/login/emailOtp";

@Component({
    selector: "app-register",
    standalone: true,
    imports: [ReactiveFormsModule, SharedModule, OtpComponent],
    templateUrl: "./register.component.html",
    styleUrl: "./register.component.scss",
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
export class RegisterComponent {
    isAdminUser: boolean;
    isLoading: boolean = false;
    isInvalidOtp: boolean = false;

    newUser!: User;

    modalRef?: BsModalRef;

    // Variable to represent login form
    registerForm: FormGroup = new FormGroup({
        email: new FormControl("", [Validators.required, Validators.email]),
        firstName: new FormControl("", [
            Validators.required,
            Validators.pattern(Constants.alphabetPattern),
            Validators.minLength(2),
            Validators.maxLength(50),
        ]),
        lastName: new FormControl("", [
            Validators.pattern(Constants.alphabetPattern),
            Validators.maxLength(50),
        ]),
        password: new FormControl("", [
            Validators.required,
            Validators.minLength(8),
            Validators.maxLength(20),
            Validators.pattern(Constants.passwordPattern),
        ]),
        confirmPassword: new FormControl("", Validators.required),
        userRole: new FormControl(false, []),
    });

    constructor(
        private apiService: ApiService,
        private route: Router,
        private modalService: BsModalService
    ) {
        apiService.checkSession();
        this.isAdminUser = this.apiService.isAdminUser;
    }

    /**
     * Getter for email
     */
    get email() {
        return this.registerForm.get("email");
    }

    /**
     * Getter for firstName
     */
    get firstName() {
        return this.registerForm.get("firstName");
    }

    /**
     * Getter for lastName
     */
    get lastName() {
        return this.registerForm.get("lastName");
    }

    /**
     * Getter for password
     */
    get password() {
        return this.registerForm.get("password");
    }

    /**
     * Getter for confirm password
     */
    get confirmPassword() {
        return this.registerForm.get("confirmPassword");
    }

    /**
     * Method to register new user.
     */
    register(template: TemplateRef<any>) {
        this.isLoading = true;
        console.log("Register - ", this.registerForm.value);
        this.newUser = {
            email: this.registerForm.value.email,
            firstName: this.registerForm.value.firstName,
            lastName: this.registerForm.value.lastName,
            role: this.registerForm.value.userRole ? Role.ADMIN : Role.USER,
            password: this.registerForm.value.password,
        };

        if (this.isAdminUser) {
            this.handleRegisterOtp({
                otp: "",
                purpose: VerificationPurpose.USER_REGISTRATION,
            });
        } else {
            const emailOtp: EmailOtp = {
                otp: "",
                email: this.newUser.email,
                purpose: VerificationPurpose.USER_REGISTRATION,
            };
            this.apiService
                .sendEmailOtp(emailOtp)
                .pipe(
                    tap((data) => {
                        this.isLoading = false;
                        console.log("otp sent");
                        this.registerOtp(template);
                    }),
                    catchError((error) => {
                        this.isLoading = false;
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
    }

    registerOtp(template: TemplateRef<any>) {
        console.log("User login OTP");
        this.modalRef = this.modalService.show(template, {
            backdrop: "static",
            class: "modal-xl",
        });
    }

    handleRegisterOtp = (otp: Otp) => {
        otp.purpose = VerificationPurpose.USER_REGISTRATION;
        this.newUser.otp = otp;
        this.isLoading = true;

        this.apiService
            .register(this.newUser)
            .pipe(
                tap((data) => {
                    this.isLoading = false;
                    console.log("Register success");
                    if (this.newUser.role === Role.ADMIN) this.goToDashboard();
                    else this.goToMessaging();
                    this.closeModal();
                }),
                catchError((error) => {
                    this.isLoading = false;
                    if (!this.isAdminUser) {
                        this.isInvalidOtp = true;
                    }
                    console.log(error);
                    console.log(error.status);
                    if (error.mesage != undefined) {
                        alert(error.mesage);
                    }
                    this.closeModal();
                    return [];
                })
            )
            .subscribe();
    };

    closeModal() {
        if (this.modalRef) {
            this.modalRef.hide(); // Close the modal
        }
    }

    /**
     * CLose button event handler to redirect page to home
     */
    goToHome() {
        this.route.navigate([""]).then((r) => {
            console.log("Route changed to home: ", r);
        });
    }

    /**
     * CLose button event handler to redirect page to dashboard
     */
    goToDashboard() {
        this.route.navigate(["dashboard"]).then((r) => {
            console.log("Route changed to home: ", r);
        });
    }

    /**
     * CLose button event handler to redirect page to messaging
     */
    goToMessaging() {
        this.route.navigate(["messaging"]).then((r) => {
            console.log("Route changed to messaging: ", r);
        });
    }
}
