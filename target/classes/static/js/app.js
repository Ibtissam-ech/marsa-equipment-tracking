const API_BASE = '/api';
let currentUser = null;

document.addEventListener('DOMContentLoaded', function() {
    const userData = sessionStorage.getItem('user');
    if (!userData) { window.location.href = '/login'; return; }
    currentUser = JSON.parse(userData);
    initApp();
});

function initApp() {
    document.getElementById('userName').textContent = currentUser.fullName;
    loadDashboard();
    loadProducts();
    loadUsers();
    loadAssignments();
    loadTickets();
}

function showTab(tabName) {
    document.querySelectorAll('.tab-content').forEach(t => t.classList.remove('active'));
    document.querySelectorAll('.nav-menu a').forEach(l => l.classList.remove('active'));
    document.getElementById(tabName + '-tab').classList.add('active');
    event.target.closest('a').classList.add('active');
}

function loadDashboard() {
    Promise.all([
        fetch(API_BASE + '/equipment').then(r => r.json()),
        fetch(API_BASE + '/tickets/open').then(r => r.json())
    ]).then(([products, tickets]) => {
        document.getElementById('totalProducts').textContent = products.length;
        document.getElementById('availableProducts').textContent = products.filter(p => p.status === 'AVAILABLE').length;
        document.getElementById('assignedProducts').textContent = products.filter(p => p.status === 'ASSIGNED').length;
        document.getElementById('openTickets').textContent = tickets.length;
    });
}

function loadProducts() {
    fetch(API_BASE + '/equipment').then(r => r.json()).then(products => {
        window.allProducts = products;
        renderProductsTable(products);
    });
}

function renderProductsTable(products) {
    const tbody = document.getElementById('products-list');
    if (products.length === 0) { tbody.innerHTML = '<tr><td colspan="5">Aucun équipement</td></tr>'; return; }
    tbody.innerHTML = products.map(p => `
        <tr>
            <td>#${p.id}</td>
            <td>${p.model}</td>
            <td>${p.serialNumber}</td>
            <td><span class="status-badge status-${p.status}">${p.status}</span></td>
            <td><button class="btn btn-sm" onclick="deleteProduct(${p.id})">Supprimer</button></td>
        </tr>
    `).join('');
}

function filterProducts() {
    const search = document.getElementById('productSearch').value.toLowerCase();
    const filtered = window.allProducts.filter(p => p.model?.toLowerCase().includes(search));
    renderProductsTable(filtered);
}

function loadUsers() {
    fetch(API_BASE + '/users').then(r => r.json()).then(users => {
        const tbody = document.getElementById('users-list');
        tbody.innerHTML = users.map(u => `<tr><td>#${u.id}</td><td>${u.fullName}</td><td>${u.email}</td><td>${u.role}</td></tr>`).join('');
    });
}

function loadAssignments() {
    fetch(API_BASE + '/assignments/current').then(r => r.json()).then(assignments => {
        const tbody = document.getElementById('assignments-list');
        if (assignments.length === 0) { tbody.innerHTML = '<tr><td colspan="4">Aucune affectation</td></tr>'; return; }
        tbody.innerHTML = assignments.map(a => `
            <tr>
                <td>${a.equipment?.model}</td>
                <td>${a.user?.fullName}</td>
                <td>${new Date(a.startDate).toLocaleDateString('fr-FR')}</td>
                <td><button class="btn btn-sm" onclick="endAssignment(${a.equipment?.id})">Retourner</button></td>
            </tr>
        `).join('');
    });
}

function loadTickets() {
    fetch(API_BASE + '/tickets/open').then(r => r.json()).then(tickets => {
        const tbody = document.getElementById('tickets-list');
        if (tickets.length === 0) { tbody.innerHTML = '<tr><td colspan="4">Aucun ticket</td></tr>'; return; }
        tbody.innerHTML = tickets.map(t => `<tr><td>#${t.id}</td><td>${t.title}</td><td>${t.priority}</td><td>${t.status}</td></tr>`).join('');
    });
}

function showAddProductModal() { document.getElementById('add-product-modal').classList.add('active'); }
function closeModal(id) { document.getElementById(id).classList.remove('active'); }
function logout() { sessionStorage.clear(); window.location.href = '/login'; }

window.onclick = function(event) {
    if (event.target.classList.contains('modal')) closeModal(event.target.id);
};