import { Component, Input } from "@angular/core";
import { SharedModule } from "../../shared/shared.module";
import {
    FormControl,
    FormGroup,
    ReactiveFormsModule,
    Validators,
} from "@angular/forms";
import { Otp } from "../../interfaces/login/otp";
import { VerificationPurpose } from "../../interfaces/enum/verification-purpose";
import { BsModalRef } from "ngx-bootstrap/modal";
import { Constants } from "../../../environments/constants";

@Component({
    selector: "app-otp",
    standalone: true,
    imports: [ReactiveFormsModule, SharedModule],
    templateUrl: "./otp.component.html",
    styleUrl: "./otp.component.scss",
})
export class OtpComponent {
    @Input()
    modalRef?: BsModalRef;
    // @Input()
    // onSubmitOtp!: (otp: Otp, data?: any) => void; // Function to handle OTP submission with optional second parameter
    @Input()
    onSubmitOtp!: (otp: Otp) => void;

    isLoading: boolean = false;

    horizontalKeys = [
        { label: "1", value: "1" },
        { label: "2", value: "2" },
        { label: "3", value: "3" },
        { label: "4", value: "4" },
        { label: "5", value: "5" },
        { label: "6", value: "6" },
        { label: "7", value: "7" },
        { label: "8", value: "8" },
        { label: "9", value: "9" },
        { label: "0", value: "0" },
        { label: "⌫", value: "back" },
    ];

    otpForm: FormGroup = new FormGroup({
        otp: new FormControl("", [
            Validators.required,
            Validators.pattern(Constants.otpPattern), // Only numbers, 6 digits
        ]),
    });

    get otp() {
        return this.otpForm.get("otp");
    }

    /**
     * Called when a number key is pressed on the virtual keyboard
     */
    pressKey(digit: number) {
        let current = this.otp?.value || "";
        this.otp?.setValue(current + digit);
    }

    /**
     * Called when the backspace key is pressed
     */
    backspace() {
        let current = this.otp?.value || "";
        this.otp?.setValue(current.slice(0, -1));
    }

    handleKeyPress(key: { label: string; value: string }) {
        const current = this.otp?.value || "";

        if (key.value === "back") {
            this.otp?.setValue(current.slice(0, -1));
        } else if (current.length < 6) {
            this.otp?.setValue(current + key.value);
        }
    }

    submitOtp() {
        if (this.otpForm.invalid) {
            console.log("Invalid OTP form");
            return;
        }
        this.isLoading = true;
        console.log("OTP entered:", this.otp);
        const otp: Otp = {
            otp: this.otpForm.value.otp,
            purpose: VerificationPurpose.USER_LOGIN,
        };
        // if (this.data) {
        //     this.onSubmitOtp(otp, this.data);
        // } else {
            this.onSubmitOtp(otp);
        // }
    }

    closeModal() {
        if (this.modalRef) {
            this.modalRef.hide(); // Close the modal
        }
    }
}
