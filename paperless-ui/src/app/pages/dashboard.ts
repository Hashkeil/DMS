import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ApiService, errorMessage } from '../core/api.service';
import { DocumentResponse } from '../core/models';

@Component({
  selector: 'app-dashboard',
  imports: [RouterLink, DatePipe],
  template: `
    <div class="row">
      <h2>Documents</h2>
      <a routerLink="/documents/new" class="button">+ New document</a>
    </div>

    @if (error()) {
      <p class="error">{{ error() }}</p>
    }

    @if (loading()) {
      <p>Loading…</p>
    } @else if (documents().length === 0) {
      <p>No documents yet.</p>
    } @else {
      <table>
        <thead>
          <tr><th>Title</th><th>Filename</th><th>Category</th><th>Created</th><th>Updated</th></tr>
        </thead>
        <tbody>
          @for (doc of documents(); track doc.id) {
            <tr>
              <td><a [routerLink]="['/documents', doc.id]">{{ doc.title }}</a></td>
              <td>{{ doc.filename }}</td>
              <td>{{ doc.categoryName ?? '–' }}</td>
              <td>{{ doc.createdAt | date: 'short' }}</td>
              <td>{{ doc.updatedAt | date: 'short' }}</td>
            </tr>
          }
        </tbody>
      </table>
    }
  `,
})
export class Dashboard implements OnInit {
  private readonly api = inject(ApiService);

  protected readonly documents = signal<DocumentResponse[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.api.getDocuments().subscribe({
      next: (docs) => {
        this.documents.set(docs);
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(errorMessage(err));
        this.loading.set(false);
      },
    });
  }
}
