import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
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

@Injectable({ providedIn: 'root' })
export class ContactApiService {
  private readonly http = inject(HttpClient);
  private readonly contactsUrl = `${environment.apiBaseUrl}/contacts`;

  list(): Observable<Contact[]> {
    return this.http.get<Contact[]>(this.contactsUrl);
  }

  create(request: ContactRequest): Observable<Contact> {
    return this.http.post<Contact>(this.contactsUrl, request);
  }

  update(id: number, request: ContactRequest): Observable<Contact> {
    return this.http.put<Contact>(`${this.contactsUrl}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.contactsUrl}/${id}`);
  }
}
