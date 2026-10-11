import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ApiService, errorMessage } from '../core/api.service';
import { CategoryResponse, DocumentRequest, DocumentResponse } from '../core/models';
import { documentValidators, fieldError } from '../core/validation';

// Handles both /documents/new (create) and /documents/:id (detail, edit, delete).
@Component({
  selector: 'app-document-detail',
  imports: [ReactiveFormsModule, RouterLink, DatePipe],
  template: `
    <p><a routerLink="/">← Back to dashboard</a></p>
    <h2>{{ id ? 'Document #' + id : 'New document' }}</h2>

    @if (error()) {
      <p class="error">{{ error() }}</p>
    }

    @if (document(); as doc) {
      <p class="meta">
        Created {{ doc.createdAt | date: 'medium' }} · Updated {{ doc.updatedAt | date: 'medium' }}
      </p>
    }

    <form [formGroup]="form" (ngSubmit)="save()">
      <label>
        Title *
        <input formControlName="title" />
      </label>
      @if (fieldError(form.controls.title); as msg) {
        <span class="error">{{ msg }}</span>
      }

      <label>
        Filename *
        <input formControlName="filename" />
      </label>
      @if (fieldError(form.controls.filename); as msg) {
        <span class="error">{{ msg }}</span>
      }

      <label>
        Description
        <textarea formControlName="description" rows="4"></textarea>
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

      <div class="row">
        <button type="submit" [disabled]="form.invalid || saving()">
          {{ id ? 'Save changes' : 'Create' }}
        </button>
        @if (id) {
          <button type="button" class="danger" (click)="remove()" [disabled]="saving()">Delete</button>
        }
      </div>
    </form>
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
  protected readonly error = signal<string | null>(null);
  protected readonly saving = signal(false);

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

    if (this.id !== null) {
      this.api.getDocument(this.id).subscribe({
        next: (doc) => {
          this.document.set(doc);
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
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    const request: DocumentRequest = {
      title: value.title.trim(),
      filename: value.filename.trim(),
      description: value.description.trim() || null,
      categoryId: value.categoryId,
    };

    this.saving.set(true);
    this.error.set(null);
    const call = this.id === null
      ? this.api.createDocument(request)
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
