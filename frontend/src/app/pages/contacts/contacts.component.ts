import { Component } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { NgTemplateOutlet } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { ConfirmDialogComponent } from '../../shared/confirm-dialog/confirm-dialog.component';
import { HomeComponent } from '../home/home.component';

@Component({
  selector: 'app-contacts',
  standalone: true,
  imports: [FormsModule, ReactiveFormsModule, RouterLink, RouterLinkActive, NgTemplateOutlet, ConfirmDialogComponent],
  templateUrl: './contacts.component.html',
  styleUrl: '../home/home.component.css'
})
export class ContactsComponent extends HomeComponent {}
