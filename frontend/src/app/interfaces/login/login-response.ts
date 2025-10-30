import {Role} from '../enum/role';

/**
 * Interface to represent user login credentials.
 */
export interface LoginResponse {
    email: string;
    firstName?: string;
    lastName?: string;
    role: Role;
    token: string;
    publicKey?: string;
}
