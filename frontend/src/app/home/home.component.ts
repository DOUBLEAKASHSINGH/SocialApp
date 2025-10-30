import { Component } from "@angular/core";
import { HeaderComponent } from "../header/header.component";
import { SharedModule } from "../shared/shared.module";
import { Router } from "@angular/router";
import { ApiService } from "../services/api.service";
import { BsModalService } from "ngx-bootstrap/modal";

@Component({
    selector: "app-home",
    imports: [HeaderComponent, SharedModule],
    templateUrl: "./home.component.html",
    styleUrl: "./home.component.scss",
    providers: [BsModalService],
    standalone: true,
})
export class HomeComponent {
    isLoggedIn: boolean = false;

    constructor(private router: Router, private apiService: ApiService) {
        this.isLoggedIn = apiService.isLoggedIn;
    }

    goToLogin() {
        this.router.navigate(["/login"]);
    }

    goToRegister() {
        this.router.navigate(["/register"]);
    }

    goToDashboard() {
        if (this.isLoggedIn) {
            this.router.navigate(["/dashboard"]);
        } else {
            alert("Please log in to access the Dashboard");
            this.router.navigate(["/login"]);
        }
    }

    goToMessaging() {
        if (this.isLoggedIn) {
            this.router.navigate(["/messaging"]);
        } else {
            alert("Please log in to access Messaging");
            this.router.navigate(["/login"]);
        }
    }
}
