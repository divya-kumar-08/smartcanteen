/**
 * ====================================================================
 * Smart Canteen Queue & Food Ordering System - Admin & Kitchen Portal
 * ====================================================================
 */

const API_BASE = '/api/admin';

let currentTab = 'kitchen';
let autoRefreshTimer = null;
let editingItemId = null;

document.addEventListener('DOMContentLoaded', () => {
    loadDashboardStats();
    loadKitchenOrders();
    setupAdminAutoRefresh();
});

// Switch Tabs
function switchAdminTab(tabName) {
    currentTab = tabName;
    document.querySelectorAll('.admin-tab-btn').forEach(btn => btn.classList.remove('active'));
    document.querySelectorAll('.tab-content-pane').forEach(pane => pane.style.display = 'none');

    const activeBtn = document.getElementById(`tabBtn-${tabName}`);
    const activePane = document.getElementById(`tabPane-${tabName}`);

    if (activeBtn) activeBtn.classList.add('active');
    if (activePane) activePane.style.display = 'block';

    if (tabName === 'kitchen') {
        loadKitchenOrders();
    } else if (tabName === 'menu') {
        loadAdminMenu();
    } else if (tabName === 'orders') {
        loadAllOrders();
    }
}

// -------------------------------------------------------------
// Auto Refresh Setup
// -------------------------------------------------------------
function setupAdminAutoRefresh() {
    if (autoRefreshTimer) clearInterval(autoRefreshTimer);
    autoRefreshTimer = setInterval(() => {
        const toggle = document.getElementById('autoRefreshCheck');
        if (toggle && toggle.checked) {
            loadDashboardStats();
            if (currentTab === 'kitchen') {
                loadKitchenOrders(true); // silent refresh
            }
        }
    }, 5000);
}

// -------------------------------------------------------------
// Statistics
// -------------------------------------------------------------
async function loadDashboardStats() {
    try {
        const res = await fetch(`${API_BASE}/stats`);
        if (!res.ok) throw new Error('Failed to load stats');
        const stats = await res.json();

        document.getElementById('statRevenue').textContent = `₹${Number(stats.totalGrossRevenue).toFixed(2)}`;
        document.getElementById('statActiveOrders').textContent = stats.activeOrdersCount;
        document.getElementById('statPreparingOrders').textContent = stats.preparingOrdersCount;
        document.getElementById('statReadyOrders').textContent = stats.readyOrdersCount;
        document.getElementById('statCompletedOrders').textContent = stats.completedOrdersCount;
    } catch (err) {
        console.error('Error fetching admin stats:', err);
    }
}

// -------------------------------------------------------------
// Kitchen Display & Active Orders
// -------------------------------------------------------------
async function loadKitchenOrders(isSilent = false) {
    const tableBody = document.getElementById('kitchenOrdersList');
    if (!tableBody) return;

    if (!isSilent) {
        tableBody.innerHTML = `<tr><td colspan="6" style="text-align: center; padding: 24px; color: var(--text-muted);">Refreshing Kitchen Display...</td></tr>`;
    }

    try {
        const res = await fetch(`${API_BASE}/orders/active`);
        if (!res.ok) throw new Error('Could not fetch active orders');
        const orders = await res.json();

        if (orders.length === 0) {
            tableBody.innerHTML = `
                <tr>
                    <td colspan="6" style="text-align: center; padding: 36px; color: var(--text-muted);">
                        🎉 All caught up! No pending food orders in the kitchen.
                    </td>
                </tr>
            `;
            return;
        }

        tableBody.innerHTML = orders.map(ord => {
            const status = ord.status;
            let actionButtons = '';

            if (status === 'PLACED') {
                actionButtons = `
                    <button class="action-btn btn-start" onclick="updateOrderStatus(${ord.id}, 'PREPARING')">🔥 Start Cooking</button>
                    <button class="action-btn btn-cancel" onclick="updateOrderStatus(${ord.id}, 'CANCELLED')">Cancel</button>
                `;
            } else if (status === 'PREPARING') {
                actionButtons = `
                    <button class="action-btn btn-ready" onclick="updateOrderStatus(${ord.id}, 'READY')">🔔 Mark Ready</button>
                    <button class="action-btn btn-cancel" onclick="updateOrderStatus(${ord.id}, 'CANCELLED')">Cancel</button>
                `;
            } else if (status === 'READY') {
                actionButtons = `
                    <button class="action-btn btn-complete" onclick="updateOrderStatus(${ord.id}, 'COMPLETED')">✅ Complete & Collected</button>
                `;
            }

            const paymentBadge = (ord.paymentStatus === 'SUCCESS')
                ? `<span style="color: var(--success); font-weight: 600;">PAID (${ord.paymentMethodDisplayName})</span>`
                : `<span style="color: var(--warning); font-weight: 600;">UNPAID (Collect Cash: ₹${Number(ord.totalAmount).toFixed(2)})</span>`;

            return `
                <tr>
                    <td>
                        <strong style="font-size: 1rem;">#${ord.orderNumber}</strong><br>
                        <small style="color: var(--text-muted);">${new Date(ord.createdAt).toLocaleTimeString()}</small>
                    </td>
                    <td>
                        <strong>${escapeHtml(ord.customerName)}</strong><br>
                        <small style="color: var(--text-muted);">📞 ${ord.customerPhone} ${ord.customerRollNumber ? '| ' + ord.customerRollNumber : ''}</small>
                    </td>
                    <td>
                        <div style="font-weight: 500;">
                            ${ord.items.map(i => `<span style="display: block;">• <strong>${i.quantity}x</strong> ${escapeHtml(i.itemName)}</span>`).join('')}
                        </div>
                        ${ord.specialInstructions ? `<div style="margin-top: 4px; font-size: 0.8rem; color: #b45309; background: #fef3c7; padding: 2px 6px; border-radius: 4px;">Note: ${escapeHtml(ord.specialInstructions)}</div>` : ''}
                    </td>
                    <td>
                        ${paymentBadge}
                    </td>
                    <td>
                        <span class="badge-${status.toLowerCase()}">${ord.statusDisplayName}</span>
                        <div style="font-size: 0.75rem; color: var(--text-muted); margin-top: 2px;">Wait: ~${ord.estimatedWaitMinutes}m</div>
                    </td>
                    <td>
                        <div class="action-btn-group">
                            ${actionButtons}
                        </div>
                    </td>
                </tr>
            `;
        }).join('');

    } catch (err) {
        tableBody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: var(--danger); padding: 24px;">Failed to load active orders: ${escapeHtml(err.message)}</td></tr>`;
    }
}

