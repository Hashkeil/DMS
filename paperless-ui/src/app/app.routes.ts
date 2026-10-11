import { Routes } from '@angular/router';
import { Dashboard } from './pages/dashboard';
import { DocumentDetail } from './pages/document-detail';

export const routes: Routes = [
  { path: '', component: Dashboard },
  { path: 'documents/new', component: DocumentDetail },
  { path: 'documents/:id', component: DocumentDetail },
  { path: '**', redirectTo: '' },
];
