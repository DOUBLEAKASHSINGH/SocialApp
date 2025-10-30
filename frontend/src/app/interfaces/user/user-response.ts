import {Role} from '../enum/role';
import {SafeUrl} from '@angular/platform-browser';
import { Otp } from '../login/otp';

export interface UserResponse {
    email: string;
    firstName?: string;
    lastName?: string;
    bio?: string;
    role: Role;
    otp?: Otp;
    profilePictureUrl?: SafeUrl;
    isVerified?: boolean;
    isCredentialsExpired?: boolean;
    isAccountLocked?: boolean;
    createdAt?: Date;
    lastUpdatedAt?: Date;
}
