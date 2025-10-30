import {Component, Input, OnChanges, SimpleChanges} from '@angular/core';
import {BsModalService} from 'ngx-bootstrap/modal';
import {animate, style, transition, trigger} from '@angular/animations';
import {SharedModule} from '../../shared/shared.module';
import {UserResponse} from '../../interfaces/user/user-response';
import {UserApiService} from '../../services/user-api.service';
import {UserDocumentResponse} from '../../interfaces/user/user-document-response';
import {catchError, of, tap} from 'rxjs';
import {DomSanitizer, SafeUrl} from '@angular/platform-browser';
import {Constants} from '../../../environments/constants';
import {UserDocumentRequest} from '../../interfaces/user/user-document-request';

@Component({
    selector: 'app-view-document',
    imports: [
        SharedModule
    ],
    templateUrl: './view-document.component.html',
    styleUrl: './view-document.component.scss',
    providers: [
        BsModalService,
    ],
    animations: [
        trigger('pageChange', [
            transition(':enter', [
                style({opacity: 0}),
                animate('1000ms', style({opacity: 1})),
            ]),
        ]),
    ],
    standalone: true
})
export class ViewDocumentComponent implements OnChanges {
    @Input()
    user!: UserResponse;

    document!: UserDocumentResponse;
    documentUrl: SafeUrl | undefined;

    isPageLoading: boolean = false;
    errorLoadingData: boolean = false;

    constructor(private userApiService: UserApiService,
                private sanitizer: DomSanitizer) {
    }

    get isDocumentAvailable(): boolean {
        return this.documentUrl !== undefined;
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (this.user && changes['user']) {
            console.log(changes['user']);
            this.fetchDocument();
        }
    }

    /**
     * Method to fetch user doc from backend.
     */
    fetchDocument(): void {
        console.log("Fetching User doc ", this.isDocumentAvailable);
        this.isPageLoading = true;
        console.log('Fetching User doc');
        this.userApiService.getUserDoc(this.user.email)
            .pipe(
                tap((data) => {
                    this.document = data;
                    console.log('User document =', this.document);
                    this.documentUrl = this.convertToFile(this.document.document, this.document.docType);
                    setTimeout(() => {
                        this.isPageLoading = false;
                    }, 300);
                }),
                catchError((error) => {
                    if (error.status < 400 || error.status > 404) {
                        this.errorLoadingData = true;
                        console.log('User doc Fetch error');
                    }
                    this.isPageLoading = false;
                    return of(false);
                })
            )
            .subscribe();
    }

    convertToFile(byteArray: Uint8Array, fileType: string): SafeUrl | undefined {
        if (byteArray.length !== 0) {
            // Decode Base64
            const binary = atob(byteArray.toString());
            const len = binary.length;
            const bytes = new Uint8Array(len);
            for (let i = 0; i < len; i++) {
                bytes[i] = binary.charCodeAt(i);
            }
            const blob = new Blob([bytes], {type: fileType});
            const objectURL = URL.createObjectURL(blob);
            return this.sanitizer.bypassSecurityTrustResourceUrl(objectURL);
        }
        return undefined;
    }

    onFileSelected = ($event: Event, target: UserResponse) => {
        const file = ($event.target as HTMLInputElement).files?.[0];
        if (!file) return;

        console.log('User for file: ', target.email);
        console.log('File selected: ', file.size / (1024 * 1024), file.type);

        // Validate file size (Max 10MB)
        const maxSize = Constants.maxFileSize;
        if (file.size > maxSize) {
            alert('File size exceeds 5MB. Please select a smaller file.');
            return;
        }

        if (!Constants.allowedDocumentTypes.includes(file.type)) {
            alert('Invalid file type. Allowed types: PDF.');
            return;
        }

        // Confirm before upload
        if (!confirm('Do you want to upload this document?')) {
            return;
        }
        this.uploadUserDoc(file, target);
    }

    uploadUserDoc(file: File, target: UserResponse) {
        console.log('Upload User doc clicked');
        this.isPageLoading = true;
        const documentRequest: UserDocumentRequest = {
            user: target,
            document: file,
            docType: file.type
        }
        console.log("file upload doc: ", JSON.stringify(documentRequest.user))
        this.userApiService.setUserDoc(documentRequest)
            .pipe(
                tap(() => {
                    console.log('Upload success');
                    this.isPageLoading = false;
                    this.fetchDocument();
                }),
                catchError(() => {
                    this.isPageLoading = false;
                    this.errorLoadingData = true;
                    console.log('Upload error');
                    return of(false);
                })
            )
            .subscribe();
    }

}
