/**
 * ====================================================================
 * Smart Canteen Queue & Food Ordering System - Student Portal Script
 * ====================================================================
 */

const API_BASE = '/api';

// Application State
let menuItems = [];
let selectedCategory = 'all';
let searchQuery = '';
let cart = []; // [{ menuItem, quantity }]
let activeTrackingOrderNumber = null;
let trackingInterval = null;

// Initialize on DOM load
document.addEventListener('DOMContentLoaded', () => {
    loadCartFromStorage();
    fetchCategories();
    fetchMenuItems();
    setupEventListeners();

    // Check if there's a recent order in localStorage to offer quick tracking
    const lastOrder = localStorage.getItem('last_placed_order');
    if (lastOrder) {
        const quickTrackBtn = document.getElementById('quickTrackBanner');
        if (quickTrackBtn) {
            quickTrackBtn.style.display = 'block';
            quickTrackBtn.innerHTML = `🔔 You have an active order: <strong>#${lastOrder}</strong>. <a href="javascript:void(0)" onclick="openTrackingModal('${lastOrder}')">Track Live &rarr;</a>`;
        }
    }
});

// Event Listeners
function setupEventListeners() {
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.addEventListener('input', (e) => {
            searchQuery = e.target.value.toLowerCase().trim();
            renderMenu();
        });
    }

    // Payment method selector changes
    const paymentRadios = document.querySelectorAll('input[name="paymentMethod"]');
    paymentRadios.forEach(radio => {
        radio.addEventListener('change', (e) => {
            document.querySelectorAll('.payment-card-opt').forEach(opt => opt.classList.remove('selected'));
            const parent = e.target.closest('.payment-card-opt');
            if (parent) parent.classList.add('selected');

            const demoBox = document.getElementById('demoPaymentDetails');
            if (demoBox) {
                demoBox.style.display = (e.target.value === 'DEMO_ONLINE') ? 'block' : 'none';
            }
        });
    });
}

// -------------------------------------------------------------
// Menu & Category Functions
// -------------------------------------------------------------
async function fetchCategories() {
    try {
        const res = await fetch(`${API_BASE}/menu/categories`);
        if (!res.ok) throw new Error('Failed to load categories');
        const categories = await res.json();
        renderCategories(['All', ...categories]);
    } catch (err) {
        console.error('Error fetching categories:', err);
    }
}

function renderCategories(categories) {
    const container = document.getElementById('categoriesBar');
    if (!container) return;

    container.innerHTML = categories.map(cat => {
        const isCatActive = (cat.toLowerCase() === selectedCategory.toLowerCase());
        return `<button class="filter-btn ${isCatActive ? 'active' : ''}" onclick="selectCategory('${cat}')">${cat}</button>`;
    }).join('');
}

function selectCategory(category) {
    selectedCategory = category;
    document.querySelectorAll('.filter-btn').forEach(btn => {
        btn.classList.toggle('active', btn.textContent.trim().toLowerCase() === category.toLowerCase());
    });
    renderMenu();
}

async function fetchMenuItems() {
    const grid = document.getElementById('foodGrid');
    if (grid) grid.innerHTML = `<div style="grid-column: 1/-1; text-align: center; padding: 40px; color: var(--text-muted);">🍱 Loading fresh canteen menu...</div>`;

    try {
        const res = await fetch(`${API_BASE}/menu`);
        if (!res.ok) throw new Error('Could not fetch canteen menu');
        menuItems = await res.json();
        renderMenu();
    } catch (err) {
        console.error('Failed to load menu items:', err);
        if (grid) {
            grid.innerHTML = `<div style="grid-column: 1/-1; text-align: center; padding: 40px; color: var(--danger);">⚠️ Unable to connect to canteen backend. Please ensure the server is running on port 8080.</div>`;
        }
    }
}

