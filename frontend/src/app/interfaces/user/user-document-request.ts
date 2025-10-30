import {UserResponse} from './user-response';

export interface UserDocumentRequest {
    docId?: string; // UUID as string
    docType: string;
    user: UserResponse;
    document: File; // MultipartFile is represented as File in TypeScript
}
