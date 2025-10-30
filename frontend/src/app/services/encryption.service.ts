import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { asyncScheduler, catchError, map, Observable, scheduled } from "rxjs";
import { ApiService } from "./api.service";
import { SharedKeyRequest } from "../interfaces/shared-key-request";
import { Key } from "../interfaces/key";
import { environment } from "../../environments/environment";

@Injectable({
    providedIn: "root",
})
export class EncryptionService {
    private readonly serverUrl: string; // URL for Server.

    private keyPair!: CryptoKeyPair;
    private publicKeyBase64: string = "";
    private privateKeyBase64: string = "";

    private serverPublicKeyBase64: string = "";

    private currentP2PKey: string = "";
    private currentGroupKey: string = "";

    constructor(private httpClient: HttpClient) {
        console.log("Encryption service initialized.");
        this.serverUrl = environment.apiUrl + "/security";

        this.generateKeyPair().then((keyPair) => {
            this.keyPair = keyPair;
            // localStorage.setItem("keyPair", JSON.stringify(keyPair));
            this.exportRSAKey(keyPair.publicKey).then((publicKeyBase64) => {
                this.publicKeyBase64 = publicKeyBase64;
                // localStorage.setItem("publicKey", publicKeyBase64);
                console.log("Public Key generated");
            });
            this.exportRSAKey(keyPair.privateKey).then((privateKeyBase64) => {
                this.privateKeyBase64 = privateKeyBase64;
                // localStorage.setItem("privateKey", privateKeyBase64);
                console.log("Private Key generated");
            });
        });

        this.getServerPublicKeyBase64()
            .pipe(
                map((resp) => {
                    console.log("fetch server public key -", resp);
                    this.serverPublicKeyBase64 = resp.key;
                    console.log(
                        "Server public key -",
                        this.serverPublicKeyBase64
                    );
                    return true;
                }),
                catchError((error) => {
                    console.log("Unable to fetch server public key -", error);
                    alert("Unable to fetch server public key.");
                    return scheduled([false], asyncScheduler);
                })
            )
            .subscribe();
    }

    // PKI

    get PublicKeyBase64(): string {
        return this.publicKeyBase64;
    }

    get KeyPair(): CryptoKeyPair | null {
        return this.keyPair;
    }

    get currentP2PKeyValue(): string {
        return this.currentP2PKey;
    }
    
    get currentGroupKeyValue(): string {
        return this.currentGroupKey;
    }

    getServerPublicKeyBase64(): Observable<Key> {
        return this.httpClient.get<Key>(`${this.serverUrl}/public-key`);
    }

    async generateKeyPair(): Promise<CryptoKeyPair> {
        const keyPair = await window.crypto.subtle.generateKey(
            {
                name: "RSA-OAEP",
                modulusLength: 2048,
                publicExponent: new Uint8Array([1, 0, 1]), // 0x10001
                hash: "SHA-256",
            },
            true,
            ["encrypt", "decrypt"]
        );

        return keyPair;
    }

    async exportRSAKey(key: CryptoKey): Promise<string> {
        let format: "spki" | "pkcs8";

        if (key.type === "public") {
            format = "spki";
        } else if (key.type === "private") {
            format = "pkcs8";
        } else {
            throw new Error("Unsupported key type: " + key.type);
        }

        const exported = await window.crypto.subtle.exportKey(format, key);
        return btoa(String.fromCharCode(...new Uint8Array(exported)));
    }

    async importRSAKey(base64: string): Promise<CryptoKey> {
        const pkRaw = Uint8Array.from(atob(base64), (c) => c.charCodeAt(0));
        return crypto.subtle.importKey(
            "pkcs8",
            pkRaw,
            { name: "RSA-OAEP", hash: "SHA-256" },
            true,
            ["decrypt"]
        );
    }

