/**
 * Brightloop Enterprise Dashboard & Module Controller
 * Coordinates live REST API data, reactive views, and interactive modals.
 */

let currentUser = null;

document.addEventListener('DOMContentLoaded', () => {
    initAuth();
    initHealthPolling();
    initNavigation();
    initForms();

    // Default to dashboard view
    const initialView = window.location.hash ? window.location.hash.substring(1) : 'dashboard';
    showView(initialView);
});

// Authentication Initialization
function initAuth() {
    const storedUser = localStorage.getItem('brightloop_user');
    const storedToken = localStorage.getItem('brightloop_token');

    if (storedUser && storedToken) {
        currentUser = JSON.parse(storedUser);
        updateUserUI(currentUser);
    } else {
        // Default to demo admin for seamless hackathon preview
        loginAs('admin@brightloop.com', 'Admin@123');
    }

    const logoutBtn = document.getElementById('btn-logout');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', () => {
            localStorage.removeItem('brightloop_token');
            localStorage.removeItem('brightloop_user');
            currentUser = null;
            showToast('Logged out successfully', 'info');
            openModal('login-modal');
        });
    }
}

function updateUserUI(user) {
    const nameEl = document.getElementById('user-display-name');
    const roleEl = document.getElementById('user-display-role');
    const avatarEl = document.getElementById('user-display-avatar');

    if (nameEl) nameEl.textContent = user.fullName;
    if (roleEl) roleEl.textContent = user.role;
    if (avatarEl) {
        const initials = user.fullName.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();
        avatarEl.textContent = initials;
    }

    // Role-dependent UI controls
    const adminControls = document.querySelectorAll('.admin-only');
    adminControls.forEach(el => {
        el.style.display = user.role === 'ADMIN' ? '' : 'none';
    });
}

async function loginAs(email, password) {
    try {
        const res = await AuthAPI.login({ email, password });
        localStorage.setItem('brightloop_token', res.data.token);
        localStorage.setItem('brightloop_user', JSON.stringify(res.data));
        currentUser = res.data;
        updateUserUI(currentUser);
        closeModal('login-modal');
        showToast(`Welcome back, ${currentUser.fullName}!`, 'success');
        refreshCurrentView();
    } catch (err) {
        showToast(err.message || 'Login failed', 'error');
    }
}

// Navigation & View Switching
function initNavigation() {
    const navItems = document.querySelectorAll('.nav-item[data-view]');
    navItems.forEach(item => {
        item.addEventListener('click', (e) => {
            e.preventDefault();
            const view = item.getAttribute('data-view');
            showView(view);
        });
    });

    window.addEventListener('hashchange', () => {
        const view = window.location.hash.substring(1) || 'dashboard';
        showView(view);
    });
}

let activeView = 'dashboard';
function showView(viewName) {
    const views = document.querySelectorAll('.view-section');
    const navItems = document.querySelectorAll('.nav-item[data-view]');

    let targetSection = document.getElementById(`view-${viewName}`);
    if (!targetSection) {
        viewName = 'dashboard';
        targetSection = document.getElementById('view-dashboard');
    }

    views.forEach(v => v.classList.remove('active'));
    targetSection.classList.add('active');

    navItems.forEach(item => {
        if (item.getAttribute('data-view') === viewName) {
            item.classList.add('active');
        } else {
            item.classList.remove('active');
        }
    });

    activeView = viewName;
    window.location.hash = viewName;

    // Load dynamic data for view
    switch (viewName) {
        case 'dashboard': loadDashboard(); break;
        case 'products': loadProducts(); break;
        case 'plans': loadPlans(); break;
        case 'quotations': loadQuotations(); break;
        case 'subscriptions': loadSubscriptions(); break;
        case 'invoices': loadInvoices(); break;
        case 'discounts': loadDiscounts(); break;
        case 'payments': loadPayments(); break;
        case 'users': loadUsers(); break;
    }
}

function refreshCurrentView() {
    showView(activeView);
}

// Backend Health Indicator Polling
function initHealthPolling() {
    const statusText = document.getElementById('backend-status-text');
    const statusPill = document.getElementById('backend-status-indicator');

    async function updateStatus() {
        const result = await checkBackendHealth();
        if (result.isOnline) {
            statusPill.className = 'pulse-indicator active';
            statusText.textContent = 'Backend Online';
            statusText.style.color = 'var(--success)';
        } else {
            statusPill.className = 'pulse-indicator offline';
            statusText.textContent = 'Backend Offline';
            statusText.style.color = 'var(--text-muted)';
        }
    }

    updateStatus();
    setInterval(updateStatus, 10000);

    const refreshBtn = document.getElementById('btn-refresh-health');
    if (refreshBtn) {
        refreshBtn.addEventListener('click', async () => {
            showToast('Pinging /api/health...', 'info');
            const result = await checkBackendHealth();
            if (result.isOnline) {
                showToast(`Backend Healthy: ${result.data?.service} (${result.data?.databaseTarget?.split('?')[0]})`, 'success');
            } else {
                showToast('Backend unreachable at http://localhost:8080', 'error');
            }
        });
    }
}

