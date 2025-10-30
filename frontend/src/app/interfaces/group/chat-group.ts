import {UserResponse} from '../user/user-response';

export interface ChatGroup {
    id: string; // UUID as string
    name?: string;
    admin?: UserResponse;
    members?: UserResponse[];
    groupPicture?: Uint8Array;
    groupPictureUrl?: string;
    bio?: string;
    isGroupLocked?: boolean;
    createdAt?: Date;
    lastUpdatedAt?: Date;
}
