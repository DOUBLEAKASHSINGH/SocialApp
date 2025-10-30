import { Product } from './product';
import { User } from '../user/user';

/**
 * Interface to represent product Review.
 */
export interface Review {
    id: number;
    rating: number;
    heading: String;
    description: String;
    isApproved: boolean;
    user: User;
    product: Product;
    time: Date;
}
