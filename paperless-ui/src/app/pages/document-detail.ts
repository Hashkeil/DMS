import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ApiService, errorMessage } from '../core/api.service';
import { CategoryResponse, DocumentRequest, DocumentResponse, LabelResponse } from '../core/models';
import { LabelEditor } from '../core/label-editor';
import { uploadError } from '../core/upload-validation';
import { documentValidators, fieldError } from '../core/validation';

// Handles both /documents/new (create) and /documents/:id (detail, edit, delete).
@Component({
  selector: 'app-document-detail',
  imports: [ReactiveFormsModule, FormsModule, RouterLink, DatePipe, LabelEditor],
  template: `
    <a routerLink="/" class="back-link">← Back to documents</a>

    <div class="page-head">
      <div>
        <h1>{{ id === null ? 'Upload a document' : 'Document details' }}</h1>
        @if (document(); as doc) {
          <p class="subtle">
            Created {{ doc.createdAt | date: 'mediumDate' }} · Updated
            {{ doc.updatedAt | date: 'mediumDate' }}
          </p>
        }
      </div>
    </div>

    @if (error()) {
      <div class="alert error">{{ error() }}</div>
    }

    <div class="detail-grid">
      <form class="card form-card" [formGroup]="form" (ngSubmit)="save()">
        @if (id === null) {
          <div class="dropzone" [class.has-file]="!!file">
            <input
              hidden
              #filePicker
              type="file"
              accept="application/pdf,.pdf"
              (change)="selectFile($event)"
            />
            <button type="button" class="dropzone-btn" (click)="filePicker.click()">
              @if (file) {
                <svg viewBox="0 0 24 24" width="26" height="26" fill="none" stroke="currentColor"
                     stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                  <path d="M14 2v6h6" />
                </svg>
                <strong>{{ file.name }}</strong>
                <span class="subtle">Click to choose a different PDF</span>
              } @else {
                <svg viewBox="0 0 24 24" width="26" height="26" fill="none" stroke="currentColor"
                     stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                  <path d="m17 8-5-5-5 5" />
                  <path d="M12 3v12" />
                </svg>
                <strong>Click to choose a PDF</strong>
                <span class="subtle">PDF only, up to 10 MiB</span>
              }
            </button>
          </div>
        }

        <label>
          Title *
          <input formControlName="title" placeholder="e.g. Rental contract 2026" />
        </label>
        @if (fieldError(form.controls.title); as msg) {
          <span class="field-error">{{ msg }}</span>
        }

        <label>
          Filename *
          <input formControlName="filename" [readOnly]="id === null" />
        </label>
        @if (fieldError(form.controls.filename); as msg) {
          <span class="field-error">{{ msg }}</span>
        }

        <app-label-editor
          [(selected)]="labels"
          [allLabels]="allLabels()"
          (add)="addLabel($event)"
          (remove)="removeLabel($event)"
        />

        <label>
          Description
          <textarea formControlName="description" rows="4" placeholder="Optional notes…"></textarea>
        </label>

        <label>
          Category
          <select formControlName="categoryId">
            <option [ngValue]="null">– none –</option>
            @for (cat of categories(); track cat.id) {
              <option [ngValue]="cat.id">{{ cat.name }}</option>
            }
          </select>
        </label>

        <div class="actions">
          <button
            type="submit"
            class="button primary"
            [disabled]="form.invalid || saving() || (id === null && !file)"
          >
            @if (saving()) {
              <span class="btn-spinner" aria-hidden="true"></span>
            }
            {{ id === null ? 'Create document' : 'Save changes' }}
          </button>
          @if (id !== null) {
            <button type="button" class="button danger-outline" (click)="remove()" [disabled]="saving()">
              Delete
            </button>
          }
        </div>
      </form>

      @if (id !== null) {
        <aside class="card side-card">
          <h3>PDF file</h3>
          <p class="meta storage-name">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor"
                 stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
              <path d="M14 2v6h6" />
            </svg>
            {{ document()?.filename }}
          </p>
          <p class="subtle">Text extraction (OCR) runs automatically after upload — coming in Sprint 4.</p>
        </aside>
      }
    </div>
  `,
})
export class DocumentDetail implements OnInit {
  private readonly api = inject(ApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly fieldError = fieldError;
  protected readonly id: number | null = this.parseId(this.route.snapshot.paramMap.get('id'));

  protected readonly document = signal<DocumentResponse | null>(null);
  protected readonly categories = signal<CategoryResponse[]>([]);
  protected readonly allLabels = signal<LabelResponse[]>([]);
  protected readonly labels = signal<string[]>([]);
  protected readonly error = signal<string | null>(null);
  protected readonly saving = signal(false);
  protected file: File | null = null;

  protected selectFile(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0] ?? null;
    this.file = null;
    const problem = uploadError(file);
    if (problem) {
      this.error.set(problem);
      return;
    }
    this.file = file;
    this.form.controls.filename.setValue(file!.name);
    this.error.set(null);
  }

