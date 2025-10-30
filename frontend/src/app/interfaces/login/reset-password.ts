import { Otp } from "./otp";

/**
 * Interface to represent user login credentials.
 */
export interface ResetPassword {
    email: string;
    prevPassword?: string;
    newPassword: string;
    otp: Otp
}
