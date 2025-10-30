import {UserResponse} from './user-response';

export interface UserDocumentResponse {
    docId: string; // UUID as string
    docType: string;
    user: UserResponse;
    document: Uint8Array; // byte[] is represented as Uint8Array in TypeScript
    createdAt?: Date;
    lastUpdatedAt?: Date;
}
