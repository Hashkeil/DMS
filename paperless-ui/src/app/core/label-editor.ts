import { Component, computed, input, model, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { LabelResponse } from './models';
import { MAX_LENGTH } from './validation';

// Chip-based tag editor: pick existing labels or type a new name (Enter to add).
@Component({
  selector: 'app-label-editor',
  imports: [FormsModule],
  template: `
    <div class="chip-editor">
      @for (name of selected(); track name) {
        <button type="button" class="chip selected" (click)="remove.emit(name)" title="Remove label">
          {{ name }}
          <span class="chip-x">×</span>
        </button>
      }

      <input
        class="chip-input"
        [placeholder]="
          selected().length ? 'Add label… (Enter)' : 'Add a label and press Enter…'
        "
        [maxlength]="MAX_LENGTH"
        [ngModel]="draft()"
        (ngModelChange)="draft.set($event)"
        (keydown.enter)="addDraft(); $event.preventDefault()"
      />
    </div>

    @if (suggestions().length > 0) {
      <div class="chip-suggestions">
        <span class="chip-suggestions-label">Existing:</span>
        @for (label of suggestions(); track label.id) {
          <button
            type="button"
            class="chip suggestion"
            (click)="draft.set(''); add.emit(label.name)"
          >
            <span class="chip-dot" [style.background]="label.color ?? '#4F46E5'"></span>
            {{ label.name }}
          </button>
        }
      </div>
    }
  `,
})
export class LabelEditor {
  readonly selected = model.required<string[]>();
  readonly allLabels = input<LabelResponse[]>([]);
  readonly add = output<string>();
  readonly remove = output<string>();

  protected readonly MAX_LENGTH = MAX_LENGTH;
  protected readonly draft = signal('');

  protected readonly suggestions = computed(() =>
    this.allLabels().filter((label) => !this.isSelected(label.name)),
  );

  protected isSelected(name: string): boolean {
    return this.selected().some((n) => n.toLowerCase() === name.toLowerCase());
  }

  protected addDraft(): void {
    const name = this.draft().trim();
    this.draft.set('');
    if (name) this.add.emit(name);
  }
}
