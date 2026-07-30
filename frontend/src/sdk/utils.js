/**
 * Helper utilities for pagination, filtering, sorting, upload/download, and error handling.
 */

export class AtlasError extends Error {
  constructor(message, status = 500, details = null, path = '') {
    super(message);
    this.name = 'AtlasError';
    this.status = status;
    this.details = details;
    this.path = path;
  }

  static fromResponse(status, json) {
    const errorMsg = json?.error || json?.message || `HTTP ${status} Request Failed`;
    return new AtlasError(errorMsg, status, json?.details || null, json?.path || '');
  }
}

export function buildQueryParams(params = {}) {
  const query = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null) {
      query.append(key, value);
    }
  });
  const queryString = query.toString();
  return queryString ? `?${queryString}` : '';
}

export function sortArray(items = [], key = 'createdAt', direction = 'desc') {
  return [...items].sort((a, b) => {
    const valA = a[key];
    const valB = b[key];
    if (valA < valB) return direction === 'asc' ? -1 : 1;
    if (valA > valB) return direction === 'asc' ? 1 : -1;
    return 0;
  });
}

export function filterItems(items = [], predicate = () => true) {
  return items.filter(predicate);
}

export function paginate(items = [], page = 1, limit = 10) {
  const startIndex = (page - 1) * limit;
  const endIndex = startIndex + limit;
  const data = items.slice(startIndex, endIndex);
  const total = items.length;
  const totalPages = Math.ceil(total / limit);
  return {
    data,
    page,
    limit,
    total,
    totalPages,
    hasMore: page < totalPages,
    nextCursor: data.length > 0 ? data[data.length - 1].id : null,
  };
}

export async function uploadHelper(url, file, headers = {}) {
  const formData = new FormData();
  formData.append('file', file);
  const response = await fetch(url, {
    method: 'POST',
    headers,
    body: formData,
  });
  if (!response.ok) {
    const errJson = await response.json().catch(() => ({}));
    throw AtlasError.fromResponse(response.status, errJson);
  }
  return response.json();
}

export async function downloadHelper(url, filename = 'download', headers = {}) {
  const response = await fetch(url, {
    method: 'GET',
    headers,
  });
  if (!response.ok) {
    throw new AtlasError(`Download failed with status ${response.status}`, response.status);
  }
  const blob = await response.blob();
  const link = document.createElement('a');
  link.href = URL.createObjectURL(blob);
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
}
