/**
 * Interface to represent user login credentials.
 */
export interface UserLogin {
    email: string;
    password: string;
    publicKey?: string;
}
