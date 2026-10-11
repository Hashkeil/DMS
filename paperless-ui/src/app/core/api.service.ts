import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { CategoryResponse, DocumentRequest, DocumentResponse, ErrorResponse } from './models';

const DOCUMENTS = '/api/documents';
const CATEGORIES = '/api/categories';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);

  getDocuments(): Observable<DocumentResponse[]> {
    return this.http.get<DocumentResponse[]>(DOCUMENTS);
  }

  getDocument(id: number): Observable<DocumentResponse> {
    return this.http.get<DocumentResponse>(`${DOCUMENTS}/${id}`);
  }

  createDocument(request: DocumentRequest): Observable<DocumentResponse> {
    return this.http.post<DocumentResponse>(DOCUMENTS, request);
  }

  updateDocument(id: number, request: DocumentRequest): Observable<DocumentResponse> {
    return this.http.put<DocumentResponse>(`${DOCUMENTS}/${id}`, request);
  }

  deleteDocument(id: number): Observable<void> {
    return this.http.delete<void>(`${DOCUMENTS}/${id}`);
  }

  getCategories(): Observable<CategoryResponse[]> {
    return this.http.get<CategoryResponse[]>(CATEGORIES);
  }
}

// Error handling - translates error to readable message
export function errorMessage(error: HttpErrorResponse): string {
  const body = error.error as Partial<ErrorResponse> | null;
  if (body?.message) {
    return `${error.status} ${body.error ?? ''}: ${body.message}`;
  }
  if (error.status === 0) {
    return 'Server not reachable.';
  }
  return `${error.status} ${error.statusText}`;
}