  protected addLabel(name: string): void {
    this.error.set(null);
    if (this.labels().some((n) => n.toLowerCase() === name.toLowerCase())) return;
    // Unknown labels are created server-side automatically; verify name constraints client-side.
    const known = this.allLabels().find((l) => l.name.toLowerCase() === name.toLowerCase());
    if (known) {
      this.labels.update((current) => [...current, known.name]);
    } else {
      this.api.createLabel(name).subscribe({
        next: (label) => {
          this.allLabels.update((labels) => [...labels, label]);
          this.labels.update((current) => [...current, label.name]);
        },
        error: (err: HttpErrorResponse) => this.error.set(errorMessage(err)),
      });
    }
  }

  protected removeLabel(name: string): void {
    this.labels.update((current) => current.filter((n) => n !== name));
  }

  protected readonly form = inject(NonNullableFormBuilder).group({
    title: ['', documentValidators.title],
    filename: ['', documentValidators.filename],
    description: [''],
    categoryId: [null as number | null],
  });

  ngOnInit(): void {
    this.api.getCategories().subscribe({
      next: (cats) => this.categories.set(cats),
      error: (err: HttpErrorResponse) => this.error.set(errorMessage(err)),
    });
    this.api.getLabels().subscribe({
      next: (labels) => this.allLabels.set(labels),
      error: () => this.allLabels.set([]),
    });

    if (this.id !== null) {
      this.api.getDocument(this.id).subscribe({
        next: (doc) => {
          this.document.set(doc);
          this.labels.set(doc.labels.map((l) => l.name));
          this.form.setValue({
            title: doc.title,
            filename: doc.filename,
            description: doc.description ?? '',
            categoryId: doc.categoryId,
          });
        },
        error: (err: HttpErrorResponse) => this.error.set(errorMessage(err)),
      });
    }
  }

  protected save(): void {
    if (this.form.invalid || (this.id === null && !this.file)) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    const request: DocumentRequest = {
      title: value.title.trim(),
      filename: value.filename.trim(),
      description: value.description.trim() || null,
      categoryId: value.categoryId,
      labels: [...this.labels()],
    };

    this.saving.set(true);
    this.error.set(null);
    const call =
      this.id === null
        ? this.api.uploadDocument(request, this.file!)
        : this.api.updateDocument(this.id, request);
    call.subscribe({
      next: () => this.router.navigate(['/']),
      error: (err: HttpErrorResponse) => {
        this.error.set(errorMessage(err));
        this.saving.set(false);
      },
    });
  }

  protected remove(): void {
    if (this.id === null || !confirm('Delete this document?')) {
      return;
    }
    this.saving.set(true);
    this.api.deleteDocument(this.id).subscribe({
      next: () => this.router.navigate(['/']),
      error: (err: HttpErrorResponse) => {
        this.error.set(errorMessage(err));
        this.saving.set(false);
      },
    });
  }

  private parseId(raw: string | null): number | null {
    return raw === null ? null : Number(raw);
  }
}
