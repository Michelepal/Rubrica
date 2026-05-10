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
    favorite: false,
    phones: [{ type: 'mobile', value: '1234567890', primary: true }],
    emails: [{ type: 'email', value: 'laura@example.local', primary: true }],
    tags: [{ id: 2, name: 'Lavoro', color: '#2563eb' }]
  };

  const tag: Tag = { id: 2, name: 'Lavoro', color: '#2563eb' };

  beforeEach(async () => {
    routeUrl$ = new BehaviorSubject<unknown[]>([]);
    routeStub = { url: routeUrl$, snapshot: { routeConfig: { path: 'contacts' } } };
    contactApiService = jasmine.createSpyObj<ContactApiService>('ContactApiService', ['list', 'create', 'update', 'delete']);
    tagApiService = jasmine.createSpyObj<TagApiService>('TagApiService', ['list', 'create', 'update', 'delete']);
    authService = jasmine.createSpyObj<AuthService>('AuthService', ['logout']);
    themeService = jasmine.createSpyObj<ThemeService>('ThemeService', ['current', 'toggle']);

    contactApiService.list.and.returnValue(of([contact]));
    tagApiService.list.and.returnValue(of([tag]));
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
    expect(component.searchPlaceholder).toBe('Cerca contatti per nome, azienda, email o telefono');

    routeStub.snapshot.routeConfig.path = 'tags';
    routeUrl$.next([{ path: 'tags' }]);

    expect(component.searchPlaceholder).toBe('Cerca tag per nome o colore');
  });

  it('filters tags by name or color in the tag page', () => {
    component.tags = [
      { id: 1, name: 'Clienti', color: '#16a34a' },
      { id: 2, name: 'Lavoro', color: '#2563eb' }
    ];
    component.search = '2563';

    expect(component.filteredTags.map(item => item.name)).toEqual(['Lavoro']);
  });

  it('updates a contact, closes the edit form and refreshes the list', () => {
    const updated: Contact = { ...contact, firstName: 'Laura Nuova' };
    contactApiService.update.and.returnValue(of(updated));
    contactApiService.list.and.returnValue(of([updated]));

    component.openContactForm(contact);
    component.contactForm.patchValue({ firstName: 'Laura Nuova' });
    component.askSaveContact();
    component.confirmModal();

    expect(contactApiService.update).toHaveBeenCalledWith(7, jasmine.objectContaining({ firstName: 'Laura Nuova' }));
    expect(component.expandedContactId).toBeNull();
    expect(component.contacts[0].firstName).toBe('Laura Nuova');
    expect(component.successMessage).toBe('Contatto aggiornato correttamente.');
  });

  it('shows and dismisses CRUD errors', () => {
    contactApiService.update.and.returnValue(throwError(() => new HttpErrorResponse({ status: 500 })));

    component.openContactForm(contact);
    component.askSaveContact();
    component.confirmModal();

    expect(component.errorMessage).toContain('Non è stato possibile modificare il contatto.');
    expect(component.errorMessage).toContain('Errore del server.');

    component.dismissError();

    expect(component.errorMessage).toBe('');
  });

  it('toggles theme and logs out through user actions', () => {
    component.toggleTheme();
    component.logout();

    expect(component.themeMode).toBe('dark');
    expect(authService.logout).toHaveBeenCalled();
    expect(navigateSpy).toHaveBeenCalledWith(['/login']);
  });
});
