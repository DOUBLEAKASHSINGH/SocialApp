import {UserResponse} from './user-response';
import {RelationStatus} from '../enum/relation-status';

export interface UserRelations {
    id?: string; // UUID as string
    user: UserResponse;
    friend: UserResponse;
    status: RelationStatus;
    createdAt?: Date;
    lastUpdatedAt?: Date;
}
