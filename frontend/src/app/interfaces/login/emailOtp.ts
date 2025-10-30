import { VerificationPurpose } from "../enum/verification-purpose";

export interface EmailOtp {
    email: string;
    otp: string;
    purpose: VerificationPurpose;
}