    // const encryptedPayload = await encryptDto(authRequestDto);
    async encryptDto(dto: any): Promise<string> {
        const encodedKey = Uint8Array.from(
            atob(this.serverPublicKeyBase64),
            (c) => c.charCodeAt(0)
        );
        const publicKey = await crypto.subtle.importKey(
            "spki",
            encodedKey.buffer,
            { name: "RSA-OAEP", hash: "SHA-256" },
            true,
            ["encrypt"]
        );

        const json = JSON.stringify(dto);
        const encoded = new TextEncoder().encode(json);

        const encrypted = await crypto.subtle.encrypt(
            { name: "RSA-OAEP" },
            publicKey,
            encoded
        );
        // return btoa(String.fromCharCode(...new Uint8Array(encrypted))); // Base64
        return btoa(
            new Uint8Array(encrypted).reduce(
                (acc, byte) => acc + String.fromCharCode(byte),
                ""
            )
        );
    }

    async decryptPayload(base64Payload: string): Promise<string> {
        const encryptedBytes = Uint8Array.from(atob(base64Payload), (c) =>
            c.charCodeAt(0)
        );

        const decrypted = await window.crypto.subtle.decrypt(
            { name: "RSA-OAEP" },
            this.keyPair.privateKey,
            encryptedBytes
        );

        const json = new TextDecoder().decode(decrypted);
        console.log("Decrypted payload -", json);
        return json;
    }

    // MESSAGE ENCRYPTION

    getP2PKey(req: SharedKeyRequest) {
        req.publicKey = this.publicKeyBase64;
        this.httpClient
            .post<Key>(`${this.serverUrl}/shared-key/p2p`, req)
            .pipe(
                map((resp) => {
                    console.log("fetch p2p shared key -", resp);
                    this.decryptPayload(resp.key).then((decryptedData) => {
                        this.currentP2PKey = decryptedData;
                        console.log("Decrypted P2P key -", decryptedData);
                    });
                    return true;
                }),
                catchError((error) => {
                    console.log("Unable to fetch p2p shared key -", error);
                    alert("Unable to fetch P2P shared key.");
                    return scheduled([false], asyncScheduler);
                })
            )
            .subscribe();
    }

    getGroupKey(req: SharedKeyRequest) {
        req.publicKey = this.publicKeyBase64;
        this.httpClient
            .post<Key>(`${this.serverUrl}/shared-key/group`, req)
            .pipe(
                map((resp) => {
                    console.log("fetch group shared key -", resp);
                    this.decryptPayload(resp.key).then((decryptedData) => {
                        this.currentGroupKey = decryptedData;
                        console.log("Decrypted P2P key -", decryptedData);
                    });
                    return true;
                }),
                catchError((error) => {
                    console.log("Unable to fetch group shared key -", error);
                    alert("Unable to fetch group shared key.");
                    return scheduled([false], asyncScheduler);
                })
            )
            .subscribe();
    }

    async importAesKey(base64Key: string): Promise<CryptoKey> {
        const rawKey = Uint8Array.from(atob(base64Key), (c) => c.charCodeAt(0));
        return crypto.subtle.importKey(
            "raw",
            rawKey.buffer,
            { name: "AES-GCM" },
            false,
            ["encrypt", "decrypt"]
        );
    }

    async encryptMessage(
        plaintext: string,
        base64Key: string
    ): Promise<{ cipher: string; iv: string }> {
        const key = await this.importAesKey(base64Key);
        const iv = crypto.getRandomValues(new Uint8Array(12));
        const encoded = new TextEncoder().encode(plaintext);

        const encrypted = await crypto.subtle.encrypt(
            { name: "AES-GCM", iv },
            key,
            encoded
        );

        return {
            cipher: btoa(String.fromCharCode(...new Uint8Array(encrypted))),
            iv: btoa(String.fromCharCode(...iv)),
        };
    }

    async decryptMessage(
        cipherTextBase64: string,
        ivBase64: string,
        base64Key: string
    ): Promise<string> {
        const key = await this.importAesKey(base64Key);
        const cipherBytes = Uint8Array.from(atob(cipherTextBase64), (c) =>
            c.charCodeAt(0)
        );
        const iv = Uint8Array.from(atob(ivBase64), (c) => c.charCodeAt(0));

        const decrypted = await crypto.subtle.decrypt(
            { name: "AES-GCM", iv },
            key,
            cipherBytes
        );
        return new TextDecoder().decode(decrypted);
    }
}
