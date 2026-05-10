import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, of, throwError } from 'rxjs';
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

@Injectable({ providedIn: 'root' })
export class TagApiService {
  private readonly http = inject(HttpClient);
  private readonly tagsUrl = `${environment.apiBaseUrl}/tags`;
  private readonly storageKey = 'rubricaJavaAngular.demo.tags';

  list(): Observable<Tag[]> {
    if (environment.staticDemo) {
      return of(this.readTags());
    }
    return this.http.get<Tag[]>(this.tagsUrl);
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
      return of(savedTag);
    }
    return this.http.put<Tag>(`${this.tagsUrl}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    if (environment.staticDemo) {
      this.writeTags(this.readTags().filter(tag => tag.id !== id));
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

  private seedTags(): Tag[] {
    return [
      { id: 1, name: 'Lavoro', color: '#2563eb' },
      { id: 2, name: 'VIP', color: '#b45309' },
      { id: 3, name: 'Clienti', color: '#047857' }
    ];
  }
}
