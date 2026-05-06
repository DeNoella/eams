# E-AMS Frontend Development Prompt — React
## For: Cursor AI / Windsurf / OpenCode AI
---

## ROLE & OBJECTIVE

You are a senior React/TypeScript frontend engineer and UI/UX designer. Your task is to build the complete frontend for the **E-Asset Management System (E-AMS)** — an enterprise IT asset management platform.

The UI must look like a **premium, human-crafted enterprise product** — think the design quality of Linear, Vercel Dashboard, or Retool. It must feel custom-built, not AI-generated. No purple gradients, no generic card grids, no stock enterprise templates.

Do NOT copy patterns from generic dashboards. Every screen must feel intentionally designed.

---

## TECHNOLOGY STACK

| Layer | Technology |
|---|---|
| Language | TypeScript (strict mode) |
| Framework | React 18 + Vite |
| Routing | React Router v6 |
| State | Zustand (global) + TanStack Query v5 (server state) |
| Forms | React Hook Form + Zod validation |
| UI Components | Shadcn/ui (as base only — heavily customised) |
| Styling | Tailwind CSS v3 + CSS variables for theming |
| Charts | Recharts |
| Tables | TanStack Table v8 |
| Icons | Lucide React |
| Date | date-fns |
| File Upload | react-dropzone |
| Notifications | Sonner (toasts) |
| HTTP | Axios with interceptors |
| PDF Export | jsPDF + html2canvas |
| QR Code | qrcode.react |
| Rich Text | TipTap (for notes/descriptions) |
| Drag & Drop | @dnd-kit (for workflow designer) |
| Build | Vite |
| Linting | ESLint + Prettier |

---

## DESIGN SYSTEM — READ THIS CAREFULLY

### Color Palette — "Deep Slate & Amber"
This is a **dark-primary** enterprise design system. NOT purple, NOT blue corporate, NOT generic SaaS.

```css
:root {
  /* Backgrounds */
  --bg-base: #0D0F12;          /* Near-black base — main app background */
  --bg-surface: #141720;       /* Cards, panels, modals */
  --bg-elevated: #1C2030;      /* Hover states, secondary surfaces */
  --bg-border: #252A3A;        /* Borders, dividers */

  /* Brand / Accent — Warm Amber */
  --accent-primary: #F59E0B;   /* Primary actions, active states, key data */
  --accent-secondary: #D97706; /* Hover on primary */
  --accent-subtle: #1A1600;    /* Amber tint background */
  --accent-muted: #78350F;     /* Muted amber for tags/badges */

  /* Text */
  --text-primary: #F1F3F7;     /* Main text */
  --text-secondary: #8B93A7;   /* Labels, secondary info */
  --text-muted: #4B5368;       /* Placeholder, disabled */
  --text-inverse: #0D0F12;     /* Text on amber backgrounds */

  /* Status Colors */
  --status-critical: #EF4444;  /* Critical alerts */
  --status-warning: #F97316;   /* Warning / expiring soon */
  --status-review: #EAB308;    /* Review soon */
  --status-valid: #22C55E;     /* Valid / healthy */
  --status-info: #3B82F6;      /* Informational */
  --status-inactive: #6B7280;  /* Inactive / disabled */

  /* Typography */
  --font-display: 'DM Sans', sans-serif;      /* Headings, navigation */
  --font-body: 'IBM Plex Sans', sans-serif;   /* Body text, labels */
  --font-mono: 'IBM Plex Mono', monospace;    /* Asset IDs, codes, data */
}
```

Import fonts in `index.html`:
```html
<link href="https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;500;600;700&family=IBM+Plex+Sans:wght@400;500;600&family=IBM+Plex+Mono:wght@400;500&display=swap" rel="stylesheet">
```

