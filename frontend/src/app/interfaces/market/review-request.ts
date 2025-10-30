import { User } from '../user/user';

/**
 * Interface to represent product Review Request.
 */
export interface ReviewRequest {
    id: number;
    productCode: String;
    productName: String;
    productBrand: String;
    user: User;
    time: Date;
}
