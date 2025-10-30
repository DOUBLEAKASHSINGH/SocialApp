// import { CanActivateFn } from '@angular/router';

// export const authGuard: CanActivateFn = (route, state) => {
//   return true;
// };

import { Injectable } from "@angular/core";
import { CanActivate, Router } from "@angular/router";
import { Observable } from "rxjs";
import { DataService } from "../services/data.service";

@Injectable({
    providedIn: "root",
})
export class AuthGuard implements CanActivate {
    constructor(private dataService: DataService, private router: Router) {}

    canActivate(): boolean | Observable<boolean> | Promise<boolean> {
        if (this.dataService.isLoggedIn) {
            return true;
        } else {
            this.router.navigate(["/login"]); // redirect to login
            return false;
        }
    }
}
