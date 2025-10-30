import { HttpInterceptorFn } from "@angular/common/http";
import { ApiService } from "../services/api.service";
import { inject } from "@angular/core";
import { catchError, map, timeout } from "rxjs";
import { Router } from "@angular/router";
import { DataService } from "../services/data.service";

export const tokenInterceptor: HttpInterceptorFn = (request, next) => {
    // const apiService = inject(ApiService);
    const dataService = inject(DataService);
    const router = inject(Router);
    const token = dataService.getToken;
    console.log("InterceptorFn -", request.url, token);
    if (token != null) {
        request = request.clone({
            setHeaders: {
                Authorization: `Bearer ${token}`,
            },
        });
    }
    console.log("New request", request);
    return next(request)
        .pipe(timeout(dataService.getRequestTimeout))
        .pipe(
            map((response) => {
                console.log("Response", response);
                return response;
            }),
            catchError((error) => {
                console.log(
                    "An error occurred:",
                    error,
                    error.url,
                    error.status,
                    error.message
                );
                if (error.status === 500) {
                    // this.dataService.setData = {
                    //     code: 500,
                    //     title: 'Internal Server Error',
                    // };
                    // this.router.navigate(['error']);
                } else if (error.status === 408) {
                    console.log(
                        "Session error - ",
                        error.status,
                        " ",
                        error.statusText
                    );
                    alert("Session Expired or Connection refused by server.");
                    // apiService.checkSession();
                    router.navigate(["/"]);
                    // if (!error.url.endsWith('logout'))
                    //     this.apiService.logout();
                    // apiService.voidSession();
                    // dataService.setData = {
                    //     code: 408,
                    //     title: 'Session Time Out',
                    // };
                    // router.navigate(['error']);
                } else if(error.status === 0) {
                    alert("Unable to connect to server. Reload Page.");
                } else if (error.status === 403) {
                    // if (!error.url.endsWith('logout'))
                    //     this.apiService.logout();
                    // apiService.voidSession();
                    // this.dataService.setData = {
                    //     code: 403,
                    //     title: 'Forbidden',
                    // };
                    // this.router.navigate(['error']);
                } else if (error.status === 404) {
                    // this.dataService.setData = {
                    //     code: 404,
                    //     title: 'Not Found',
                    // };
                    // this.router.navigate(['error']);
                } else if (error.name === "TimeoutError") {
                    // this.dataService.setData = {
                    //     code: 408,
                    //     title: 'Request Timeout',
                    // };
                    // this.router.navigate(['error']);
                }
                throw error;
            })
        );
};
// import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest, HttpResponse,} from '@angular/common/http';
// import {Injectable} from '@angular/core';
// import {Router} from '@angular/router';
// import {catchError, map, Observable, timeout} from 'rxjs';
// import {ApiService} from '../services/api.service';
// import {DataApiService} from '../services/data-api.service';
//
// @Injectable()
// export class TokenInterceptor implements HttpInterceptor {
//     constructor(
//         private router: Router,
//         private apiService: ApiService,
//         private dataService: DataApiService
//     ) {
//     }
//
//     intercept(
//         request: HttpRequest<unknown>,
//         next: HttpHandler
//     ): Observable<HttpEvent<unknown>> {
//         const token = this.apiService.getToken;
//         console.log('Interceptor -', request.url, token);
//         if (token != null) {
//             request = request.clone({
//                 setHeaders: {
//                     Authorization: `Bearer ${token}`,
//                 },
//             });
//             console.log('new request: ', request);
//         }
//         return next.handle(request)
//             .pipe(timeout(this.apiService.getRequestTimeout))
//             .pipe(
//                 map((response) => {
//                     console.log("Response", response);
//                     return response;
//                 }),
//                 catchError((err) => {
//                     console.log('Error', err);
//                     throw err;
//                 })
//             );
//         // Send request and handle response errors.
//         // return next
//         //     .handle(request)
//         //     .pipe(timeout(this.apiService.getRequestTimeout))
//         //     .pipe(
//         //         catchError((error) => {
//         //             console.log('An error occurred:', error, error.url);
//         //             if (error.status === 500) {
//         //                 // this.dataService.setData = {
//         //                 //     code: 500,
//         //                 //     title: 'Internal Server Error',
//         //                 // };
//         //                 // this.router.navigate(['error']);
//         //             } else if (error.status === 408) {
//         //                 // if (!error.url.endsWith('logout'))
//         //                 //     this.apiService.logout();
//         //                 this.apiService.voidSession();
//         //                 this.dataService.setData = {
//         //                     code: 408,
//         //                     title: 'Session Time Out',
//         //                 };
//         //                 this.router.navigate(['error']);
//         //             } else if (error.status === 403) {
//         //                 // if (!error.url.endsWith('logout'))
//         //                 //     this.apiService.logout();
//         //                 this.apiService.voidSession();
//         //                 this.dataService.setData = {
//         //                     code: 403,
//         //                     title: 'Forbidden',
//         //                 };
//         //                 this.router.navigate(['error']);
//         //             } else if (error.status === 404) {
//         //                 this.dataService.setData = {
//         //                     code: 404,
//         //                     title: 'Not Found',
//         //                 };
//         //                 this.router.navigate(['error']);
//         //             } else if (error.name === 'TimeoutError') {
//         //                 this.dataService.setData = {
//         //                     code: 408,
//         //                     title: 'Request Timeout',
//         //                 };
//         //                 this.router.navigate(['error']);
//         //             }
//         //             throw error;
//         //         })
//         //     );
//     }
// }
