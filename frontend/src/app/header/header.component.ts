import {Component} from '@angular/core';
import {BsDropdownConfig, BsDropdownDirective, BsDropdownToggleDirective} from 'ngx-bootstrap/dropdown';
import {BsModalRef, BsModalService} from 'ngx-bootstrap/modal';
import {ApiService} from '../services/api.service';
import {Router} from '@angular/router';
import {SharedModule} from '../shared/shared.module';

@Component({
    selector: 'app-header',
    templateUrl: './header.component.html',
    styleUrl: './header.component.scss',
    providers: [
        {
            provide: BsDropdownConfig,
            useValue: {isAnimated: true, autoClose: true},
        },
    ],
    standalone: true,
    imports: [
        BsDropdownDirective,
        BsDropdownToggleDirective,
        SharedModule
    ]
})
export class HeaderComponent {
    isLoggedIn: boolean;
    username!: string;
    isAdminUser: boolean;

    modalRef?: BsModalRef;

    constructor(
        private apiService: ApiService,
        private router: Router,
        private modalService: BsModalService
    ) {
        this.isAdminUser = apiService.isAdminUser;
        this.isLoggedIn = apiService.isLoggedIn;
        this.username = '';
        if (this.isLoggedIn) {
            const username = apiService.getUsername;
            if (username) {
                this.username = username;
            }
        }
    }

    /**
     navigate to messaging page.
     */
    goToMessaging() {
        this.router.navigate(['/messaging']).then(r => console.log("routing to message page"));
    }

    /**
     navigate to dashboard page.
     */
    goToDashboard() {
        this.router.navigate(['/dashboard']).then(r => console.log("routing to dashboard page"));
    }

    /**
     * login/register button click event handler.
     */
    loginRegister() {
        this.router.navigate(['/login']).then(r => console.log("routing to login page"));
    }

    /**
     * Logout event handler.
     */
    logout() {
        this.apiService.logout();
    }

}
