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

  it('does not match contacts by tag name when refining API pages', () => {
    service.list({ page: 0, size: 10, q: 'VIP', sort: 'name' }).subscribe(page => {
      expect(page.content).toEqual([]);
      expect(page.totalElements).toBe(0);
      expect(page.totalPages).toBe(0);
    });

    const req = httpMock.expectOne(match => match.url === `${environment.apiBaseUrl}/contacts` && match.params.get('q') === 'VIP');
    req.flush({
      content: [{
        id: 7,
        firstName: 'Laura',
        lastName: 'Bianchi',
        company: 'Northwind',
        jobTitle: null,
        notes: null,
        createdAt: '2026-05-13T10:00:00.000Z',
        favorite: false,
        phones: [],
        emails: [],
        tags: [{ id: 2, name: 'VIP', color: '#b45309' }]
      }],
      page: 0,
      size: 10,
      totalElements: 1,
      totalPages: 1
    });
  });
});
