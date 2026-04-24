const API_BASE = '/api';
let currentUser = null;

function formatDateSafe(dateStr) {
    if (!dateStr || dateStr === 'null' || dateStr === 'undefined') return '-';
    try {
        const d = new Date(dateStr);
        if (isNaN(d.getTime())) return '-';
        return d.toLocaleDateString('fr-FR');
    } catch { return '-'; }
}

document.addEventListener('DOMContentLoaded', function() {
    const userData = sessionStorage.getItem('user');
    if (!userData) {
        window.location.href = '/login';
        return;
    }
    currentUser = JSON.parse(userData);
    initApp();
});

function initApp() {
    updateUserUI();
    applyPermissions();
    loadDashboard();
    loadProducts();
    loadUsers();
    loadCurrentAssignments();
    loadOpenTickets();
    loadProductSelectors();
    loadUserSelectors();
    
    document.getElementById('assignment-form').addEventListener('submit', handleAssignment);
    document.getElementById('intervention-form').addEventListener('submit', handleIntervention);
}

function updateUserUI() {
    if (!currentUser) return;
    document.getElementById('userName').textContent = currentUser.fullName;
    document.getElementById('userRole').textContent = currentUser.role;
    document.getElementById('userAvatar').textContent = currentUser.fullName.charAt(0).toUpperCase();
}

function applyPermissions() {
    const canAdd = currentUser.role === 'ADMIN';
    const addProductBtn = document.getElementById('addProductBtn');
    if (addProductBtn) addProductBtn.style.display = canAdd ? 'inline-flex' : 'none';
}

function showTab(tabName) {
    event.preventDefault();
    document.querySelectorAll('.tab-content').forEach(tab => tab.classList.remove('active'));
    document.querySelectorAll('.nav-menu a').forEach(link => link.classList.remove('active'));
    
    document.getElementById(tabName + '-tab').classList.add('active');
    event.target.closest('a').classList.add('active');
    
    if (tabName === 'assignments') loadCurrentAssignments();
    else if (tabName === 'interventions') loadOpenTickets();
    else if (tabName === 'dashboard') loadDashboard();
}

function showToast(title, message, type = 'success') {
    const container = document.getElementById('toastContainer');
    const toast = document.createElement('div');
    toast.className = 'toast toast-' + type;
    
    const icons = {
        success: '<path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/>',
        error: '<circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/>',
    };
    
    toast.innerHTML = '<svg class="toast-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">' + (icons[type] || icons.success) + '</svg><div><div style="font-weight:600">' + title + '</div><div style="font-size:13px;color:var(--text-secondary)">' + message + '</div></div>';
    
    container.appendChild(toast);
    setTimeout(() => { toast.classList.add('toast-exit'); setTimeout(() => toast.remove(), 300); }, 4000);
}

