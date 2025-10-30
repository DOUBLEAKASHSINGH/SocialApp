import { VerificationPurpose } from "../enum/verification-purpose";

export interface Otp {
    otp: string;
    purpose: VerificationPurpose;
}