### Design Rules (enforce all of them):
1. **Dark theme only** — bg-base is the canvas, everything sits above it
2. **Amber is precious** — use accent-primary sparingly: active nav items, primary buttons, key metrics, status badges. Not everywhere.
3. **Asset IDs always in monospace** — every asset ID, token, code value uses `font-mono`
4. **Status = color** — always show status with the correct status color. Never plain text only.
5. **Borders are subtle** — use `bg-border` (#252A3A) for borders. Never harsh lines.
6. **Spacing is generous** — padding inside cards: 24px. Gap between cards: 16px. Never cramped.
7. **Tables are the hero** — most of this app is data tables. Make them beautiful: alternating subtle row backgrounds, sticky headers, clean sort indicators.
8. **No default blue** — Tailwind's default blue is banned. All blues replaced by amber or status colors.
9. **Micro-animations** — page transitions (150ms fade), table row hovers (100ms), sidebar expand/collapse (200ms ease). Nothing jarring.
10. **Empty states** — every table/list must have a designed empty state with icon + message + action button.

---

## PROJECT STRUCTURE

```
eams-frontend/
├── public/
├── src/
│   ├── main.tsx
│   ├── App.tsx
│   │
│   ├── api/                          ← Axios API calls, one file per domain
│   │   ├── client.ts                 ← Axios instance with JWT interceptor
│   │   ├── auth.ts
│   │   ├── assets.ts
│   │   ├── certificates.ts
│   │   ├── licences.ts
│   │   ├── transactions.ts
│   │   ├── users.ts
│   │   ├── roles.ts
│   │   ├── accessGrants.ts
│   │   ├── changeRequests.ts
│   │   ├── auditLogs.ts
│   │   ├── notifications.ts
│   │   ├── reports.ts
│   │   ├── ai.ts
│   │   └── dashboard.ts
│   │
│   ├── store/                        ← Zustand stores
│   │   ├── authStore.ts              ← JWT, user info, org info
│   │   ├── uiStore.ts                ← Sidebar state, active modal, theme
│   │   └── notificationStore.ts      ← Unread count, in-app notifications
│   │
│   ├── hooks/                        ← Custom TanStack Query hooks
│   │   ├── useAssets.ts
│   │   ├── useCertificates.ts
│   │   ├── useLicences.ts
│   │   ├── useUsers.ts
│   │   ├── useAuditLogs.ts
│   │   ├── useDashboard.ts
│   │   └── useNotifications.ts
│   │
│   ├── components/
│   │   ├── layout/
│   │   │   ├── AppShell.tsx          ← Main layout wrapper
│   │   │   ├── Sidebar.tsx           ← Collapsible navigation sidebar
│   │   │   ├── TopBar.tsx            ← Search, notifications, user menu
│   │   │   └── PageHeader.tsx        ← Consistent page title + breadcrumb
│   │   │
│   │   ├── ui/                       ← Base components (extend shadcn)
│   │   │   ├── Button.tsx
│   │   │   ├── Badge.tsx             ← Status badges (critical/warning/valid etc)
│   │   │   ├── Card.tsx
│   │   │   ├── DataTable.tsx         ← Reusable TanStack Table wrapper
│   │   │   ├── Modal.tsx
│   │   │   ├── Drawer.tsx
│   │   │   ├── Input.tsx
│   │   │   ├── Select.tsx
│   │   │   ├── DatePicker.tsx
│   │   │   ├── FileUpload.tsx
│   │   │   ├── SearchInput.tsx
│   │   │   ├── Pagination.tsx
│   │   │   ├── Tooltip.tsx
│   │   │   ├── ConfirmDialog.tsx
│   │   │   ├── EmptyState.tsx        ← Reusable empty state
│   │   │   ├── LoadingSpinner.tsx
│   │   │   ├── StatusDot.tsx         ← Color dot for status
│   │   │   ├── AssetIdBadge.tsx      ← Monospace asset ID chip
│   │   │   ├── CriticalityBadge.tsx
│   │   │   ├── ExpiryCountdown.tsx   ← "14 days" with color coding
│   │   │   └── SectionHeader.tsx
│   │   │
│   │   ├── charts/
│   │   │   ├── StatusDonutChart.tsx
│   │   │   ├── ExpiryTimelineChart.tsx
│   │   │   ├── UtilisationBarChart.tsx
│   │   │   ├── AuditActivityChart.tsx
│   │   │   └── PredictionTrendChart.tsx
│   │   │
│   │   └── features/                 ← Feature-specific components
│   │       ├── assets/
│   │       ├── certificates/
│   │       ├── licences/
│   │       ├── transactions/
│   │       ├── access-grants/
│   │       ├── change-requests/
│   │       ├── users/
│   │       ├── audit/
│   │       └── ai/
│   │
│   ├── pages/
│   │   ├── auth/
│   │   │   ├── LoginPage.tsx
│   │   │   └── MagicLinkVerifyPage.tsx
│   │   ├── dashboard/
│   │   │   └── DashboardPage.tsx
│   │   ├── assets/
│   │   │   ├── AssetListPage.tsx
│   │   │   ├── AssetDetailPage.tsx
│   │   │   └── AssetCreatePage.tsx
│   │   ├── certificates/
│   │   │   ├── CertificateListPage.tsx
│   │   │   └── CertificateDetailPage.tsx
│   │   ├── licences/
│   │   │   ├── LicenceListPage.tsx
│   │   │   └── LicenceDetailPage.tsx
│   │   ├── transactions/
│   │   │   └── TransactionPage.tsx
│   │   ├── access-grants/
│   │   │   └── AccessGrantPage.tsx
│   │   ├── change-requests/
│   │   │   └── ChangeRequestPage.tsx
│   │   ├── users/
│   │   │   ├── UserListPage.tsx
│   │   │   └── UserDetailPage.tsx
│   │   ├── audit/
│   │   │   └── AuditLogPage.tsx
│   │   ├── reports/
│   │   │   └── ReportsPage.tsx
│   │   ├── ai/
│   │   │   ├── AiDashboardPage.tsx
│   │   │   └── AiChatPage.tsx
│   │   └── admin/
│   │       ├── AdminPage.tsx
│   │       ├── CategoryConfigPage.tsx
│   │       └── RoleConfigPage.tsx
│   │
│   ├── types/                        ← TypeScript interfaces matching API DTOs
│   │   ├── asset.types.ts
│   │   ├── certificate.types.ts
│   │   ├── licence.types.ts
│   │   ├── user.types.ts
│   │   ├── audit.types.ts
│   │   └── ai.types.ts
│   │
│   ├── utils/
│   │   ├── formatters.ts             ← Date, currency, bytes formatters
│   │   ├── statusColors.ts           ← Status → CSS variable mapping
│   │   └── permissions.ts            ← Client-side permission checks
│   │
│   └── router/
│       ├── index.tsx                 ← All routes defined here
│       └── ProtectedRoute.tsx        ← JWT + role guard
│
├── index.html
├── tailwind.config.js               ← Extended with design system tokens
├── vite.config.ts
├── tsconfig.json
└── package.json
```

---

## PAGE-BY-PAGE SPECIFICATIONS

### 1. LOGIN PAGE (`/login`)

Design concept: **Dark, focused, minimal**. Not a typical split-panel. Full-dark canvas with a centered login card that feels premium.

```
Layout:
- Full viewport dark background (bg-base) with subtle grid pattern (CSS background-image)
- Centered card (max-width: 400px) on bg-surface with 2px border (bg-border)
- Top: E-AMS wordmark in DM Sans 600, amber accent dot on the "A"
- Tagline: "IT Asset Intelligence Platform" in text-secondary
- Divider line
- Email input with label "Work Email"
- Primary button: "Send Magic Link" (amber fill, dark text)
- Helper text: "We'll send a secure login link to your inbox. No password needed."
- Below card: SSO options (SAML / Google) as ghost buttons if configured
- Footer: "© 2026 E-AMS. Secure Enterprise Platform."
- No illustrations, no stock images, no gradients
```

### 2. MAGIC LINK VERIFY PAGE (`/auth/verify`)

```
- Same dark layout as login
- Shows email address the link was sent to
- If valid token: auto-redirects to dashboard with brief "Authenticated ✓" success state
- If invalid/expired: shows clear error with "Request a new link" button
- Loading state: subtle pulsing amber dot animation
```

### 3. MAIN LAYOUT — APP SHELL

```
Layout structure:
┌─────────────────────────────────────────────────────────┐
│  SIDEBAR (240px, collapsible to 56px icon rail)         │
│  ┌───────────┐  ┌──────────────────────────────────┐   │
│  │           │  │  TOP BAR (60px)                  │   │
│  │  SIDEBAR  │  ├──────────────────────────────────┤   │
│  │           │  │                                  │   │
│  │           │  │  PAGE CONTENT                    │   │
│  │           │  │                                  │   │
│  │           │  │                                  │   │
│  └───────────┘  └──────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
```

**Sidebar design:**
- Background: bg-surface (slightly lighter than base)
- Top: E-AMS logo + org name
- Navigation groups with subtle group labels (text-muted, 10px uppercase tracking-widest):
  ```
  OVERVIEW
    Dashboard
  
  ASSETS
    Asset Register
    Check-In / Check-Out
    Network Topology
  
  COMPLIANCE
    Certificates
    Licences
    Access Grants
    Change Requests
  
  PEOPLE
    Users & Roles
  
  GOVERNANCE
    Audit Log
    Reports
  
  INTELLIGENCE
    AI Analytics
    AI Assistant
  
  ADMINISTRATION (admin only)
    System Config
    Org Settings
  ```
- Active item: amber left border (3px) + amber text + subtle amber background tint
- Hover: bg-elevated transition
- Collapse toggle button at bottom
- User avatar + name at very bottom with logout option

**Top Bar:**
- Search bar (global search, opens full-screen search modal on focus)
- Notification bell with unread count badge (amber dot)
- "My Actions" icon with pending approvals count
- User avatar → dropdown: Profile, Settings, Switch Org, Sign Out

---

### 4. DASHBOARD PAGE (`/dashboard`)

The executive view. Data-dense but clean. Shows the health of the entire IT estate at a glance.

**Layout: 12-column grid**

```
Row 1 — KPI Strip (5 metric cards, equal width):
┌──────────┬──────────┬──────────┬──────────┬──────────┐
│ Total    │ Critical │Certs     │ Licences │ Open     │
│ Assets   │ Assets   │ Expiring │ >85% Used│ Actions  │
│ 2,847    │ 12       │ 8        │ 3        │ 24       │
│ +5 today │ ↑2 week  │ ≤30 days │ alert    │ pending  │
└──────────┴──────────┴──────────┴──────────┴──────────┘

Row 2 — Charts:
┌────────────────────────┬───────────────────────────────┐
│ Asset Status Donut     │ Expiry Timeline (next 12mo)   │
│ (col-span-4)           │ (col-span-8)                  │
└────────────────────────┴───────────────────────────────┘

Row 3 — Lists:
┌──────────────────┬──────────────────┬──────────────────┐
│ Certificates     │ Overdue          │ AI Insights       │
│ Expiring Soon    │ Returns          │ (plain English    │
│ (col-span-4)     │ (col-span-4)     │  summary)         │
│                  │                  │ (col-span-4)      │
└──────────────────┴──────────────────┴──────────────────┘

Row 4 — Recent Activity:
┌──────────────────────────────────────────────────────────┐
│ Recent Audit Activity (last 20 events, live-updating)    │
└──────────────────────────────────────────────────────────┘
```

**KPI Card design:**
- bg-surface, border bg-border
- Large number: DM Sans 700, 36px, text-primary
- Label: IBM Plex Sans 400, 13px, text-secondary
- Trend indicator: small colored arrow + delta text
- Amber accent line on top border for "alert" state

**AI Insights Panel:**
- Special treatment — slightly different bg (#161C2A) with amber left border 3px
- Header: "AI Intelligence" with small AI sparkle icon
- Shows 3-4 bullet insights from the AI in plain English
- "View Full Analysis →" link
- Subtle "Powered by AI" label in text-muted (bottom, 11px)

---

### 5. ASSET LIST PAGE (`/assets`)

The most-used page. Must be fast, filterable, and scannable.

**Layout:**
```
PageHeader: "Asset Register" | breadcrumb | "Add Asset" button (amber)

Filter Bar (horizontal, below header):
[Search...] [Category ▼] [Location ▼] [Department ▼] [Status ▼] [Criticality ▼] [Clear Filters]

Active filters shown as dismissable chips below filter bar.

Data Table (full width, sticky header):
Columns:
- Asset ID (monospace, amber text, clickable)
- Name
- Category (icon + text)
- Location
- Status (colored badge)
- Criticality (colored badge)
- Assigned To
- Last Audited
- Actions (⋯ menu: View, Edit, Check Out, Relationships, Delete)

Table features:
- Column sorting (click header)
- Row selection (checkboxes) for bulk operations
- Click row → Asset Detail Drawer slides in from right (don't navigate away)
- Pagination: 25/50/100 per page
- Export button: XLSX / PDF / CSV (top right)
- Bulk Update button appears when rows selected
```

**Status Badges:**
```tsx
// Use consistent styling:
active      → green text, green-tint bg
inactive    → gray text, gray-tint bg
in_maintenance → amber text, amber-tint bg
decommissioned → red text, red-tint bg
lost/stolen → red bold
```

**Asset Detail Drawer (right-side panel, 600px):**
- Full asset details in tabbed layout:
  - Overview | Extended Fields | Relationships | Certificates/Licences | History | Attachments
- Edit button opens full-page edit form
- Certificate expiry and licence status prominently shown
- CMDB relationships shown as connected nodes diagram (simple)

---

### 6. ASSET CREATE / EDIT PAGE

**Multi-step form (wizard pattern):**

```
Step indicator at top:
① Basic Info → ② Category Details → ③ Ownership → ④ Financial → ⑤ Review

Step 1 - Basic Info:
  Asset Name*, Category* (card select, not dropdown), Description, Status, Criticality, Location*, Barcode

Step 2 - Category Details:
  Dynamic form — renders fields based on selected category
  (Software: app_name, version, licence_type, hosting_type, etc.)
  (Server: server_type, manufacturer, serial_number, OS, etc.)
  (Network: device_category, firmware_version, mgmt_ip, etc.)
  Also renders any custom fields defined for the category

Step 3 - Ownership:
  Technical Owner, Business Owner, Backup Owner, Department, Assigned User

Step 4 - Financial:
  Acquisition Cost, Annual Cost, Currency, Purchase Order Ref, Vendor, 
  Warranty Expiry, Support Contract, Support Expiry

Step 5 - Review:
  Summary of all entered data before submission
  "Save Asset" button (amber, full width)
```

---

### 7. CERTIFICATES PAGE (`/certificates`)

**Layout:**
```
PageHeader: "Certificate Register" | Add Certificate button

Status Summary Strip:
[CRITICAL: 3] [WARNING: 8] [REVIEW: 15] [VALID: 142] [EXPIRED: 2]
(Clicking each filters the table)

View Toggle: Table | Calendar (Renewal Calendar for next 12 months)

Table columns:
- Common Name (CN)
- Type (badge)
- Issuing Authority
- Expiry Date
- Days Remaining (ExpiryCountdown component — colored based on urgency)
- Status (badge)
- Environment (badge)
- Owner
- Actions

ExpiryCountdown component:
- ≤30 days: red pulsing badge "30 days"
- 31-90 days: orange badge "58 days"
- 91-180 days: yellow badge "145 days"
- >180 days: muted text "256 days"
- EXPIRED: red bold "EXPIRED"
```

**Renewal Calendar View:**
- Monthly grid layout showing which certs expire each month
- Color-coded by urgency
- Click a cert → opens detail drawer

---

### 8. LICENCES PAGE (`/licences`)

```
PageHeader: "Licence Management"

Table columns:
- Software / Asset Name
- Licence Type (badge)
- Seats: "142 / 200" with progress bar (amber → red when >85%)
- Utilisation % (colored)
- Expiry Date + countdown
- Status (badge)
- Vendor
- Actions

Seat Utilisation Progress Bar:
- 0-70%: subtle green progress
- 71-85%: amber progress
- 86-100%: red progress, pulsing
- Over 100%: red background, "OVER LIMIT" badge

Compliance Summary Card at top:
"Your licence estate is 94.2% compliant. 3 licences require attention."
```

---

### 9. ACCESS GRANTS PAGE (`/access-grants`)

```
PageHeader: "Temporary Access Log"

IMPORTANT: Overdue grants highlighted in red rows (not just badges)

Table columns:
- Grant Type (badge: vendor_session / emergency_access / etc.)
- Target Asset
- Granted To
- Approved By
- Granted At
- Expires At
- Time Remaining (countdown — aggressive red when overdue)
- Status
- Actions: Revoke | View Details

"Request New Access" button opens a modal form with:
- Access Type (dropdown)
- Target Asset (searchable select)
- Business Justification (required textarea)
- Risk Level
- Ticket Reference
- Expiry Date/Time (mandatory)
System enforces: approver ≠ requestor
```

---

### 10. CHANGE REQUESTS PAGE (`/change-requests`)

```
Kanban-style status board (top section) showing counts per status:
[PROPOSED: 5] [APPROVED: 3] [APPLIED: 8] [PENDING REVERT: 4 🔴] [REVERTED: 23]

PENDING REVERT items are ALWAYS shown first, flagged with overdue indicator.

Table below kanban:
Columns:
- Change Ref (monospace, CHG-2026-0142)
- Target Asset
- Change Type
- Proposed By
- Approved By
- Scheduled Revert (colored countdown)
- Status
- Is Overdue (boolean indicator)
- Actions

"Overdue Reverts" alert banner at top of page if any exist:
"⚠ 2 configuration changes are past their scheduled revert date. Immediate action required."
```

---

### 11. AUDIT LOG PAGE (`/audit-logs`)

```
PageHeader: "Audit Trail" (read-only for most roles)

Advanced filter bar:
[Date Range] [User] [Action Type] [Resource Type] [Asset]

Table — dense, monospace timestamps:
Columns:
- Timestamp (IBM Plex Mono, 13px)
- Actor
- Action (colored badge: create/update/delete/login/export/approve)
- Resource Type
- Resource Name
- Field Changed
- Old Value → New Value
- IP Address

"Export Evidence Package" button → PDF with digital timestamp and watermark

"Tamper Detection" indicator at top:
Green check "✓ Audit log integrity verified" or Red warning if HMAC mismatch
```

---

### 12. AI ASSISTANT PAGE (`/ai/chat`)

```
Design: Split panel — conversational interface

Left panel (300px): Session History
- List of past sessions with auto-generated titles
- "New Chat" button
- Search sessions

Right panel: Chat Interface
- Messages displayed in clean chat bubbles
  - User messages: right-aligned, amber-tinted bubble
  - AI messages: left-aligned, bg-elevated bubble
- AI responses can include:
  - Plain text narrative
  - Embedded data tables (styled like the main tables)
  - Asset ID chips (clickable, open asset detail)
  - Charts (inline Recharts)
  - "Action" buttons (e.g., "View All Unpatched Servers →")
- Input bar at bottom:
  - Textarea (auto-expand)
  - "Currently viewing: {asset name}" context chip (if scoped)
  - Send button (amber)
  - "Clear Context" option

Example queries shown as chips above input when no messages:
"Which servers haven't been patched in 90 days?"
"Show critical assets in Nairobi branch"
"Certificates expiring in the next 30 days"
"Licences with utilisation above 85%"

AI responses always end with:
Confidence indicator (e.g., "Based on 247 asset records") in text-muted
```

---

### 13. REPORTS PAGE (`/reports`)

```
PageHeader: "Reports & Analytics"

Report Cards Grid (2 columns):
┌─────────────────────────────┬─────────────────────────────┐
│ Asset Inventory Report       │ Licence Compliance Report    │
│ Full asset register export   │ Seat utilisation + expiry    │
│ [Generate PDF] [Export XLSX] │ [Generate PDF] [Export XLSX] │
├─────────────────────────────┼─────────────────────────────┤
│ IT Audit Report              │ Security Posture Report      │
│ Full change/access trail     │ Certificate + access health  │
│ Date range picker            │ AI-enhanced summary          │
├─────────────────────────────┼─────────────────────────────┤
│ Depreciation Report          │ Custom Report Builder        │
│ Asset value over time        │ Build your own report        │
└─────────────────────────────┴─────────────────────────────┘

All reports generated on demand via API.
PDF reports open in new tab (or download).
"Schedule Report" button on each card opens cron-style scheduler modal.

AI Report Narration:
Each generated report has an "AI Summary" section at the top:
Bordered amber-accent box with AI-generated plain English executive summary.
```

---

### 14. ADMIN PAGES (`/admin/*`)

```
Tabbed admin area:
Tabs: General | Asset Categories | Custom Fields | Roles & Permissions | 
      Notification Rules | Workflow Designer | Integrations | Branding

Asset Categories tab:
- Table of categories: name, code, icon, fields count, active
- Edit: modal form with all category settings
- Drag to reorder (dnd-kit)

Custom Fields tab:
- Per-category field management
- Drag to reorder fields
- Field type configuration

Roles & Permissions tab:
- Role cards showing role name + permission matrix
- Toggle switches for can_create/can_read/can_update/can_delete/can_approve/can_export
- Category-scoped and location-scoped permission assignment

Workflow Designer tab:
- Visual drag-and-drop workflow builder (dnd-kit)
- Nodes: Initiate → Approve → Notify → Complete
- Connect nodes with arrows
- Configure each node: actor role, timeout, escalation target
```

---

## MY ACTIONS — GLOBAL PANEL

```
Persistent "My Actions" drawer accessible from top bar icon.
Shows all items requiring the current user's attention:
- Pending approval requests (approve/reject inline)
- Overdue asset returns I manage
- Change reverts I need to action
- Access grants I need to revoke
- Upcoming certificate renewals I own

Badge count on icon in top bar updates in real-time via polling every 30 seconds.
```

---

## NOTIFICATIONS PANEL

```
Notification bell in top bar opens a right-side panel (not a dropdown).
Panel shows last 50 notifications:
- Icon (cert/licence/access/change/system) + title + timestamp
- Color-coded by severity
- "Mark all as read" button
- Infinite scroll for older notifications
- Click → navigates to relevant page with item highlighted
```

---

## COMPONENT SPECIFICATIONS

### DataTable (most important component):

```tsx
// Must support:
// - Column sorting with visual indicators
// - Column visibility toggle
// - Global search filter
// - Row selection with checkboxes
// - Bulk action bar when rows selected
// - Pagination (server-side)
// - Loading skeleton rows
// - Empty state with icon + message + optional action
// - Sticky header on scroll
// - Responsive (horizontal scroll on mobile)
// - Export to XLSX / PDF / CSV
// - Row hover: subtle bg-elevated transition
// - Alternating row bg: transparent / very subtle white 2% opacity

// All column definitions typed with TanStack Table ColumnDef<T>
```

### StatusBadge:
```tsx
// Maps status string → consistent pill badge
// critical → red pill
// warning → orange pill  
// review_soon → yellow pill
// valid / active → green pill
// inactive → gray pill
// Customise: size (sm/md), variant (solid/outline)
```

### ExpiryCountdown:
```tsx
// daysRemaining: number
// if <= 0: "EXPIRED" — red bold
// if 1-7: "{n} days" — red pulsing
// if 8-30: "{n} days" — orange
// if 31-90: "{n} days" — yellow
// if 91-180: "{n} days" — text-secondary
// if > 180: just the date — text-muted
```

### AssetIdBadge:
```tsx
// Renders: APP-NBI-2026-0042
// Font: IBM Plex Mono, 12px
// Bg: bg-elevated, border bg-border
// Amber text
// Copy-to-clipboard on click with toast feedback
```

---

## ROUTING & GUARDS

```tsx
// All routes wrapped in ProtectedRoute
// ProtectedRoute checks: JWT valid + not expired + user.is_active
// Role-based route access: admin routes check isSystemAdmin
// Unknown routes → 404 page
// Unauthenticated → redirect to /login

// Route structure:
/login                    → LoginPage (public)
/auth/verify              → MagicLinkVerifyPage (public)
/dashboard                → DashboardPage (protected)
/assets                   → AssetListPage
/assets/new               → AssetCreatePage
/assets/:id               → AssetDetailPage
/assets/:id/edit          → AssetEditPage
/certificates             → CertificateListPage
/certificates/:id         → CertificateDetailPage
/licences                 → LicenceListPage
/licences/:id             → LicenceDetailPage
/transactions             → TransactionPage
/access-grants            → AccessGrantPage
/change-requests          → ChangeRequestPage
/users                    → UserListPage
/users/:id                → UserDetailPage
/audit                    → AuditLogPage
/reports                  → ReportsPage
/ai                       → AiDashboardPage
/ai/chat                  → AiChatPage
/admin                    → AdminPage (admin only)
/admin/categories         → CategoryConfigPage
/admin/roles              → RoleConfigPage
```

---

## API CLIENT SETUP

```typescript
// src/api/client.ts
// Axios instance with:
// - Base URL from VITE_API_BASE_URL env var
// - Request interceptor: attach Bearer token from authStore
// - Response interceptor: 
//   - 401 → attempt token refresh via /auth/refresh
//   - If refresh fails → clear auth, redirect to /login
//   - 403 → show toast "You don't have permission for this action"
//   - 422 → extract validation errors, attach to form errors
//   - 500 → show generic error toast
// - Request/response logging in dev mode
```

---

## TAILWIND CONFIG

Extend tailwind.config.js with custom colors matching the design system CSS variables:

```javascript
// tailwind.config.js
colors: {
  base: '#0D0F12',
  surface: '#141720',
  elevated: '#1C2030',
  border: '#252A3A',
  accent: {
    DEFAULT: '#F59E0B',
    secondary: '#D97706',
    subtle: '#1A1600',
    muted: '#78350F',
  },
  // Status colors
  critical: '#EF4444',
  warning: '#F97316',
  review: '#EAB308',
  valid: '#22C55E',
}

fontFamily: {
  display: ['DM Sans', 'sans-serif'],
  body: ['IBM Plex Sans', 'sans-serif'],
  mono: ['IBM Plex Mono', 'monospace'],
}
```

---

## ENVIRONMENT VARIABLES

```env
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_APP_NAME=E-AMS
VITE_ENABLE_AI=true
```

---

## DELIVERABLES CHECKLIST

Ensure ALL of the following are implemented:

- [ ] All 14+ pages fully implemented (not stubs)
- [ ] Sidebar navigation with all sections and active state
- [ ] AppShell with topbar, search, notifications
- [ ] Magic Link login + verify flow
- [ ] Protected routes with role guards
- [ ] DataTable component (reusable, all features)
- [ ] All status badges and ExpiryCountdown components
- [ ] Asset ID badge (monospace, copy-to-clipboard)
- [ ] Dashboard with all KPI cards and charts (Recharts)
- [ ] Asset list with filters, sort, bulk select, export
- [ ] Asset create wizard (multi-step, dynamic category fields)
- [ ] Asset detail drawer (tabbed, shows all related data)
- [ ] Certificate list with calendar view toggle
- [ ] Licence list with seat utilisation progress bars
- [ ] Access grants with overdue highlighting
- [ ] Change requests with kanban + overdue banner
- [ ] Audit log with tamper detection indicator
- [ ] AI chat interface (streaming responses, embedded tables)
- [ ] Reports page with all report types
- [ ] Admin pages with role/permission matrix toggles
- [ ] My Actions drawer
- [ ] Notifications panel
- [ ] Empty states for every table
- [ ] Loading skeleton states
- [ ] Toast notifications via Sonner
- [ ] Full TypeScript typing (no `any`)
- [ ] Responsive layout (minimum 768px)
- [ ] Dark theme applied everywhere (no white backgrounds)
- [ ] Consistent design tokens (CSS variables) used throughout
- [ ] README with setup instructions
