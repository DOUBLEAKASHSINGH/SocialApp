import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { environment } from "../../environments/environment";
import {
    asyncScheduler,
    catchError,
    from,
    map,
    Observable,
    scheduled,
    switchMap,
    tap,
} from "rxjs";
import { LoginResponse } from "../interfaces/login/login-response";
import { Stats } from "../interfaces/market/stats";
import { UserLogin } from "../interfaces/login/login-request";
import { User } from "../interfaces/user/user";
import { Otp } from "../interfaces/login/otp";
import { UserResponse } from "../interfaces/user/user-response";
import { Role } from "../interfaces/enum/role";
import { VerificationPurpose } from "../interfaces/enum/verification-purpose";
import { EmailOtp } from "../interfaces/login/emailOtp";
import { ResetPassword } from "../interfaces/login/reset-password";
import { EncryptionService } from "./encryption.service";
import { DataService } from "./data.service";
import { EncryptedPayload } from "../interfaces/encrypted-payload";

@Injectable({
    providedIn: "root",
})
export class ApiService {
    // isAdminUser!: boolean;
    // isVerifiedUser!: boolean;
    // isLoggedIn!: boolean;
    // isOtpToken!: boolean;
    private readonly serverUrl: string; // URL for Server.
    private readonly token: string; // Name of token storage.
    private readonly username: string; // Name of username storage.
    private readonly userid: string; // Name of username storage.
    private readonly role: string; // Name of role storage.
    private readonly requestTimeout: number; // timeout time of http request.
    private storage;

    constructor(
        private httpClient: HttpClient,
        private encryptionService: EncryptionService,
        private dataService: DataService
    ) {
        console.log("API service initialized.");
        this.serverUrl = dataService.getServerUrl;
        this.token = "token";
        this.username = "username";
        this.userid = "userid";
        this.role = "role";
        this.requestTimeout = 30000; // 30 seconds
        this.storage = localStorage;
        // this.isLoggedIn = this.storage.getItem(this.userid) != null;
        // this.isAdminUser = this.storage.getItem(this.role) === Role.ADMIN;
        // this.isVerifiedUser =
        //     this.storage.getItem(this.role) !== Role.UNVERIFIED;
        // this.isOtpToken = false;
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
        return this.dataService.getRequestTimeout;
    }

    /**
     * Getter for username.
     */
    public get getUsername(): string | null {
        return this.dataService.getUsername;
    }

    /**
     * Getter for user id.
     */
    public get getUserId(): string | null {
        return this.dataService.getUserId;
    }

    /**
     * Getter for role.
     */
    public get getRole(): string | null {
        return this.dataService.getRole;
    }

    /**
     * Getter for if user is verified.
     */
    public get getIsVerifiedUser(): boolean {
        return this.dataService.getIsVerifiedUser;
    }

    /**
     * Getter for token.
     */
    public get getToken(): string | null {
        return this.dataService.getToken;
    }

    public get isLoggedIn(): boolean {
        return this.dataService.isLoggedIn;
    }

    public get isAdminUser(): boolean {
        return this.dataService.isAdminUser;
    }

    public get isVerifiedUser(): boolean {
        return this.dataService.isVerifiedUser;
    }

    public get isOtpToken(): boolean {
        return this.dataService.isOtpToken;
    }

    /**
     * Method to verify if user is logged in.
     */
    checkLogin(): void {
        this.dataService.checkLogin();
        // this.isLoggedIn = this.storage.getItem(this.userid) != null;
        // this.isAdminUser = this.storage.getItem(this.role) === Role.ADMIN;
        // this.isVerifiedUser =
        //     this.storage.getItem(this.role) !== Role.UNVERIFIED;
    }

    storeData(data: LoginResponse | UserResponse): void {
        this.dataService.storeData(data);
        // const role: Role = data.role === "ADMIN" ? Role.ADMIN : Role.USER;
        // this.storage.setItem(this.role, role);
        // this.storage.setItem(this.userid, data.email);
        // this.storage.setItem(
        //     this.username,
        //     data.firstName + " " + data.lastName
        // );
        // this.checkLogin();
    }

    /**
     * Method to clear current login details
     */
    voidSession() {
        this.dataService.voidSession();
        // this.storage.removeItem(this.token);
        // this.storage.removeItem(this.username);
        // this.storage.removeItem(this.userid);
        // this.storage.removeItem(this.role);
        // this.isLoggedIn = false;
        // this.isAdminUser = false;
        // this.isOtpToken = false;
        // this.isVerifiedUser = false;
    }