function renderMenu() {
    const grid = document.getElementById('foodGrid');
    if (!grid) return;

    let filtered = menuItems;

    // Filter by Category
    if (selectedCategory && selectedCategory.toLowerCase() !== 'all') {
        filtered = filtered.filter(item => item.category.toLowerCase() === selectedCategory.toLowerCase());
    }

    // Filter by Search Query
    if (searchQuery) {
        filtered = filtered.filter(item =>
            item.name.toLowerCase().includes(searchQuery) ||
            (item.description && item.description.toLowerCase().includes(searchQuery))
        );
    }

    if (filtered.length === 0) {
        grid.innerHTML = `<div style="grid-column: 1/-1; text-align: center; padding: 40px; color: var(--text-muted);">No delicious items match your search. Try another category!</div>`;
        return;
    }

    grid.innerHTML = filtered.map(item => {
        const isAvailable = item.available;
        const imgUrl = item.imageUrl || 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400&q=80';

        return `
            <div class="food-card ${!isAvailable ? 'out-of-stock' : ''}">
                <div class="card-img-wrapper">
                    <img src="${imgUrl}" alt="${escapeHtml(item.name)}" loading="lazy" onerror="this.src='https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400&q=80'">
                    <span class="category-tag">${escapeHtml(item.category)}</span>
                    <span class="prep-time-tag">⏱️ ~${item.preparationTimeMinutes || 5}m</span>
                </div>
                <div class="card-body">
                    <h3 class="card-title">${escapeHtml(item.name)}</h3>
                    <p class="card-desc">${escapeHtml(item.description || 'Freshly prepared at the college canteen.')}</p>
                    <div class="card-footer">
                        <div class="price">₹${Number(item.price).toFixed(2)}</div>
                        <button class="add-btn" ${!isAvailable ? 'disabled' : ''} onclick="addToCart(${item.id})">
                            ${isAvailable ? '+ Add to Cart' : '🚫 Sold Out'}
                        </button>
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

// -------------------------------------------------------------
// Cart Management
// -------------------------------------------------------------
function addToCart(itemId) {
    const item = menuItems.find(m => m.id === itemId);
    if (!item) return;

    if (!item.available) {
        showToast(`Sorry, ${item.name} is currently out of stock!`, 'error');
        return;
    }

    const existing = cart.find(c => c.menuItem.id === itemId);
    if (existing) {
        if (existing.quantity >= 50) {
            showToast('Maximum 50 items allowed per order.', 'error');
            return;
        }
        existing.quantity += 1;
    } else {
        cart.push({ menuItem: item, quantity: 1 });
    }

    saveCartToStorage();
    updateCartUI();
    showToast(`Added ${item.name} to cart!`, 'success');
}

function updateCartQty(itemId, delta) {
    const itemIndex = cart.findIndex(c => c.menuItem.id === itemId);
    if (itemIndex === -1) return;

    cart[itemIndex].quantity += delta;
    if (cart[itemIndex].quantity <= 0) {
        cart.splice(itemIndex, 1);
    }

    saveCartToStorage();
    updateCartUI();
}

function removeFromCart(itemId) {
    cart = cart.filter(c => c.menuItem.id !== itemId);
    saveCartToStorage();
    updateCartUI();
}

function updateCartUI() {
    const badge = document.getElementById('cartBadge');
    const totalQty = cart.reduce((acc, curr) => acc + curr.quantity, 0);
    if (badge) badge.textContent = totalQty;

    const drawerBody = document.getElementById('cartDrawerItems');
    const totalElem = document.getElementById('cartTotalValue');
    const checkoutBtn = document.getElementById('checkoutSubmitBtn');

    if (!drawerBody) return;

    if (cart.length === 0) {
        drawerBody.innerHTML = `
            <div class="empty-cart-state">
                <div class="empty-cart-icon">🛒</div>
                <p>Your food cart is empty.</p>
                <p style="font-size: 0.85rem; margin-top: 6px;">Add your favorite canteen snacks & meals!</p>
            </div>
        `;
        if (totalElem) totalElem.textContent = '₹0.00';
        if (checkoutBtn) checkoutBtn.disabled = true;
        return;
    }

    let grandTotal = 0;
    drawerBody.innerHTML = cart.map(c => {
        const subtotal = c.menuItem.price * c.quantity;
        grandTotal += subtotal;
        return `
            <div class="cart-item">
                <img src="${c.menuItem.imageUrl}" class="cart-item-img" alt="${escapeHtml(c.menuItem.name)}">
                <div class="cart-item-details">
                    <div class="cart-item-title">${escapeHtml(c.menuItem.name)}</div>
                    <div class="cart-item-price">₹${Number(c.menuItem.price).toFixed(2)} each</div>
                </div>
                <div class="cart-qty-ctrl">
                    <button class="qty-btn" onclick="updateCartQty(${c.menuItem.id}, -1)">-</button>
                    <span style="font-weight: 600; min-width: 16px; text-align: center;">${c.quantity}</span>
                    <button class="qty-btn" onclick="updateCartQty(${c.menuItem.id}, 1)">+</button>
                </div>
                <div class="cart-item-subtotal">₹${subtotal.toFixed(2)}</div>
            </div>
        `;
    }).join('');

    if (totalElem) totalElem.textContent = `₹${grandTotal.toFixed(2)}`;
    if (checkoutBtn) checkoutBtn.disabled = false;
}

function saveCartToStorage() {
    localStorage.setItem('smart_canteen_cart', JSON.stringify(cart));
}

function loadCartFromStorage() {
    try {
        const stored = localStorage.getItem('smart_canteen_cart');
        if (stored) {
            cart = JSON.parse(stored);
            updateCartUI();
        }
    } catch (e) {
        cart = [];
    }
}

function toggleCartDrawer(open) {
    const overlay = document.getElementById('cartDrawerOverlay');
    const drawer = document.getElementById('cartDrawer');
    if (overlay && drawer) {
        if (open) {
            overlay.classList.add('active');
            drawer.classList.add('active');
        } else {
            overlay.classList.remove('active');
            drawer.classList.remove('active');
        }
    }
}

// -------------------------------------------------------------
// Checkout & Order Placement
// -------------------------------------------------------------
async function handleCheckoutSubmit(e) {
    e.preventDefault();

    if (cart.length === 0) {
        showToast('Your cart is empty! Please select food items first.', 'error');
        return;
    }

    const name = document.getElementById('custName').value.trim();
    const phone = document.getElementById('custPhone').value.trim();
    const rollNumber = document.getElementById('custRoll').value.trim();
    const instructions = document.getElementById('custNotes').value.trim();
    const paymentMethod = document.querySelector('input[name="paymentMethod"]:checked').value;

    if (!name || !phone) {
        showToast('Please provide your name and phone number.', 'error');
        return;
    }

    const payload = {
        customerName: name,
        customerPhone: phone,
        customerRollNumber: rollNumber,
        paymentMethod: paymentMethod,
        specialInstructions: instructions,
        items: cart.map(c => ({
            menuItemId: c.menuItem.id,
            quantity: c.quantity
        }))
    };

    const submitBtn = document.getElementById('checkoutSubmitBtn');
    submitBtn.disabled = true;
    submitBtn.innerHTML = '⏳ Placing Order...';

    try {
        const res = await fetch(`${API_BASE}/orders`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const data = await res.json();

        if (!res.ok) {
            const errorMsg = data.message || (data.validationErrors ? data.validationErrors.join(', ') : 'Order placement failed');
            throw new Error(errorMsg);
        }

        // Order success!
        cart = [];
        saveCartToStorage();
        updateCartUI();
        toggleCartDrawer(false);

        // Store last order for quick resume
        localStorage.setItem('last_placed_order', data.orderNumber);

        // Open Confirmation & Tracking
        showOrderConfirmation(data);

    } catch (err) {
        showToast(err.message, 'error');
    } finally {
        submitBtn.disabled = false;
        submitBtn.innerHTML = '⚡ Place Order Now';
    }
}

function showOrderConfirmation(order) {
    const modal = document.getElementById('confirmationModal');
    if (!modal) return;

    document.getElementById('confOrderNumber').textContent = order.orderNumber;
    document.getElementById('confTotal').textContent = `₹${Number(order.totalAmount).toFixed(2)}`;
    document.getElementById('confPaymentMethod').textContent = order.paymentMethodDisplayName;
    document.getElementById('confWaitTime').textContent = `~${order.estimatedWaitMinutes} minutes`;
    document.getElementById('confQueuePos').textContent = `#${order.queuePosition}`;

    modal.classList.add('active');
}

function closeConfirmationModal() {
    const modal = document.getElementById('confirmationModal');
    if (modal) modal.classList.remove('active');
}

// -------------------------------------------------------------
// Live Queue Tracking
// -------------------------------------------------------------
function openTrackingModal(orderNum) {
    closeConfirmationModal();
    const modal = document.getElementById('trackingModal');
    if (!modal) return;

    modal.classList.add('active');
    if (orderNum) {
        document.getElementById('trackingOrderInput').value = orderNum;
        startOrderTracking(orderNum);
    }
}

function closeTrackingModal() {
    const modal = document.getElementById('trackingModal');
    if (modal) modal.classList.remove('active');
    stopTrackingInterval();
}

function handleManualTrackSubmit(e) {
    e.preventDefault();
    const orderNum = document.getElementById('trackingOrderInput').value.trim();
    if (!orderNum) {
        showToast('Please enter an order number (e.g. SC-20261020-0001)', 'error');
        return;
    }
    startOrderTracking(orderNum);
}

function startOrderTracking(orderNum) {
    activeTrackingOrderNumber = orderNum;
    fetchTrackingData(orderNum);

    stopTrackingInterval();
    // Poll every 5 seconds for live status updates
    trackingInterval = setInterval(() => {
        if (activeTrackingOrderNumber) {
            fetchTrackingData(activeTrackingOrderNumber);
        }
    }, 5000);
}

function stopTrackingInterval() {
    if (trackingInterval) {
        clearInterval(trackingInterval);
        trackingInterval = null;
    }
}

async function fetchTrackingData(orderNum) {
    try {
        const [orderRes, queueRes] = await Promise.all([
            fetch(`${API_BASE}/orders/${orderNum}`),
            fetch(`${API_BASE}/orders/${orderNum}/queue`)
        ]);

        if (!orderRes.ok) throw new Error('Order number not found');
        const orderData = await orderRes.json();
        const queueData = await queueRes.json();

        renderTrackingView(orderData, queueData);
    } catch (err) {
        document.getElementById('trackingContentArea').innerHTML = `
            <div style="text-align: center; padding: 30px; color: var(--danger);">
                ⚠️ ${escapeHtml(err.message)}. Please verify your order number.
            </div>
        `;
    }
}

function renderTrackingView(order, queue) {
    const container = document.getElementById('trackingContentArea');
    if (!container) return;

    // Stepper active states
    const status = order.status;
    const isPlaced = status === 'PLACED' || status === 'PREPARING' || status === 'READY' || status === 'COMPLETED';
    const isPrep = status === 'PREPARING' || status === 'READY' || status === 'COMPLETED';
    const isReady = status === 'READY' || status === 'COMPLETED';
    const isComp = status === 'COMPLETED';

    const queuePosText = (status === 'READY') ? 'Ready!' : (queue.queuePosition > 0 ? `#${queue.queuePosition}` : 'Done');
    const waitTimeText = (status === 'READY') ? '0 mins' : `~${queue.estimatedWaitMinutes} mins`;

    container.innerHTML = `
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
            <div>
                <h3 style="font-size: 1.15rem; font-weight: 800;">Order #${order.orderNumber}</h3>
                <div style="font-size: 0.85rem; color: var(--text-muted);">Customer: ${escapeHtml(order.customerName)} (${order.customerPhone})</div>
            </div>
            <span class="badge-${status.toLowerCase()}">${order.statusDisplayName}</span>
        </div>

        <!-- 4-Stage Queue Stepper -->
        <div class="queue-stepper">
            <div class="step-item ${isPlaced ? 'active' : ''} ${isPrep ? 'completed' : ''}">
                <div class="step-circle">1</div>
                <div class="step-label">Placed</div>
            </div>
            <div class="step-item ${isPrep ? 'active' : ''} ${isReady ? 'completed' : ''}">
                <div class="step-circle">2</div>
                <div class="step-label">Preparing</div>
            </div>
            <div class="step-item ${isReady ? 'active' : ''} ${isComp ? 'completed' : ''}">
                <div class="step-circle">3</div>
                <div class="step-label">Ready</div>
            </div>
            <div class="step-item ${isComp ? 'completed' : ''}">
                <div class="step-circle">4</div>
                <div class="step-label">Picked Up</div>
            </div>
        </div>

        <!-- Live Queue Metrics Card -->
        <div class="queue-kpi-card">
            <div>
                <div class="queue-kpi-val">${queuePosText}</div>
                <div class="queue-kpi-lbl">Queue Position</div>
            </div>
            <div style="width: 1px; height: 40px; background: #bfdbfe;"></div>
            <div>
                <div class="queue-kpi-val">${queue.ordersAheadCount}</div>
                <div class="queue-kpi-lbl">Orders Ahead</div>
            </div>
            <div style="width: 1px; height: 40px; background: #bfdbfe;"></div>
            <div>
                <div class="queue-kpi-val">${waitTimeText}</div>
                <div class="queue-kpi-lbl">Est. Wait Time</div>
            </div>
        </div>

        <!-- Smart Estimator Algorithm Explanation -->
        <div class="queue-explanation">
            <strong>🧠 Smart Queue Estimation Breakdown:</strong><br>
            ${escapeHtml(queue.calculationExplanation)}
        </div>

        <p class="queue-disclaimer">${escapeHtml(queue.disclaimer)}</p>

        <!-- Ordered Items Summary -->
        <div style="margin-top: 20px; border-top: 1px solid var(--border); padding-top: 14px;">
            <div style="font-size: 0.85rem; font-weight: 700; color: var(--text-muted); text-transform: uppercase; margin-bottom: 8px;">Items in this order:</div>
            ${order.items.map(i => `
                <div style="display: flex; justify-content: space-between; font-size: 0.9rem; padding: 4px 0;">
                    <span>${i.quantity}x ${escapeHtml(i.itemName)}</span>
                    <span style="font-weight: 600;">₹${Number(i.subtotal).toFixed(2)}</span>
                </div>
            `).join('')}
            <div style="display: flex; justify-content: space-between; font-size: 1rem; font-weight: 800; border-top: 1px dashed var(--border); margin-top: 8px; padding-top: 8px;">
                <span>Total Amount:</span>
                <span style="color: #047857;">₹${Number(order.totalAmount).toFixed(2)} (${order.paymentMethodDisplayName})</span>
            </div>
        </div>
    `;
}

// -------------------------------------------------------------
// Order History
// -------------------------------------------------------------
function openHistoryModal() {
    const modal = document.getElementById('historyModal');
    if (modal) modal.classList.add('active');
}

function closeHistoryModal() {
    const modal = document.getElementById('historyModal');
    if (modal) modal.classList.remove('active');
}

async function searchOrderHistory() {
    const query = document.getElementById('historySearchInput').value.trim();
    const resultBox = document.getElementById('historyResults');
    if (!resultBox) return;

    resultBox.innerHTML = '<div style="text-align: center; padding: 20px; color: var(--text-muted);">Searching order history...</div>';

    let url = `${API_BASE}/orders/history`;
    if (query) {
        if (/^\d{7,15}$/.test(query)) {
            url += `?phone=${encodeURIComponent(query)}`;
        } else {
            url += `?rollNumber=${encodeURIComponent(query)}`;
        }
    }

    try {
        const res = await fetch(url);
        if (!res.ok) throw new Error('Could not load order history');
        const orders = await res.json();

        if (orders.length === 0) {
            resultBox.innerHTML = '<div style="text-align: center; padding: 20px; color: var(--text-muted);">No orders found for this roll number or phone number.</div>';
            return;
        }

        resultBox.innerHTML = orders.map(ord => `
            <div style="background: var(--bg-main); border: 1px solid var(--border); border-radius: var(--radius-sm); padding: 14px; margin-bottom: 12px;">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px;">
                    <strong style="font-size: 1rem;">#${ord.orderNumber}</strong>
                    <span class="badge-${ord.status.toLowerCase()}">${ord.statusDisplayName}</span>
                </div>
                <div style="font-size: 0.85rem; color: var(--text-muted); margin-bottom: 8px;">
                    Date: ${new Date(ord.createdAt).toLocaleString()} | Total: <strong>₹${Number(ord.totalAmount).toFixed(2)}</strong> (${ord.paymentMethodDisplayName})
                </div>
                <div style="font-size: 0.85rem; margin-bottom: 10px;">
                    ${ord.items.map(i => `${i.quantity}x ${escapeHtml(i.itemName)}`).join(', ')}
                </div>
                <button class="nav-btn" style="background: #ffffff; border: 1px solid var(--border); font-size: 0.8rem; padding: 4px 10px;" onclick="closeHistoryModal(); openTrackingModal('${ord.orderNumber}')">
                    📍 Track Order
                </button>
            </div>
        `).join('');

    } catch (err) {
        resultBox.innerHTML = `<div style="text-align: center; padding: 20px; color: var(--danger);">${escapeHtml(err.message)}</div>`;
    }
}

// -------------------------------------------------------------
// Toast Notifications
// -------------------------------------------------------------
function showToast(message, type = 'info') {
    const container = document.getElementById('toastContainer');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.textContent = message;

    container.appendChild(toast);
    setTimeout(() => {
        toast.style.opacity = '0';
        setTimeout(() => toast.remove(), 300);
    }, 3500);
}

// Utility: HTML Escape
function escapeHtml(text) {
    if (!text) return '';
    return text.toString()
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}
