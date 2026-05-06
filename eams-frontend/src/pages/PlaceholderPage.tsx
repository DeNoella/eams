export default function PlaceholderPage({ title }: { title: string }) {
  return (
    <div className="page-content">
      <div className="page-header">
        <h1 className="page-title">{title}</h1>
        <p className="page-subtitle">This module is reserved for the next SRS iteration. Navigation and RBAC are wired.</p>
      </div>
      <div className="card">
        <p className="page-subtitle">Use Assets, Certificates, Licences, Users, Reports, Dashboard, and AI Analytics for live flows.</p>
      </div>
    </div>
  )
}
