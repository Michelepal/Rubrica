import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { NgTemplateOutlet } from '@angular/common';
import { FormsModule, NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { distinctUntilChanged, finalize, forkJoin, of, timeout } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { Contact, ContactApiService, ContactRequest } from '../../core/contacts/contact-api.service';
import { ErrorService } from '../../core/errors/error.service';
import { Tag, TagApiService, TagRequest } from '../../core/tags/tag-api.service';
import { ThemeMode, ThemeService } from '../../core/theme/theme.service';
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
  private readonly changeDetectorRef = inject(ChangeDetectorRef);
  private readonly errorService = inject(ErrorService);

  pageMode: PageMode = 'dashboard';
  search = '';
  favoriteFilter: 'all' | 'favorites' = 'all';
  filterTagId: number | null = null;
  tagColorFilter: string | null = null;
  tagColorMenuOpen = false;
  readonly pageSize = 10;
  contactPage = 0;
  contactTotalPages = 0;
  contactTotalElements = 0;
  tagPage = 0;
  tagTotalPages = 0;
  tagTotalElements = 0;
  contacts: Contact[] = [];
  tags: Tag[] = [];
  expandedContactId: number | 'new' | null = null;
  selectedContact: Contact | null = null;
  selectedTag: Tag | null = null;
  showTagForm = false;
  loading = false;
  saving = false;
  errorMessage = '';
  successMessage = '';
  private feedbackTimer: ReturnType<typeof setTimeout> | null = null;
  modalOpen = false;
  modalTitle = '';
  modalMessage = '';
  modalDestructive = false;
  themeMode: ThemeMode = 'light';
  private pendingAction: PendingAction = null;

  readonly contactForm = this.formBuilder.group({
    firstName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(80), Validators.pattern(/^[\p{L}][\p{L} '\-]*$/u)]],
    lastName: ['', [Validators.maxLength(80)]],
    company: ['', [Validators.maxLength(120)]],
    jobTitle: ['', [Validators.maxLength(120)]],
    email: ['', [Validators.email, Validators.maxLength(254)]],
    phone: ['', [Validators.maxLength(30)]],
    notes: ['', [Validators.maxLength(1000)]],
    tagIds: [[] as number[]]
  });

  readonly tagForm = this.formBuilder.group({
    name: ['', [Validators.required, Validators.maxLength(40), Validators.pattern(/^[\p{L}0-9][\p{L}0-9 _\-]*$/u)]],
    color: ['#1f7a6b', [Validators.maxLength(20)]]
  });
  readonly presetTagColors = ['#1f7a6b', '#2563eb', '#7c3aed', '#db2777', '#dc2626', '#ea580c', '#ca8a04', '#16a34a'];

  ngOnInit(): void {
    this.themeMode = this.themeService.current();
    this.route.url.pipe(distinctUntilChanged((previous, current) => previous.join('/') === current.join('/'))).subscribe(() => {
      this.pageMode = this.resolvePageMode();
      this.loadDashboard();
    });
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

  get searchPlaceholder(): string {
    if (this.pageMode === 'tags') {
      return 'Cerca tag per nome';
    }
    if (this.pageMode === 'contacts') {
      return 'Cerca contatti per nome, azienda, email o telefono';
    }
    return 'Cerca nella rubrica per nome, azienda, email, telefono o tag';
  }

  get showSummary(): boolean {
    return this.pageMode === 'dashboard';
  }

  get showContactsSection(): boolean {
    return this.pageMode !== 'tags';
  }

  get showTagsSection(): boolean {
    return this.pageMode === 'tags';
  }

  get filteredContacts(): Contact[] {
    return this.contacts;
  }

  get filteredTags(): Tag[] {
    const term = this.search.trim().toLowerCase();
    return this.tags.filter(tag => {
      const matchesName = !term || tag.name.toLowerCase().includes(term);
      const matchesColor = !this.tagColorFilter || tag.color === this.tagColorFilter;
      return matchesName && matchesColor;
    });
  }

  get favoriteCount(): number {
    return this.contacts.filter(contact => contact.favorite).length;
  }

  get contactsWithoutEmail(): number {
    return this.contacts.filter(contact => this.needsReview(contact)).length;
  }

  get hasPreviousContactPage(): boolean {
    return this.contactPage > 0;
  }

  get hasNextContactPage(): boolean {
    return this.contactPage + 1 < this.contactTotalPages;
  }

  get hasPreviousTagPage(): boolean {
    return this.tagPage > 0;
  }

  get hasNextTagPage(): boolean {
    return this.tagPage + 1 < this.tagTotalPages;
  }

  get contactPageLabel(): string {
    return this.contactTotalPages ? `${this.contactPage + 1} / ${this.contactTotalPages}` : '0 / 0';
  }

  get tagPageLabel(): string {
    return this.tagTotalPages ? `${this.tagPage + 1} / ${this.tagTotalPages}` : '0 / 0';
  }

  get tagColorOptions(): { value: string; label: string }[] {
    const colors = new Set(this.tags.map(tag => tag.color).filter((color): color is string => Boolean(color)));
    return Array.from(colors).map(color => ({ value: color, label: this.colorLabel(color) }));
  }

  toggleTheme(): void {
    try {
      this.themeMode = this.themeService.toggle();
    } catch (error) {
      console.error('Cambio tema fallito.', { currentTheme: this.themeMode, error });
      this.errorMessage = 'Non è stato possibile cambiare tema.';
    }
  }

  get themeToggleLabel(): string {
    return this.themeMode === 'dark' ? 'Passa al tema chiaro' : 'Passa al tema scuro';
  }

  logout(): void {
    try {
      this.authService.logout();
      this.router.navigate(['/login']).catch(error => console.error('Navigazione al login fallita dopo logout.', error));
    } catch (error) {
      console.error('Logout fallito.', error);
      this.errorMessage = 'Non è stato possibile completare il logout.';
    }
  }

  openNewContactForm(): void {
    this.expandedContactId = 'new';
    this.selectedContact = null;
    this.contactForm.reset({ firstName: '', lastName: '', company: '', jobTitle: '', email: '', phone: '', notes: '', tagIds: [] });
    this.clearMessages();
  }

  onSearchChanged(): void {
    this.contactPage = 0;
    this.tagPage = 0;
    this.loadDashboard();
  }

  onTagFiltersChanged(): void {
    this.tagPage = 0;
    this.tagColorMenuOpen = false;
  }

  toggleTagColorMenu(): void {
    this.tagColorMenuOpen = !this.tagColorMenuOpen;
  }

  chooseTagColorFilter(color: string | null): void {
    this.tagColorFilter = color;
    this.onTagFiltersChanged();
  }

  onContactFiltersChanged(): void {
    this.contactPage = 0;
    this.loadDashboard();
  }

  previousContactPage(): void {
    if (!this.hasPreviousContactPage) {
      return;
    }
    this.contactPage -= 1;
    this.loadDashboard();
  }

  nextContactPage(): void {
    if (!this.hasNextContactPage) {
      return;
    }
    this.contactPage += 1;
    this.loadDashboard();
  }

  previousTagPage(): void {
    if (!this.hasPreviousTagPage) {
      return;
    }
    this.tagPage -= 1;
    this.loadDashboard();
  }

  nextTagPage(): void {
    if (!this.hasNextTagPage) {
      return;
    }
    this.tagPage += 1;
    this.loadDashboard();
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

    const value = this.contactForm.getRawValue();
    if (!value.email.trim() && !value.phone.trim()) {
      this.openModal(
        'Contatto da verificare',
        'Email e telefono non sono stati inseriti. Il contatto sara segnalato come da verificare. Vuoi continuare?',
        false,
        () => this.saveContact()
      );
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

  askToggleFavorite(contact: Contact): void {
    const displayName = this.displayName(contact);
    this.openModal(
      contact.favorite ? 'Rimuovi preferito' : 'Aggiungi preferito',
      contact.favorite
        ? `Vuoi rimuovere ${displayName} dai preferiti?`
        : `Vuoi inserire ${displayName} tra i preferiti?`,
      false,
      () => this.toggleFavorite(contact)
    );
  }

  private toggleFavorite(contact: Contact): void {
    const request = this.toContactRequestFromContact({ ...contact, favorite: !contact.favorite });
    this.contactApiService.update(contact.id, request).subscribe({
      next: savedContact => {
        this.upsertContact(savedContact);
        this.setSuccess(savedContact.favorite ? 'Contatto aggiunto ai preferiti.' : 'Contatto rimosso dai preferiti.');
        this.loadDashboard();
      },
      error: error => {
        console.error('Aggiornamento preferito fallito.', { contactId: contact.id, error });
        this.setCrudError('aggiornare il preferito', error);
      }
    });
  }

  needsReview(contact: Contact): boolean {
    return !contact.emails?.length || !contact.phones?.length;
  }

  selectTag(tag: Tag): void {
    this.selectedTag = tag;
    this.showTagForm = true;
    this.tagForm.setValue({ name: tag.name, color: tag.color ?? '#1f7a6b' });
    this.clearMessages();
  }

  newTag(): void {
    this.selectedTag = null;
    this.showTagForm = true;
    this.tagForm.reset({ name: '', color: '#1f7a6b' });
    this.clearMessages();
  }

  closeTagForm(): void {
    this.selectedTag = null;
    this.showTagForm = false;
    this.tagForm.reset({ name: '', color: '#1f7a6b' });
  }

  chooseTagColor(color: string): void {
    this.tagForm.controls.color.setValue(color);
  }

  colorLabel(color: string | null): string {
    const labels: Record<string, string> = {
      '#1f7a6b': 'Verde petrolio',
      '#2563eb': 'Blu',
      '#7c3aed': 'Viola',
      '#db2777': 'Rosa',
      '#dc2626': 'Rosso',
      '#ea580c': 'Arancione',
      '#ca8a04': 'Oro',
      '#16a34a': 'Verde'
    };
    return color ? labels[color.toLowerCase()] ?? 'Colore personalizzato' : 'Senza colore';
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
    try {
      action?.();
    } catch (error) {
      console.error('Esecuzione azione confermata fallita.', { modalTitle: this.modalTitle, error });
      this.errorMessage = "Non è stato possibile completare l'operazione richiesta.";
    }
  }

  closeModal(): void {
    this.modalOpen = false;
    this.pendingAction = null;
  }

  dismissError(): void {
    this.errorMessage = '';
    this.clearFeedbackTimer();
  }

  dismissSuccess(): void {
    this.successMessage = '';
    this.clearFeedbackTimer();
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

  contactSubtitle(contact: Contact): string {
    return [contact.jobTitle, contact.company].filter(Boolean).join(', ');
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
      contacts: this.showContactsSection ? this.contactApiService.list({
        page: this.contactPage,
        size: this.pageSize,
        q: this.search,
        tagId: this.filterTagId,
        favorite: this.favoriteFilter === 'favorites' ? true : null
      }) : of({ content: [], page: 0, size: this.pageSize, totalElements: 0, totalPages: 0 }),
      tags: this.tagApiService.list(this.tagPage, this.pageSize)
    }).pipe(finalize(() => {
      this.loading = false;
      this.changeDetectorRef.detectChanges();
    })).subscribe({
      next: result => {
        this.contacts = result.contacts.content;
        this.contactPage = result.contacts.page;
        this.contactTotalPages = result.contacts.totalPages;
        this.contactTotalElements = result.contacts.totalElements;
        this.tags = result.tags.content;
        this.tagPage = result.tags.page;
        this.tagTotalPages = result.tags.totalPages;
        this.tagTotalElements = result.tags.totalElements;
        const contactToKeepOpen = selectId ? this.contacts.find(contact => contact.id === selectId) : null;
        if (contactToKeepOpen) {
          this.openContactForm(contactToKeepOpen);
        }
      },
      error: error => {
        console.error('Caricamento dashboard fallito.', { pageMode: this.pageMode, selectId, error });
        this.setCrudError('caricare la rubrica', error);
        this.changeDetectorRef.detectChanges();
      }
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
        this.setSuccess(selectedId ? 'Contatto aggiornato correttamente.' : 'Contatto creato correttamente.');
        this.upsertContact(savedContact);
        this.closeContactForm();
        this.loadDashboard();
      },
      error: error => {
        console.error('Salvataggio contatto fallito.', { selectedId, request, error });
        this.setCrudError(selectedId ? 'modificare il contatto' : 'creare il contatto', error);
        this.loadDashboard();
      }
    });
  }

  private deleteContact(contact: Contact): void {
    this.contactApiService.delete(contact.id).subscribe({
      next: () => {
        this.setSuccess('Contatto cancellato correttamente.');
        this.closeContactForm();
        this.loadDashboard();
      },
      error: error => {
        console.error('Cancellazione contatto fallita.', { contactId: contact.id, error });
        this.setCrudError('cancellare il contatto', error);
      }
    });
  }

  private saveTag(): void {
    const request: TagRequest = this.tagForm.getRawValue();
    const operation = this.selectedTag
      ? this.tagApiService.update(this.selectedTag.id, request)
      : this.tagApiService.create(request);

    operation.subscribe({
      next: () => {
        this.setSuccess(this.selectedTag ? 'Tag aggiornato correttamente.' : 'Tag creato correttamente.');
        this.closeTagForm();
        this.loadDashboard(this.selectedContact?.id);
      },
      error: error => {
        console.error('Salvataggio tag fallito.', { selectedTagId: this.selectedTag?.id, request, error });
        this.setCrudError(this.selectedTag ? 'modificare il tag' : 'creare il tag', error);
      }
    });
  }

  private deleteTag(tag: Tag): void {
    this.tagApiService.delete(tag.id).subscribe({
      next: () => {
        this.setSuccess('Tag cancellato correttamente.');
        this.closeTagForm();
        this.loadDashboard(this.selectedContact?.id);
      },
      error: error => {
        console.error('Cancellazione tag fallita.', { tagId: tag.id, error });
        this.setCrudError('cancellare il tag', error);
      }
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

  private toContactRequestFromContact(contact: Contact): ContactRequest {
    return {
      firstName: contact.firstName,
      lastName: contact.lastName,
      company: contact.company,
      jobTitle: contact.jobTitle,
      notes: contact.notes,
      favorite: contact.favorite,
      phones: contact.phones,
      emails: contact.emails,
      addresses: [],
      tagIds: contact.tags.map(tag => tag.id)
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

  private upsertContact(savedContact: Contact): void {
    const index = this.contacts.findIndex(contact => contact.id === savedContact.id);
    if (index === -1) {
      this.contacts = [savedContact, ...this.contacts];
      return;
    }
    this.contacts = this.contacts.map(contact => contact.id === savedContact.id ? savedContact : contact);
  }

  private clearMessages(): void {
    this.errorMessage = '';
    this.successMessage = '';
    this.clearFeedbackTimer();
  }

  private setCrudError(action: string, error: unknown): void {
    const apiError = this.errorService.toMessage(error);
    this.successMessage = '';
    this.errorMessage = `Non è stato possibile ${action}. ${apiError.message}`;
    this.scheduleFeedbackDismiss();
  }

  private setSuccess(message: string): void {
    this.errorMessage = '';
    this.successMessage = message;
    this.scheduleFeedbackDismiss();
  }

  private scheduleFeedbackDismiss(): void {
    this.clearFeedbackTimer();
    this.feedbackTimer = setTimeout(() => {
      this.errorMessage = '';
      this.successMessage = '';
      this.feedbackTimer = null;
      this.changeDetectorRef.detectChanges();
    }, 3500);
  }

  private clearFeedbackTimer(): void {
    if (this.feedbackTimer) {
      clearTimeout(this.feedbackTimer);
      this.feedbackTimer = null;
    }
  }

  private resolvePageMode(): PageMode {
    const path = this.route.snapshot.routeConfig?.path;
    if (path === 'contacts' || path === 'tags') {
      return path;
    }
    return 'dashboard';
  }
}
