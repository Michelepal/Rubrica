import { Component } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { NgTemplateOutlet } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { ConfirmDialogComponent } from '../../shared/confirm-dialog/confirm-dialog.component';
import { ContactTableComponent } from '../../shared/contact-table/contact-table.component';
import { ContactToolsComponent } from '../../shared/contact-tools/contact-tools.component';
import { HomeComponent } from '../home/home.component';

@Component({
  selector: 'app-contacts',
  standalone: true,
  imports: [FormsModule, ReactiveFormsModule, RouterLink, RouterLinkActive, NgTemplateOutlet, ConfirmDialogComponent, ContactTableComponent, ContactToolsComponent],
  templateUrl: './contacts.component.html',
  styleUrl: '../home/home.component.css'
})
export class ContactsComponent extends HomeComponent {}
