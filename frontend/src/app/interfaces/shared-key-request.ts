import { UserResponse } from "./user/user-response";

/**
 * Interface to represent review requests.
 */
export interface SharedKeyRequest {
    user1?: string;
    user2?: string;
    groupId?: string;
    publicKey?: string;
}
