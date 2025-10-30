import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {ApiService} from './api.service';
import {Observable} from 'rxjs';
import {UserResponse} from '../interfaces/user/user-response';
import {ProfilePictureRequest} from '../interfaces/user/profile-picture-request';
import {ProfilePictureResponse} from '../interfaces/user/profile-picture-response';
import {UserDocumentRequest} from '../interfaces/user/user-document-request';
import {UserDocumentResponse} from '../interfaces/user/user-document-response';
import {UserSearch} from '../interfaces/user/user-search';
import {PageResponse} from '../interfaces/page-response';
import {PageRequest} from '../interfaces/page-request';

@Injectable({
    providedIn: 'root'
})
export class UserApiService {
    private readonly serverUrl: string; // URL for Server.

    constructor(private httpClient: HttpClient, private apiService: ApiService) {
        this.serverUrl = this.apiService.getServerUrl + '/user';
    }

    /**
     * Method to fetch user from backend.
     * ID is fetched from JWT token.
     *
     * @returns
     */
    getUserById(): Observable<UserResponse> {
        console.log('getUserById');const headers = new HttpHeaders({
            Authorization: `Bearer ${localStorage.getItem('token') || ''}`
        });
        return this.httpClient.get<UserResponse>(`${this.serverUrl}/id`, {headers});
    }

    /**
     * Method to fetch all users from backend.
     * It is ADMIN operation.
     *
     * @returns
     */
    getAllUsers(page: PageRequest): Observable<PageResponse<UserResponse>> {
        return this.httpClient.post<PageResponse<UserResponse>>(`${this.serverUrl}/all`, page);
    }

    /**
     * Method to set user image.
     *
     * @param pictureDto
     * @returns
     */
    setUserImage(pictureDto: ProfilePictureRequest): Observable<boolean> {
        const formData = new FormData();
        formData.append('email', pictureDto.email);
        formData.append('profilePicture', pictureDto.profilePicture);
        return this.httpClient.post<boolean>(`${this.serverUrl}/image`, formData);
    }

    /**
     * Method to get user image.
     *
     * @param userId
     * @returns
     */
    getUserImage(userId: String): Observable<ProfilePictureResponse> {
        return this.httpClient.get<ProfilePictureResponse>(`${this.serverUrl}/image/${userId}`);
    }

    /**
     * Method to set user documents.
     *
     * @param userDoc
     * @returns
     */
    setUserDoc(userDoc: UserDocumentRequest): Observable<boolean> {
        const formData = new FormData();
        formData.append('document', userDoc.document);
        formData.append('docType', userDoc.docType);
        formData.append('user', new Blob([JSON.stringify(userDoc.user)], { type: 'application/json' }));
        return this.httpClient.post<boolean>(`${this.serverUrl}/doc`, formData);
    }

    /**
     * Method to get user documents.
     *
     * @param userId
     * @returns
     */
    getUserDoc(userId: String): Observable<UserDocumentResponse> {
        return this.httpClient.get<UserDocumentResponse>(`${this.serverUrl}/doc/${userId}`);
    }

    /**
     * Method to search users.
     *
     * @param userSearch
     * @returns
     */
    searchUsers(userSearch: UserSearch): Observable<PageResponse<UserResponse>> {
        return this.httpClient.post<PageResponse<UserResponse>>(`${this.serverUrl}/search`, userSearch);
    }
}