function loadDashboard() {
    Promise.all([
        fetch(API_BASE + '/equipment').then(r => r.json()),
        fetch(API_BASE + '/tickets/open').then(r => r.json()),
        fetch(API_BASE + '/assignments/current').then(r => r.json())
    ]).then(([products, tickets, assignments]) => {
        document.getElementById('totalProducts').textContent = products.length;
        document.getElementById('availableProducts').textContent = products.filter(p => p.status === 'AVAILABLE').length;
        document.getElementById('assignedProducts').textContent = products.filter(p => p.status === 'ASSIGNED').length;
        document.getElementById('openTickets').textContent = tickets.length;
        
        const tbody = document.getElementById('recent-assignments');
        if (assignments.length === 0) {
            tbody.innerHTML = '<tr><td colspan="3"><div class="empty-state"><p>Aucune affectation</p></div></td></tr>';
        } else {
            tbody.innerHTML = assignments.slice(0, 5).map(a => '<tr><td>' + (a.user?.username || '-') + '</td><td>' + (a.equipment?.model || '-') + '</td><td>' + formatDateSafe(a.startDate) + '</td></tr>').join('');
        }
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
    if (products.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7"><div class="empty-state"><p>Aucun équipement</p></div></td></tr>';
        return;
    }
    const statusMap = {'AVAILABLE': 'Disponible', 'ASSIGNED': 'Affecté', 'MAINTENANCE': 'En maintenance', 'RETIRED': 'Retiré'};
    tbody.innerHTML = products.map(p => '<tr><td>#' + p.id + '</td><td>' + (p.name || '-') + '</td><td>' + (p.model || '-') + '</td><td><code>' + (p.serialNumber || '-') + '</code></td><td>' + (p.currentUser ? ((p.currentUser.name ? p.currentUser.name : p.currentUser.username)) : '<span style="color:#999">Non affecté</span>') + '</td><td><span class="status-badge status-' + p.status + '">' + (statusMap[p.status] || p.status) + '</span></td><td><button class="btn btn-secondary btn-sm" onclick="viewProductDetails(' + p.id + ')">Détails</button> <button class="btn btn-primary btn-sm" onclick="generateEquipmentPdf(' + p.id + ')">PDF</button></td></tr>').join('');
}

function filterProducts() {
    const search = document.getElementById('productSearch').value.toLowerCase();
    const filtered = window.allProducts.filter(p => p.model?.toLowerCase().includes(search) || p.serialNumber?.toLowerCase().includes(search));
    renderProductsTable(filtered);
}

function loadUsers() {
    fetch(API_BASE + '/users').then(r => r.json()).then(users => {
        const affectataires = users.filter(u => u.role !== 'ADMIN');
        const tbody = document.getElementById('users-list');
        if (affectataires.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7"><div class="empty-state"><p>Aucun affectataire</p></div></td></tr>';
            return;
        }
        tbody.innerHTML = affectataires.map(u => {
            const displayName = u.name ? u.name : u.username;
            return '<tr><td>#' + u.id + '</td><td><strong>' + displayName + '</strong></td><td>' + (u.email || '-') + '</td><td>' + (u.department || '-') + '</td><td>' + (u.fonction || '-') + '</td><td><span class="status-badge status-AFFECTATAIRE">Affectataire</span></td><td><button class="btn btn-secondary btn-sm" onclick="viewUserDetail(' + u.id + ')">Détails</button> <button class="btn btn-primary btn-sm" onclick="generateUserFiche(' + u.id + ')">PDF</button></td></tr>';
        }).join('');
    });
}

function loadCurrentAssignments() {
    fetch(API_BASE + '/assignments/history').then(r => r.json()).then(assignments => {
        const tbody = document.getElementById('current-assignments-list');
        if (assignments.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6"><div class="empty-state"><p>Aucune affectation</p></div></td></tr>';
            return;
        }
        tbody.innerHTML = assignments.map(a => {
            const userName = a.user && a.user.name ? a.user.name : (a.user ? a.user.username : '-');
            return '<tr><td>' + (a.equipment?.model || '-') + '</td><td><strong>' + userName + '</strong></td><td>' + formatDateSafe(a.startDate) + '</td><td>' + formatDateSafe(a.endDate) + '</td><td>' + (a.endDate ? '<span class="status-badge status-RETURNED">Terminé</span>' : '<span class="status-badge status-ACTIVE">Actif</span>') + '</td><td><button class="btn btn-primary btn-sm" onclick="generateEquipmentPdf(' + a.equipment?.id + ')">PDF Équipement</button></td></tr>';
        }).join('');
    });
}

function loadOpenTickets() {
    fetch(API_BASE + '/tickets/open').then(r => r.json()).then(tickets => {
        const tbody = document.getElementById('open-tickets-list');
        if (tickets.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7"><div class="empty-state"><p>Aucun ticket ouvert</p></div></td></tr>';
            return;
        }
        tbody.innerHTML = tickets.map(t => '<tr><td>#' + t.id + '</td><td>' + t.title + '</td><td>' + (t.equipment?.model || '-') + '</td><td><span class="priority-' + t.priority + '">' + t.priority + '</span></td><td><span class="status-badge status-' + t.status + '">' + t.status + '</span></td><td>' + formatDateSafe(t.createdAt) + '</td><td><button class="btn btn-success btn-sm" onclick="showCloseTicketModal(' + t.id + ')">Fermer</button> <button class="btn btn-primary btn-sm" onclick="generateTicketPdf(' + t.id + ')">PDF</button></td></tr>').join('');
    });
}

function loadProductSelectors() {
    fetch(API_BASE + '/equipment').then(r => r.json()).then(products => {
        const assignSelect = document.getElementById('assign-product-id');
        const ticketSelect = document.getElementById('ticket-product-id');
        
        [assignSelect, ticketSelect].forEach(s => { if (s) s.innerHTML = '<option value="">Sélectionner</option>'; });
        
        products.forEach(p => {
            const isAffected = p.status && p.status.toUpperCase() === 'ASSIGNED';
            if (assignSelect && !isAffected) assignSelect.innerHTML += '<option value="' + p.id + '">' + p.model + ' - ' + p.serialNumber + '</option>';
            if (ticketSelect) ticketSelect.innerHTML += '<option value="' + p.id + '">' + p.model + ' - ' + p.serialNumber + '</option>';
        });
    });
}

function loadUserSelectors() {
    fetch(API_BASE + '/users').then(r => r.json()).then(users => {
        const assignUserSelect = document.getElementById('assign-user-id');
        
        if (assignUserSelect) {
            assignUserSelect.innerHTML = '<option value="">Sélectionner</option>';
            users.forEach(u => { 
                if (u.role !== 'ADMIN') {
                    assignUserSelect.innerHTML += '<option value="' + u.id + '">' + u.username + ' (' + (u.department || 'N/A') + ')</option>'; 
                }
            });
        }
    });
}

function handleAssignment(e) {
    if (e) e.preventDefault();
    const productId = document.getElementById('assign-product-id').value;
    const userId = document.getElementById('assign-user-id').value;
    const notes = document.getElementById('assign-notes').value;
    
    if (!productId || !userId) {
        showToast('Erreur', 'Veuillez sélectionner un produit et un utilisateur', 'error');
        return;
    }
    
    fetch(API_BASE + '/assignments/assign', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ productId: parseInt(productId), userId: parseInt(userId), assignedBy: currentUser.id, notes })
    }).then(() => {
        showToast('Succès', 'Produit affecté avec succès!', 'success');
        document.getElementById('assignment-form').reset();
        closeModal('add-assignment-modal');
        loadProducts();
        loadCurrentAssignments();
        loadProductSelectors();
        loadDashboard();
    }).catch(() => showToast('Erreur', "Erreur lors de l'affectation", 'error'));
}

