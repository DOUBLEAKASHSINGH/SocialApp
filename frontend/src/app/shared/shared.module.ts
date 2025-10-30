import {NgModule} from '@angular/core';
import {CommonModule, NgOptimizedImage} from '@angular/common';
// import {BrowserModule} from '@angular/platform-browser';
import {AlertModule} from 'ngx-bootstrap/alert';
import {TooltipModule} from 'ngx-bootstrap/tooltip';
import {PaginationModule} from 'ngx-bootstrap/pagination';
import {BsDropdownModule} from 'ngx-bootstrap/dropdown';
// import {BrowserAnimationsModule} from '@angular/platform-browser/animations';

@NgModule({
    declarations: [],
    imports: [
        CommonModule,
        // NgOptimizedImage,
        // BrowserAnimationsModule,
        AlertModule.forRoot(), // Configure the ngx-bootstrap module with forRoot()
        TooltipModule.forRoot(),
        PaginationModule.forRoot(),
        BsDropdownModule.forRoot(),
    ],
    exports: [
        CommonModule,
        // NgOptimizedImage,
        // BrowserAnimationsModule,
        AlertModule,
        TooltipModule,
        PaginationModule,
        BsDropdownModule,
    ]
})
export class SharedModule {
}
