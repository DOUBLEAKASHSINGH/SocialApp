/**
 * Interface to represent product search fields.
 */
export interface ProductSearchRequest {
    name: String;
    code: String;
    brand: String;
    page: number;
    pageSize: number;
}
