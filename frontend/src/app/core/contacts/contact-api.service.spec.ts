import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { ContactApiService, ContactRequest } from './contact-api.service';

describe('ContactApiService', () => {
  let service: ContactApiService;
  let httpMock: HttpTestingController;

  const request: ContactRequest = {
    firstName: 'Laura',
    lastName: 'Bianchi',
    company: 'Northwind',
    jobTitle: null,
    notes: null,
    favorite: false,
    phones: [],
    emails: [],
    addresses: [],
    tagIds: []
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(ContactApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('updates a contact through PUT with the contact id', () => {
    service.update(7, request).subscribe();

    const req = httpMock.expectOne(`${environment.apiBaseUrl}/contacts/7`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body.firstName).toBe('Laura');
    req.flush({ id: 7, ...request, tags: [] });
  });
});
