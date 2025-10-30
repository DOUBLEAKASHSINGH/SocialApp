import { animate, style, transition, trigger } from "@angular/animations";
import { SharedModule } from "../../shared/shared.module";
import { UserResponse } from "../../interfaces/user/user-response";
import { catchError, of, tap } from "rxjs";
import { RelationApiService } from "../../services/relation-api.service";
import { UserRelations } from "../../interfaces/user/user-relations";
import { RelationStatus } from "../../interfaces/enum/relation-status";
import { FormsModule } from "@angular/forms";
import { Component, Input, OnChanges, SimpleChanges } from "@angular/core";
import { BsModalService } from "ngx-bootstrap/modal";

@Component({
    selector: "app-view-user",
    imports: [SharedModule, FormsModule],
    templateUrl: "./view-user.component.html",
    styleUrl: "./view-user.component.scss",
    providers: [BsModalService],
    animations: [
        trigger("pageChange", [
            transition(":enter", [
                style({ opacity: 0 }),
                animate("1000ms", style({ opacity: 1 })),
            ]),
        ]),
    ],
    standalone: true,
})
export class ViewUserComponent implements OnChanges {
    @Input()
    user: UserResponse | null = null;
    @Input()
    target: UserResponse | null = null;

    errorLoadingData: boolean = false;
    isLoading: boolean = false;

    relationStatus: RelationStatus = RelationStatus.UNKNOWN;
    relation!: UserRelations;

    constructor(private relationApiService: RelationApiService) {}

    ngOnChanges(changes: SimpleChanges): void {
        if (
            this.user &&
            this.target //&&
            // (changes["target"] || changes["user"])
        ) {
            console.log("view user", this.target);
            this.fetchRelation();
        }
    }

    get isPageLoading(): boolean {
        return this.isLoading;
    }

    get isBlocked(): boolean {
        return this.relationStatus === RelationStatus.BLOCKED;
    }

    get isFriend(): boolean {
        return this.relationStatus === RelationStatus.ACCEPTED;
    }

    get isRequestPending(): boolean {
        return this.relationStatus === RelationStatus.PENDING;
    }

    fetchRelation() {
        if (!this.user || !this.target) {
            return;
        }
        this.isLoading = true;
        this.relationApiService
            .getRelation(this.target.email)
            .pipe(
                tap((relation) => {
                    console.log("Relation:", relation);
                    this.relationStatus = relation.status;
                    this.relation = relation;
                    this.isLoading = false;
                }),
                catchError((error) => {
                    this.isLoading = false;
                    this.relationStatus = RelationStatus.UNKNOWN;
                    // this.errorLoadingData = true;
                    // console.log("Relation change error");
                    return of(false);
                })
            )
            .subscribe();
    }

    blockUser() {
        if (!this.user || !this.target) {
            return;
        }
        console.log("blockUser");
        let newRelation: UserRelations = {
            user: this.user,
            friend: this.target,
            status: RelationStatus.BLOCKED,
        };
        this.handleRequest(newRelation);
    }

    addFriend() {
        if (!this.user || !this.target) {
            return;
        }
        console.log("addFriend");
        let newRelation: UserRelations = {
            user: this.user,
            friend: this.target,
            status: RelationStatus.PENDING,
        };
        this.handleRequest(newRelation);
    }

    acceptRequest() {
        if (!this.user || !this.target) {
            return;
        }
        console.log("acceptRequest");
        let newRelation: UserRelations = {
            user: this.user,
            friend: this.target,
            status: RelationStatus.ACCEPTED,
        };

        this.handleRequest(newRelation);
    }

    removeFriend() {
        console.log("removeFriend");
        if (!this.user || !this.target) {
            return;
        }
        let newRelation: UserRelations = {
            user: this.user,
            friend: this.target,
            status: RelationStatus.UNKNOWN,
        };
        this.handleRequest(newRelation);
    }

    rejectRequest() {
        if (!this.user || !this.target) {
            return;
        }
        console.log("rejectRequest");
        let newRelation: UserRelations = {
            user: this.user,
            friend: this.target,
            status: RelationStatus.REJECTED,
        };
        this.handleRequest(newRelation);
    }

    handleRequest(relation: UserRelations) {
        this.isLoading = true;
        this.relationApiService
            .editRelation(relation)
            .pipe(
                tap(() => {
                    console.log("Change relation success");
                    this.fetchRelation();
                    this.isLoading = false;
                }),
                catchError((error) => {
                    this.isLoading = false;
                    this.errorLoadingData = true;
                    console.log("Relation change error");
                    return of(false);
                })
            )
            .subscribe();
    }
}
