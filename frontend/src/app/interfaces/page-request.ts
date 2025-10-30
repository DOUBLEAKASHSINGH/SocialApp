export interface PageRequest {
    page: number;
    pageSize: number;
    sortBy?: string[];
    sortDesc?: boolean;
}
