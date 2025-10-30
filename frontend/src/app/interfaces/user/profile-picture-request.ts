export interface ProfilePictureRequest {
    email: string; // UUID as string
    profilePicture: File; // MultipartFile is represented as File in TypeScript
}
