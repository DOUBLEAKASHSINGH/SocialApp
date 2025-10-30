export interface ProfilePictureResponse {
    email: string;
    profilePicture: Uint8Array; // byte[] is represented as Uint8Array in TypeScript
    profilePictureType: string;
}
