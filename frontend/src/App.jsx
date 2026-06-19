import React, { useState, useEffect } from 'react';

// API Gateway base URL.
// This works for both local Docker Compose and EC2/public-IP access.
const GATEWAY_URL = `${window.location.protocol}//${window.location.hostname}:8080`;

export default function App() {
  // Navigation & Page State
  const [currentPage, setCurrentPage] = useState('home');
  const [products, setProducts] = useState([]);
  const [selectedProduct, setSelectedProduct] = useState(null);
  
  // Search & Filters
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('');
  const [categories] = useState(['All', 'Electronics', 'Accessories', 'Apparel']);

  // Authentication State
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('user');
    return saved ? JSON.parse(saved) : null;
  });
  
  // Login / Register Form State
  const [authMode, setAuthMode] = useState('login'); // 'login' or 'register'
  const [usernameInput, setUsernameInput] = useState('');
  const [passwordInput, setPasswordInput] = useState('');
  const [emailInput, setEmailInput] = useState('');
  const [isAdminInput, setIsAdminInput] = useState(false);

  // Cart State
  const [cart, setCart] = useState({ items: [], totalAmount: 0 });
  const [shippingAddress, setShippingAddress] = useState('');
  const [cardNumber, setCardNumber] = useState('4111111111111111');
  const [cvv, setCvv] = useState('123');

  // Orders State
  const [orders, setOrders] = useState([]);

  // Admin Product Form State
  const [newProdName, setNewProdName] = useState('');
  const [newProdDesc, setNewProdDesc] = useState('');
  const [newProdPrice, setNewProdPrice] = useState('');
  const [newProdCat, setNewProdCat] = useState('Electronics');
  const [newProdImg, setNewProdImg] = useState('');
  
  // Notifications Debug Logs (Fetched from notification-service via Gateway)
  const [notificationLogs, setNotificationLogs] = useState([]);

  // UI Status State
  const [loading, setLoading] = useState(false);
  const [alert, setAlert] = useState({ show: false, message: '', type: '' });

  // Helper to show banners
  const showAlert = (message, type = 'success') => {
    setAlert({ show: true, message, type });
    setTimeout(() => setAlert({ show: false, message: '', type: '' }), 5000);
  };

  // Fetch Products catalog
  const fetchProducts = async (search = '', cat = '') => {
    setLoading(true);
    try {
      let url = `${GATEWAY_URL}/api/products`;
      if (search) {
        url += `?search=${encodeURIComponent(search)}`;
      } else if (cat && cat !== 'All') {
        url += `?category=${encodeURIComponent(cat)}`;
      }
      const res = await fetch(url);
      if (res.ok) {
        const data = await res.json();
        setProducts(data);
      } else {
        showAlert('Failed to load products catalog', 'error');
      }
    } catch (e) {
      showAlert('Gateway unreachable. Make sure services are running!', 'error');
    } finally {
      setLoading(false);
    }
  };

  // Fetch Cart (if logged in)
  const fetchCart = async () => {
    if (!user) return;
    try {
      const res = await fetch(`${GATEWAY_URL}/api/cart?username=${user.username}`);
      if (res.ok) {
        const data = await res.json();
        setCart(data);
      }
    } catch (e) {
      console.error('Error fetching cart:', e);
    }
  };

  // Fetch Orders (if logged in)
  const fetchOrders = async () => {
    if (!user) return;
    try {
      const res = await fetch(`${GATEWAY_URL}/api/orders?username=${user.username}`);
      if (res.ok) {
        const data = await res.json();
        setOrders(data);
      }
    } catch (e) {
      console.error('Error fetching orders:', e);
    }
  };

  // Fetch Notification Logs
  const fetchNotifications = async () => {
    try {
      const res = await fetch(`${GATEWAY_URL}/api/notifications`);
      if (res.ok) {
        const data = await res.json();
        setNotificationLogs(data);
      }
    } catch (e) {
      console.error('Error fetching notification logs:', e);
    }
  };

  // Run on mount and filter change
  useEffect(() => {
    fetchProducts(searchQuery, selectedCategory);
  }, [selectedCategory]);

  useEffect(() => {
    if (user) {
      fetchCart();
      fetchOrders();
    } else {
      setCart({ items: [], totalAmount: 0 });
      setOrders([]);
    }
    fetchNotifications();
  }, [user]);

  // Periodic check for notification updates
  useEffect(() => {
    const interval = setInterval(fetchNotifications, 7000);
    return () => clearInterval(interval);
  }, []);

  // Handle Authentication (Login / Register)
  const handleAuth = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const endpoint = authMode === 'login' ? '/api/users/login' : '/api/users/register';
      const payload = authMode === 'login' 
        ? { username: usernameInput, password: passwordInput }
        : { username: usernameInput, password: passwordInput, email: emailInput, role: isAdminInput ? 'ROLE_ADMIN' : 'ROLE_USER' };

      const res = await fetch(`${GATEWAY_URL}${endpoint}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      if (res.ok) {
        const data = await res.json();
        if (authMode === 'login') {
          setUser(data);
          localStorage.setItem('user', JSON.stringify(data));
          showAlert(`Welcome back, ${data.username}!`);
          setCurrentPage('products');
        } else {
          showAlert('Registration successful! Please login.');
          setAuthMode('login');
        }
        setPasswordInput('');
      } else {
        const errText = await res.text();
        showAlert(errText || 'Authentication failed', 'error');
      }
    } catch (err) {
      showAlert('Connection to user-service failed', 'error');
    } finally {
      setLoading(false);
    }
  };

  // Logout
  const handleLogout = () => {
    setUser(null);
    localStorage.removeItem('user');
    showAlert('Logged out successfully');
    setCurrentPage('home');
  };

  // Add Item to Cart
  const handleAddToCart = async (product) => {
    if (!user) {
      showAlert('Please login to add items to cart', 'error');
      setCurrentPage('login');
      return;
    }
    try {
      const payload = {
        productId: product.id,
        productName: product.name,
        price: product.price,
        quantity: 1,
        imageUrl: product.imageUrl
      };
      const res = await fetch(`${GATEWAY_URL}/api/cart?username=${user.username}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
      if (res.ok) {
        const updatedCart = await res.json();
        setCart(updatedCart);
        showAlert(`${product.name} added to cart!`);
      } else {
        showAlert('Could not add item to cart', 'error');
      }
    } catch (e) {
      showAlert('Cart service unavailable', 'error');
    }
  };

  // Update Cart Item Quantity
  const handleUpdateQuantity = async (productId, newQuantity) => {
    try {
      const res = await fetch(`${GATEWAY_URL}/api/cart/quantity?username=${user.username}&productId=${productId}&quantity=${newQuantity}`, {
        method: 'PUT'
      });
      if (res.ok) {
        const updatedCart = await res.json();
        setCart(updatedCart);
      }
    } catch (e) {
      showAlert('Failed to update cart quantity', 'error');
    }
  };

  // Remove Cart Item
  const handleRemoveFromCart = async (productId) => {
    try {
      const res = await fetch(`${GATEWAY_URL}/api/cart?username=${user.username}&productId=${productId}`, {
        method: 'DELETE'
      });
      if (res.ok) {
        const updatedCart = await res.json();
        setCart(updatedCart);
        showAlert('Item removed from cart');
      }
    } catch (e) {
      showAlert('Failed to remove item', 'error');
    }
  };

  // Place Order
  const handlePlaceOrder = async (e) => {
    e.preventDefault();
    if (!shippingAddress.trim()) {
      showAlert('Shipping address is required!', 'error');
      return;
    }
    setLoading(true);
    try {
      const payload = {
        username: user.username,
        shippingAddress,
        cardNumber,
        cvv
      };
      const res = await fetch(`${GATEWAY_URL}/api/orders`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      if (res.ok) {
        const completedOrder = await res.json();
        showAlert(`Order #${completedOrder.id} placed successfully!`);
        setShippingAddress('');
        fetchCart();
        fetchOrders();
        fetchNotifications();
        setCurrentPage('orders');
      } else {
        const errMsg = await res.text();
        showAlert(`Checkout failed: ${errMsg}`, 'error');
      }
    } catch (e) {
      showAlert('Order service failed', 'error');
    } finally {
      setLoading(false);
    }
  };

  // Create Product (Admin Only)
  const handleCreateProduct = async (e) => {
    e.preventDefault();
    if (!newProdName || !newProdPrice) {
      showAlert('Product name and price are required!', 'error');
      return;
    }
    setLoading(true);
    try {
      const payload = {
        name: newProdName,
        description: newProdDesc,
        price: parseFloat(newProdPrice),
        category: newProdCat,
        imageUrl: newProdImg || 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=500'
      };
      const res = await fetch(`${GATEWAY_URL}/api/products`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      if (res.ok) {
        showAlert('Product added to catalog!');
        setNewProdName('');
        setNewProdDesc('');
        setNewProdPrice('');
        setNewProdImg('');
        fetchProducts();
      } else {
        showAlert('Failed to add product', 'error');
      }
    } catch (e) {
      showAlert('Product service unavailable', 'error');
    } finally {
      setLoading(false);
    }
  };

  // Delete Product (Admin Only)
  const handleDeleteProduct = async (id) => {
    if (!window.confirm('Delete this product from catalog?')) return;
    try {
      const res = await fetch(`${GATEWAY_URL}/api/products/${id}`, {
        method: 'DELETE'
      });
      if (res.ok) {
        showAlert('Product deleted');
        fetchProducts();
      } else {
        showAlert('Failed to delete product', 'error');
      }
    } catch (e) {
      showAlert('Product service down', 'error');
    }
  };

  return (
    <div className="app-container">
      {/* Premium Navbar */}
      <header className="navbar">
        <div className="nav-brand" onClick={() => setCurrentPage('home')} style={{ cursor: 'pointer' }}>
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" style={{ stroke: 'url(#brandGrad)' }}>
            <defs>
              <linearGradient id="brandGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" stopColor="#6366f1" />
                <stop offset="100%" stopColor="#a855f7" />
              </linearGradient>
            </defs>
            <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"></path>
            <line x1="3" y1="6" x2="21" y2="6"></line>
            <path d="M16 10a4 4 0 0 1-8 0"></path>
          </svg>
          E-Shop DevOps
        </div>

        <nav className="nav-links">
          <span className={`nav-link ${currentPage === 'home' ? 'active' : ''}`} onClick={() => setCurrentPage('home')}>Home</span>
          <span className={`nav-link ${currentPage === 'products' ? 'active' : ''}`} onClick={() => setCurrentPage('products')}>Catalog</span>
          
          {user && (
            <>
              <span className={`nav-link ${currentPage === 'cart' ? 'active' : ''}`} onClick={() => setCurrentPage('cart')}>
                Cart {cart.items && cart.items.length > 0 && <span className="badge">{cart.items.reduce((acc, item) => acc + item.quantity, 0)}</span>}
              </span>
              <span className={`nav-link ${currentPage === 'orders' ? 'active' : ''}`} onClick={() => setCurrentPage('orders')}>Orders</span>
              {user.role === 'ROLE_ADMIN' && (
                <span className={`nav-link ${currentPage === 'admin' ? 'active' : ''}`} onClick={() => { setCurrentPage('admin'); fetchProducts(); }}>Admin</span>
              )}
            </>
          )}

          {user ? (
            <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
              <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Hello, <strong>{user.username}</strong></span>
              <button className="btn btn-secondary" onClick={handleLogout} style={{ padding: '0.4rem 0.8rem', fontSize: '0.85rem' }}>Logout</button>
            </div>
          ) : (
            <button className="btn btn-primary" onClick={() => { setAuthMode('login'); setCurrentPage('login'); }} style={{ padding: '0.4rem 1rem', fontSize: '0.85rem' }}>Login</button>
          )}
        </nav>
      </header>

      {/* Global Alert Notification Banner */}
      {alert.show && (
        <div style={{ maxWidth: '1280px', width: '100%', margin: '1rem auto 0 auto', padding: '0 1rem' }}>
          <div className={`alert-banner alert-${alert.type}`}>
            <span>{alert.message}</span>
            <button style={{ background: 'none', border: 'none', color: 'inherit', cursor: 'pointer' }} onClick={() => setAlert({ show: false })}>✕</button>
          </div>
        </div>
      )}

      {/* Main Pages router */}
      <main className="main-content">
        {loading && <div style={{ display: 'flex', justifyContent: 'center', margin: '3rem' }}><div className="spinner"></div></div>}

        {/* 1. HOME PAGE */}
        {currentPage === 'home' && (
          <div style={{ animation: 'fadeIn 0.5s ease' }}>
            {/* Hero Section */}
            <div style={{
              background: 'linear-gradient(135deg, rgba(99, 102, 241, 0.15) 0%, rgba(168, 85, 247, 0.15) 100%)',
              border: '1px solid var(--border-color)',
              borderRadius: 'var(--radius-md)',
              padding: '4rem 3rem',
              textAlign: 'center',
              marginBottom: '3rem',
              position: 'relative',
              overflow: 'hidden'
            }}>
              <div style={{
                position: 'absolute',
                top: '-50px',
                right: '-50px',
                width: '300px',
                height: '300px',
                borderRadius: '50%',
                background: 'var(--accent-gradient)',
                filter: 'blur(100px)',
                opacity: 0.15,
                pointerEvents: 'none'
              }}></div>
              
              <h1 style={{ fontSize: '3.5rem', marginBottom: '1rem', lineHeight: '1.2' }}>
                Next-Gen E-Commerce <br/>
                <span style={{ background: 'var(--accent-gradient)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
                  Microservices Playground
                </span>
              </h1>
              <p style={{ color: 'var(--text-secondary)', fontSize: '1.2rem', maxWidth: '700px', margin: '0 auto 2rem auto' }}>
                A premium full-stack Java Spring Boot application utilizing React.js, Docker, Kubernetes, and CI/CD pipelines. Built explicitly to master modern DevOps practices.
              </p>
              <div style={{ display: 'flex', gap: '1rem', justifyContent: 'center' }}>
                <button className="btn btn-primary" onClick={() => setCurrentPage('products')}>Explore Catalog</button>
                {!user && <button className="btn btn-secondary" onClick={() => { setAuthMode('register'); setCurrentPage('login'); }}>Join as Developer</button>}
              </div>
            </div>

            {/* Microservices Feature Cards */}
            <h2 style={{ fontSize: '2rem', marginBottom: '1.5rem', textAlign: 'center' }}>Architecture Showcase</h2>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '1.5rem', marginBottom: '3rem' }}>
              <div className="glass-panel" style={{ padding: '1.5rem', borderRadius: 'var(--radius-sm)' }}>
                <h3 style={{ color: 'var(--accent-primary)', marginBottom: '0.5rem' }}>API Gateway</h3>
                <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>Single entry point using Spring Cloud Gateway routing requests to respective service ports. Manages global CORS policies.</p>
              </div>
              <div className="glass-panel" style={{ padding: '1.5rem', borderRadius: 'var(--radius-sm)' }}>
                <h3 style={{ color: 'var(--accent-secondary)', marginBottom: '0.5rem' }}>Maven & Gradle</h3>
                <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>Split project build structures demonstrating parallel pipeline configurations, build times, and dependency compilation strategies.</p>
              </div>
              <div className="glass-panel" style={{ padding: '1.5rem', borderRadius: 'var(--radius-sm)' }}>
                <h3 style={{ color: '#10b981', marginBottom: '0.5rem' }}>Database Per Service</h3>
                <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>Isolated databases per service (PostgreSQL/H2) showcasing bounded context principles and schema independence.</p>
              </div>
            </div>
          </div>
        )}

        {/* 2. CATALOG / PRODUCTS LISTING */}
        {currentPage === 'products' && (
          <div style={{ animation: 'fadeIn 0.3s ease' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem', marginBottom: '2rem' }}>
              <div>
                <h1 style={{ fontSize: '2rem' }}>Product Catalog</h1>
                <p style={{ color: 'var(--text-secondary)' }}>Explore our range of premium products</p>
              </div>
              
              {/* Search input */}
              <div style={{ display: 'flex', gap: '0.5rem', minWidth: '300px' }}>
                <input 
                  type="text" 
                  className="form-input" 
                  placeholder="Search products..." 
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && fetchProducts(searchQuery)}
                />
                <button className="btn btn-primary" onClick={() => fetchProducts(searchQuery)}>Search</button>
              </div>
            </div>

            {/* Category Filter list */}
            <div style={{ display: 'flex', gap: '0.75rem', marginBottom: '2rem', flexWrap: 'wrap' }}>
              {categories.map(cat => (
                <button 
                  key={cat} 
                  className={`btn ${selectedCategory === cat || (cat === 'All' && !selectedCategory) ? 'btn-primary' : 'btn-secondary'}`}
                  onClick={() => setSelectedCategory(cat === 'All' ? '' : cat)}
                  style={{ padding: '0.4rem 1.2rem', borderRadius: '50px', fontSize: '0.9rem' }}
                >
                  {cat}
                </button>
              ))}
            </div>

            {/* Product Card Grid */}
            {products.length === 0 ? (
              <div style={{ textAlign: 'center', padding: '3rem', color: 'var(--text-secondary)' }}>No products found. Start backend services to load data!</div>
            ) : (
              <div className="product-grid">
                {products.map(prod => (
                  <div key={prod.id} className="product-card">
                    <div className="product-card-img-wrapper" onClick={() => setSelectedProduct(prod)} style={{ cursor: 'pointer' }}>
                      <img src={prod.imageUrl} alt={prod.name} className="product-card-img" />
                    </div>
                    <div className="product-card-info">
                      <div className="product-card-category">{prod.category}</div>
                      <h3 className="product-card-title" onClick={() => setSelectedProduct(prod)} style={{ cursor: 'pointer' }}>{prod.name}</h3>
                      <p className="product-card-desc">{prod.description}</p>
                      <div className="product-card-footer">
                        <div className="product-card-price">${prod.price.toFixed(2)}</div>
                        <button className="btn btn-primary" style={{ padding: '0.5rem 1rem', fontSize: '0.85rem' }} onClick={() => handleAddToCart(prod)}>
                          Add to Cart
                        </button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {/* PRODUCT DETAILS MODAL */}
        {selectedProduct && (
          <div style={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            background: 'rgba(0,0,0,0.8)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 1000,
            padding: '1rem',
            animation: 'fadeIn 0.2s ease'
          }}>
            <div className="glass-panel" style={{ maxWidth: '700px', width: '100%', position: 'relative', padding: '2rem' }}>
              <button 
                style={{ position: 'absolute', top: '1rem', right: '1.5rem', background: 'none', border: 'none', fontSize: '1.5rem', color: 'var(--text-secondary)', cursor: 'pointer' }}
                onClick={() => setSelectedProduct(null)}
              >
                ✕
              </button>
              
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '2rem' }}>
                <div style={{ borderRadius: 'var(--radius-sm)', overflow: 'hidden', height: '280px' }}>
                  <img src={selectedProduct.imageUrl} alt={selectedProduct.name} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
                  <div>
                    <span style={{ fontSize: '0.75rem', textTransform: 'uppercase', color: 'var(--accent-primary)', fontWeight: 700 }}>{selectedProduct.category}</span>
                    <h2 style={{ fontSize: '1.8rem', margin: '0.5rem 0' }}>{selectedProduct.name}</h2>
                    <p style={{ color: 'var(--text-secondary)', fontSize: '0.95rem', marginBottom: '1.5rem' }}>{selectedProduct.description}</p>
                  </div>
                  
                  <div>
                    <div style={{ fontSize: '2rem', fontWeight: 800, marginBottom: '1.5rem' }}>${selectedProduct.price.toFixed(2)}</div>
                    <div style={{ display: 'flex', gap: '1rem' }}>
                      <button className="btn btn-primary" style={{ flex: 1 }} onClick={() => { handleAddToCart(selectedProduct); setSelectedProduct(null); }}>
                        Add to Cart
                      </button>
                      <button className="btn btn-secondary" onClick={() => setSelectedProduct(null)}>Close</button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* 3. LOGIN & REGISTER PAGE */}
        {currentPage === 'login' && (
          <div style={{ display: 'flex', justifyContent: 'center', padding: '2rem 0' }}>
            <div className="glass-panel" style={{ maxWidth: '450px', width: '100%', animation: 'fadeIn 0.3s ease' }}>
              <h2 style={{ fontSize: '2rem', textAlign: 'center', marginBottom: '1.5rem' }}>
                {authMode === 'login' ? 'Welcome Back' : 'Create Account'}
              </h2>
              
              <form onSubmit={handleAuth}>
                <div className="form-group">
                  <label className="form-label">Username</label>
                  <input 
                    type="text" 
                    className="form-input" 
                    required 
                    value={usernameInput}
                    onChange={(e) => setUsernameInput(e.target.value)}
                  />
                </div>

                {authMode === 'register' && (
                  <div className="form-group">
                    <label className="form-label">Email Address</label>
                    <input 
                      type="email" 
                      className="form-input" 
                      required 
                      value={emailInput}
                      onChange={(e) => setEmailInput(e.target.value)}
                    />
                  </div>
                )}

                <div className="form-group">
                  <label className="form-label">Password</label>
                  <input 
                    type="password" 
                    className="form-input" 
                    required 
                    value={passwordInput}
                    onChange={(e) => setPasswordInput(e.target.value)}
                  />
                </div>

                {authMode === 'register' && (
                  <div className="form-group" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginTop: '1rem' }}>
                    <input 
                      type="checkbox" 
                      id="adminCheck"
                      checked={isAdminInput}
                      onChange={(e) => setIsAdminInput(e.target.checked)}
                    />
                    <label htmlFor="adminCheck" className="form-label" style={{ margin: 0, cursor: 'pointer' }}>Register as Admin Role</label>
                  </div>
                )}

                <button type="submit" className="btn btn-primary" style={{ width: '100%', marginTop: '1rem' }}>
                  {authMode === 'login' ? 'Login' : 'Register'}
                </button>
              </form>

              <div style={{ textAlign: 'center', marginTop: '1.5rem', fontSize: '0.9rem', color: 'var(--text-secondary)' }}>
                {authMode === 'login' ? (
                  <span>New to the platform? <strong style={{ color: 'var(--text-primary)', cursor: 'pointer' }} onClick={() => setAuthMode('register')}>Create account</strong></span>
                ) : (
                  <span>Already have an account? <strong style={{ color: 'var(--text-primary)', cursor: 'pointer' }} onClick={() => setAuthMode('login')}>Login here</strong></span>
                )}
              </div>
            </div>
          </div>
        )}

        {/* 4. CART & CHECKOUT PAGE */}
        {currentPage === 'cart' && (
          <div style={{ animation: 'fadeIn 0.3s ease' }}>
            <h1 style={{ fontSize: '2rem', marginBottom: '1.5rem' }}>Your Cart</h1>
            
            {cart.items.length === 0 ? (
              <div className="glass-panel" style={{ textAlign: 'center', padding: '3rem' }}>
                <p style={{ color: 'var(--text-secondary)', marginBottom: '1.5rem' }}>Your shopping cart is empty</p>
                <button className="btn btn-primary" onClick={() => setCurrentPage('products')}>Go to Shop</button>
              </div>
            ) : (
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '2rem' }}>
                {/* Cart Items List */}
                <div>
                  {cart.items.map(item => (
                    <div key={item.productId} style={{
                      display: 'flex',
                      gap: '1rem',
                      background: 'var(--bg-secondary)',
                      border: '1px solid var(--border-color)',
                      borderRadius: 'var(--radius-sm)',
                      padding: '1rem',
                      marginBottom: '1rem',
                      alignItems: 'center'
                    }}>
                      <img src={item.imageUrl} alt={item.productName} style={{ width: '70px', height: '70px', objectFit: 'cover', borderRadius: '4px' }} />
                      <div style={{ flex: 1 }}>
                        <h4 style={{ fontSize: '1.1rem' }}>{item.productName}</h4>
                        <div style={{ color: 'var(--accent-primary)', fontWeight: '600' }}>${item.price.toFixed(2)}</div>
                      </div>
                      
                      {/* Quantity Controls */}
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                        <button className="btn btn-secondary" style={{ padding: '0.2rem 0.5rem' }} onClick={() => handleUpdateQuantity(item.productId, item.quantity - 1)}>-</button>
                        <span>{item.quantity}</span>
                        <button className="btn btn-secondary" style={{ padding: '0.2rem 0.5rem' }} onClick={() => handleUpdateQuantity(item.productId, item.quantity + 1)}>+</button>
                      </div>

                      <button className="btn btn-danger" style={{ padding: '0.5rem' }} onClick={() => handleRemoveFromCart(item.productId)}>
                        ✕
                      </button>
                    </div>
                  ))}
                </div>

                {/* Checkout Orchestration Billing Form */}
                <div className="glass-panel" style={{ height: 'fit-content' }}>
                  <h3 style={{ fontSize: '1.5rem', marginBottom: '1rem' }}>Order Checkout</h3>
                  <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '1.5rem', borderBottom: '1px solid var(--border-color)', paddingBottom: '1rem' }}>
                    <span>Total Cost:</span>
                    <strong style={{ fontSize: '1.5rem', color: 'var(--text-primary)' }}>${cart.totalAmount.toFixed(2)}</strong>
                  </div>

                  <form onSubmit={handlePlaceOrder}>
                    <div className="form-group">
                      <label className="form-label">Shipping Address</label>
                      <input 
                        type="text" 
                        className="form-input" 
                        required 
                        placeholder="123 Main St, Springfield"
                        value={shippingAddress}
                        onChange={(e) => setShippingAddress(e.target.value)}
                      />
                    </div>
                    
                    <div className="form-group">
                      <label className="form-label">Card Number (Ends in 0000 will fail payment)</label>
                      <input 
                        type="text" 
                        className="form-input" 
                        required 
                        value={cardNumber}
                        onChange={(e) => setCardNumber(e.target.value)}
                      />
                    </div>

                    <div className="form-group">
                      <label className="form-label">CVV</label>
                      <input 
                        type="text" 
                        className="form-input" 
                        required 
                        maxLength="3"
                        value={cvv}
                        onChange={(e) => setCvv(e.target.value)}
                      />
                    </div>

                    <button type="submit" className="btn btn-primary" style={{ width: '100%', marginTop: '1rem' }}>
                      Submit Order &amp; Pay
                    </button>
                  </form>
                </div>
              </div>
            )}
          </div>
        )}

        {/* 5. ORDERS HISTORICAL LOG PAGE */}
        {currentPage === 'orders' && (
          <div style={{ animation: 'fadeIn 0.3s ease' }}>
            <h1 style={{ fontSize: '2rem', marginBottom: '1.5rem' }}>Order History</h1>
            
            {orders.length === 0 ? (
              <div className="glass-panel" style={{ textAlign: 'center', padding: '3rem' }}>
                <p style={{ color: 'var(--text-secondary)' }}>You haven't placed any orders yet.</p>
              </div>
            ) : (
              <div>
                {orders.map(order => (
                  <div key={order.id} className="glass-panel" style={{ padding: '1.5rem', marginBottom: '1.5rem', borderRadius: 'var(--radius-sm)' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', flexWrap: 'wrap', borderBottom: '1px solid var(--border-color)', paddingBottom: '0.75rem', marginBottom: '1rem' }}>
                      <div>
                        <strong>Order #{order.id}</strong>
                        <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Date: {new Date(order.orderDate).toLocaleString()}</div>
                      </div>
                      <div style={{ textAlign: 'right' }}>
                        <span className="badge" style={{ background: order.status === 'PAID' ? 'var(--success)' : 'var(--accent-gradient)' }}>
                          {order.status}
                        </span>
                        <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginTop: '0.25rem' }}>Txn: {order.paymentTransactionId}</div>
                      </div>
                    </div>

                    {/* Order items list */}
                    <div>
                      {order.items.map(item => (
                        <div key={item.id} style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.95rem', margin: '0.5rem 0' }}>
                          <span>{item.productName} <strong>x{item.quantity}</strong></span>
                          <span>${(item.price * item.quantity).toFixed(2)}</span>
                        </div>
                      ))}
                    </div>

                    <div style={{ display: 'flex', justifyContent: 'space-between', borderTop: '1px solid var(--border-color)', marginTop: '1rem', paddingTop: '0.75rem', fontWeight: 'bold' }}>
                      <span>Total Paid:</span>
                      <span>${order.totalAmount.toFixed(2)}</span>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {/* 6. ADMIN DASHBOARD PAGE */}
        {currentPage === 'admin' && user && user.role === 'ROLE_ADMIN' && (
          <div style={{ animation: 'fadeIn 0.3s ease' }}>
            <h1 style={{ fontSize: '2rem', marginBottom: '1.5rem' }}>Admin Dashboard</h1>
            
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '2rem' }}>
              {/* Product Creator Form */}
              <div className="glass-panel" style={{ height: 'fit-content' }}>
                <h3 style={{ fontSize: '1.5rem', marginBottom: '1.2rem' }}>Add Product to Catalog</h3>
                <form onSubmit={handleCreateProduct}>
                  <div className="form-group">
                    <label className="form-label">Product Name</label>
                    <input 
                      type="text" 
                      className="form-input" 
                      required 
                      value={newProdName}
                      onChange={(e) => setNewProdName(e.target.value)}
                    />
                  </div>
                  
                  <div className="form-group">
                    <label className="form-label">Description</label>
                    <textarea 
                      className="form-input" 
                      rows="3"
                      value={newProdDesc}
                      onChange={(e) => setNewProdDesc(e.target.value)}
                    />
                  </div>

                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                    <div className="form-group">
                      <label className="form-label">Price ($)</label>
                      <input 
                        type="number" 
                        step="0.01"
                        className="form-input" 
                        required 
                        value={newProdPrice}
                        onChange={(e) => setNewProdPrice(e.target.value)}
                      />
                    </div>
                    
                    <div className="form-group">
                      <label className="form-label">Category</label>
                      <select 
                        className="form-input" 
                        value={newProdCat}
                        onChange={(e) => setNewProdCat(e.target.value)}
                        style={{ height: '43px' }}
                      >
                        <option value="Electronics">Electronics</option>
                        <option value="Accessories">Accessories</option>
                        <option value="Apparel">Apparel</option>
                      </select>
                    </div>
                  </div>

                  <div className="form-group">
                    <label className="form-label">Image URL</label>
                    <input 
                      type="text" 
                      className="form-input" 
                      placeholder="https://..."
                      value={newProdImg}
                      onChange={(e) => setNewProdImg(e.target.value)}
                    />
                  </div>

                  <button type="submit" className="btn btn-primary" style={{ width: '100%', marginTop: '1rem' }}>
                    Add Product
                  </button>
                </form>
              </div>

              {/* Product Catalog Manager list */}
              <div className="glass-panel">
                <h3 style={{ fontSize: '1.5rem', marginBottom: '1.2rem' }}>Manage Catalog Items</h3>
                <div style={{ maxHeight: '450px', overflowY: 'auto', paddingRight: '0.5rem' }}>
                  {products.map(prod => (
                    <div key={prod.id} style={{
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center',
                      padding: '0.75rem 0',
                      borderBottom: '1px solid var(--border-color)'
                    }}>
                      <div>
                        <strong>{prod.name}</strong>
                        <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>ID: {prod.id} | Price: ${prod.price.toFixed(2)}</div>
                      </div>
                      <button className="btn btn-danger" style={{ padding: '0.3rem 0.6rem', fontSize: '0.8rem' }} onClick={() => handleDeleteProduct(prod.id)}>
                        Delete
                      </button>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </div>
        )}
      </main>

      {/* DEV TOOLS / LOGS DISPLAY FOOTER */}
      <footer style={{
        background: '#0d111d',
        borderTop: '2px solid rgba(99, 102, 241, 0.2)',
        padding: '2rem 3rem',
        marginTop: 'auto',
        color: 'var(--text-secondary)',
        fontSize: '0.85rem'
      }}>
        <div style={{ maxWidth: '1280px', margin: '0 auto', display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '2rem' }}>
          <div>
            <h4 style={{ color: 'var(--text-primary)', marginBottom: '0.75rem' }}>DevOps Playground Info</h4>
            <p style={{ marginBottom: '0.5rem' }}>API Gateway running on port <code>8080</code></p>
            <p style={{ marginBottom: '0.5rem' }}>To trigger a mock payment decline during checkout, enter a card ending in <code>0000</code>.</p>
            <p>Unit tests are located inside each backend service folder under <code>src/test/</code>.</p>
          </div>
          
          <div>
            <h4 style={{ color: 'var(--text-primary)', marginBottom: '0.75rem' }}>Notification Service Logs (Live Audit)</h4>
            <div style={{
              background: '#040711',
              border: '1px solid var(--border-color)',
              borderRadius: '6px',
              padding: '0.75rem',
              height: '150px',
              overflowY: 'auto',
              fontFamily: 'monospace',
              fontSize: '0.75rem',
              lineHeight: '1.4'
            }}>
              {notificationLogs.length === 0 ? (
                <div style={{ color: 'var(--text-muted)' }}>Waiting for transactions... logs will stream here.</div>
              ) : (
                notificationLogs.map((log, idx) => (
                  <div key={idx} style={{ borderBottom: '1px solid rgba(255,255,255,0.05)', paddingBottom: '0.25rem', marginBottom: '0.25rem' }}>
                    <span style={{ color: 'var(--accent-primary)' }}>[{new Date(log.timestamp).toLocaleTimeString()}]</span> <br/>
                    <strong style={{ color: '#10b981' }}>To: {log.email}</strong> <br/>
                    <span>Msg: {log.message}</span>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
        
        <div style={{ textAlign: 'center', marginTop: '2rem', paddingTop: '1.5rem', borderTop: '1px solid var(--border-color)', color: 'var(--text-muted)' }}>
          © 2026 E-Shop DevOps Sandbox. Designed for hands-on architectural learning.
        </div>
      </footer>
    </div>
  );
}
