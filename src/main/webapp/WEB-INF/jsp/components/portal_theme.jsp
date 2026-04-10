<style>
    body[data-theme='dark'] {
        background: #020617;
        color: #e2e8f0;
    }

    body[data-theme='dark'] .bg-background-light {
        background-color: #020617 !important;
    }

    body[data-theme='dark'] .bg-white,
    body[data-theme='dark'] .portal-summary-card,
    body[data-theme='dark'] .portal-panel,
    body[data-theme='dark'] .portal-filter-bar,
    body[data-theme='dark'] .portal-stat-card,
    body[data-theme='dark'] .portal-upload-surface,
    body[data-theme='dark'] .portal-modal-card {
        background: rgba(15, 23, 42, 0.96) !important;
        border-color: #334155 !important;
        box-shadow: 0 12px 32px -24px rgba(2, 6, 23, 0.85) !important;
    }

    body[data-theme='dark'] .portal-panel--accent,
    body[data-theme='dark'] .portal-callout {
        background: linear-gradient(135deg, rgba(15, 23, 42, 0.96), rgba(30, 41, 59, 0.98)) !important;
        border-color: #334155 !important;
    }

    body[data-theme='dark'] .portal-panel--danger {
        background: rgba(69, 10, 10, 0.45) !important;
        border-color: rgba(248, 113, 113, 0.35) !important;
    }

    body[data-theme='dark'] .bg-slate-50,
    body[data-theme='dark'] .bg-slate-50\/60,
    body[data-theme='dark'] .bg-slate-50\/80,
    body[data-theme='dark'] .bg-slate-100,
    body[data-theme='dark'] .bg-slate-200 {
        background-color: #0f172a !important;
    }

    body[data-theme='dark'] .border-slate-50,
    body[data-theme='dark'] .border-slate-100,
    body[data-theme='dark'] .border-slate-200,
    body[data-theme='dark'] .border-primary\/10,
    body[data-theme='dark'] .divide-slate-100 > *,
    body[data-theme='dark'] .divide-primary\/5 > * {
        border-color: #334155 !important;
    }

    body[data-theme='dark'] .text-slate-900,
    body[data-theme='dark'] .text-slate-800,
    body[data-theme='dark'] .text-slate-700 {
        color: #e2e8f0 !important;
    }

    body[data-theme='dark'] .text-slate-600,
    body[data-theme='dark'] .text-slate-500,
    body[data-theme='dark'] .text-slate-400 {
        color: #94a3b8 !important;
    }

    body[data-theme='dark'] .text-primary {
        color: #e2e8f0 !important;
    }

    body[data-theme='dark'] .portal-page-title,
    body[data-theme='dark'] .portal-section-title,
    body[data-theme='dark'] .portal-section-link {
        color: #f8fafc !important;
    }

    body[data-theme='dark'] .portal-btn-secondary {
        background: #0f172a !important;
        border-color: #334155 !important;
        color: #e2e8f0 !important;
    }

    body[data-theme='dark'] .portal-btn-secondary:hover {
        background: #1e293b !important;
    }

    body[data-theme='dark'] .settings-input {
        background: #0f172a !important;
        border-color: #334155 !important;
        color: #e2e8f0 !important;
    }

    body[data-theme='dark'] .settings-input[readonly] {
        background: #111827 !important;
        color: #94a3b8 !important;
    }

    body[data-theme='dark'] .settings-label,
    body[data-theme='dark'] .portal-kicker {
        color: #94a3b8 !important;
    }

    body[data-theme='dark'] header,
    body[data-theme='dark'] aside {
        background: rgba(2, 6, 23, 0.95) !important;
        border-color: #334155 !important;
    }

    body[data-theme='dark'] table thead,
    body[data-theme='dark'] tr.bg-slate-50 {
        background: #0f172a !important;
    }

    body[data-theme='dark'] input::placeholder,
    body[data-theme='dark'] textarea::placeholder {
        color: #64748b !important;
    }

    .portal-page {
        width: 100%;
        max-width: 72rem;
        margin-left: auto;
        margin-right: auto;
        display: flex;
        flex-direction: column;
        gap: 1.5rem;
    }

    .portal-page--compact {
        max-width: 64rem;
    }

    .portal-page--detail {
        max-width: 68rem;
    }

    .portal-page-header {
        display: flex;
        flex-direction: column;
        gap: 1rem;
        justify-content: space-between;
    }

    @media (min-width: 768px) {
        .portal-page-header {
            flex-direction: row;
            align-items: flex-end;
        }
    }

    .portal-page-title {
        color: rgb(15 23 42);
        font-size: 1.875rem;
        line-height: 2.25rem;
        font-weight: 900;
        letter-spacing: -0.025em;
    }

    @media (min-width: 1024px) {
        .portal-page-title {
            font-size: 2.25rem;
            line-height: 2.5rem;
        }
    }

    .portal-page-copy {
        margin-top: 0.5rem;
        max-width: 42rem;
        font-size: 0.95rem;
        line-height: 1.6;
        color: rgb(100 116 139);
    }

    .portal-summary-card,
    .portal-panel,
    .portal-filter-bar,
    .portal-stat-card,
    .portal-callout,
    .portal-upload-surface,
    .portal-modal-card {
        border: 1px solid rgb(226 232 240);
        background: rgba(255, 255, 255, 0.98);
        box-shadow: 0 1px 2px rgba(15, 23, 42, 0.06);
    }

    .portal-summary-card,
    .portal-panel,
    .portal-filter-bar,
    .portal-stat-card,
    .portal-callout,
    .portal-upload-surface {
        border-radius: 1rem;
    }

    .portal-modal-card {
        border-radius: 1.25rem;
    }

    .portal-summary-card {
        padding: 1rem 1.25rem;
    }

    .portal-panel--soft {
        background: linear-gradient(180deg, rgba(248, 250, 252, 0.96), rgba(255, 255, 255, 0.98));
    }

    .portal-panel--accent,
    .portal-callout {
        border-color: rgba(15, 23, 42, 0.08);
        background: linear-gradient(135deg, rgba(239, 246, 255, 0.96), rgba(248, 250, 252, 0.98));
    }

    .portal-panel--danger {
        border-color: rgb(254 202 202);
        background: rgba(254, 242, 242, 0.76);
    }

    .portal-kicker {
        font-size: 11px;
        font-weight: 800;
        letter-spacing: 0.18em;
        text-transform: uppercase;
        color: rgb(148 163 184);
    }

    .portal-section-bar {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 1rem;
        padding-left: 0.5rem;
        padding-right: 0.5rem;
    }

    .portal-section-title {
        color: rgb(15 23 42);
        font-size: 1.125rem;
        line-height: 1.75rem;
        font-weight: 800;
    }

    .portal-section-link {
        font-size: 0.875rem;
        font-weight: 700;
        color: rgb(15 23 42);
        transition: color 0.2s ease;
    }

    .portal-section-link:hover {
        color: rgb(59 130 246);
    }

    .portal-btn {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        gap: 0.5rem;
        min-height: 2.75rem;
        border-radius: 0.875rem;
        padding: 0.75rem 1.25rem;
        font-size: 0.875rem;
        font-weight: 700;
        transition: background-color 0.2s ease, color 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
    }

    .portal-btn:hover {
        transform: translateY(-1px);
    }

    .portal-btn:focus-visible {
        outline: 2px solid rgba(59, 130, 246, 0.4);
        outline-offset: 2px;
    }

    .portal-btn-primary {
        background: rgb(15 23 42);
        color: rgb(255 255 255);
        box-shadow: 0 12px 24px -18px rgba(15, 23, 42, 0.85);
    }

    .portal-btn-primary:hover {
        background: rgb(30 41 59);
    }

    .portal-btn-secondary {
        border: 1px solid rgb(226 232 240);
        background: rgb(255 255 255);
        color: rgb(51 65 85);
        box-shadow: 0 1px 2px rgba(15, 23, 42, 0.06);
    }

    .portal-btn-secondary:hover {
        background: rgb(248 250 252);
    }

    .portal-btn-danger {
        background: rgb(220 38 38);
        color: rgb(255 255 255);
        box-shadow: 0 12px 24px -18px rgba(220, 38, 38, 0.9);
    }

    .portal-btn-danger:hover {
        background: rgb(185 28 28);
    }

    .portal-stat-card {
        padding: 1.5rem;
        transition: box-shadow 0.2s ease, transform 0.2s ease;
    }

    .portal-stat-card:hover {
        transform: translateY(-1px);
        box-shadow: 0 12px 24px -20px rgba(15, 23, 42, 0.28);
    }

    .portal-filter-bar {
        padding: 1rem;
    }

    .portal-callout {
        padding: 1rem;
    }

    .portal-upload-surface {
        border-style: dashed;
        border-width: 1.5px;
        padding: 2.5rem;
        text-align: center;
        transition: background-color 0.2s ease, border-color 0.2s ease;
    }

    .portal-upload-surface:hover {
        border-color: rgba(15, 23, 42, 0.18);
        background: rgba(248, 250, 252, 0.96);
    }
</style>
