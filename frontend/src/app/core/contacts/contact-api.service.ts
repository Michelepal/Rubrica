import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map, of, throwError } from 'rxjs';
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
  createdAt: string;
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
  sort?: 'name' | 'recent';
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
    if (query.sort) {
      params['sort'] = query.sort;
    }
    return this.http.get<PageResponse<Contact> | Contact[]>(this.contactsUrl, { params }).pipe(
      map(response => Array.isArray(response)
        ? this.toPage(this.sortContacts(this.filterContacts(this.normalizeContacts(response), query), query.sort), query.page, query.size)
        : this.normalizeAndRefinePage(response, query)
      )
    );
  }

  create(request: ContactRequest): Observable<Contact> {
    if (environment.staticDemo) {
      const contacts = this.readContacts();
      const duplicate = this.findDuplicateChannel(contacts, request);
      if (duplicate) {
        return throwError(() => new Error(duplicate));
      }
      const savedContact = this.toContact(Date.now(), request);
      this.writeContacts([savedContact, ...contacts]);
      return of(savedContact);
    }
    return this.http.post<Contact>(this.contactsUrl, request);
  }

  update(id: number, request: ContactRequest): Observable<Contact> {
    if (environment.staticDemo) {
      const existingContact = this.readContacts().find(contact => contact.id === id);
      const duplicate = this.findDuplicateChannel(this.readContacts(), request, id);
      if (duplicate) {
        return throwError(() => new Error(duplicate));
      }
      const savedContact = this.toContact(id, request, existingContact?.createdAt);
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
      return stored ? this.withDemoContacts(this.normalizeContacts(JSON.parse(stored) as Contact[])) : this.seedContacts();
    } catch (error) {
      console.error('Errore durante la lettura dei contatti demo.', error);
      return this.seedContacts();
    }
  }

  private normalizeContacts(contacts: Contact[]): Contact[] {
    return contacts.map(contact => ({
      ...contact,
      createdAt: contact.createdAt ?? this.demoCreatedAt(contact.id)
    }));
  }

  private normalizePage(page: PageResponse<Contact>): PageResponse<Contact> {
    return {
      ...page,
      content: this.normalizeContacts(page.content)
    };
  }

  private normalizeAndRefinePage(page: PageResponse<Contact>, query: ContactQuery): PageResponse<Contact> {
    const normalized = this.normalizePage(page);
    const refinedContent = this.filterContacts(normalized.content, query);
    if (refinedContent.length === normalized.content.length) {
      return normalized;
    }
    return {
      ...normalized,
      content: refinedContent,
      totalElements: refinedContent.length,
      totalPages: refinedContent.length ? 1 : 0
    };
  }

  private withDemoContacts(contacts: Contact[]): Contact[] {
    const existingIds = new Set(contacts.map(contact => contact.id));
    return [...contacts, ...this.seedContacts().filter(contact => !existingIds.has(contact.id))];
  }

  private demoCreatedAt(id: number): string {
    return new Date(Date.UTC(2026, 4, Math.max(1, 16 - id), 8 + (id % 5), 15 + id)).toISOString();
  }

  private filterContacts(contacts: Contact[], query: ContactQuery): Contact[] {
    const term = query.q?.trim().toLowerCase();
    return contacts.filter(contact => {
      const searchableText = [
        contact.firstName,
        contact.lastName,
        contact.notes,
        ...contact.emails.map(email => email.value),
        ...contact.phones.map(phone => phone.value)
      ].filter(Boolean).join(' ').toLowerCase();
      const matchesText = !term || searchableText.includes(term);
      const matchesTag = !query.tagId || contact.tags.some(tag => tag.id === query.tagId);
      const matchesFavorite = query.favorite === null || query.favorite === undefined || contact.favorite === query.favorite;
      return matchesText && matchesTag && matchesFavorite;
    });
  }

  private sortContacts(contacts: Contact[], sort: ContactQuery['sort'] = 'name'): Contact[] {
    return [...contacts].sort((left, right) => {
      if (sort === 'recent') {
        return right.id - left.id;
      }

      const leftName = `${left.lastName ?? ''} ${left.firstName}`.trim().toLowerCase();
      const rightName = `${right.lastName ?? ''} ${right.firstName}`.trim().toLowerCase();
      return leftName.localeCompare(rightName, 'it');
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

  private toContact(id: number, request: ContactRequest, createdAt = new Date().toISOString()): Contact {
    const tagMap = new Map(this.readTags().map(tag => [tag.id, tag]));
    return {
      id,
      firstName: request.firstName,
      lastName: request.lastName,
      company: request.company,
      jobTitle: request.jobTitle,
      notes: request.notes,
      createdAt,
      favorite: request.favorite,
      phones: request.phones,
      emails: request.emails,
      tags: request.tagIds.map(tagId => tagMap.get(tagId)).filter((tag): tag is TagSummary => Boolean(tag))
    };
  }

  private findDuplicateChannel(contacts: Contact[], request: ContactRequest, currentId?: number): string | null {
    const emails = new Set(request.emails.map(email => email.value.trim().toLowerCase()).filter(Boolean));
    const phones = new Set(request.phones.map(phone => this.normalizePhone(phone.value)).filter(Boolean));
    if (emails.size < request.emails.filter(email => email.value.trim()).length) {
      return 'Esiste gia un contatto con questa email.';
    }
    if (phones.size < request.phones.filter(phone => phone.value.trim()).length) {
      return 'Esiste gia un contatto con questo numero di telefono.';
    }
    for (const contact of contacts) {
      if (currentId !== undefined && contact.id === currentId) {
        continue;
      }
      if (contact.emails.some(email => emails.has(email.value.trim().toLowerCase()))) {
        return 'Esiste gia un contatto con questa email.';
      }
      if (contact.phones.some(phone => phones.has(this.normalizePhone(phone.value)))) {
        return 'Esiste gia un contatto con questo numero di telefono.';
      }
    }
    return null;
  }

  private normalizePhone(value: string): string {
    return value.trim().replace(/[^0-9+]/g, '');
  }

  private seedContacts(): Contact[] {
    return [
      this.demoContact(1, 'Laura', 'Bianchi', 'Northwind', 'Account manager', true, '+39 333 123 4567', 'laura.bianchi@example.local', [1, 2]),
      this.demoContact(2, 'Marco', 'Rossi', 'Studio Rossi', 'Consulente', false, '02 555 0199', 'm.rossi@example.local', [3]),
      this.demoContact(3, 'Giulia', 'Verdi', 'Contoso', 'Product owner', true, '+39 349 555 0103', 'giulia.verdi@example.local', [1]),
      this.demoContact(4, 'Andrea', 'Neri', 'Fabrikam', 'Sviluppatore', false, '+39 347 555 0104', 'andrea.neri@example.local', [1, 4]),
      this.demoContact(5, 'Sara', 'Ferrari', 'Alpine Studio', 'Designer', false, '+39 348 555 0105', 'sara.ferrari@example.local', [4]),
      this.demoContact(6, 'Paolo', 'Romano', 'Blue Moon', 'Commerciale', true, '+39 331 555 0106', 'paolo.romano@example.local', [2, 3]),
      this.demoContact(7, 'Elena', 'Gallo', 'Green Lab', 'Ricercatrice', false, '+39 332 555 0107', 'elena.gallo@example.local', [5]),
      this.demoContact(8, 'Davide', 'Costa', 'Studio Costa', 'Avvocato', false, '+39 333 555 0108', 'davide.costa@example.local', [3]),
      this.demoContact(9, 'Marta', 'Ricci', 'Ricci & Co', 'HR manager', true, '+39 334 555 0109', 'marta.ricci@example.local', [1, 5]),
      this.demoContact(10, 'Luca', 'Moretti', 'Moretti Impianti', 'Tecnico', false, '+39 335 555 0110', 'luca.moretti@example.local', [4]),
      this.demoContact(11, 'Chiara', 'Marino', 'Northwind', 'Supporto clienti', false, '+39 336 555 0111', 'chiara.marino@example.local', [3]),
      this.demoContact(12, 'Fabio', 'Greco', 'Greco Food', 'Fornitore', true, '+39 337 555 0112', 'fabio.greco@example.local', [2, 4]),
      this.demoContact(13, 'Irene', 'Rinaldi', 'Rinaldi Group', 'CFO', false, '+39 338 555 0113', 'irene.rinaldi@example.local', [2]),
      this.demoContact(14, 'Simone', 'Lombardi', 'Lombardi Casa', 'Architetto', false, '+39 339 555 0114', 'simone.lombardi@example.local', [5]),
      this.demoContact(15, 'Valeria', 'Fontana', 'Fontana Media', 'Copywriter', true, '+39 340 555 0115', 'valeria.fontana@example.local', [1, 3])
    ];
  }

  private demoContact(
    id: number,
    firstName: string,
    lastName: string,
    company: string,
    jobTitle: string,
    favorite: boolean,
    phone: string,
    email: string,
    tagIds: number[]
  ): Contact {
    const tagMap = new Map(this.seedTags().map(tag => [tag.id, tag]));
    return {
      id,
      firstName,
      lastName,
      company,
      jobTitle,
      notes: 'Contatto demo modificabile.',
      createdAt: this.demoCreatedAt(id),
      favorite,
      phones: [{ type: 'mobile', value: phone, primary: true }],
      emails: [{ type: 'email', value: email, primary: true }],
      tags: tagIds.map(tagId => tagMap.get(tagId)).filter((tag): tag is TagSummary => Boolean(tag))
    };
  }

  private seedTags(): TagSummary[] {
    return [
      { id: 1, name: 'Lavoro', color: '#2563eb' },
      { id: 2, name: 'VIP', color: '#b45309' },
      { id: 3, name: 'Clienti', color: '#047857' },
      { id: 4, name: 'Fornitori', color: '#dc2626' },
      { id: 5, name: 'Famiglia', color: '#7c3aed' }
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
