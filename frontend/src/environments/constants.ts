
export class Constants {
    static allowedImageTypes = ['image/png', 'image/jpeg', 'image/jpg']; // Allowed MIME types
    static allowedDocumentTypes = ['application/pdf']; // Allowed MIME types

    static maxFileSize = 10 * 1024 * 1024; // 10MB

    static maxGroupSize = 20; // Maximum number of members in a group

    static alphabetPattern: RegExp = /^[A-Za-z\s]+$/;
    static bioPattern: RegExp = /^[^;]*$/;
    static passwordPattern: RegExp = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;
    static otpPattern: RegExp = /^[0-9]{6}$/; // 6 digit OTP
}