    /**
     * Method to check if session is still valid.
     */
    checkSession(): void {
        if (this.dataService.isLoggedIn) {
            console.log("check login");
            this.validate()
                .pipe(
                    map((resp) => {
                        console.log("check login validate -", resp);
                        const role: Role =
                            resp === "ADMIN" ? Role.ADMIN : Role.USER;
                        this.storage.setItem(this.role, role);
                        this.checkLogin();
                        return true;
                    }),
                    catchError((error) => {
                        console.log("check login validate error -", error);
                        this.voidSession();
                        return scheduled([false], asyncScheduler);
                        // return of(false);
                    })
                )
                .subscribe();
        }
    }

    /**
     * Method to check valid user login.
     *
     * @returns
     */
    validate(): Observable<String> {
        // return of("ADMIN");
        return this.httpClient
            .post<LoginResponse>(`${this.serverUrl}/user/validate`, "")
            .pipe(map((data) => data.role));
    }

    /**
     * Method to logout user.
     */
    logout() {
        console.log("Logout 1");
        this.httpClient
            .post(`${this.serverUrl}/user/logout`, "")
            .pipe(
                tap((data) => {
                    console.log("Logout 2", data);
                    this.voidSession();
                    window.location.href = "";
                }),
                catchError((error) => {
                    console.log("logout error -", error);
                    this.voidSession();
                    return scheduled([false], asyncScheduler);
                    // return of(false);
                })
            )
            .subscribe();
    }

    sendOtp(otp: Otp): Observable<boolean> {
        this.checkLogin();
        if (!this.dataService.isLoggedIn) {
            return scheduled([true], asyncScheduler);
        }
        return this.httpClient.post<boolean>(
            `${this.serverUrl}/user/generate-otp`,
            otp
        );
    }

    sendEmailOtp(request: EmailOtp): Observable<boolean> {
        return this.httpClient.post<boolean>(
            `${this.serverUrl}/user/generate-email-otp`,
            request
        );
    }

    resetPassword(request: ResetPassword): Observable<boolean> {
        return from(this.encryptionService.encryptDto(request)).pipe(
            switchMap((encryptedData) => {
                // console.log("Login - encrypted data - ", encryptedData);
                const data: EncryptedPayload = { payload: encryptedData };
                return this.httpClient.post<boolean>(
                    `${this.serverUrl}/user/reset-password`,
                    data
                );
            })
        );
    }

    /**
     * Method to check login with rest api.
     *
     * @param user
     * @returns
     */
    login(user: UserLogin): Observable<boolean> {
        if (this.dataService.isLoggedIn) {
            this.logout();
        }
        console.log("login service - ", user.email, user.password);

        return from(this.encryptionService.encryptDto(user)).pipe(
            switchMap((encryptedData) => {
                console.log("Login - encrypted data - ", encryptedData);
                const data: EncryptedPayload = { payload: encryptedData };
                return this.httpClient
                    .post<LoginResponse>(`${this.serverUrl}/user/login`, data)
                    .pipe(
                        map((data) => {
                            console.log("login response", data);
                            this.dataService.isOtpToken = true;
                            this.storeData(data);
                            this.storage.setItem(this.token, data.token);
                            return true;
                        })
                    );
            })
        );
        // return this.httpClient
        //     .post<LoginResponse>(`${this.serverUrl}/user/login`, user)
        //     .pipe(
        //         map((data) => {
        //             console.log("login response", data);
        //             this.isOtpToken = true;
        //             this.storeData(data);
        //             this.storage.setItem(this.token, data.token);
        //             return true;
        //         })
        //     );
    }

    /**
     * Method to check login with otp with rest api.
     *
     * @param otp
     * @returns
     */
    loginOtp(otp: Otp): Observable<boolean> {
        this.checkLogin();
        if (!this.dataService.isOtpToken) {
            return scheduled([false], asyncScheduler);
            // return of(false);
        }
        otp.purpose = VerificationPurpose.USER_LOGIN;
        console.log("login-otp service - ", otp.otp, otp.purpose);

        return from(this.encryptionService.encryptDto(otp)).pipe(
            switchMap((encryptedData) => {
                console.log("Login - encrypted data - ", encryptedData);
                const data: EncryptedPayload = { payload: encryptedData };
                return this.httpClient
                    .post<LoginResponse>(
                        `${this.serverUrl}/user/login-otp`,
                        data
                    )
                    .pipe(
                        map((data) => {
                            console.log("login-otp response", data);
                            this.dataService.isOtpToken = false;
                            this.storeData(data);
                            this.storage.setItem(this.token, data.token);
                            return true;
                        })
                    );
            })
        );
        // return this.httpClient
        //     .post<LoginResponse>(`${this.serverUrl}/user/login-otp`, otp)
        //     .pipe(
        //         map((data) => {
        //             console.log("login-otp response", data);
        //             this.dataService.isOtpToken = false;
        //             this.storeData(data);
        //             this.storage.setItem(this.token, data.token);
        //             return true;
        //         })
        //     );
    }

