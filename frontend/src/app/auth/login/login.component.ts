import { Component, TemplateRef } from "@angular/core";
import {
    FormControl,
    FormGroup,
    ReactiveFormsModule,
    Validators,
} from "@angular/forms";
import { Router } from "@angular/router";
import { UserLogin } from "../../interfaces/login/login-request";
import { catchError, tap } from "rxjs";
import { ApiService } from "../../services/api.service";
import { SharedModule } from "../../shared/shared.module";
import { BsModalRef, BsModalService } from "ngx-bootstrap/modal";
import { animate, style, transition, trigger } from "@angular/animations";
import { OtpComponent } from "../otp/otp.component";
import { Otp } from "../../interfaces/login/otp";
import { VerificationPurpose } from "../../interfaces/enum/verification-purpose";

@Component({
    selector: "app-login",
    standalone: true,
    imports: [ReactiveFormsModule, SharedModule, OtpComponent],
    templateUrl: "./login.component.html",
    styleUrl: "./login.component.scss",
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
export class LoginComponent {
    // To store invalid login status.
    isInvalidLogin: boolean = false;
    isAccountLocked: boolean = false;
    isAttemptExceeded: boolean = false;
    isLoading: boolean = false;

    modalRef?: BsModalRef;

    // Variable to represent login form
    loginForm: FormGroup = new FormGroup({
        email: new FormControl("", Validators.required),
        password: new FormControl("", Validators.required),
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
        return this.loginForm.get("email");
    }

    /**
     * Getter for password
     */
    get password() {
        return this.loginForm.get("password");
    }

    clearStatus() {
        this.isInvalidLogin = false;
        this.isAccountLocked = false;
        this.isAttemptExceeded = false;
    }

    /**
     * Check login.
     */
    login(template: TemplateRef<any>) {
        this.clearStatus();
        this.isLoading = true;
        console.log("Login Form Submit", this.loginForm.value);
        let user: UserLogin = {
            email: this.loginForm.value.email,
            password: this.loginForm.value.password,
        };
        this.apiService
            .login(user)
            .pipe(
                tap((data) => {
                    this.isLoading = false;
                    console.log("Login status =", data);
                    // this.goToMessaging();
                    this.loginOtp(template);
                }),
                catchError((error) => {
                    this.isLoading = false;
                    console.log(error.status);
                    if (error.status === 401) {
                        this.isInvalidLogin = true;
                        // alert('Invalid Login Credentials');
                    } else if (error.status === 423) {
                        this.isAccountLocked = true;
                    } else if (error.status === 429) {
                        this.isAttemptExceeded = true;
                    } else if (error.mesage != undefined) {
                        alert(error.mesage);
                    } else {
                        alert("Some internal error");
                    }
                    return [];
                })
            )
            .subscribe();
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
            console.log("Route changed to dashboard: ", r);
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

    loginOtp(template: TemplateRef<any>) {
        console.log("User login OTP");
        this.modalRef = this.modalService.show(template, {
            backdrop: "static",
            class: "modal-xl",
            initialState: {
                purpose: VerificationPurpose.USER_LOGIN,
            },
        });
    }

    handleLoginOtp = (otp: Otp) => {
        otp.purpose = VerificationPurpose.USER_LOGIN;
        this.apiService.loginOtp(otp).subscribe({
            next: (response) => {
                console.log("OTP verification successful:", response);
                this.isLoading = false;
                this.closeModal();
                this.goToMessaging();
            },
            error: (error) => {
                console.error("Error verifying OTP:", error);
                alert("Invalid OTP. Please try again.");
                this.isLoading = false;
                this.closeModal();
            },
        });
    };

    closeModal() {
        if (this.modalRef) {
            this.modalRef.hide(); // Close the modal
        }
    }
}
