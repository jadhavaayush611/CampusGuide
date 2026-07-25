# Mobile UI & Design Guidelines

This document specifies the visual design tokens, typography, component library rules, and accessibility standards for mobile clients.

---

## 1. Design Tokens & Color Palette

Mobile components map to standard CampusGuide CSS design tokens:
- **Primary Accent**: `#4F46E5` (Indigo 600)
- **Secondary Accent**: `#06B6D4` (Cyan 500)
- **Background Dark**: `#0F172A` (Slate 900)
- **Background Light**: `#F8FAFC` (Slate 50)
- **Surface Glass**: `rgba(255, 255, 255, 0.08)` with backdrop blur

---

## 2. Typography

- Primary Font: **Inter** / **System Default** (San Francisco on iOS, Roboto on Android)
- Scale: Title (24pt/bold), Subtitle (18pt/semibold), Body (15pt/regular), Caption (12pt/medium)

---

## 3. Accessibility Standards

- Maintain WCAG 2.1 AA contrast ratios (4.5:1 minimum for body text).
- Provide explicit `accessibilityLabel` and `accessibilityHint` props for all interactive buttons and icons.
- Support native dynamic type font scaling.

---

## Cross-References
- [Mobile Overview](file:///D:/CampusGuide/docs/mobile/mobile-overview.md)
- [Mobile Navigation](file:///D:/CampusGuide/docs/mobile/navigation.md)
