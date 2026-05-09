import { Component, OnInit, inject } from '@angular/core';
import { FormsModule, NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { TimeoutError, finalize, timeout } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { Contact, ContactApiService, ContactRequest } from '../../core/contacts/contact-api.service';
import { ThemeService } from '../../core/theme/theme.service';
import { ConfirmDialogComponent } from '../../shared/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [FormsModule, ReactiveFormsModule, ConfirmDialogComponent],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly contactApiService = inject(ContactApiService);
  private readonly formBuilder = inject(NonNullableFormBuilder);
  private readonly themeService = inject(ThemeService);
  private readonly router = inject(Router);

  search = '';
  contacts: Contact[] = [];
  selectedContact: Contact | null = null;
  loading = false;
  saving = false;
  errorMessage = '';
  successMessage = '';
  modalOpen = false;
  modalTitle = '';
  modalMessage = '';
  modalDestructive = false;
  private pendingAction: (() => void) | null = null;

  readonly contactForm = this.formBuilder.group({
    firstName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(80), Validators.pattern(/^[\p{L}][\p{L} '\-]*$/u)]],
    lastName: ['', [Validators.maxLength(80)]],
    company: ['', [Validators.maxLength(120)]],
    email: ['', [Validators.email, Validators.maxLength(254)]],
    phone: ['', [Validators.maxLength(254)]],
    notes: ['', [Validators.maxLength(1000)]]
  });

  ngOnInit(): void {
    this.loadContacts();
  }

  get filteredContacts(): Contact[] {
    const term = this.search.trim().toLowerCase();
    if (!term) {
      return this.contacts;
    }
    return this.contacts.filter(contact => JSON.stringify(contact).toLowerCase().includes(term));
  }

  toggleTheme(): void {
    this.themeService.toggle();
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  selectContact(contact: Contact): void {
    this.selectedContact = contact;
    this.contactForm.setValue({
      firstName: contact.firstName ?? '',
      lastName: contact.lastName ?? '',
      company: contact.company ?? '',
      email: contact.emails?.[0]?.value ?? '',
      phone: contact.phones?.[0]?.value ?? '',
      notes: contact.notes ?? ''
    });
    this.errorMessage = '';
    this.successMessage = '';
  }

  startNewContact(): void {
    this.selectedContact = null;
    this.contactForm.reset();
    this.errorMessage = '';
    this.successMessage = '';
  }

  askSaveContact(): void {
    if (this.contactForm.invalid) {
      this.contactForm.markAllAsTouched();
      this.errorMessage = 'Correggi i campi evidenziati prima di salvare.';
      return;
    }

    const isUpdate = Boolean(this.selectedContact);
    this.openModal(
      isUpdate ? 'Conferma modifica contatto' : 'Conferma creazione contatto',
      isUpdate ? 'Vuoi salvare le modifiche al contatto?' : 'Vuoi creare questo contatto?',
      false,
      () => this.saveContact()
    );
  }

  askDeleteContact(contact: Contact): void {
    this.openModal('Conferma cancellazione contatto', `Vuoi cancellare ${this.displayName(contact)}?`, true, () => this.deleteContact(contact));
  }

  askEditTag(tag: string): void {
    this.openModal('Conferma modifica tag', `Vuoi modificare il tag "${tag}"?`, false);
  }

  askDeleteTag(tag: string): void {
    this.openModal('Conferma cancellazione tag', `Vuoi cancellare il tag "${tag}"?`, true);
  }

  confirmModal(): void {
    const action = this.pendingAction;
    this.closeModal();
    action?.();
  }

  closeModal(): void {
    this.modalOpen = false;
    this.pendingAction = null;
  }

  openModal(title: string, message: string, destructive: boolean, action: (() => void) | null = null): void {
    this.modalTitle = title;
    this.modalMessage = message;
    this.modalDestructive = destructive;
    this.pendingAction = action;
    this.modalOpen = true;
  }

  displayName(contact: Contact): string {
    return `${contact.firstName} ${contact.lastName ?? ''}`.trim();
  }

  primaryEmail(contact: Contact): string {
    return contact.emails?.[0]?.value ?? '-';
  }

  primaryPhone(contact: Contact): string {
    return contact.phones?.[0]?.value ?? '-';
  }

  private loadContacts(selectId?: number): void {
    this.loading = true;
    this.contactApiService.list()
      .pipe(finalize(() => this.loading = false))
      .subscribe({
        next: contacts => {
          this.contacts = contacts;
          const contactToSelect = contacts.find(contact => contact.id === selectId) ?? contacts[0] ?? null;
          if (contactToSelect) {
            this.selectContact(contactToSelect);
          } else {
            this.startNewContact();
          }
        },
        error: () => this.errorMessage = 'Non e stato possibile caricare i contatti. Riprova piu tardi.'
      });
  }

  private saveContact(): void {
    const request = this.toContactRequest();
    const selectedId = this.selectedContact?.id;
    const operation = selectedId
      ? this.contactApiService.update(selectedId, request)
      : this.contactApiService.create(request);

    this.saving = true;
    if (selectedId) {
      this.applyOptimisticUpdate(selectedId, request);
      this.successMessage = 'Modifica inviata. La lista e stata aggiornata.';
      this.errorMessage = '';
      this.saving = false;
    }

    operation.pipe(
      timeout({ first: 10000 }),
      finalize(() => this.saving = false)
    ).subscribe({
      next: savedContact => {
        this.successMessage = selectedId ? 'Contatto aggiornato correttamente.' : 'Contatto creato correttamente.';
        this.errorMessage = '';
        this.loadContacts(savedContact.id);
      },
      error: error => {
        this.errorMessage = error instanceof TimeoutError
          ? 'La risposta del server ha richiesto troppo tempo. La lista viene aggiornata per verificare il risultato.'
          : 'Non e stato possibile salvare il contatto. Verifica i dati o riprova piu tardi.';
        this.loadContacts(selectedId);
      }
    });
  }

  private deleteContact(contact: Contact): void {
    this.contactApiService.delete(contact.id).subscribe({
      next: () => {
        this.successMessage = 'Contatto cancellato correttamente.';
        this.errorMessage = '';
        this.loadContacts();
      },
      error: () => this.errorMessage = 'Non e stato possibile cancellare il contatto. Riprova piu tardi.'
    });
  }

  private toContactRequest(): ContactRequest {
    const value = this.contactForm.getRawValue();
    const phone = value.phone.trim();
    const email = value.email.trim();

    return {
      firstName: value.firstName.trim(),
      lastName: this.optionalText(value.lastName),
      company: this.optionalText(value.company),
      jobTitle: null,
      notes: this.optionalText(value.notes),
      favorite: this.selectedContact?.favorite ?? false,
      phones: phone ? [{ type: 'mobile', value: phone, primary: true }] : [],
      emails: email ? [{ type: 'email', value: email, primary: true }] : [],
      addresses: [],
      tagIds: this.selectedContact?.tags.map(tag => tag.id) ?? []
    };
  }

  private optionalText(value: string): string | null {
    const normalized = value.trim();
    return normalized ? normalized : null;
  }

  private applyOptimisticUpdate(id: number, request: ContactRequest): void {
    this.contacts = this.contacts.map(contact => {
      if (contact.id !== id) {
        return contact;
      }

      return {
        ...contact,
        firstName: request.firstName,
        lastName: request.lastName,
        company: request.company,
        jobTitle: request.jobTitle,
        notes: request.notes,
        favorite: request.favorite,
        phones: request.phones,
        emails: request.emails
      };
    });

    const updatedContact = this.contacts.find(contact => contact.id === id);
    if (updatedContact) {
      this.selectedContact = updatedContact;
      this.selectContact(updatedContact);
    }
  }
}
