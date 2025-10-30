import { Injectable } from "@angular/core";
import { environment } from "../../environments/environment";
import { Role } from "../interfaces/enum/role";
import { LoginResponse } from "../interfaces/login/login-response";
import { UserResponse } from "../interfaces/user/user-response";

@Injectable({
    providedIn: "root",
})
export class DataService {
    isAdminUser!: boolean;
    isVerifiedUser!: boolean;
    isLoggedIn!: boolean;
    isOtpToken!: boolean;
    private readonly serverUrl: string; // URL for Server.
    private readonly token: string; // Name of token storage.
    private readonly username: string; // Name of username storage.
    private readonly userid: string; // Name of username storage.
    private readonly role: string; // Name of role storage.
    private readonly requestTimeout: number; // timeout time of http request.
    private storage;

    constructor() {
        console.log("data service initialized.");
        this.serverUrl = environment.apiUrl;
        this.token = "token";
        this.username = "username";
        this.userid = "userid";
        this.role = "role";
        this.requestTimeout = 30000; // 30 seconds
        this.storage = localStorage;
        this.isLoggedIn = this.storage.getItem(this.userid) != null;
        this.isAdminUser = this.storage.getItem(this.role) === Role.ADMIN;
        this.isVerifiedUser =
            this.storage.getItem(this.role) !== Role.UNVERIFIED;
        this.isOtpToken = false;
    }

    /**
     * Getter for serverUrl.
     */
    public get getServerUrl(): string {
        return this.serverUrl;
    }

    /**
     * Getter for timeout time.
     */
    public get getRequestTimeout(): number {
        return this.requestTimeout;
    }

    /**
     * Getter for username.
     */
    public get getUsername(): string | null {
        return this.storage.getItem(this.username);
    }

    /**
     * Getter for user id.
     */
    public get getUserId(): string | null {
        return this.storage.getItem(this.userid);
    }

    /**
     * Getter for role.
     */
    public get getRole(): string | null {
        return this.storage.getItem(this.role);
    }

    /**
     * Getter for if user is verified.
     */
    public get getIsVerifiedUser(): boolean {
        return this.storage.getItem(this.role) !== Role.UNVERIFIED;
    }

    /**
     * Getter for token.
     */
    public get getToken(): string | null {
        return this.storage.getItem(this.token);
    }

    public setItem(key: string, value: string) {
        this.storage.setItem(key, value);
    }

    /**
     * Method to verify if user is logged in.
     */
    checkLogin(): void {
        this.isLoggedIn = this.storage.getItem(this.userid) != null;
        this.isAdminUser = this.storage.getItem(this.role) === Role.ADMIN;
        this.isVerifiedUser =
            this.storage.getItem(this.role) !== Role.UNVERIFIED;
    }

    storeData(data: LoginResponse | UserResponse): void {
        const role: Role = data.role === "ADMIN" ? Role.ADMIN : Role.USER;
        this.storage.setItem(this.role, role);
        this.storage.setItem(this.userid, data.email);
        this.storage.setItem(
            this.username,
            data.firstName + " " + data.lastName
        );
        this.checkLogin();
    }

    /**
     * Method to clear current login details
     */
    voidSession() {
        this.storage.removeItem(this.token);
        this.storage.removeItem(this.username);
        this.storage.removeItem(this.userid);
        this.storage.removeItem(this.role);
        this.isLoggedIn = false;
        this.isAdminUser = false;
        this.isOtpToken = false;
        this.isVerifiedUser = false;
    }
}
