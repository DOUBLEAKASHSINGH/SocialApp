import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { ApiService } from "./api.service";
import { from, Observable, switchMap } from "rxjs";
import { MessageRequest } from "../interfaces/message/message-request";
import { MessagePageRequest } from "../interfaces/message/message-page-request";
import { PageResponse } from "../interfaces/page-response";
import { MessageResponse } from "../interfaces/message/message-response";
import { UserResponse } from "../interfaces/user/user-response";
import { ChatGroup } from "../interfaces/group/chat-group";
import { PageRequest } from "../interfaces/page-request";
import { EncryptionService } from "./encryption.service";

@Injectable({
    providedIn: "root",
})
export class MessageApiService {
    private readonly serverUrl: string; // URL for Server.

    constructor(
        private httpClient: HttpClient,
        private apiService: ApiService,
        private encryptionService: EncryptionService
    ) {
        this.serverUrl = this.apiService.getServerUrl + "/message";
    }

    /**
     * Method to fetch users.
     *
     * @param pageRequest
     * @returns
     */
    getRecentUsers(
        pageRequest: PageRequest
    ): Observable<PageResponse<UserResponse>> {
        return this.httpClient.post<PageResponse<UserResponse>>(
            `${this.serverUrl}/user-list`,
            pageRequest
        );
    }

    /**
     * Method to fetch users.
     *
     * @param pageRequest
     * @returns
     */
    getRecentGroups(
        pageRequest: PageRequest
    ): Observable<PageResponse<ChatGroup>> {
        return this.httpClient.post<PageResponse<ChatGroup>>(
            `${this.serverUrl}/group-list`,
            pageRequest
        );
    }

    /**
     * Method to fetch p2p chat messages from backend.
     *
     * @param messagePageRequest
     * @returns
     */
    getChatMessages(
        messagePageRequest: MessagePageRequest
    ): Observable<PageResponse<MessageResponse>> {
        return this.httpClient
            .post<PageResponse<MessageResponse>>(
                `${this.serverUrl}/chat`,
                messagePageRequest
            )
            .pipe(
                switchMap(async (response) => {
                    for (let msg of response.items) {
                        if (msg.message && msg.iv) {
                            msg.message =
                                await this.encryptionService.decryptMessage(
                                    msg.message,
                                    msg.iv,
                                    this.encryptionService.currentP2PKeyValue
                                );
                        }
                    }
                    return response;
                })
            );
    }

    /**
     * Method to fetch group messages from backend.
     *
     * @param messagePageRequest
     * @returns
     */
    getGroupMessages(
        messagePageRequest: MessagePageRequest
    ): Observable<PageResponse<MessageResponse>> {
        return this.httpClient
            .post<PageResponse<MessageResponse>>(
                `${this.serverUrl}/group`,
                messagePageRequest
            )
            .pipe(
                switchMap(async (response) => {
                    for (let msg of response.items) {
                        if (msg.message && msg.iv) {
                            msg.message =
                                await this.encryptionService.decryptMessage(
                                    msg.message,
                                    msg.iv,
                                    this.encryptionService.currentP2PKeyValue
                                );
                        }
                    }
                    return response;
                })
            );
    }

    /**
     * Method to send message.
     *
     * @param message
     * @returns
     */
    sendMessage(message: MessageRequest): Observable<MessageResponse> {
        const isGroup = message.group;
        const key = isGroup
            ? this.encryptionService.currentGroupKeyValue
            : this.encryptionService.currentP2PKeyValue;

        return from(
            this.encryptionService.encryptMessage(message.message || "", key)
        ).pipe(
            switchMap(({ cipher, iv }) => {
                if (message.message) {
                    message.message = cipher;
                }
                message.iv = iv;
                return this.httpClient.post<MessageResponse>(
                    `${this.serverUrl}`,
                    this.messageToFormData(message)
                );
            })
        );
    }

    /**
     * Method to edit message.
     *
     * @param message
     * @returns
     */
    editMessage(message: MessageRequest): Observable<MessageResponse> {
        return this.httpClient.put<MessageResponse>(
            `${this.serverUrl}`,
            this.messageToFormData(message)
        );
    }

    /**
     * Method to delete message.
     *
     * @param messageId
     * @returns
     */
    deleteMessage(messageId: String): Observable<boolean> {
        return this.httpClient.delete<boolean>(
            `${this.serverUrl}/${messageId}`
        );
    }

    messageToFormData(message: MessageRequest): FormData {
        const formData = new FormData();
        if (message.file) {
            formData.append("file", message.file);
        }
        formData.append(
            "message",
            new Blob([JSON.stringify(message)], { type: "application/json" })
        );
        return formData;
    }
}
