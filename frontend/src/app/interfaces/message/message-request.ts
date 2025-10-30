import {ChatGroup} from '../group/chat-group';
import {UserResponse} from '../user/user-response';

export interface MessageRequest {
    id: string; // UUID as string
    message?: string;
    iv?: string;
    file?: File; // MultipartFile is represented as File in TypeScript
    contentType?: string;
    sender: UserResponse;
    receiver?: UserResponse;
    group?: ChatGroup;
    createdAt?: Date;
    lastUpdatedAt?: Date;
}