function endAssignment(productId) {
    if (!confirm('Voulez-vous vraiment terminer cette affectation?')) return;
    
    fetch(API_BASE + '/assignments/end/' + productId, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ notes: 'Retourné' })
    }).then(() => {
        showToast('Succès', 'Affectation terminée!', 'success');
        loadProducts();
        loadCurrentAssignments();
        loadProductSelectors();
        loadDashboard();
    }).catch(() => showToast('Erreur', 'Erreur lors de la fin de l\'affectation', 'error'));
}

function handleIntervention(e) {
    if (e) e.preventDefault();
    const productId = document.getElementById('ticket-product-id').value;
    const title = document.getElementById('ticket-title').value;
    const description = document.getElementById('ticket-description').value;
    const priority = document.getElementById('ticket-priority').value;
    const interventionType = document.getElementById('ticket-type').value;
    
    if (!productId || !title) {
        showToast('Erreur', 'Veuillez remplir tous les champs obligatoires', 'error');
        return;
    }
    
    fetch(API_BASE + '/tickets', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ productId: parseInt(productId), title: title, description: description, priority: priority, interventionType: interventionType, createdBy: currentUser.id })
    }).then(r => r.json()).then(() => {
        showToast('Succès', 'Ticket créé avec succès!', 'success');
        document.getElementById('intervention-form').reset();
        closeModal('add-ticket-modal');
        loadOpenTickets();
        loadDashboard();
    }).catch(() => showToast('Erreur', 'Erreur lors de la création du ticket', 'error'));
}

function showAddProductModal() { openModal('add-product-modal'); }
function showAddAssignmentModal() { openModal('add-assignment-modal'); }
function showAddTicketModal() { openModal('add-ticket-modal'); }
function showAddUserModal() { openModal('add-user-modal'); }

function addProduct() {
    const product = {
        name: document.getElementById('product-name')?.value,
        model: document.getElementById('product-model').value,
        serialNumber: document.getElementById('product-serial').value,
        brand: document.getElementById('product-brand').value,
        category: document.getElementById('product-category').value,
        createdBy: currentUser.id
    };
    
    if (!product.model || !product.serialNumber) {
        showToast('Erreur', 'Modèle et numéro de série sont obligatoires', 'error');
        return;
    }
    
    fetch(API_BASE + '/equipment', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(product)
    }).then(r => r.json()).then(() => {
        showToast('Succès', 'Produit ajouté avec succès!', 'success');
        closeModal('add-product-modal');
        document.getElementById('add-product-form').reset();
        loadProducts();
        loadProductSelectors();
        loadDashboard();
    }).catch(() => showToast('Erreur', 'Erreur lors de l\'ajout du produit', 'error'));
}

