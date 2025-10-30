import {Role} from '../enum/role';
import { Otp } from '../login/otp';

/**
 * Interface to represent user registration details.
 */
export interface User {
    email: string;
    firstName?: string;
    lastName?: string;
    password?: string;
    bio?: string;
    role?: Role;
    otp?: Otp;
    publicKey?: string;
    isVerified?: boolean;
    isCredentialsExpired?: boolean;
    isAccountLocked?: boolean;
}
