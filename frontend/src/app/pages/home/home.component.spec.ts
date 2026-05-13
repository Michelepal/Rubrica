import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { provideRouter } from '@angular/router';
import { BehaviorSubject, of, throwError } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { Contact, ContactApiService } from '../../core/contacts/contact-api.service';
import { ErrorService } from '../../core/errors/error.service';
import { Tag, TagApiService } from '../../core/tags/tag-api.service';
import { ThemeService } from '../../core/theme/theme.service';
import { HomeComponent } from './home.component';

describe('HomeComponent', () => {
  let fixture: ComponentFixture<HomeComponent>;
  let component: HomeComponent;
  let contactApiService: jasmine.SpyObj<ContactApiService>;
  let tagApiService: jasmine.SpyObj<TagApiService>;
  let authService: jasmine.SpyObj<AuthService>;
  let themeService: jasmine.SpyObj<ThemeService>;
  let router: Router;
  let navigateSpy: jasmine.Spy;
  let routeUrl$: BehaviorSubject<unknown[]>;
  let routeStub: { url: BehaviorSubject<unknown[]>; snapshot: { routeConfig: { path: string } } };

  const contact: Contact = {
    id: 7,
    firstName: 'Laura',
    lastName: 'Bianchi',
    company: 'Northwind',
    jobTitle: 'Manager',
    notes: null,
    createdAt: '2026-05-10T08:30:00.000Z',
    favorite: false,
    phones: [{ type: 'mobile', value: '1234567890', primary: true }],
    emails: [{ type: 'email', value: 'laura@example.local', primary: true }],
    tags: [{ id: 2, name: 'Lavoro', color: '#2563eb' }]
  };

  const tag: Tag = { id: 2, name: 'Lavoro', color: '#2563eb' };
  const contactPage = (content: Contact[]) => of({ content, page: 0, size: 10, totalElements: content.length, totalPages: content.length ? 1 : 0 });
  const tagPage = (content: Tag[]) => of({ content, page: 0, size: 10, totalElements: content.length, totalPages: content.length ? 1 : 0 });

  beforeEach(async () => {
    routeUrl$ = new BehaviorSubject<unknown[]>([]);
    routeStub = { url: routeUrl$, snapshot: { routeConfig: { path: 'contacts' } } };
    contactApiService = jasmine.createSpyObj<ContactApiService>('ContactApiService', ['list', 'create', 'update', 'delete']);
    tagApiService = jasmine.createSpyObj<TagApiService>('TagApiService', ['list', 'create', 'update', 'delete']);
    authService = jasmine.createSpyObj<AuthService>('AuthService', ['logout']);
    themeService = jasmine.createSpyObj<ThemeService>('ThemeService', ['current', 'toggle']);

    contactApiService.list.and.returnValue(contactPage([contact]));
    tagApiService.list.and.returnValue(tagPage([tag]));
    themeService.current.and.returnValue('light');
    themeService.toggle.and.returnValue('dark');

    await TestBed.configureTestingModule({
      imports: [HomeComponent],
      providers: [
        provideRouter([]),
        ErrorService,
        { provide: ActivatedRoute, useValue: routeStub },
        { provide: ContactApiService, useValue: contactApiService },
        { provide: TagApiService, useValue: tagApiService },
        { provide: AuthService, useValue: authService },
        { provide: ThemeService, useValue: themeService }
      ]
    }).compileComponents();

    router = TestBed.inject(Router);
    navigateSpy = spyOn(router, 'navigate').and.resolveTo(true);
    fixture = TestBed.createComponent(HomeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('uses page-specific search placeholders', () => {
    expect(component.searchPlaceholder).toBe('Cerca per nome, cognome, descrizione, email o telefono');

    routeStub.snapshot.routeConfig.path = 'tags';
    routeUrl$.next([{ path: 'tags' }]);

    expect(component.searchPlaceholder).toBe('Cerca tag per nome');
  });

  it('filters tags by name in the tag page', () => {
    component.tags = [
      { id: 1, name: 'Clienti', color: '#16a34a' },
      { id: 2, name: 'Lavoro', color: '#2563eb' }
    ];
    component.search = 'lavo';

    expect(component.filteredTags.map(item => item.name)).toEqual(['Lavoro']);
  });

  it('shows tag search controls and tooltips for tag action buttons', () => {
    routeStub.snapshot.routeConfig.path = 'tags';
    routeUrl$.next([{ path: 'tags' }]);
    fixture.detectChanges();

    const newTagButton = fixture.nativeElement.querySelector('.create-record-button') as HTMLButtonElement;
    const queryButton = fixture.nativeElement.querySelector('.tag-section-toolbar .toolbar__action') as HTMLButtonElement;

    expect(newTagButton.title).toBe('Crea un nuovo tag');
    expect(queryButton.title).toBe('Applica ricerca e filtri tag');
    expect(fixture.nativeElement.querySelector('#tagColorFilter')).toBeNull();
  });

  it('counts contacts to review when email or phone is missing', () => {
    component.contacts = [
      { ...contact, emails: [] },
      { ...contact, id: 8, phones: [] },
      { ...contact, id: 9 }
    ];

    expect(component.contactsWithoutEmail).toBe(2);
  });

  it('updates a contact, closes the edit form and refreshes the list', () => {
    const updated: Contact = { ...contact, firstName: 'Laura Nuova' };
    contactApiService.update.and.returnValue(of(updated));
    contactApiService.list.and.returnValue(contactPage([updated]));

    component.openContactForm(contact);
    component.contactForm.patchValue({ firstName: 'Laura Nuova' });
    component.askSaveContact();
    component.confirmModal();

    expect(contactApiService.update).toHaveBeenCalledWith(7, jasmine.objectContaining({ firstName: 'Laura Nuova' }));
    expect(component.expandedContactId).toBeNull();
    expect(component.contacts[0].firstName).toBe('Laura Nuova');
    expect(component.successMessage).toBe('Contatto aggiornato correttamente.');
  });

  it('asks confirmation when contact has no email and phone', () => {
    contactApiService.create.and.returnValue(of({ ...contact, id: 9, emails: [], phones: [] }));

    component.openNewContactForm();
    component.contactForm.patchValue({ firstName: 'Mario', email: '', phone: '' });
    component.askSaveContact();

    expect(component.modalTitle).toBe('Contatto da verificare');
    expect(contactApiService.create).not.toHaveBeenCalled();

    component.confirmModal();

    expect(contactApiService.create).toHaveBeenCalled();
  });

  it('asks confirmation before toggling a contact as favorite', () => {
    const favoriteContact: Contact = { ...contact, favorite: true };
    contactApiService.update.and.returnValue(of(favoriteContact));
    contactApiService.list.and.returnValue(contactPage([favoriteContact]));

    component.askToggleFavorite(contact);

    expect(component.modalMessage).toContain('Laura Bianchi');
    expect(contactApiService.update).not.toHaveBeenCalled();

    component.confirmModal();

    expect(contactApiService.update).toHaveBeenCalledWith(7, jasmine.objectContaining({ favorite: true }));
    expect(component.contacts[0].favorite).toBeTrue();
    expect(component.expandedContactId).toBeNull();
  });

  it('shows CRUD errors in a modal', () => {
    spyOn(console, 'error');
    contactApiService.update.and.returnValue(throwError(() => new HttpErrorResponse({ status: 500 })));

    component.openContactForm(contact);
    component.askSaveContact();
    component.confirmModal();

    expect(component.modalOpen).toBeTrue();
    expect(component.modalTitle).toBe('Errore');
    expect(component.modalMessage).toContain('Non è stato possibile modificare il contatto.');
    expect(component.modalMessage).toContain('Errore del server.');
    expect(component.modalConfirmLabel).toBe('Chiudi');
    expect(component.modalShowCancel).toBeFalse();
    expect(console.error).toHaveBeenCalledWith('Salvataggio contatto fallito.', jasmine.anything());

    component.confirmModal();

    expect(component.modalOpen).toBeFalse();
  });

  it('toggles theme and logs out through user actions', () => {
    component.toggleTheme();
    component.logout();

    expect(component.themeMode).toBe('dark');
    expect(authService.logout).toHaveBeenCalled();
    expect(navigateSpy).toHaveBeenCalledWith(['/login']);
  });
});
