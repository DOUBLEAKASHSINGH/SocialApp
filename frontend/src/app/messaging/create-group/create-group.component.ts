import { Component, Input } from "@angular/core";
import {
    FormControl,
    FormGroup,
    ReactiveFormsModule,
    Validators,
} from "@angular/forms";
import { BsModalRef, BsModalService } from "ngx-bootstrap/modal";
import { SharedModule } from "../../shared/shared.module";
import { GroupApiService } from "../../services/group-api.service";
import { Router } from "@angular/router";
import { UserResponse } from "../../interfaces/user/user-response";
import { ChatGroup } from "../../interfaces/group/chat-group";

@Component({
    selector: "app-create-group",
    imports: [ReactiveFormsModule, SharedModule],
    templateUrl: "./create-group.component.html",
    styleUrl: "./create-group.component.scss",
    providers: [BsModalService],
    standalone: true,
})
export class CreateGroupComponent {
    @Input()
    modalRef?: BsModalRef;

    groupForm: FormGroup;
    isLoading: boolean = false;
    members: UserResponse[] = []; // This should be populated from a user search or selection dialog

    constructor(private groupService: GroupApiService) {
        this.groupForm = new FormGroup({
            name: new FormControl("", [
                Validators.required,
                Validators.maxLength(100),
            ]),
            bio: new FormControl("", [Validators.maxLength(255)]),
            members: new FormControl([]), // This will hold the selected members
        });
    }

    createGroup() {
        if (this.groupForm.invalid) return;

        this.isLoading = true;
        const formValue = this.groupForm.value;
        const group: ChatGroup = {
            id: "", // Let backend assign
            name: formValue.name,
            bio: formValue.bio,
            members: formValue.members,
            // admin is set to the current user in the backend
        };

        this.groupService.addGroup(group).subscribe({
            next: (res) => {
                this.isLoading = false;
                this.closeModal();
            },
            error: (err) => {
                this.isLoading = false;
                console.error("Failed to create group", err);
                this.closeModal();
            },
        });
    }

    closeModal() {
        if (this.modalRef) {
            this.modalRef.hide();
        }
    }
}
