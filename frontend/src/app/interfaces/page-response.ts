/**
 * Interface to represent review requests.
 */
export interface PageResponse<T> {
    items: T[];
    pageSize: number;
    totalSize: number;
}
