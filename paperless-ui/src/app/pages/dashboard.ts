import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ApiService, errorMessage } from '../core/api.service';
import { DocumentResponse } from '../core/models';

@Component({
  selector: 'app-dashboard',
  imports: [RouterLink, DatePipe, FormsModule],
  template: `
    <div class="page-head">
      <div>
        <h1>Your documents</h1>
        <p class="subtle">Everything you've archived, one search away.</p>
      </div>
      <a routerLink="/documents/new" class="button primary">
        <span class="btn-ico">＋</span> Upload PDF
      </a>
    </div>

    @if (error()) {
      <div class="alert error">{{ error() }}</div>
    }

    <div class="stats">
      <div class="stat-card">
        <span class="stat-value">{{ documents().length }}</span>
        <span class="stat-label">Documents</span>
      </div>
      <div class="stat-card">
        <span class="stat-value">{{ categories().length }}</span>
        <span class="stat-label">Categories</span>
      </div>
      <div class="stat-card">
        <span class="stat-value">{{ labels().length }}</span>
        <span class="stat-label">Labels</span>
      </div>
    </div>

    <div class="toolbar">
      <span class="search-ico" aria-hidden="true">
        <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor"
             stroke-width="2" stroke-linecap="round">
          <circle cx="11" cy="11" r="7" />
          <path d="m21 21-4.3-4.3" />
        </svg>
      </span>
      <input
        type="search"
        placeholder="Search title, filename, category or label…"
        [ngModel]="query()"
        (ngModelChange)="query.set($event)"
      />
    </div>

    @if (loading()) {
      <div class="card empty-state">
        <div class="spinner" aria-hidden="true"></div>
        <p>Fetching your documents…</p>
      </div>
    } @else if (filtered().length === 0) {
      <div class="card empty-state">
        @if (documents().length === 0) {
          <svg viewBox="0 0 24 24" width="40" height="40" fill="none" stroke="currentColor"
               stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
            <path d="M14 2v6h6" />
            <circle cx="11" cy="14" r="3" />
            <path d="m13.5 16.5 2 2" />
          </svg>
          <h3>No documents yet</h3>
          <p class="subtle">Upload your first PDF and give it a title to get started.</p>
          <a routerLink="/documents/new" class="button primary">Upload a PDF</a>
        } @else {
          <h3>Nothing matches “{{ query() }}”</h3>
          <p class="subtle">Try a different search term.</p>
        }
      </div>
    } @else {
      <div class="card table-card">
        <table>
          <thead>
            <tr>
              <th>Title</th>
              <th>File</th>
              <th>Labels</th>
              <th>Category</th>
              <th>Updated</th>
            </tr>
          </thead>
          <tbody>
            @for (doc of filtered(); track doc.id) {
              <tr>
                <td>
                  <a [routerLink]="['/documents', doc.id]" class="doc-link">{{ doc.title }}</a>
                  @if (doc.description) {
                    <span class="doc-desc">{{ doc.description }}</span>
                  }
                </td>
                <td class="meta cell-file">{{ doc.filename }}</td>
                <td>
                  @if (doc.labels.length > 0) {
                    <div class="chip-row">
                      @for (label of doc.labels; track label.id) {
                        <span class="chip tag">
                          <span class="chip-dot" [style.background]="label.color ?? '#4F46E5'"></span>
                          {{ label.name }}
                        </span>
                      }
                    </div>
                  } @else {
                    <span class="subtle">–</span>
                  }
                </td>
                <td>
                  @if (doc.categoryName) {
                    <span class="chip category">{{ doc.categoryName }}</span>
                  } @else {
                    <span class="subtle">–</span>
                  }
                </td>
                <td class="meta">{{ doc.updatedAt | date: 'MMM d, HH:mm' }}</td>
              </tr>
            }
          </tbody>
        </table>
      </div>
    }
  `,
})
export class Dashboard implements OnInit {
  private readonly api = inject(ApiService);

  protected readonly documents = signal<DocumentResponse[]>([]);
  protected readonly categories = signal<{ name: string }[]>([]);
  protected readonly labels = signal<{ name: string }[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly query = signal('');

  protected readonly filtered = computed(() => {
    const term = this.query().trim().toLowerCase();
    if (!term) return this.documents();
    return this.documents().filter(
      (doc) =>
        doc.title.toLowerCase().includes(term) ||
        doc.filename.toLowerCase().includes(term) ||
        doc.categoryName?.toLowerCase().includes(term) ||
        doc.labels.some((l) => l.name.toLowerCase().includes(term)),
    );
  });

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
    this.api.getCategories().subscribe({
      next: (cats) => this.categories.set(cats),
      error: () => this.categories.set([]),
    });
    this.api.getLabels().subscribe({
      next: (labels) => this.labels.set(labels),
      error: () => this.labels.set([]),
    });
  }
}
