export default function SimilarProductsSkeleton() {
  return (
    <section className="similar-products" style={{ marginTop: '3rem', paddingTop: '2rem', borderTop: '1px solid #e2e8f0' }}>
      <div className="section-header" style={{ marginBottom: '1.5rem' }}>
        <div style={{ height: '2rem', width: '200px', backgroundColor: '#e2e8f0', borderRadius: '4px' }}></div>
      </div>
      <div 
        className="similar-products-grid" 
        style={{ 
          display: 'grid', 
          gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', 
          gap: '1.5rem' 
        }}
      >
        {[1, 2, 3, 4].map(i => (
          <div key={i} style={{ height: '300px', backgroundColor: '#f8fafc', borderRadius: '8px' }}></div>
        ))}
      </div>
    </section>
  );
}
