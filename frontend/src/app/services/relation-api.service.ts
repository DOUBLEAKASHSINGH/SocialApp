import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {ApiService} from './api.service';
import {Observable} from 'rxjs';
import {PageResponse} from '../interfaces/page-response';
import {PageRequest} from '../interfaces/page-request';
import {UserRelations} from '../interfaces/user/user-relations';
import { RelationStatus } from '../interfaces/enum/relation-status';

@Injectable({
  providedIn: 'root'
})
export class RelationApiService {
    private readonly serverUrl: string; // URL for Server.

    constructor(private httpClient: HttpClient, private apiService: ApiService) {
        this.serverUrl = this.apiService.getServerUrl + '/relations';
    }

    /**
     * Method to fetch user's relation.
     *
     * @param userRelationsId
     * @returns
     */
    getRelationsById(userRelationsId: String): Observable<UserRelations> {
        return this.httpClient.get<UserRelations>(`${this.serverUrl}/${userRelationsId}`);
    }

    /**
     * Method to fetch user's relation.
     *
     * @param page
     * @returns
     */
    getRelations(page: PageRequest): Observable<PageResponse<UserRelations>> {
        return this.httpClient.post<PageResponse<UserRelations>>(`${this.serverUrl}/all`, page);
    }

    /**
     * Method to fetch user's friends.
     *
     * @param page
     * @returns
     */
    getFriends(page: PageRequest): Observable<PageResponse<UserRelations>> {
        return this.httpClient.post<PageResponse<UserRelations>>(`${this.serverUrl}/friends`, page);
    }

    /**
     * Method to fetch user's received requests.
     *
     * @param page
     * @returns
     */
    getReceived(page: PageRequest): Observable<PageResponse<UserRelations>> {
        return this.httpClient.post<PageResponse<UserRelations>>(`${this.serverUrl}/received`, page);
    }

    /**
     * Method to fetch user's sent requests.
     *
     * @param page
     * @returns
     */
    getSent(page: PageRequest): Observable<PageResponse<UserRelations>> {
        return this.httpClient.post<PageResponse<UserRelations>>(`${this.serverUrl}/sent`, page);
    }

    /**
     * Method to fetch user's blocked relations.
     *
     * @param page
     * @returns
     */
    getBlocked(page: PageRequest): Observable<PageResponse<UserRelations>> {
        return this.httpClient.post<PageResponse<UserRelations>>(`${this.serverUrl}/blocked`, page);
    }

    /**
     * Method to fetch user's relation with id.
     *
     * @param id
     * @returns
     */
    getRelation(id: string): Observable<UserRelations> {
        return this.httpClient.get<UserRelations>(`${this.serverUrl}/status/${id}`);
    }

    /**
     * Method to add relations.
     *
     * @param userRelations
     * @returns
     */
    addRelation(userRelations: UserRelations): Observable<UserRelations> {
        return this.httpClient.post<UserRelations>(`${this.serverUrl}/add`, userRelations);
    }

    /**
     * Method to edit relations.
     *
     * @param userRelations
     * @returns
     */
    editRelation(userRelations: UserRelations): Observable<boolean> {
        return this.httpClient.put<boolean>(`${this.serverUrl}/change-status`, userRelations);
    }

    /**
     * Method to delete relation.
     *
     * @param relationId
     * @returns
     */
    deleteRelation(relationId: String): Observable<boolean> {
        return this.httpClient.delete<boolean>(`${this.serverUrl}/delete/${relationId}`);
    }
}
