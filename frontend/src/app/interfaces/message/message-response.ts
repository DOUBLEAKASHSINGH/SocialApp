import {ChatGroup} from '../group/chat-group';
import {UserResponse} from '../user/user-response';

export interface MessageResponse {
    id: string; // UUID as string
    message?: string;
    iv?: string;
    file?: Uint8Array; // byte[] is represented as Uint8Array in TypeScript
    contentType?: string;
    sender: UserResponse;
    receiver?: UserResponse;
    group?: ChatGroup;
    createdAt?: Date;
    lastUpdatedAt?: Date;
}
