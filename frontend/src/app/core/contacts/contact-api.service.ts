import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map, of } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface ContactChannel {
  id?: number;
  type: string;
  value: string;
  primary: boolean;
}

export interface TagSummary {
  id: number;
  name: string;
  color: string | null;
}

export interface Contact {
  id: number;
  firstName: string;
  lastName: string | null;
  company: string | null;
  jobTitle: string | null;
  notes: string | null;
  favorite: boolean;
  phones: ContactChannel[];
  emails: ContactChannel[];
  tags: TagSummary[];
}

export interface ContactRequest {
  firstName: string;
  lastName: string | null;
  company: string | null;
  jobTitle: string | null;
  notes: string | null;
  favorite: boolean;
  phones: ContactChannel[];
  emails: ContactChannel[];
  addresses: unknown[];
  tagIds: number[];
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface ContactQuery {
  page: number;
  size: number;
  q?: string;
  tagId?: number | null;
  favorite?: boolean | null;
}

@Injectable({ providedIn: 'root' })
export class ContactApiService {
  private readonly http = inject(HttpClient);
  private readonly contactsUrl = `${environment.apiBaseUrl}/contacts`;
  private readonly storageKey = 'rubricaJavaAngular.demo.contacts';
  private readonly tagStorageKey = 'rubricaJavaAngular.demo.tags';

  list(query: ContactQuery = { page: 0, size: 10 }): Observable<PageResponse<Contact>> {
    if (environment.staticDemo) {
      return of(this.toPage(this.filterContacts(this.readContacts(), query), query.page, query.size));
    }
    const params: Record<string, string> = {
      page: String(query.page),
      size: String(query.size)
    };
    if (query.q?.trim()) {
      params['q'] = query.q.trim();
    }
    if (query.tagId) {
      params['tagId'] = String(query.tagId);
    }
    if (query.favorite !== null && query.favorite !== undefined) {
      params['favorite'] = String(query.favorite);
    }
    return this.http.get<PageResponse<Contact> | Contact[]>(this.contactsUrl, { params }).pipe(
      map(response => Array.isArray(response) ? this.toPage(this.filterContacts(response, query), query.page, query.size) : response)
    );
  }

  create(request: ContactRequest): Observable<Contact> {
    if (environment.staticDemo) {
      const contacts = this.readContacts();
      const savedContact = this.toContact(Date.now(), request);
      this.writeContacts([savedContact, ...contacts]);
      return of(savedContact);
    }
    return this.http.post<Contact>(this.contactsUrl, request);
  }

  update(id: number, request: ContactRequest): Observable<Contact> {
    if (environment.staticDemo) {
      const savedContact = this.toContact(id, request);
      this.writeContacts(this.readContacts().map(contact => contact.id === id ? savedContact : contact));
      return of(savedContact);
    }
    return this.http.put<Contact>(`${this.contactsUrl}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    if (environment.staticDemo) {
      this.writeContacts(this.readContacts().filter(contact => contact.id !== id));
      return of(void 0);
    }
    return this.http.delete<void>(`${this.contactsUrl}/${id}`);
  }

  private readContacts(): Contact[] {
    try {
      const stored = localStorage.getItem(this.storageKey);
      return stored ? JSON.parse(stored) as Contact[] : this.seedContacts();
    } catch (error) {
      console.error('Errore durante la lettura dei contatti demo.', error);
      return this.seedContacts();
    }
  }

  private filterContacts(contacts: Contact[], query: ContactQuery): Contact[] {
    const term = query.q?.trim().toLowerCase();
    return contacts.filter(contact => {
      const matchesText = !term || JSON.stringify(contact).toLowerCase().includes(term);
      const matchesTag = !query.tagId || contact.tags.some(tag => tag.id === query.tagId);
      const matchesFavorite = query.favorite === null || query.favorite === undefined || contact.favorite === query.favorite;
      return matchesText && matchesTag && matchesFavorite;
    });
  }

  private toPage<T>(items: T[], page: number, size: number): PageResponse<T> {
    const safeSize = Math.min(Math.max(size, 1), 10);
    const safePage = Math.max(page, 0);
    const start = safePage * safeSize;
    return {
      content: items.slice(start, start + safeSize),
      page: safePage,
      size: safeSize,
      totalElements: items.length,
      totalPages: Math.ceil(items.length / safeSize)
    };
  }

  private writeContacts(contacts: Contact[]): void {
    try {
      localStorage.setItem(this.storageKey, JSON.stringify(contacts));
    } catch (error) {
      console.error('Errore durante il salvataggio dei contatti demo.', error);
      throw error;
    }
  }

  private toContact(id: number, request: ContactRequest): Contact {
    const tagMap = new Map(this.readTags().map(tag => [tag.id, tag]));
    return {
      id,
      firstName: request.firstName,
      lastName: request.lastName,
      company: request.company,
      jobTitle: request.jobTitle,
      notes: request.notes,
      favorite: request.favorite,
      phones: request.phones,
      emails: request.emails,
      tags: request.tagIds.map(tagId => tagMap.get(tagId)).filter((tag): tag is TagSummary => Boolean(tag))
    };
  }

  private seedContacts(): Contact[] {
    return [
      {
        id: 1,
        firstName: 'Laura',
        lastName: 'Bianchi',
        company: 'Northwind',
        jobTitle: 'Account manager',
        notes: 'Contatto demo modificabile.',
        favorite: true,
        phones: [{ type: 'mobile', value: '+39 333 123 4567', primary: true }],
        emails: [{ type: 'email', value: 'laura.bianchi@example.local', primary: true }],
        tags: [{ id: 1, name: 'Lavoro', color: '#2563eb' }, { id: 2, name: 'VIP', color: '#b45309' }]
      },
      {
        id: 2,
        firstName: 'Marco',
        lastName: 'Rossi',
        company: 'Studio Rossi',
        jobTitle: 'Consulente',
        notes: null,
        favorite: false,
        phones: [{ type: 'ufficio', value: '02 555 0199', primary: true }],
        emails: [{ type: 'email', value: 'm.rossi@example.local', primary: true }],
        tags: [{ id: 3, name: 'Clienti', color: '#047857' }]
      }
    ];
  }

  private seedTags(): TagSummary[] {
    return [
      { id: 1, name: 'Lavoro', color: '#2563eb' },
      { id: 2, name: 'VIP', color: '#b45309' },
      { id: 3, name: 'Clienti', color: '#047857' }
    ];
  }

  private readTags(): TagSummary[] {
    try {
      const stored = localStorage.getItem(this.tagStorageKey);
      return stored ? JSON.parse(stored) as TagSummary[] : this.seedTags();
    } catch (error) {
      console.error('Errore durante la lettura dei tag demo associati ai contatti.', error);
      return this.seedTags();
    }
  }
}
