// Mirrors the DTOs of backend

export interface LabelResponse {
  id: number;
  name: string;
  color: string | null;
}

export interface DocumentResponse {
  id: number;
  title: string;
  filename: string;
  description: string | null;
  createdAt: string;
  updatedAt: string;
  categoryId: number | null;
  categoryName: string | null;
  labels: LabelResponse[];
}

export interface DocumentRequest {
  title: string;
  filename: string;
  description: string | null;
  categoryId: number | null;
  labels: string[];
}

export interface CategoryResponse {
  id: number;
  name: string;
  description: string | null;
}

export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
}
