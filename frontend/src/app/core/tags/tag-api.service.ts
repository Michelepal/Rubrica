import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map, of, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Tag {
  id: number;
  name: string;
  color: string | null;
}

export interface TagRequest {
  name: string;
  color: string | null;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

@Injectable({ providedIn: 'root' })
export class TagApiService {
  private readonly http = inject(HttpClient);
  private readonly tagsUrl = `${environment.apiBaseUrl}/tags`;
  private readonly storageKey = 'rubricaJavaAngular.demo.tags';
  private readonly contactStorageKey = 'rubricaJavaAngular.demo.contacts';

  list(page = 0, size = 10): Observable<PageResponse<Tag>> {
    if (environment.staticDemo) {
      return of(this.toPage(this.readTags(), page, size));
    }
    return this.http.get<PageResponse<Tag> | Tag[]>(this.tagsUrl, { params: { page: String(page), size: String(size) } }).pipe(
      map(response => Array.isArray(response) ? this.toPage(response, page, size) : response)
    );
  }

  create(request: TagRequest): Observable<Tag> {
    if (environment.staticDemo) {
      const tags = this.readTags();
      if (tags.some(tag => tag.name.toLowerCase() === request.name.toLowerCase())) {
        return throwError(() => new Error('Esiste già un tag con questo nome.'));
      }
      const savedTag = { id: Date.now(), ...request };
      this.writeTags([...tags, savedTag]);
      return of(savedTag);
    }
    return this.http.post<Tag>(this.tagsUrl, request);
  }

  update(id: number, request: TagRequest): Observable<Tag> {
    if (environment.staticDemo) {
      const tags = this.readTags();
      if (tags.some(tag => tag.id !== id && tag.name.toLowerCase() === request.name.toLowerCase())) {
        return throwError(() => new Error('Esiste già un tag con questo nome.'));
      }
      const savedTag = { id, ...request };
      this.writeTags(tags.map(tag => tag.id === id ? savedTag : tag));
      this.updateTagInContacts(savedTag);
      return of(savedTag);
    }
    return this.http.put<Tag>(`${this.tagsUrl}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    if (environment.staticDemo) {
      this.writeTags(this.readTags().filter(tag => tag.id !== id));
      this.removeTagFromContacts(id);
      return of(void 0);
    }
    return this.http.delete<void>(`${this.tagsUrl}/${id}`);
  }

  private readTags(): Tag[] {
    try {
      const stored = localStorage.getItem(this.storageKey);
      return stored ? JSON.parse(stored) as Tag[] : this.seedTags();
    } catch (error) {
      console.error('Errore durante la lettura dei tag demo.', error);
      return this.seedTags();
    }
  }

  private writeTags(tags: Tag[]): void {
    try {
      localStorage.setItem(this.storageKey, JSON.stringify(tags));
    } catch (error) {
      console.error('Errore durante il salvataggio dei tag demo.', error);
      throw error;
    }
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

  private seedTags(): Tag[] {
    return [
      { id: 1, name: 'Lavoro', color: '#2563eb' },
      { id: 2, name: 'VIP', color: '#b45309' },
      { id: 3, name: 'Clienti', color: '#047857' }
    ];
  }

  private updateTagInContacts(tag: Tag): void {
    const contacts = this.readContacts();
    this.writeContacts(contacts.map(contact => ({
      ...contact,
      tags: contact.tags.map(existingTag => existingTag.id === tag.id ? tag : existingTag)
    })));
  }

  private removeTagFromContacts(tagId: number): void {
    const contacts = this.readContacts();
    this.writeContacts(contacts.map(contact => ({
      ...contact,
      tags: contact.tags.filter(tag => tag.id !== tagId)
    })));
  }

  private readContacts(): Array<{ tags: Tag[] }> {
    try {
      const stored = localStorage.getItem(this.contactStorageKey);
      return stored ? JSON.parse(stored) as Array<{ tags: Tag[] }> : [];
    } catch (error) {
      console.error('Errore durante la lettura dei contatti demo per i tag.', error);
      return [];
    }
  }

  private writeContacts(contacts: Array<{ tags: Tag[] }>): void {
    try {
      localStorage.setItem(this.contactStorageKey, JSON.stringify(contacts));
    } catch (error) {
      console.error('Errore durante il salvataggio dei contatti demo per i tag.', error);
      throw error;
    }
  }
}
