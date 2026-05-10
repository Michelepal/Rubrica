import { Component, OnInit, inject } from '@angular/core';
import { NgTemplateOutlet } from '@angular/common';
import { FormsModule, NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { forkJoin, finalize, of, timeout } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { Contact, ContactApiService, ContactRequest } from '../../core/contacts/contact-api.service';
import { Tag, TagApiService, TagRequest } from '../../core/tags/tag-api.service';
import { ThemeService } from '../../core/theme/theme.service';
import { ConfirmDialogComponent } from '../../shared/confirm-dialog/confirm-dialog.component';

type PendingAction = (() => void) | null;
type PageMode = 'dashboard' | 'contacts' | 'tags';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [FormsModule, ReactiveFormsModule, RouterLink, RouterLinkActive, NgTemplateOutlet, ConfirmDialogComponent],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly contactApiService = inject(ContactApiService);
  private readonly tagApiService = inject(TagApiService);
  private readonly formBuilder = inject(NonNullableFormBuilder);
  private readonly themeService = inject(ThemeService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  pageMode: PageMode = 'dashboard';
  search = '';
  contacts: Contact[] = [];
  tags: Tag[] = [];
  expandedContactId: number | 'new' | null = null;
  selectedContact: Contact | null = null;
  selectedTag: Tag | null = null;
  loading = false;
  saving = false;
  errorMessage = '';
  successMessage = '';
  modalOpen = false;
  modalTitle = '';
  modalMessage = '';
  modalDestructive = false;
  private pendingAction: PendingAction = null;

  readonly contactForm = this.formBuilder.group({
    firstName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(80), Validators.pattern(/^[\p{L}][\p{L} '\-]*$/u)]],
    lastName: ['', [Validators.maxLength(80)]],
    company: ['', [Validators.maxLength(120)]],
    jobTitle: ['', [Validators.maxLength(120)]],
    email: ['', [Validators.email, Validators.maxLength(254)]],
    phone: ['', [Validators.maxLength(254)]],
    notes: ['', [Validators.maxLength(1000)]],
    tagIds: [[] as number[]]
  });

  readonly tagForm = this.formBuilder.group({
    name: ['', [Validators.required, Validators.maxLength(40), Validators.pattern(/^[\p{L}0-9][\p{L}0-9 _\-]*$/u)]],
    color: ['#1f7a6b', [Validators.maxLength(20)]]
  });

  ngOnInit(): void {
    this.pageMode = this.resolvePageMode();
    this.loadDashboard();
  }

  get pageTitle(): string {
    if (this.pageMode === 'contacts') {
      return 'Contatti';
    }
    if (this.pageMode === 'tags') {
      return 'Tag';
    }
    return 'Home rubrica';
  }

  get pageSubtitle(): string {
    if (this.pageMode === 'contacts') {
      return 'Consulta, crea e modifica i contatti della rubrica.';
    }
    if (this.pageMode === 'tags') {
    return 'Gestisci archivio tag e colori associati.';
    }
    return 'Gestione contatti con validazioni, conferme e dati isolati per utente.';
  }

  get showSummary(): boolean {
    return this.pageMode === 'dashboard';
  }

  get showContactsSection(): boolean {
    return this.pageMode !== 'tags';
  }

  get showTagsSection(): boolean {
    return this.pageMode !== 'contacts';
  }

  get filteredContacts(): Contact[] {
    const term = this.search.trim().toLowerCase();
    if (!term) {
      return this.contacts;
    }
    return this.contacts.filter(contact => JSON.stringify(contact).toLowerCase().includes(term));
  }

  get favoriteCount(): number {
    return this.contacts.filter(contact => contact.favorite).length;
  }

  get contactsWithoutEmail(): number {
    return this.contacts.filter(contact => !contact.emails?.length).length;
  }

  toggleTheme(): void {
    this.themeService.toggle();
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  openNewContactForm(): void {
    this.expandedContactId = 'new';
    this.selectedContact = null;
    this.contactForm.reset({ firstName: '', lastName: '', company: '', jobTitle: '', email: '', phone: '', notes: '', tagIds: [] });
    this.clearMessages();
  }

  openContactForm(contact: Contact): void {
    this.expandedContactId = contact.id;
    this.selectedContact = contact;
    this.contactForm.setValue({
      firstName: contact.firstName ?? '',
      lastName: contact.lastName ?? '',
      company: contact.company ?? '',
      jobTitle: contact.jobTitle ?? '',
      email: contact.emails?.[0]?.value ?? '',
      phone: contact.phones?.[0]?.value ?? '',
      notes: contact.notes ?? '',
      tagIds: contact.tags.map(tag => tag.id)
    });
    this.clearMessages();
  }

  closeContactForm(): void {
    this.expandedContactId = null;
    this.selectedContact = null;
    this.contactForm.reset({ firstName: '', lastName: '', company: '', jobTitle: '', email: '', phone: '', notes: '', tagIds: [] });
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

  selectTag(tag: Tag): void {
    this.selectedTag = tag;
    this.tagForm.setValue({ name: tag.name, color: tag.color ?? '#1f7a6b' });
    this.clearMessages();
  }

  newTag(): void {
    this.selectedTag = null;
    this.tagForm.reset({ name: '', color: '#1f7a6b' });
    this.clearMessages();
  }

  askSaveTag(): void {
    if (this.tagForm.invalid) {
      this.tagForm.markAllAsTouched();
      this.errorMessage = 'Correggi il form del tag prima di salvare.';
      return;
    }

    this.openModal(
      this.selectedTag ? 'Conferma modifica tag' : 'Conferma creazione tag',
      this.selectedTag ? 'Vuoi salvare le modifiche al tag?' : 'Vuoi creare questo tag?',
      false,
      () => this.saveTag()
    );
  }

  askDeleteTag(tag: Tag): void {
    this.openModal('Conferma cancellazione tag', `Vuoi cancellare il tag "${tag.name}"?`, true, () => this.deleteTag(tag));
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

  openModal(title: string, message: string, destructive: boolean, action: PendingAction = null): void {
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

  private loadDashboard(selectId?: number): void {
    this.loading = true;
    forkJoin({
      contacts: this.showContactsSection ? this.contactApiService.list() : of([]),
      tags: this.tagApiService.list()
    }).pipe(finalize(() => this.loading = false)).subscribe({
      next: result => {
        this.contacts = result.contacts;
        this.tags = result.tags;
        const contactToKeepOpen = selectId ? this.contacts.find(contact => contact.id === selectId) : null;
        if (contactToKeepOpen) {
          this.openContactForm(contactToKeepOpen);
        }
      },
      error: () => this.errorMessage = 'Non e stato possibile caricare la rubrica. Riprova piu tardi.'
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
    }

    operation.pipe(timeout({ first: 10000 }), finalize(() => this.saving = false)).subscribe({
      next: savedContact => {
        this.successMessage = selectedId ? 'Contatto aggiornato correttamente.' : 'Contatto creato correttamente.';
        this.errorMessage = '';
        this.closeContactForm();
        this.loadDashboard(selectedId ? undefined : savedContact.id);
      },
      error: () => {
        this.errorMessage = 'Non e stato possibile salvare il contatto. Verifica i dati o riprova piu tardi.';
        this.loadDashboard(selectedId);
      }
    });
  }

  private deleteContact(contact: Contact): void {
    this.contactApiService.delete(contact.id).subscribe({
      next: () => {
        this.successMessage = 'Contatto cancellato correttamente.';
        this.errorMessage = '';
        this.closeContactForm();
        this.loadDashboard();
      },
      error: () => this.errorMessage = 'Non e stato possibile cancellare il contatto. Riprova piu tardi.'
    });
  }

  private saveTag(): void {
    const request: TagRequest = this.tagForm.getRawValue();
    const operation = this.selectedTag
      ? this.tagApiService.update(this.selectedTag.id, request)
      : this.tagApiService.create(request);

    operation.subscribe({
      next: () => {
        this.successMessage = this.selectedTag ? 'Tag aggiornato correttamente.' : 'Tag creato correttamente.';
        this.errorMessage = '';
        this.newTag();
        this.loadDashboard(this.selectedContact?.id);
      },
      error: () => this.errorMessage = 'Non e stato possibile salvare il tag. Verifica i dati o riprova piu tardi.'
    });
  }

  private deleteTag(tag: Tag): void {
    this.tagApiService.delete(tag.id).subscribe({
      next: () => {
        this.successMessage = 'Tag cancellato correttamente.';
        this.errorMessage = '';
        this.newTag();
        this.loadDashboard(this.selectedContact?.id);
      },
      error: () => this.errorMessage = 'Non e stato possibile cancellare il tag. Potrebbe essere associato a un contatto.'
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
      jobTitle: this.optionalText(value.jobTitle),
      notes: this.optionalText(value.notes),
      favorite: this.selectedContact?.favorite ?? false,
      phones: phone ? [{ type: 'mobile', value: phone, primary: true }] : [],
      emails: email ? [{ type: 'email', value: email, primary: true }] : [],
      addresses: [],
      tagIds: value.tagIds
    };
  }

  private optionalText(value: string): string | null {
    const normalized = value.trim();
    return normalized ? normalized : null;
  }

  private applyOptimisticUpdate(id: number, request: ContactRequest): void {
    const selectedTags = this.tags.filter(tag => request.tagIds.includes(tag.id));
    this.contacts = this.contacts.map(contact => contact.id === id ? {
      ...contact,
      firstName: request.firstName,
      lastName: request.lastName,
      company: request.company,
      jobTitle: request.jobTitle,
      notes: request.notes,
      favorite: request.favorite,
      phones: request.phones,
      emails: request.emails,
      tags: selectedTags
    } : contact);
  }

  private clearMessages(): void {
    this.errorMessage = '';
    this.successMessage = '';
  }

  private resolvePageMode(): PageMode {
    const path = this.route.snapshot.routeConfig?.path;
    if (path === 'contacts' || path === 'tags') {
      return path;
    }
    return 'dashboard';
  }
}
