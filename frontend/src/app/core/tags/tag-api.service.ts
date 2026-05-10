import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
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

  list(): Observable<Tag[]> {
    return this.http.get<Tag[]>(this.tagsUrl);
  }

  create(request: TagRequest): Observable<Tag> {
    return this.http.post<Tag>(this.tagsUrl, request);
  }

  update(id: number, request: TagRequest): Observable<Tag> {
    return this.http.put<Tag>(`${this.tagsUrl}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.tagsUrl}/${id}`);
  }
}
