// Browser convenience checks; the server also verifies the PDF signature.
export function uploadError(file: { name: string; size: number } | null): string | null {
  if (!file) return 'Choose a PDF file.';
  if (!file.name.toLowerCase().endsWith('.pdf')) return 'Choose a PDF file.';
  if (file.name.length > 255) return 'Filename must not exceed 255 characters.';
  if (file.size === 0) return 'PDF must not be empty.';
  if (file.size > 10 * 1024 * 1024) return 'PDF must not exceed 10 MiB.';
  return null;
}
