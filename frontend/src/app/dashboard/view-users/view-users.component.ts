import {Component, Input, OnChanges, SimpleChanges} from '@angular/core';
import {BsModalService} from 'ngx-bootstrap/modal';
import {animate, style, transition, trigger} from '@angular/animations';
import {SharedModule} from '../../shared/shared.module';
import {UserResponse} from '../../interfaces/user/user-response';
import {catchError, Observable, of, tap} from 'rxjs';
import {PageResponse} from '../../interfaces/page-response';
import {PageRequest} from '../../interfaces/page-request';
import {RelationApiService} from '../../services/relation-api.service';
import {UserRelations} from '../../interfaces/user/user-relations';
import {RelationStatus} from '../../interfaces/enum/relation-status';
import {FormsModule} from '@angular/forms';

@Component({
    selector: 'app-view-users',
    imports: [
        SharedModule,
        FormsModule
    ],
    templateUrl: './view-users.component.html',
    styleUrl: './view-users.component.scss',
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
export class ViewUsersComponent implements OnChanges {
    @Input()
    userCategory: String | null = null; // "friends", "blocked", "received", "sent"
    @Input()
    user: UserResponse | null = null;

    totalItems = 0; // Total number of items.
    currentPage = 1; // Current active page.
    itemsPerPage = 10; // Number of items per page.
    maxPageDisplay = 5; // Number of page numbers to display at a time.

    relationPageResponse!: PageResponse<UserRelations>;
    relationPageRequest!: PageRequest;
    relationList: UserRelations[];

    errorLoadingData: boolean = false;
    isRelationListLoading: boolean = false;

    constructor(private relationApiService: RelationApiService) {
        this.relationList = [];
        this.relationPageRequest = {
            page: this.currentPage,
            pageSize: this.itemsPerPage,
        };
    }

    get isPageLoading(): boolean {
        return this.isRelationListLoading;
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (this.user && this.userCategory && changes['userCategory']) {
            console.log(changes['userCategory']);
            this.fetchRelationList();
        }
    }

    targetUser(relation: UserRelations): UserResponse {
        if (relation.user.email === this.user?.email) {
            return relation.friend;
        }
        return relation.user;
    }

    /**
     * Page change event handler.
     *
     * @param event
     */
    pageChanged(event: any) {
        console.log('page changed', event);
        this.currentPage = event.page;
        // this.fetchRelationList();
    }

    /**
     * Method to fetch Relation list from backend.
     */
    fetchRelationList(): void {
        this.isRelationListLoading = true;
        this.relationPageRequest.page = this.currentPage;
        console.log('Fetching relation page: ', this.userCategory);

        let list: Observable<PageResponse<UserRelations>>;
        if (this.userCategory === 'friends') {
            list = this.relationApiService.getFriends(this.relationPageRequest)
        } else if (this.userCategory === 'blocked') {
            list = this.relationApiService.getBlocked(this.relationPageRequest)
        } else if (this.userCategory === 'received') {
            list = this.relationApiService.getReceived(this.relationPageRequest)
        } else if (this.userCategory === 'sent') {
            list = this.relationApiService.getSent(this.relationPageRequest)
        } else {
            console.log('Invalid user category');
            return;
        }
        list.pipe(
                tap((data) => {
                    console.log('Relation data =', data);
                    this.relationPageResponse = data;
                    this.relationList = this.relationPageResponse.items;
                    this.totalItems = this.relationPageResponse.totalSize;
                    console.log('Relation =', this.relationList);
                    setTimeout(() => {
                        this.isRelationListLoading = false;
                    }, 300);
                }),
                catchError((error) => {
                    this.isRelationListLoading = false;
                    this.errorLoadingData = true;
                    console.log('Relation list Fetch error');
                    return of(false);
                })
            )
            .subscribe();
    }

    /**
     * Method to find min of two numbers.
     *
     * @param a
     * @param b
     * @returns
     */
    min(a: number, b: number) {
        return a < b ? a : b;
    }

    acceptRequest(relation: UserRelations) {
        console.log('acceptRequest');
        this.isRelationListLoading = true;
        let newRelation: UserRelations = {...relation};
        newRelation.status = RelationStatus.ACCEPTED;

        this.handleRequest(this.relationApiService.editRelation(newRelation));
    }

    rejectRequest(relation: UserRelations) {
        console.log('rejectRequest');
        this.isRelationListLoading = true;
        let newRelation: UserRelations = {...relation};
        newRelation.status = RelationStatus.REJECTED;
        this.handleRequest(this.relationApiService.editRelation(newRelation));
    }

    removeBlocked(relation: UserRelations) {
        console.log('removeBlocked');
        this.isRelationListLoading = true;
        let newRelation: UserRelations = {...relation};
        newRelation.status = RelationStatus.UNKNOWN;
        this.handleRequest(this.relationApiService.editRelation(newRelation));
    }

    removeFriend(relation: UserRelations) {
        console.log('removeFriend');
        this.isRelationListLoading = true;
        let newRelation: UserRelations = {...relation};
        newRelation.status = RelationStatus.UNKNOWN;
        this.handleRequest(this.relationApiService.editRelation(newRelation));
    }

    handleRequest(req: Observable<boolean>) {
        req.pipe(
            tap((data) => {
                console.log('Change relation success');
                this.isRelationListLoading = false;
                this.fetchRelationList();
            }),
            catchError((error) => {
                this.isRelationListLoading = false;
                this.errorLoadingData = true;
                console.log('Relation change error');
                return of(false);
            })
        )
            .subscribe();
    }
}
