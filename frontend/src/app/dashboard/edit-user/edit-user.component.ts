import {Component, Input, OnChanges, SimpleChanges} from '@angular/core';
import {UserResponse} from '../../interfaces/user/user-response';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {ApiService} from '../../services/api.service';
import {Router} from '@angular/router';
import {Role} from '../../interfaces/enum/role';
import {catchError, tap} from 'rxjs';
import {NgIf} from '@angular/common';
import {Constants} from '../../../environments/constants';

@Component({
    selector: 'app-edit-user',
    imports: [
        ReactiveFormsModule,
        NgIf
    ],
    templateUrl: './edit-user.component.html',
    standalone: true,
    styleUrl: './edit-user.component.scss'
})
export class EditUserComponent implements OnChanges {
    @Input()
    user!: UserResponse;
    @Input()
    onFileSelected!: (($event: Event, target: UserResponse) => void);

    isAdminUser: boolean;
    isLoading: boolean = false;

    userForm: FormGroup = new FormGroup({
        firstName: new FormControl('', [
            Validators.required,
            Validators.pattern(Constants.alphabetPattern),
            Validators.minLength(2),
            Validators.maxLength(50),
        ]),
        lastName: new FormControl('', [
            Validators.pattern(Constants.alphabetPattern),
            Validators.maxLength(50),
        ]),
        bio: new FormControl('', [
            Validators.maxLength(255),
            Validators.pattern(Constants.bioPattern) // Block semicolons (SQL injection)
        ]),
        userRole: new FormControl(false, []),
    });

    constructor(private apiService: ApiService, private route: Router) {
        this.isAdminUser = this.apiService.isAdminUser;
    }

    /**
     * Getter for firstName
     */
    get firstName() {
        return this.userForm.get('firstName');
    }

    /**
     * Getter for email
     //  */
    // get email() {
    //     return this.userForm.get('email');
    // }

    /**
     * Getter for lastName
     */
    get lastName() {
        return this.userForm.get('lastName');
    }

    /**
     * Getter for bio
     */
    get bio() {
        return this.userForm.get('bio');
    }

    get isCurrentUser(): boolean {
        return this.apiService.getUserId === this.user?.email;
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (this.user && changes['user']) {
            this.userForm.setValue({
                firstName: this.user.firstName,
                lastName: this.user.lastName,
                bio: this.user.bio,
                userRole: this.user.role === Role.ADMIN,
            });
        }
    }

    /**
     * Method to update  user.
     */
    updateUser() {
        if (!this.user) {
            console.log('User not found');
            return;
        }
        this.isLoading = true;
        console.log('updateUser - ', this.userForm.value);
        const updatedUser: UserResponse = {
            firstName: this.userForm.value.firstName,
            lastName: this.userForm.value.lastName,
            bio: this.userForm.value.bio,
            email: this.user.email,
            role: this.userForm.value.userRole ? Role.ADMIN : Role.USER
        }

        this.apiService
            .editUser(updatedUser)
            .pipe(
                tap((data) => {
                    this.user = data
                    this.isLoading = false;
                    console.log('edit status =', data);
                    // this.route.navigate(['/dashboard']);
                    // window.location.reload()
                }),
                catchError((error) => {
                    this.isLoading = false;
                    console.log("edit error");
                    return [];
                })
            )
            .subscribe();
    }

    changeImage(event: Event) {
        if (!this.user) {
            console.log('User not found');
            return;
        }
        console.log('changeImage - ', this.user.email);
        this.onFileSelected(event, this.user);
    }
}