// -------------------------------------------------------------
// 1. DASHBOARD OVERVIEW LOADER
// -------------------------------------------------------------
async function loadDashboard() {
    try {
        const res = await DashboardAPI.getSummary();
        const data = res.data;

        // KPI Values
        document.getElementById('val-active-subs').textContent = data.activeSubscriptionsCount || 0;
        document.getElementById('val-mrr').textContent = `$${parseFloat(data.monthlyRecurringRevenue || 0).toFixed(2)}`;
        document.getElementById('val-outstanding').textContent = `$${parseFloat(data.totalOutstanding || 0).toFixed(2)}`;
        document.getElementById('val-payments').textContent = `$${parseFloat(data.totalPaymentsReceived || 0).toFixed(2)}`;

        const overdueCountEl = document.getElementById('badge-overdue-count');
        if (overdueCountEl) {
            overdueCountEl.textContent = `${data.overdueInvoicesCount || 0} Overdue`;
        }

        // Render Recent Subscriptions
        const subsBody = document.getElementById('dashboard-subs-table');
        if (subsBody) {
            if (data.recentSubscriptions && data.recentSubscriptions.length > 0) {
                subsBody.innerHTML = data.recentSubscriptions.map(s => `
                    <tr>
                        <td><strong>${s.subscriptionNumber}</strong></td>
                        <td>${escapeHtml(s.customerName)}</td>
                        <td>${escapeHtml(s.planName || '-')}</td>
                        <td>$${parseFloat(s.recurringAmount).toFixed(2)}</td>
                        <td><span class="badge badge-info">${s.billingInterval}</span></td>
                        <td><span class="badge badge-${getStatusBadge(s.status)}">${s.status}</span></td>
                    </tr>
                `).join('');
            } else {
                subsBody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: var(--text-muted); padding: 24px;">No active subscriptions found.</td></tr>`;
            }
        }

        // Render Recent Invoices
        const invBody = document.getElementById('dashboard-invoices-table');
        if (invBody) {
            if (data.recentInvoices && data.recentInvoices.length > 0) {
                invBody.innerHTML = data.recentInvoices.map(i => `
                    <tr>
                        <td><strong>${i.invoiceNumber}</strong></td>
                        <td>${escapeHtml(i.customerName)}</td>
                        <td>${i.dueDate}</td>
                        <td>$${parseFloat(i.totalAmount).toFixed(2)}</td>
                        <td>$${parseFloat(i.outstandingAmount).toFixed(2)}</td>
                        <td><span class="badge badge-${getStatusBadge(i.status)}">${i.status}</span></td>
                    </tr>
                `).join('');
            } else {
                invBody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: var(--text-muted); padding: 24px;">No invoices generated.</td></tr>`;
            }
        }
    } catch (err) {
        console.error('Failed to load dashboard:', err);
    }
}

// -------------------------------------------------------------
// 2. PRODUCTS LOADER
// -------------------------------------------------------------
async function loadProducts() {
    try {
        const res = await ProductAPI.getAll();
        const tbody = document.getElementById('products-table-body');
        if (!tbody) return;

        if (res.data && res.data.length > 0) {
            tbody.innerHTML = res.data.map(p => `
                <tr>
                    <td><strong>${escapeHtml(p.name)}</strong></td>
                    <td><span class="badge badge-info">${p.type}</span></td>
                    <td>$${parseFloat(p.sellingPrice).toFixed(2)}</td>
                    <td>$${parseFloat(p.costPrice).toFixed(2)}</td>
                    <td>${p.variants && p.variants.length > 0
                        ? p.variants.map(v => `<span class="badge badge-draft" style="margin-right: 4px;">${escapeHtml(v.variantName)} (+${v.additionalPrice})</span>`).join('')
                        : '<span style="color: var(--text-muted)">None</span>'}
                    </td>
                    <td>${escapeHtml(p.notes || '-')}</td>
                    <td class="admin-only">
                        <button class="btn btn-secondary btn-sm" onclick="deleteProduct(${p.id})">Delete</button>
                    </td>
                </tr>
            `).join('');
        } else {
            tbody.innerHTML = `<tr><td colspan="7" style="text-align: center; color: var(--text-muted); padding: 32px;">No products in catalog.</td></tr>`;
        }
    } catch (err) {
        showToast('Failed to load products: ' + err.message, 'error');
    }
}

