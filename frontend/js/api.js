/**
 * Brightloop API Client
 * Enterprise REST client connecting all frontend views to the Spring Boot backend.
 */

const API_CONFIG = {
    baseUrl: 'http://localhost:8080/api',
    timeout: 10000
};

// Generic authenticated fetch wrapper
async function apiFetch(endpoint, options = {}) {
    const url = `${API_CONFIG.baseUrl}${endpoint.startsWith('/') ? endpoint : '/' + endpoint}`;
    const defaultHeaders = {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    };

    const token = localStorage.getItem('brightloop_token');
    if (token) {
        defaultHeaders['Authorization'] = `Bearer ${token}`;
    }

    const config = {
        ...options,
        headers: {
            ...defaultHeaders,
            ...options.headers
        }
    };

    try {
        const response = await fetch(url, config);
        const data = await response.json().catch(() => null);

        if (!response.ok) {
            if (response.status === 401) {
                // If unauthorized and not on login page, clear token
                localStorage.removeItem('brightloop_token');
                localStorage.removeItem('brightloop_user');
            }
            const errorMessage = data?.message || `Request failed with status ${response.status}`;
            throw new Error(errorMessage);
        }

        return data;
    } catch (err) {
        console.error(`[API Error] ${endpoint}:`, err);
        throw err;
    }
}

// System Health
async function checkBackendHealth() {
    try {
        const response = await apiFetch('/health');
        return { isOnline: true, data: response.data, message: response.message };
    } catch (error) {
        return { isOnline: false, error: error.message };
    }
}

// Authentication & Users
const AuthAPI = {
    login: (credentials) => apiFetch('/auth/login', { method: 'POST', body: JSON.stringify(credentials) }),
    signup: (data) => apiFetch('/auth/signup', { method: 'POST', body: JSON.stringify(data) }),
    createStaff: (data) => apiFetch('/auth/create-staff', { method: 'POST', body: JSON.stringify(data) }),
    forgotPassword: (email) => apiFetch('/auth/forgot-password', { method: 'POST', body: JSON.stringify({ email }) }),
    resetPassword: (token, newPassword) => apiFetch('/auth/reset-password', { method: 'POST', body: JSON.stringify({ token, newPassword }) }),
    me: () => apiFetch('/auth/me'),
    getUsers: () => apiFetch('/auth/users')
};

// Products & Plans
const ProductAPI = {
    getAll: () => apiFetch('/products'),
    getById: (id) => apiFetch(`/products/${id}`),
    create: (data) => apiFetch('/products', { method: 'POST', body: JSON.stringify(data) }),
    update: (id, data) => apiFetch(`/products/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
    delete: (id) => apiFetch(`/products/${id}`, { method: 'DELETE' })
};

const PlanAPI = {
    getAll: () => apiFetch('/plans'),
    getById: (id) => apiFetch(`/plans/${id}`),
    create: (data) => apiFetch('/plans', { method: 'POST', body: JSON.stringify(data) }),
    update: (id, data) => apiFetch(`/plans/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
    delete: (id) => apiFetch(`/plans/${id}`, { method: 'DELETE' })
};

// Quotations
const QuotationAPI = {
    getAll: () => apiFetch('/quotations'),
    getById: (id) => apiFetch(`/quotations/${id}`),
    create: (data) => apiFetch('/quotations', { method: 'POST', body: JSON.stringify(data) }),
    confirm: (id) => apiFetch(`/quotations/${id}/confirm`, { method: 'PATCH' }),
    convertToSubscription: (id) => apiFetch(`/quotations/${id}/convert-to-subscription`, { method: 'POST' })
};

// Subscriptions
const SubscriptionAPI = {
    getAll: (params = {}) => {
        const query = new URLSearchParams(params).toString();
        return apiFetch(`/subscriptions${query ? '?' + query : ''}`);
    },
    getById: (id) => apiFetch(`/subscriptions/${id}`),
    create: (data) => apiFetch('/subscriptions', { method: 'POST', body: JSON.stringify(data) }),
    pause: (id) => apiFetch(`/subscriptions/${id}/pause`, { method: 'PATCH' }),
    resume: (id) => apiFetch(`/subscriptions/${id}/resume`, { method: 'PATCH' }),
    renew: (id, cycles = 1) => apiFetch(`/subscriptions/${id}/renew?cycles=${cycles}`, { method: 'PATCH' }),
    close: (id) => apiFetch(`/subscriptions/${id}/close`, { method: 'PATCH' })
};

// Invoices
const InvoiceAPI = {
    getAll: (params = {}) => {
        const query = new URLSearchParams(params).toString();
        return apiFetch(`/invoices${query ? '?' + query : ''}`);
    },
    getById: (id) => apiFetch(`/invoices/${id}`),
    create: (data) => apiFetch('/invoices', { method: 'POST', body: JSON.stringify(data) }),
    confirm: (id) => apiFetch(`/invoices/${id}/confirm`, { method: 'PATCH' }),
    cancel: (id) => apiFetch(`/invoices/${id}/cancel`, { method: 'PATCH' }),
    generateRecurring: () => apiFetch('/invoices/generate-recurring', { method: 'POST' })
};

// Discounts
const DiscountAPI = {
    getAll: () => apiFetch('/discounts'),
    create: (data) => apiFetch('/discounts', { method: 'POST', body: JSON.stringify(data) }),
    delete: (id) => apiFetch(`/discounts/${id}`, { method: 'DELETE' }),
    validate: (code, purchaseAmount, quantity = 1) => apiFetch('/discounts/validate', {
        method: 'POST',
        body: JSON.stringify({ code, purchaseAmount, quantity })
    })
};

// Payments
const PaymentAPI = {
    getAll: () => apiFetch('/payments'),
    getById: (id) => apiFetch(`/payments/${id}`),
    getByInvoice: (invoiceId) => apiFetch(`/payments/by-invoice/${invoiceId}`),
    record: (data) => apiFetch('/payments', { method: 'POST', body: JSON.stringify(data) })
};

// Dashboard
const DashboardAPI = {
    getSummary: () => apiFetch('/dashboard/summary')
};
