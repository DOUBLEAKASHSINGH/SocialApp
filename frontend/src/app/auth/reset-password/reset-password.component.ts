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
import { ResetPassword } from "../../interfaces/login/reset-password";

@Component({
    selector: "app-reset-password",
    standalone: true,
    imports: [ReactiveFormsModule, SharedModule, OtpComponent],
    templateUrl: "./reset-password.component.html",
    styleUrl: "./reset-password.component.scss",
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
export class ResetPasswordComponent {
    isLoading: boolean = false;
    isInvalidOtp: boolean = false;

    newCredentials!: ResetPassword;

    modalRef?: BsModalRef;

    // Variable to represent login form
    resetForm: FormGroup = new FormGroup({
        email: new FormControl("", [Validators.required, Validators.email]),
        password: new FormControl("", [
            Validators.required,
            Validators.minLength(8),
            Validators.maxLength(20),
            Validators.pattern(Constants.passwordPattern),
        ]),
        confirmPassword: new FormControl("", Validators.required),
    });

    constructor(
        private apiService: ApiService,
        private route: Router,
        private modalService: BsModalService
    ) {
        apiService.checkSession();
    }

    /**
     * Getter for email
     */
    get email() {
        return this.resetForm.get("email");
    }

    /**
     * Getter for password
     */
    get password() {
        return this.resetForm.get("password");
    }

    /**
     * Getter for confirm password
     */
    get confirmPassword() {
        return this.resetForm.get("confirmPassword");
    }

    /**
     * Method to register new user.
     */
    reset(template: TemplateRef<any>) {
        this.isLoading = true;
        const emailOtp: EmailOtp = {
            otp: "",
            email: this.resetForm.value.email,
            purpose: VerificationPurpose.PASSWORD_RESET,
        };
        this.newCredentials = {
            email: this.resetForm.value.email,
            newPassword: this.resetForm.value.password,
            otp: emailOtp
        };
        this.apiService
            .sendEmailOtp(emailOtp)
            .pipe(
                tap((data) => {
                    this.isLoading = false;
                    console.log("otp sent");
                    this.resetOtp(template);
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

    resetOtp(template: TemplateRef<any>) {
        console.log("User login OTP");
        this.modalRef = this.modalService.show(template, {
            backdrop: "static",
            class: "modal-xl",
        });
    }

    handleResetOtp = (otp: Otp) => {
        otp.purpose = VerificationPurpose.PASSWORD_RESET;
        this.newCredentials.otp = otp;
        this.isLoading = true;

        this.apiService
            .resetPassword(this.newCredentials)
            .pipe(
                tap((data) => {
                    this.isLoading = false;
                    console.log("Reset success");
                    this.goToLogin();
                    this.closeModal();
                }),
                catchError((error) => {
                    this.isLoading = false;
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
     * CLose button event handler to redirect page
     */
    goToLogin() {
        this.route.navigate(["login"]).then((r) => {
            console.log("Route changed to login: ", r);
        });
    }
}
