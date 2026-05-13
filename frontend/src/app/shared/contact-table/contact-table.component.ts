import { NgTemplateOutlet } from '@angular/common';
import { Component, EventEmitter, Input, Output, TemplateRef } from '@angular/core';
import { Contact } from '../../core/contacts/contact-api.service';

@Component({
  selector: 'app-contact-table',
  standalone: true,
  imports: [NgTemplateOutlet],
  templateUrl: './contact-table.component.html',
  styleUrl: './contact-table.component.css'
})
export class ContactTableComponent {
  @Input({ required: true }) contacts: Contact[] = [];
  @Input() loading = false;
  @Input() expandedContactId: number | 'new' | null = null;
  @Input() formTemplate: TemplateRef<unknown> | null = null;
  @Input() hasPreviousPage = false;
  @Input() hasNextPage = false;
  @Input() pageLabel = '1';
  @Input() totalElements = 0;
  @Input() showCreatedAt = false;

  @Output() favoriteToggle = new EventEmitter<Contact>();
  @Output() editToggle = new EventEmitter<Contact>();
  @Output() deleteContact = new EventEmitter<Contact>();
  @Output() previousPage = new EventEmitter<void>();
  @Output() nextPage = new EventEmitter<void>();

  displayName(contact: Contact): string {
    return [contact.lastName, contact.firstName].filter(Boolean).join(' ');
  }

  createdAtLabel(contact: Contact): string {
    if (!contact.createdAt) {
      return '-';
    }
    return new Intl.DateTimeFormat('it-IT', { day: '2-digit', month: '2-digit', year: 'numeric' }).format(new Date(contact.createdAt));
  }

  contactSubtitle(contact: Contact): string {
    return [contact.jobTitle, contact.company].filter(Boolean).join(', ');
  }

  primaryEmail(contact: Contact): string {
    return contact.emails?.[0]?.value ?? '-';
  }

  primaryPhone(contact: Contact): string {
    return contact.phones?.[0]?.value ?? '-';
  }

  needsReview(contact: Contact): boolean {
    return !contact.emails?.length || !contact.phones?.length;
  }

  isEditing(contact: Contact): boolean {
    return this.expandedContactId === contact.id;
  }

  toggleEdit(contact: Contact): void {
    this.editToggle.emit(contact);
  }
}
