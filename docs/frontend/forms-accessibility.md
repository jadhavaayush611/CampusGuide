# CampusGuide Forms & CRUD Accessibility Guidelines

This document details the accessibility standards and patterns implemented across all forms, input controls, and CRUD workflows in the CampusGuide frontend. Developers should follow these standards when creating new form controls or modifying existing ones.

---

## 1. Label-Input Bindings

Every user input must be associated programmatically with a label. Do not rely solely on visual placeholders or positioning.

### Rule: Explicit Association

Use the `htmlFor` attribute on the `<label>` and a matching `id` attribute on the corresponding `<input>`, `<textarea>`, or `<select>`.

```tsx
// Correct
<label htmlFor="task-title">Task Title</label>
<input id="task-title" type="text" ... />
```

### Exceptions & Alternatives

If a visual label is not design-compliant (e.g., in headers or search queries):
1. **Visually Hidden Labels:** Use Tailwind's `sr-only` class to hide the label visually while keeping it accessible to screen readers.
2. **ARIA Labels:** Use `aria-label` directly on the input if there is no visual text label to associate.

```tsx
// Visual search query input
<input
  id="search-query"
  type="text"
  aria-label="Search notifications"
  placeholder="Search..."
/>
```

---

## 2. Dialog & Modal Behavior

All simulated overlay modals (created via fixed containers rather than native `<dialog>` tags) must fulfill three key keyboard accessibility criteria:

1. **Escape Key Dismissal:** Listen to keypress events to close the dialog when the `Escape` key is pressed.
2. **ARIA Roles:** Mark the modal container with `role="dialog"`, `aria-modal="true"`, and label it using `aria-labelledby` pointing to the modal title element.
3. **Close Button Description:** Close buttons (especially those containing only an icon like an "X") must have a clear `aria-label="Close dialog"`.

```tsx
// Example useEffect for Escape key
useEffect(() => {
  if (!isOpen) return;
  const handleKeyDown = (e: KeyboardEvent) => {
    if (e.key === 'Escape') {
      onClose();
    }
  };
  window.addEventListener('keydown', handleKeyDown);
  return () => window.removeEventListener('keydown', handleKeyDown);
}, [isOpen, onClose]);
```

```tsx
// Modal Container Structure
<div
  role="dialog"
  aria-modal="true"
  aria-labelledby="dialog-title"
  className="fixed inset-0 ..."
>
  <h3 id="dialog-title">Create New Task</h3>
  <button onClick={onClose} aria-label="Close dialog">
    <X className="w-5 h-5" />
  </button>
  ...
</div>
```

---

## 3. Accessible Error Reporting & Validation

Errors must be programmatically associated with inputs so screen readers announce them when the input receives focus.

### Association using `aria-describedby`

1. Display the validation error paragraph with a unique `id`.
2. Link the input to the error message using `aria-describedby`.
3. Set `aria-invalid="true"` dynamically on the input if there is an active validation error.

```tsx
<div>
  <label htmlFor="email-input">Email Address</label>
  <input
    id="email-input"
    type="email"
    aria-describedby="email-error"
    aria-invalid={!!errors.email}
    ...
  />
  {errors.email && (
    <p id="email-error" className="text-red-500 text-xs">
      {errors.email}
    </p>
  )}
</div>
```

### Auto-focus on First Failure

When form validation fails on submit, programmatic focus should shift automatically to the first invalid field to allow immediate keyboard editing.

```typescript
const validate = () => {
  const newErrors: Record<string, string> = {};
  if (!email) newErrors.email = "Email is required";
  
  if (Object.keys(newErrors).length > 0) {
    setErrors(newErrors);
    // Focus the first invalid element
    const firstErrorKey = Object.keys(newErrors)[0];
    const element = document.getElementById(`${firstErrorKey}-input`);
    if (element) {
      element.focus();
    }
    return false;
  }
  return true;
};
```

---

## 4. Standard Input & State Attributes

Ensure standard properties are configured correctly on interactive controls:

| Attribute | Use Case / Application | Example / Value |
| :--- | :--- | :--- |
| `autoComplete` | Authentication input prompts, names, and contact details | `username`, `current-password`, `new-password`, `email`, `name` |
| `aria-busy` | Submit buttons or loading overlays to announce loading status | `aria-busy={isSubmitting}` |
| `aria-pressed` | Toggle/View buttons to announce whether the state is active | `aria-pressed={viewMode === 'grid'}` |
| `aria-haspopup` | Custom selector/dropdown buttons | `aria-haspopup="listbox"` |
| `aria-expanded` | Custom dropdown buttons to state open/collapsed status | `aria-expanded={showOptions}` |
| `role="listbox"` | Dropdown wrapper element for options list | `role="listbox"` |
| `role="option"` | Individual option buttons inside custom listbox menus | `role="option" aria-selected={selected}` |

---

## 5. File Pickers & Drag-and-Drop Zones

Keyboard users must be able to focus and trigger file selectors easily:

1. **Do not hide input with `display: none` / `className="hidden"`.** Hidden inputs are omitted from the browser focus cycle.
2. **Use Tailwind's `sr-only`** (Screen Reader Only) instead to hide the raw file input element visually while maintaining it in the tab cycle.
3. **Focus Styling:** Apply styling on the wrapping dropzone border using Tailwind's `focus-within:` variants (e.g. `focus-within:ring-2 focus-within:ring-blue-600`) so keyboard users receive visual confirmation when they focus the input.

```tsx
<div className="border-2 border-dashed border-gray-300 hover:border-blue-500 rounded-2xl p-6 text-center focus-within:ring-2 focus-within:ring-blue-600">
  <input
    type="file"
    id="file-input"
    className="sr-only"
    onChange={handleFileChange}
  />
  <label htmlFor="file-input" className="cursor-pointer">
    <span>Click to upload file</span>
  </label>
</div>
```