async function updateOrderStatus(orderId, newStatus) {
    try {
        const res = await fetch(`${API_BASE}/orders/${orderId}/status`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ status: newStatus })
        });

        if (!res.ok) {
            const err = await res.json();
            throw new Error(err.message || 'Status update failed');
        }

        showAdminToast(`Order #${orderId} marked as ${newStatus}!`, 'success');
        loadDashboardStats();
        loadKitchenOrders();
        if (currentTab === 'orders') {
            loadAllOrders();
        }
    } catch (err) {
        showAdminToast(err.message, 'error');
    }
}

// -------------------------------------------------------------
// Menu Management (CRUD)
// -------------------------------------------------------------
async function loadAdminMenu() {
    const tableBody = document.getElementById('adminMenuList');
    if (!tableBody) return;

    tableBody.innerHTML = `<tr><td colspan="6" style="text-align: center; padding: 20px; color: var(--text-muted);">Loading canteen menu items...</td></tr>`;

    try {
        const res = await fetch(`${API_BASE}/menu`);
        if (!res.ok) throw new Error('Could not fetch menu items');
        const items = await res.json();

        tableBody.innerHTML = items.map(item => `
            <tr style="${!item.active ? 'opacity: 0.5; background: #f8fafc;' : ''}">
                <td>
                    <img src="${item.imageUrl}" style="width: 44px; height: 44px; border-radius: 6px; object-fit: cover;" onerror="this.src='https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400&q=80'">
                </td>
                <td>
                    <strong>${escapeHtml(item.name)}</strong>
                    <div style="font-size: 0.8rem; color: var(--text-muted);">${escapeHtml(item.description || '')}</div>
                </td>
                <td><span class="badge-placed">${escapeHtml(item.category)}</span></td>
                <td><strong style="color: #047857;">₹${Number(item.price).toFixed(2)}</strong></td>
                <td>
                    <label style="cursor: pointer; display: inline-flex; align-items: center; gap: 6px;">
                        <input type="checkbox" ${item.available ? 'checked' : ''} onchange="toggleItemAvailability(${item.id})">
                        <span style="font-size: 0.85rem; font-weight: 600; color: ${item.available ? 'var(--success)' : 'var(--danger)'};">
                            ${item.available ? 'In Stock' : 'Out of Stock'}
                        </span>
                    </label>
                </td>
                <td>
                    <div class="action-btn-group">
                        <button class="action-btn" style="background: #e2e8f0;" onclick="openEditItemModal(${item.id})">✏️ Edit</button>
                        <button class="action-btn ${item.active ? 'btn-cancel' : 'btn-ready'}" onclick="toggleItemActive(${item.id})">
                            ${item.active ? 'Deactivate' : 'Reactivate'}
                        </button>
                    </div>
                </td>
            </tr>
        `).join('');

    } catch (err) {
        tableBody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: var(--danger);">${escapeHtml(err.message)}</td></tr>`;
    }
}

async function toggleItemAvailability(id) {
    try {
        const res = await fetch(`${API_BASE}/menu/${id}/toggle-availability`, { method: 'PATCH' });
        if (!res.ok) throw new Error('Toggle failed');
        showAdminToast('Stock availability updated!', 'success');
        loadAdminMenu();
    } catch (err) {
        showAdminToast(err.message, 'error');
    }
}

async function toggleItemActive(id) {
    try {
        const res = await fetch(`${API_BASE}/menu/${id}/toggle-active`, { method: 'PATCH' });
        if (!res.ok) throw new Error('Toggle active failed');
        showAdminToast('Menu item status changed!', 'success');
        loadAdminMenu();
    } catch (err) {
        showAdminToast(err.message, 'error');
    }
}