    /**
     * Method to register new user with rest api.
     *
     * @param user
     * @returns
     */
    register(user: User): Observable<boolean> {
        this.checkSession();
        console.log("Register - ", user);
        let url = "register";
        if (
            this.dataService.isLoggedIn &&
            this.dataService.isAdminUser &&
            !this.dataService.isOtpToken
        ) {
            url = "register-admin";
        }
        return from(this.encryptionService.encryptDto(user)).pipe(
            switchMap((encryptedData) => {
                // console.log("Login - encrypted data - ", encryptedData);
                const data: EncryptedPayload = { payload: encryptedData };
                return this.httpClient
                    .post<LoginResponse>(`${this.serverUrl}/user/${url}`, data)
                    .pipe(
                        map((data) => {
                            console.log("Register response - ", data);
                            if (
                                this.dataService.isLoggedIn &&
                                this.dataService.isAdminUser &&
                                !this.dataService.isOtpToken
                            ) {
                                console.log("New user registered by admin");
                                return true;
                            }
                            this.storeData(data);
                            this.storage.setItem(this.token, data.token);
                            return true;
                        })
                    );
            })
        );
    }

    /**
     * Method to edit user with rest api.
     *
     * @param user
     * @returns
     */
    editUser(user: UserResponse): Observable<UserResponse> {
        console.log("editUser - ", user);
        return this.httpClient
            .post<UserResponse>(`${this.serverUrl}/user/edit`, user)
            .pipe(
                map((data) => {
                    console.log("editUser response - ", data);
                    this.storeData(data);
                    return data;
                })
            );
    }

    /**
     * Method to lock/suspend a user account.
     *
     * @param user
     * @returns
     */
    suspend(user: UserResponse): Observable<UserResponse> {
        console.log("suspend user - ", user);
        this.checkSession();
        if (
            !this.dataService.isLoggedIn ||
            !this.dataService.isAdminUser ||
            this.dataService.isOtpToken
        )
            throw new Error("Unauthorized");
        return this.httpClient
            .put<UserResponse>(`${this.serverUrl}/user/lock`, user)
            .pipe(
                map((data) => {
                    console.log("suspend response - ", data);
                    return data;
                })
            );
    }

    /**
     * Method to verify a user account.
     *
     * @param user
     * @returns
     */
    verify(user: UserResponse): Observable<UserResponse> {
        console.log("verify user - ", user);
        this.checkSession();
        console.log(
            "verify api",
            this.dataService.isLoggedIn,
            this.dataService.isAdminUser,
            this.dataService.isOtpToken
        );
        if (
            !this.dataService.isLoggedIn ||
            !this.dataService.isAdminUser ||
            this.dataService.isOtpToken
        )
            throw new Error("Unauthorized");
        return this.httpClient
            .put<UserResponse>(`${this.serverUrl}/user/verify`, user)
            .pipe(
                map((data) => {
                    console.log("verify response - ", data);
                    return data;
                })
            );
    }

    /**
     * Method to verify a user account.
     *
     * @param user
     * @returns
     */
    delete(user: UserResponse): Observable<boolean> {
        console.log("delete user - ", user);
        this.checkSession();
        if (
            !this.dataService.isLoggedIn ||
            !this.dataService.isAdminUser ||
            this.dataService.isOtpToken
        )
            throw new Error("Unauthorized");
        return this.httpClient
            .put<boolean>(`${this.serverUrl}/user/delete`, user)
            .pipe(
                map((data) => {
                    console.log("delete response - ", data);
                    return data;
                })
            );
    }

    downloadLogsZip(): Observable<Blob> {
        return this.httpClient.get(`${this.serverUrl}/user/download-logs`, {
            responseType: "blob",
        });
    }
}