async function deleteProduct(id) {
    if (!confirm('Are you sure you want to delete this product?')) return;
    try {
        await ProductAPI.delete(id);
        showToast('Product deleted', 'success');
        loadProducts();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// -------------------------------------------------------------
// 3. PLANS LOADER
// -------------------------------------------------------------
async function loadPlans() {
    try {
        const res = await PlanAPI.getAll();
        const tbody = document.getElementById('plans-table-body');
        if (!tbody) return;

        if (res.data && res.data.length > 0) {
            tbody.innerHTML = res.data.map(plan => `
                <tr>
                    <td><strong>${escapeHtml(plan.planName)}</strong></td>
                    <td><span class="badge badge-info">${plan.billingInterval} (x${plan.intervalCount})</span></td>
                    <td>$${parseFloat(plan.basePrice).toFixed(2)}</td>
                    <td>${plan.minQuantity} unit(s)</td>
                    <td>${plan.startDate} ${plan.endDate ? 'to ' + plan.endDate : '(ongoing)'}</td>
                    <td>${escapeHtml(plan.description || '-')}</td>
                    <td class="admin-only">
                        <button class="btn btn-secondary btn-sm" onclick="deletePlan(${plan.id})">Delete</button>
                    </td>
                </tr>
            `).join('');
        } else {
            tbody.innerHTML = `<tr><td colspan="7" style="text-align: center; color: var(--text-muted); padding: 32px;">No subscription plans configured.</td></tr>`;
        }
    } catch (err) {
        showToast('Failed to load plans: ' + err.message, 'error');
    }
}

async function deletePlan(id) {
    if (!confirm('Are you sure you want to delete this plan?')) return;
    try {
        await PlanAPI.delete(id);
        showToast('Plan deleted', 'success');
        loadPlans();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// -------------------------------------------------------------
// 4. QUOTATIONS LOADER
// -------------------------------------------------------------
async function loadQuotations() {
    try {
        const res = await QuotationAPI.getAll();
        const tbody = document.getElementById('quotations-table-body');
        if (!tbody) return;

        if (res.data && res.data.length > 0) {
            tbody.innerHTML = res.data.map(q => `
                <tr>
                    <td><strong>${q.quotationNumber}</strong></td>
                    <td>${escapeHtml(q.customerName)}</td>
                    <td>${q.validUntil}</td>
                    <td>$${parseFloat(q.subtotal).toFixed(2)}</td>
                    <td>$${parseFloat(q.taxAmount).toFixed(2)} (10%)</td>
                    <td>$${parseFloat(q.discountAmount).toFixed(2)}</td>
                    <td><strong>$${parseFloat(q.totalAmount).toFixed(2)}</strong></td>
                    <td><span class="badge badge-${getStatusBadge(q.status)}">${q.status}</span></td>
                    <td>
                        ${q.status === 'DRAFT' ? `
                            <button class="btn btn-primary btn-sm" onclick="confirmQuotation(${q.id})">Confirm</button>
                        ` : ''}
                        ${q.status === 'CONFIRMED' ? `
                            <button class="btn btn-primary btn-sm" onclick="convertQuoteToSubscription(${q.id})">Convert to Sub</button>
                        ` : ''}
                        ${q.status === 'CONVERTED' ? `<span style="color: var(--success); font-size: 0.8rem; font-weight: 600;">✓ Converted</span>` : ''}
                    </td>
                </tr>
            `).join('');
        } else {
            tbody.innerHTML = `<tr><td colspan="9" style="text-align: center; color: var(--text-muted); padding: 32px;">No quotations created.</td></tr>`;
        }
    } catch (err) {
        showToast('Failed to load quotations: ' + err.message, 'error');
    }
}

async function confirmQuotation(id) {
    try {
        await QuotationAPI.confirm(id);
        showToast('Quotation confirmed!', 'success');
        loadQuotations();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function convertQuoteToSubscription(id) {
    try {
        const res = await QuotationAPI.convertToSubscription(id);
        showToast(`Successfully converted to ${res.data.length} active subscription(s)!`, 'success');
        loadQuotations();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// -------------------------------------------------------------
// 5. SUBSCRIPTIONS LOADER
// -------------------------------------------------------------
async function loadSubscriptions() {
    try {
        const res = await SubscriptionAPI.getAll();
        const tbody = document.getElementById('subscriptions-table-body');
        if (!tbody) return;

        if (res.data && res.data.length > 0) {
            tbody.innerHTML = res.data.map(s => `
                <tr>
                    <td><strong>${s.subscriptionNumber}</strong></td>
                    <td>${escapeHtml(s.customerName)}<br><small style="color: var(--text-muted)">${escapeHtml(s.customerEmail)}</small></td>
                    <td>${escapeHtml(s.planName || '-')}</td>
                    <td>${s.quantity}x @ $${parseFloat(s.unitPrice).toFixed(2)}</td>
                    <td><strong>$${parseFloat(s.recurringAmount).toFixed(2)}</strong></td>
                    <td><span class="badge badge-info">${s.billingInterval}</span></td>
                    <td>${s.nextBillingDate}</td>
                    <td><span class="badge badge-${getStatusBadge(s.status)}">${s.status}</span></td>
                    <td>
                        <div style="display: flex; gap: 6px; flex-wrap: wrap;">
                            ${s.status === 'ACTIVE' ? `
                                <button class="btn btn-secondary btn-sm" onclick="pauseSubscription(${s.id})">Pause</button>
                            ` : ''}
                            ${s.status === 'PAUSED' ? `
                                <button class="btn btn-primary btn-sm" onclick="resumeSubscription(${s.id})">Resume</button>
                            ` : ''}
                            ${s.status !== 'CLOSED' ? `
                                <button class="btn btn-secondary btn-sm" onclick="renewSubscription(${s.id})">Renew</button>
                                <button class="btn btn-secondary btn-sm" style="color: var(--danger)" onclick="closeSubscription(${s.id})">Close</button>
                            ` : '<span style="color: var(--text-muted); font-size: 0.8rem;">Terminated</span>'}
                        </div>
                    </td>
                </tr>
            `).join('');
        } else {
            tbody.innerHTML = `<tr><td colspan="9" style="text-align: center; color: var(--text-muted); padding: 32px;">No active subscriptions.</td></tr>`;
        }
    } catch (err) {
        showToast('Failed to load subscriptions: ' + err.message, 'error');
    }
}

async function pauseSubscription(id) {
    try {
        await SubscriptionAPI.pause(id);
        showToast('Subscription paused', 'info');
        loadSubscriptions();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function resumeSubscription(id) {
    try {
        await SubscriptionAPI.resume(id);
        showToast('Subscription resumed to active status', 'success');
        loadSubscriptions();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function renewSubscription(id) {
    try {
        await SubscriptionAPI.renew(id, 1);
        showToast('Subscription renewed for 1 billing cycle', 'success');
        loadSubscriptions();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function closeSubscription(id) {
    if (!confirm('Are you sure you want to close/terminate this subscription?')) return;
    try {
        await SubscriptionAPI.close(id);
        showToast('Subscription closed', 'info');
        loadSubscriptions();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// -------------------------------------------------------------
// 6. INVOICES LOADER
// -------------------------------------------------------------
async function loadInvoices() {
    try {
        const res = await InvoiceAPI.getAll();
        const tbody = document.getElementById('invoices-table-body');
        if (!tbody) return;

        if (res.data && res.data.length > 0) {
            tbody.innerHTML = res.data.map(i => `
                <tr>
                    <td><strong>${i.invoiceNumber}</strong></td>
                    <td>${escapeHtml(i.customerName)}</td>
                    <td>${i.issueDate}</td>
                    <td>${i.dueDate}</td>
                    <td>$${parseFloat(i.totalAmount).toFixed(2)}</td>
                    <td style="color: var(--success)">$${parseFloat(i.paidAmount).toFixed(2)}</td>
                    <td style="color: ${parseFloat(i.outstandingAmount) > 0 ? 'var(--warning)' : 'var(--text-muted)'}">
                        $${parseFloat(i.outstandingAmount).toFixed(2)}
                    </td>
                    <td><span class="badge badge-${getStatusBadge(i.status)}">${i.status}</span></td>
                    <td>
                        <div style="display: flex; gap: 6px;">
                            ${parseFloat(i.outstandingAmount) > 0 && i.status !== 'CANCELLED' ? `
                                <button class="btn btn-primary btn-sm" onclick="openPaymentModalForInvoice(${i.id}, '${i.invoiceNumber}', ${i.outstandingAmount})">Pay</button>
                            ` : ''}
                            <button class="btn btn-secondary btn-sm" onclick="viewInvoiceDetails(${i.id})">View</button>
                        </div>
                    </td>
                </tr>
            `).join('');
        } else {
            tbody.innerHTML = `<tr><td colspan="9" style="text-align: center; color: var(--text-muted); padding: 32px;">No invoices generated.</td></tr>`;
        }
    } catch (err) {
        showToast('Failed to load invoices: ' + err.message, 'error');
    }
}

async function triggerRecurringInvoices() {
    try {
        showToast('Scanning due subscriptions and generating invoices...', 'info');
        const res = await InvoiceAPI.generateRecurring();
        showToast(res.message, 'success');
        loadInvoices();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function viewInvoiceDetails(id) {
    try {
        const res = await InvoiceAPI.getById(id);
        const inv = res.data;

        document.getElementById('modal-inv-number').textContent = inv.invoiceNumber;
        document.getElementById('modal-inv-customer').textContent = `${inv.customerName} (${inv.customerEmail})`;
        document.getElementById('modal-inv-issue').textContent = inv.issueDate;
        document.getElementById('modal-inv-due').textContent = inv.dueDate;
        document.getElementById('modal-inv-status').textContent = inv.status;
        document.getElementById('modal-inv-subtotal').textContent = `$${parseFloat(inv.subtotal).toFixed(2)}`;
        document.getElementById('modal-inv-tax').textContent = `$${parseFloat(inv.taxAmount).toFixed(2)} (${inv.taxRate}%)`;
        document.getElementById('modal-inv-discount').textContent = `-$${parseFloat(inv.discountAmount).toFixed(2)}`;
        document.getElementById('modal-inv-total').textContent = `$${parseFloat(inv.totalAmount).toFixed(2)}`;
        document.getElementById('modal-inv-paid').textContent = `$${parseFloat(inv.paidAmount).toFixed(2)}`;
        document.getElementById('modal-inv-outstanding').textContent = `$${parseFloat(inv.outstandingAmount).toFixed(2)}`;

        const itemsBody = document.getElementById('modal-inv-items');
        if (itemsBody && inv.items) {
            itemsBody.innerHTML = inv.items.map(item => `
                <tr>
                    <td>${escapeHtml(item.description)}</td>
                    <td>${item.quantity}</td>
                    <td>$${parseFloat(item.unitPrice).toFixed(2)}</td>
                    <td>$${parseFloat(item.lineTotal).toFixed(2)}</td>
                </tr>
            `).join('');
        }

        openModal('invoice-view-modal');
    } catch (err) {
        showToast('Failed to load invoice: ' + err.message, 'error');
    }
}

// -------------------------------------------------------------
// 7. DISCOUNTS LOADER
// -------------------------------------------------------------
async function loadDiscounts() {
    try {
        const res = await DiscountAPI.getAll();
        const tbody = document.getElementById('discounts-table-body');
        if (!tbody) return;

        if (res.data && res.data.length > 0) {
            tbody.innerHTML = res.data.map(d => `
                <tr>
                    <td><strong>${escapeHtml(d.code)}</strong></td>
                    <td><span class="badge badge-info">${d.type}</span></td>
                    <td>${d.type === 'PERCENTAGE' ? d.amount + '%' : '$' + parseFloat(d.amount).toFixed(2)}</td>
                    <td>$${parseFloat(d.minPurchaseAmount).toFixed(2)}</td>
                    <td>${d.timesUsed} / ${d.usageLimit}</td>
                    <td>${d.startDate} to ${d.endDate}</td>
                    <td><span class="badge badge-${d.active ? 'success' : 'draft'}">${d.active ? 'Active' : 'Disabled'}</span></td>
                    <td class="admin-only">
                        <button class="btn btn-secondary btn-sm" onclick="deleteDiscount(${d.id})">Delete</button>
                    </td>
                </tr>
            `).join('');
        } else {
            tbody.innerHTML = `<tr><td colspan="8" style="text-align: center; color: var(--text-muted); padding: 32px;">No discount codes available.</td></tr>`;
        }
    } catch (err) {
        showToast('Failed to load discounts: ' + err.message, 'error');
    }
}

async function deleteDiscount(id) {
    if (!confirm('Are you sure you want to delete this coupon?')) return;
    try {
        await DiscountAPI.delete(id);
        showToast('Coupon deleted', 'success');
        loadDiscounts();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// -------------------------------------------------------------
// 8. PAYMENTS LOADER
// -------------------------------------------------------------
async function loadPayments() {
    try {
        const res = await PaymentAPI.getAll();
        const tbody = document.getElementById('payments-table-body');
        if (!tbody) return;

        if (res.data && res.data.length > 0) {
            tbody.innerHTML = res.data.map(p => `
                <tr>
                    <td><strong>${p.paymentReference}</strong></td>
                    <td>${p.invoiceNumber || '-'}</td>
                    <td>${escapeHtml(p.customerName || '-')}</td>
                    <td><strong style="color: var(--success)">$${parseFloat(p.amount).toFixed(2)}</strong></td>
                    <td><span class="badge badge-info">${p.paymentMethod}</span></td>
                    <td>${p.paymentDate ? p.paymentDate.replace('T', ' ').substring(0, 16) : '-'}</td>
                    <td>${escapeHtml(p.recordedBy || 'System')}</td>
                    <td>${escapeHtml(p.notes || '-')}</td>
                </tr>
            `).join('');
        } else {
            tbody.innerHTML = `<tr><td colspan="8" style="text-align: center; color: var(--text-muted); padding: 32px;">No payment transactions recorded.</td></tr>`;
        }
    } catch (err) {
        showToast('Failed to load payments: ' + err.message, 'error');
    }
}

function openPaymentModalForInvoice(invoiceId, invoiceNumber, outstandingAmount) {
    document.getElementById('pay-invoice-id').value = invoiceId;
    document.getElementById('pay-invoice-display').textContent = `${invoiceNumber} (Remaining: $${parseFloat(outstandingAmount).toFixed(2)})`;
    document.getElementById('pay-amount').value = parseFloat(outstandingAmount).toFixed(2);
    document.getElementById('pay-amount').max = outstandingAmount;
    openModal('record-payment-modal');
}

// -------------------------------------------------------------
// 9. USERS & STAFF LOADER
// -------------------------------------------------------------
async function loadUsers() {
    try {
        const res = await AuthAPI.getUsers();
        const tbody = document.getElementById('users-table-body');
        if (!tbody) return;

        if (res.data && res.data.length > 0) {
            tbody.innerHTML = res.data.map(u => `
                <tr>
                    <td><strong>${escapeHtml(u.fullName)}</strong></td>
                    <td>${escapeHtml(u.email)}</td>
                    <td><span class="badge badge-${u.role === 'ADMIN' ? 'warning' : u.role === 'STAFF' ? 'info' : 'success'}">${u.role}</span></td>
                    <td><span class="badge badge-${u.active ? 'success' : 'draft'}">${u.active ? 'Active' : 'Disabled'}</span></td>
                    <td>${u.createdAt ? u.createdAt.substring(0, 10) : '-'}</td>
                </tr>
            `).join('');
        } else {
            tbody.innerHTML = `<tr><td colspan="5" style="text-align: center; color: var(--text-muted); padding: 32px;">No users found.</td></tr>`;
        }
    } catch (err) {
        showToast('Failed to load users: ' + err.message, 'error');
    }
}

// -------------------------------------------------------------
// MODAL FORMS HANDLERS
// -------------------------------------------------------------
function initForms() {
    // Form: Add Product
    const productForm = document.getElementById('form-add-product');
    if (productForm) {
        productForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const variantName = document.getElementById('prod-variant-name').value.trim();
            const variantPrice = parseFloat(document.getElementById('prod-variant-price').value || 0);

            const variants = variantName ? [{ variantName, additionalPrice: variantPrice, sku: 'SKU-' + Date.now() }] : [];

            const payload = {
                name: document.getElementById('prod-name').value.trim(),
                type: document.getElementById('prod-type').value,
                sellingPrice: parseFloat(document.getElementById('prod-selling-price').value),
                costPrice: parseFloat(document.getElementById('prod-cost-price').value),
                notes: document.getElementById('prod-notes').value.trim(),
                variants
            };

            try {
                await ProductAPI.create(payload);
                showToast('Product created successfully!', 'success');
                productForm.reset();
                closeModal('add-product-modal');
                loadProducts();
            } catch (err) {
                showToast(err.message, 'error');
            }
        });
    }

    // Form: Create Plan
    const planForm = document.getElementById('form-add-plan');
    if (planForm) {
        planForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const payload = {
                planName: document.getElementById('plan-name').value.trim(),
                billingInterval: document.getElementById('plan-interval').value,
                intervalCount: parseInt(document.getElementById('plan-interval-count').value || 1),
                basePrice: parseFloat(document.getElementById('plan-price').value),
                minQuantity: parseInt(document.getElementById('plan-min-qty').value || 1),
                startDate: document.getElementById('plan-start-date').value || new Date().toISOString().substring(0, 10),
                description: document.getElementById('plan-desc').value.trim()
            };

            try {
                await PlanAPI.create(payload);
                showToast('Plan created successfully!', 'success');
                planForm.reset();
                closeModal('add-plan-modal');
                loadPlans();
            } catch (err) {
                showToast(err.message, 'error');
            }
        });
    }

    // Form: Create Quotation
    const quoteForm = document.getElementById('form-add-quotation');
    if (quoteForm) {
        quoteForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const payload = {
                customerId: parseInt(document.getElementById('quote-customer-id').value),
                validUntil: document.getElementById('quote-valid-until').value,
                discountAmount: parseFloat(document.getElementById('quote-discount').value || 0),
                notes: document.getElementById('quote-notes').value.trim(),
                items: [{
                    planId: parseInt(document.getElementById('quote-plan-id').value),
                    productId: parseInt(document.getElementById('quote-product-id').value || null) || null,
                    quantity: parseInt(document.getElementById('quote-qty').value || 1)
                }]
            };

            try {
                await QuotationAPI.create(payload);
                showToast('Quotation created successfully!', 'success');
                quoteForm.reset();
                closeModal('add-quotation-modal');
                loadQuotations();
            } catch (err) {
                showToast(err.message, 'error');
            }
        });
    }

    // Form: Create Subscription
    const subForm = document.getElementById('form-add-subscription');
    if (subForm) {
        subForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const payload = {
                customerId: parseInt(document.getElementById('sub-customer-id').value),
                planId: parseInt(document.getElementById('sub-plan-id').value),
                productId: parseInt(document.getElementById('sub-product-id').value || null) || null,
                quantity: parseInt(document.getElementById('sub-qty').value || 1),
                startDate: document.getElementById('sub-start-date').value || new Date().toISOString().substring(0, 10)
            };

            try {
                await SubscriptionAPI.create(payload);
                showToast('Subscription created successfully!', 'success');
                subForm.reset();
                closeModal('add-subscription-modal');
                loadSubscriptions();
                if (activeView === 'dashboard') loadDashboard();
            } catch (err) {
                showToast(err.message, 'error');
            }
        });
    }

    // Form: Record Payment
    const payForm = document.getElementById('form-record-payment');
    if (payForm) {
        payForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const payload = {
                invoiceId: parseInt(document.getElementById('pay-invoice-id').value),
                amount: parseFloat(document.getElementById('pay-amount').value),
                paymentMethod: document.getElementById('pay-method').value,
                notes: document.getElementById('pay-notes').value.trim()
            };

            try {
                await PaymentAPI.record(payload);
                showToast('Payment recorded successfully!', 'success');
                payForm.reset();
                closeModal('record-payment-modal');
                if (activeView === 'invoices') loadInvoices();
                if (activeView === 'payments') loadPayments();
                if (activeView === 'dashboard') loadDashboard();
            } catch (err) {
                showToast(err.message, 'error');
            }
        });
    }

    // Form: Create Discount
    const discountForm = document.getElementById('form-add-discount');
    if (discountForm) {
        discountForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const payload = {
                code: document.getElementById('disc-code').value.trim().toUpperCase(),
                type: document.getElementById('disc-type').value,
                amount: parseFloat(document.getElementById('disc-amount').value),
                minPurchaseAmount: parseFloat(document.getElementById('disc-min-spend').value || 0),
                minQuantity: parseInt(document.getElementById('disc-min-qty').value || 1),
                startDate: document.getElementById('disc-start-date').value,
                endDate: document.getElementById('disc-end-date').value,
                usageLimit: parseInt(document.getElementById('disc-limit').value || 100)
            };

            try {
                await DiscountAPI.create(payload);
                showToast('Discount code created!', 'success');
                discountForm.reset();
                closeModal('add-discount-modal');
                loadDiscounts();
            } catch (err) {
                showToast(err.message, 'error');
            }
        });
    }

    // Form: Provision Staff
    const staffForm = document.getElementById('form-create-staff');
    if (staffForm) {
        staffForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const payload = {
                fullName: document.getElementById('staff-name').value.trim(),
                email: document.getElementById('staff-email').value.trim(),
                password: document.getElementById('staff-password').value
            };

            try {
                await AuthAPI.createStaff(payload);
                showToast('Staff account created!', 'success');
                staffForm.reset();
                closeModal('create-staff-modal');
                loadUsers();
            } catch (err) {
                showToast(err.message, 'error');
            }
        });
    }

    // Form: Custom Login
    const loginForm = document.getElementById('form-login');
    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const email = document.getElementById('login-email').value.trim();
            const password = document.getElementById('login-password').value;
            await loginAs(email, password);
        });
    }

    // Global Search Table Filter
    const searchInput = document.getElementById('global-search');
    if (searchInput) {
        searchInput.addEventListener('input', (e) => {
            const query = e.target.value.toLowerCase().trim();
            const activeSection = document.querySelector('.view-section.active');
            if (!activeSection) return;
            const rows = activeSection.querySelectorAll('.data-table tbody tr');
            rows.forEach(row => {
                const text = row.textContent.toLowerCase();
                row.style.display = text.includes(query) ? '' : 'none';
            });
        });
    }
}

// -------------------------------------------------------------
// DROPDOWN PREPARATION FOR MODALS
// -------------------------------------------------------------
async function prepareQuoteModal() {
    try {
        const [usersRes, plansRes, prodsRes] = await Promise.all([
            AuthAPI.getUsers(),
            PlanAPI.getAll(),
            ProductAPI.getAll()
        ]);

        const custSelect = document.getElementById('quote-customer-id');
        if (custSelect && usersRes.data) {
            custSelect.innerHTML = usersRes.data.map(u => `
                <option value="${u.id}">${escapeHtml(u.fullName)} (${escapeHtml(u.email)}) - ${u.role}</option>
            `).join('');
        }

        const planSelect = document.getElementById('quote-plan-id');
        if (planSelect && plansRes.data) {
            planSelect.innerHTML = plansRes.data.map(p => `
                <option value="${p.id}">${escapeHtml(p.planName)} ($${parseFloat(p.basePrice).toFixed(2)} / ${p.billingInterval})</option>
            `).join('');
        }

        const prodSelect = document.getElementById('quote-product-id');
        if (prodSelect && prodsRes.data) {
            prodSelect.innerHTML = '<option value="">-- No Add-on Product --</option>' + prodsRes.data.map(p => `
                <option value="${p.id}">${escapeHtml(p.name)} ($${parseFloat(p.sellingPrice).toFixed(2)})</option>
            `).join('');
        }

        const dateInput = document.getElementById('quote-valid-until');
        if (dateInput && !dateInput.value) {
            const defaultDate = new Date();
            defaultDate.setDate(defaultDate.getDate() + 30);
            dateInput.value = defaultDate.toISOString().substring(0, 10);
        }
    } catch (err) {
        console.error('Failed to prepare quote modal:', err);
    }
}

async function prepareSubscriptionModal() {
    try {
        const [usersRes, plansRes, prodsRes] = await Promise.all([
            AuthAPI.getUsers(),
            PlanAPI.getAll(),
            ProductAPI.getAll()
        ]);

        const custSelect = document.getElementById('sub-customer-id');
        if (custSelect && usersRes.data) {
            custSelect.innerHTML = usersRes.data.map(u => `
                <option value="${u.id}">${escapeHtml(u.fullName)} (${escapeHtml(u.email)})</option>
            `).join('');
        }

        const planSelect = document.getElementById('sub-plan-id');
        if (planSelect && plansRes.data) {
            planSelect.innerHTML = plansRes.data.map(p => `
                <option value="${p.id}">${escapeHtml(p.planName)} ($${parseFloat(p.basePrice).toFixed(2)} / ${p.billingInterval})</option>
            `).join('');
        }

        const prodSelect = document.getElementById('sub-product-id');
        if (prodSelect && prodsRes.data) {
            prodSelect.innerHTML = '<option value="">-- No Add-on Product --</option>' + prodsRes.data.map(p => `
                <option value="${p.id}">${escapeHtml(p.name)} ($${parseFloat(p.sellingPrice).toFixed(2)})</option>
            `).join('');
        }

        const startInput = document.getElementById('sub-start-date');
        if (startInput && !startInput.value) {
            startInput.value = new Date().toISOString().substring(0, 10);
        }
    } catch (err) {
        console.error('Failed to prepare subscription modal:', err);
    }
}

// Utility: Map status string to badge CSS class
function getStatusBadge(status) {
    if (!status) return 'draft';
    switch (status.toUpperCase()) {
        case 'ACTIVE':
        case 'PAID':
        case 'CONFIRMED':
            return 'success';
        case 'OVERDUE':
        case 'EXPIRED':
        case 'CANCELLED':
            return 'danger';
        case 'PAUSED':
        case 'SENT':
            return 'warning';
        case 'DRAFT':
        case 'CLOSED':
        case 'CONVERTED':
        default:
            return 'draft';
    }
}

function escapeHtml(text) {
    if (!text) return '';
    const map = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' };
    return String(text).replace(/[&<>"']/g, m => map[m]);
}
