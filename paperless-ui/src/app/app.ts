import { Component } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink],
  template: `
    <header class="topbar">
      <a routerLink="/" class="brand" aria-label="Paperless DMS home">
        <span class="brand-mark" aria-hidden="true">
          <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor"
               stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
            <path d="M14 2v6h6" />
            <path d="M8 13h8M8 17h5" />
          </svg>
        </span>
        <span class="brand-name">Paperless<span class="brand-accent">DMS</span></span>
      </a>
      <span class="tagline">Organise. Tag. Find.</span>
    </header>
    <main>
      <router-outlet />
    </main>
    <footer class="footer">Paperless DMS — your documents, always findable.</footer>
  `,
})
export class App {}