function openAddMenuItemModal() {
    editingItemId = null;
    document.getElementById('menuModalTitle').textContent = 'Add New Menu Item';
    document.getElementById('menuItemForm').reset();
    document.getElementById('itemModalPrep').value = 5;
    document.getElementById('itemModalAvailable').checked = true;
    document.getElementById('menuItemModal').classList.add('active');
}

async function openEditItemModal(id) {
    editingItemId = id;
    document.getElementById('menuModalTitle').textContent = 'Edit Menu Item';

    try {
        const res = await fetch(`/api/menu/${id}`);
        if (!res.ok) throw new Error('Failed to retrieve item');
        const item = await res.json();

        document.getElementById('itemModalName').value = item.name;
        document.getElementById('itemModalCategory').value = item.category;
        document.getElementById('itemModalPrice').value = item.price;
        document.getElementById('itemModalPrep').value = item.preparationTimeMinutes;
        document.getElementById('itemModalImage').value = item.imageUrl || '';
        document.getElementById('itemModalDesc').value = item.description || '';
        document.getElementById('itemModalAvailable').checked = item.available;

        document.getElementById('menuItemModal').classList.add('active');
    } catch (err) {
        showAdminToast(err.message, 'error');
    }
}

function closeMenuItemModal() {
    document.getElementById('menuItemModal').classList.remove('active');
    editingItemId = null;
}

async function handleMenuItemSubmit(e) {
    e.preventDefault();

    const payload = {
        name: document.getElementById('itemModalName').value.trim(),
        category: document.getElementById('itemModalCategory').value.trim(),
        price: parseFloat(document.getElementById('itemModalPrice').value),
        preparationTimeMinutes: parseInt(document.getElementById('itemModalPrep').value) || 5,
        imageUrl: document.getElementById('itemModalImage').value.trim(),
        description: document.getElementById('itemModalDesc').value.trim(),
        available: document.getElementById('itemModalAvailable').checked
    };

    const isEdit = (editingItemId !== null);
    const url = isEdit ? `${API_BASE}/menu/${editingItemId}` : `${API_BASE}/menu`;
    const method = isEdit ? 'PUT' : 'POST';

    try {
        const res = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!res.ok) {
            const err = await res.json();
            throw new Error(err.message || 'Saving menu item failed');
        }

        showAdminToast(isEdit ? 'Menu item updated!' : 'New menu item created!', 'success');
        closeMenuItemModal();
        loadAdminMenu();
        loadDashboardStats();
    } catch (err) {
        showAdminToast(err.message, 'error');
    }
}

// -------------------------------------------------------------
// All Orders History Tab
// -------------------------------------------------------------
async function loadAllOrders() {
    const tableBody = document.getElementById('allOrdersList');
    const filterSelect = document.getElementById('historyStatusFilter');
    const filter = filterSelect ? filterSelect.value : '';

    let url = `${API_BASE}/orders`;
    if (filter && filter !== 'ALL') {
        url += `?status=${filter}`;
    }

    tableBody.innerHTML = `<tr><td colspan="7" style="text-align: center; padding: 24px; color: var(--text-muted);">Loading full order records...</td></tr>`;

    try {
        const res = await fetch(url);
        if (!res.ok) throw new Error('Could not fetch orders history');
        const orders = await res.json();

        if (orders.length === 0) {
            tableBody.innerHTML = `<tr><td colspan="7" style="text-align: center; padding: 24px; color: var(--text-muted);">No orders match this status.</td></tr>`;
            return;
        }

        tableBody.innerHTML = orders.map(ord => `
            <tr>
                <td><strong>#${ord.orderNumber}</strong></td>
                <td>${new Date(ord.createdAt).toLocaleString()}</td>
                <td>
                    ${escapeHtml(ord.customerName)}<br>
                    <small style="color: var(--text-muted);">${ord.customerPhone} ${ord.customerRollNumber ? '(' + ord.customerRollNumber + ')' : ''}</small>
                </td>
                <td>
                    ${ord.items.map(i => `${i.quantity}x ${escapeHtml(i.itemName)}`).join(', ')}
                </td>
                <td><strong>₹${Number(ord.totalAmount).toFixed(2)}</strong></td>
                <td>
                    <span style="font-size: 0.8rem; font-weight: 600; color: ${ord.paymentStatus === 'SUCCESS' ? 'var(--success)' : 'var(--warning)'};">
                        ${ord.paymentStatus}
                    </span><br>
                    <small style="color: var(--text-muted);">${ord.paymentMethodDisplayName}</small>
                </td>
                <td><span class="badge-${ord.status.toLowerCase()}">${ord.statusDisplayName}</span></td>
            </tr>
        `).join('');

    } catch (err) {
        tableBody.innerHTML = `<tr><td colspan="7" style="text-align: center; color: var(--danger); padding: 20px;">${escapeHtml(err.message)}</td></tr>`;
    }
}

// Toast
function showAdminToast(message, type = 'info') {
    const container = document.getElementById('adminToastContainer');
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

function escapeHtml(text) {
    if (!text) return '';
    return text.toString()
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}