function showCloseTicketModal(ticketId) {
    document.getElementById('close-ticket-id').value = ticketId;
    openModal('close-ticket-modal');
}

function addUser() {
    const user = {
        username: document.getElementById('user-name').value,
        email: document.getElementById('user-email').value,
        department: document.getElementById('user-department').value,
        fonction: document.getElementById('user-fonction').value,
        role: 'AFFECTATAIRE',
        password: 'password'
    };
    
    if (!user.username) {
        showToast('Erreur', 'Le nom est obligatoire', 'error');
        return;
    }
    
    fetch(API_BASE + '/users', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(user)
    }).then(r => r.json()).then(() => {
        showToast('Succès', 'Affectataire ajouté avec succès!', 'success');
        closeModal('add-user-modal');
        document.getElementById('add-user-form').reset();
        loadUsers();
        loadUserSelectors();
    }).catch(() => showToast('Erreur', 'Erreur lors de l\'ajout de l\'affectataire', 'error'));
}

function confirmCloseTicket() {
    const ticketId = document.getElementById('close-ticket-id').value;
    const notes = document.getElementById('close-ticket-notes').value;
    
    fetch(API_BASE + '/tickets/' + ticketId + '/close', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ resolutionNotes: notes, closedBy: currentUser.id })
    }).then(() => {
        showToast('Succès', 'Ticket fermé!', 'success');
        closeModal('close-ticket-modal');
        document.getElementById('close-ticket-notes').value = '';
        loadOpenTickets();
        loadDashboard();
    }).catch(() => showToast('Erreur', 'Erreur lors de la fermeture du ticket', 'error'));
}

function viewProductHistory(productId) {
    showToast('Info', 'Fonctionnalité en cours de développement', 'info');
}

function viewUserDetail(userId) {
    fetch(API_BASE + '/users/' + userId).then(r => r.json()).then(user => {
        fetch(API_BASE + '/users/' + userId + '/assignments').then(r => r.json()).then(assignments => {
            const currentAssignments = assignments.filter(a => !a.endDate);
            document.getElementById('user-detail-title').textContent = user.fullName;
            document.getElementById('user-detail-content').innerHTML = '<div class="form-row"><div class="form-group"><label>Email</label><p>' + (user.email || '-') + '</p></div><div class="form-group"><label>CIN</label><p>' + (user.cin || '-') + '</p></div></div><div class="form-row"><div class="form-group"><label>Département</label><p>' + (user.department || '-') + '</p></div><div class="form-group"><label>Fonction</label><p>' + (user.fonction || '-') + '</p></div></div><h4 style="margin:24px 0 16px">Équipements affectés (' + currentAssignments.length + ')</h4>' + (currentAssignments.length ? '<table class="data-table"><thead><tr><th>Modèle</th><th>N° Série</th><th>Date</th></tr></thead><tbody>' + currentAssignments.map(a => '<tr><td>' + (a.equipment?.model || '-') + '</td><td><code>' + (a.equipment?.serialNumber || '-') + '</code></td><td>' + formatDateSafe(a.startDate) + '</td></tr>').join('') + '</tbody></table>' : '<p>Aucun équipement affecté</p>');
            openModal('user-detail-modal');
        });
    });
}

function generateUserFiche(userId) {
    window.open(API_BASE + '/pdf/fiche-affectation/user/' + userId, '_blank');
}

function generateEquipmentPdf(equipmentId) {
    window.open(API_BASE + '/pdf/fiche-affectation/equipment/' + equipmentId, '_blank');
}

function generateTicketPdf(ticketId) {
    window.open(API_BASE + '/pdf/intervention/' + ticketId, '_blank');
}

function openModal(modalId) {
    document.getElementById(modalId).classList.add('active');
    document.body.style.overflow = 'hidden';
}

function closeModal(modalId) {
    document.getElementById(modalId).classList.remove('active');
    document.body.style.overflow = '';
}

function logout() {
    if (confirm('Voulez-vous vous déconnecter?')) {
        sessionStorage.clear();
        window.location.href = '/login';
    }
}

window.onclick = function(event) {
    ['add-product-modal', 'add-assignment-modal', 'add-ticket-modal', 'close-ticket-modal', 'user-detail-modal'].forEach(id => {
        if (event.target === document.getElementById(id)) closeModal(id);
    });
};

document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
        ['add-product-modal', 'add-assignment-modal', 'add-ticket-modal', 'close-ticket-modal', 'user-detail-modal'].forEach(id => {
            closeModal(id);
        });
    }
});