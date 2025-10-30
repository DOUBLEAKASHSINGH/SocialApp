import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {ApiService} from './api.service';
import {UserResponse} from '../interfaces/user/user-response';
import {Observable} from 'rxjs';
import {ChatGroup} from '../interfaces/group/chat-group';
import {GroupPageRequest} from '../interfaces/group/group-page-request';
import {PageResponse} from '../interfaces/page-response';
import {MessageRequest} from '../interfaces/message/message-request';
import {MessageResponse} from '../interfaces/message/message-response';

@Injectable({
  providedIn: 'root'
})
export class GroupApiService {
    private readonly serverUrl: string; // URL for Server.

    constructor(private httpClient: HttpClient, private apiService: ApiService) {
        this.serverUrl = this.apiService.getServerUrl + '/group';
    }

    /**
     * Method to fetch group from backend.
     *
     * @param groupId
     * @returns
     */
    getGroupById(groupId: String): Observable<ChatGroup> {
        return this.httpClient.get<ChatGroup>(`${this.serverUrl}/${groupId}`);
    }

    /**
     * Method to fetch group by name from backend.
     *
     * @param group
     * @returns
     */
    getGroupByName(group: GroupPageRequest): Observable<PageResponse<ChatGroup>> {
        return this.httpClient.post<PageResponse<ChatGroup>>(`${this.serverUrl}/search`, group);
    }

    /**
     * Method to fetch group by user from backend.
     *
     * @param group
     * @returns
     */
    getGroupByUser(group: GroupPageRequest): Observable<PageResponse<ChatGroup>> {
        return this.httpClient.post<PageResponse<ChatGroup>>(`${this.serverUrl}/my-groups`, group);
    }

    /**
     * Method to add group.
     *
     * @param group
     * @returns
     */
    addGroup(group: ChatGroup): Observable<ChatGroup> {
        return this.httpClient.post<ChatGroup>(`${this.serverUrl}`, group);
    }

    /**
     * Method to add user/s to group.
     *
     * @param group
     * @returns
     */
    addUsersToGroup(group: ChatGroup): Observable<ChatGroup> {
        return this.httpClient.post<ChatGroup>(`${this.serverUrl}/add`, group);
    }

    /**
     * Method to remove user/s from group.
     *
     * @param group
     * @returns
     */
    removeUsersFromGroup(group: ChatGroup): Observable<ChatGroup> {
        return this.httpClient.post<ChatGroup>(`${this.serverUrl}/remove`, group);
    }

    /**
     * Method to delete group.
     *
     * @param groupId
     * @returns
     */
    deleteGroup(groupId: String): Observable<boolean> {
        return this.httpClient.delete<boolean>(`${this.serverUrl}/delete/${groupId}`);
    }
}
