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
    applyRoleBasedView();
    loadDashboard();
    loadProducts();
    loadUsers();
    loadCurrentAssignments();
    loadTickets();
    loadProductSelectors();
    loadUserSelectors();
    
    document.getElementById('assignment-form').addEventListener('submit', handleAssignment);
    const ticketForm = document.getElementById('ticket-form');
    if (ticketForm) ticketForm.addEventListener('submit', handleCreateTicket);
}

function updateUserUI() {
    if (!currentUser) return;
    document.getElementById('userName').textContent = currentUser.fullName;
    document.getElementById('userRole').textContent = currentUser.role;
    document.getElementById('userAvatar').textContent = currentUser.fullName.charAt(0).toUpperCase();
}

function applyPermissions() {
    const isAdmin = currentUser.role === 'ADMIN';
    const isPersonnel = currentUser.role === 'PERSONNEL';

    document.querySelectorAll('.btn-primary').forEach(btn => {
        if (btn.id === 'create-ticket-btn') return;
        if (btn.closest('.personnel-only')) return;
        btn.style.display = isAdmin ? '' : 'none';
    });

    const addProductBtn = document.getElementById('addProductBtn');
    if (addProductBtn) addProductBtn.style.display = isAdmin ? 'inline-flex' : 'none';
}

function applyRoleBasedView() {
    const role = currentUser.role;
    const isPersonnel = role === 'PERSONNEL';
    const isTechnician = role === 'TECHNICIEN';
    const isAdmin = role === 'ADMIN';

    // Show/hide ticket creation section for personnel and admin
    const createSection = document.getElementById('ticket-create-section');
    if (createSection) createSection.style.display = (isPersonnel || isAdmin) ? 'block' : 'none';

    // Personnel: hide nav items they shouldn't see
    document.querySelectorAll('.nav-menu li').forEach(li => {
        const link = li.querySelector('a');
        if (!link) return;
        const tab = link.getAttribute('data-tab');
        if (isPersonnel) {
            if (tab === 'products' || tab === 'users' || tab === 'assignments' || tab === 'dashboard') {
                li.style.display = 'none';
            }
        } else if (isTechnician) {
            if (tab === 'products' || tab === 'users' || tab === 'assignments' || tab === 'dashboard') {
                li.style.display = 'none';
            }
        }
    });

    // Show add buttons for admin only
    document.querySelectorAll('.btn-primary').forEach(btn => {
        if (btn.classList.contains('personnel-only')) return;
        if (!isAdmin) btn.style.display = 'none';
    });
}

function showTab(tabName, el) {
    document.querySelectorAll('.tab-content').forEach(tab => tab.classList.remove('active'));
    document.querySelectorAll('.nav-menu a').forEach(link => link.classList.remove('active'));
    
    document.getElementById(tabName + '-tab').classList.add('active');
    if (el) el.classList.add('active');
    
    if (tabName === 'products') loadProducts();
    else if (tabName === 'users') loadUsers();
    else if (tabName === 'assignments') loadCurrentAssignments();
    else if (tabName === 'interventions') loadTickets();
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
        fetch(API_BASE + '/tickets').then(r => r.json()),
        fetch(API_BASE + '/assignments/current').then(r => r.json())
    ]).then(([products, tickets, assignments]) => {
        document.getElementById('totalProducts').textContent = products.length;
        document.getElementById('availableProducts').textContent = products.filter(p => (p.status || '').toUpperCase() === 'AVAILABLE').length;
        document.getElementById('assignedProducts').textContent = products.filter(p => (p.status || '').toUpperCase() === 'ASSIGNED').length;
        document.getElementById('openTickets').textContent = tickets.length;
        
        const tbody = document.getElementById('recent-assignments');
        if (!assignments || assignments.length === 0) {
            tbody.innerHTML = '<tr><td colspan="3"><div class="empty-state"><p>Aucune affectation</p></div></td></tr>';
        } else {
            tbody.innerHTML = assignments.slice(0, 5).map(a => {
                const aff = a.affectataire || {};
                const name = aff.fullName || aff.nom || aff.prenom || '-';
                const equip = a.equipment || {};
                const date = a.startDate || '-';
                return '<tr><td><strong>' + name + '</strong></td><td>' + (equip.model || '-') + '</td><td>' + date + '</td></tr>';
            }).join('');
        }
    }).catch(err => console.error('Dashboard load error:', err));
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
    tbody.innerHTML = products.map(p => {
        const aff = p.currentAffectataire || {};
        const affName = aff.fullName || aff.prenom || aff.nom || '<span style="color:#999">Non affecté</span>';
        const displayStatus = (p.status || '').toUpperCase() === 'ASSIGNED' ? 'Affecté' : p.status;
        const isAssigned = (p.status || '').toUpperCase() === 'ASSIGNED';
        const pdfBtn = isAssigned ? '<button class="btn btn-primary btn-sm" onclick="generateEquipmentPdf(' + p.id + ')">PDF</button>' : '<button class="btn btn-secondary btn-sm" disabled style="opacity:0.4">PDF</button>';
        return '<tr><td>#' + p.id + '</td><td>' + (p.name || '-') + '</td><td>' + (p.model || '-') + '</td><td><code>' + (p.serialNumber || '-') + '</code></td><td>' + affName + '</td><td><span class="status-badge status-' + p.status + '">' + displayStatus + '</span></td><td><button class="btn btn-secondary btn-sm" onclick="viewProductDetails(' + p.id + ')">Détails</button> ' + pdfBtn + '</td></tr>';
    }).join('');
}

function filterProducts() {
    const search = document.getElementById('productSearch').value.toLowerCase();
    const filtered = window.allProducts.filter(p => p.model?.toLowerCase().includes(search) || p.serialNumber?.toLowerCase().includes(search));
    renderProductsTable(filtered);
}

function loadUsers() {
    fetch(API_BASE + '/affectataires').then(r => r.json()).then(affectataires => {
        const tbody = document.getElementById('users-list');
        if (affectataires.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7"><div class="empty-state"><p>Aucun affectataire</p></div></td></tr>';
            return;
        }
        tbody.innerHTML = affectataires.map(u => {
            const fullName = u.nom || u.username;
            return '<tr><td>#' + u.id + '</td><td><strong>' + fullName + '</strong></td><td>' + (u.email || '-') + '</td><td>' + (u.department || '-') + '</td><td>' + (u.fonction || '-') + '</td><td><span class="status-badge status-AFFECTATAIRE">Affectataire</span></td><td><button class="btn btn-secondary btn-sm" onclick="viewUserDetail(' + u.id + ')">Détails</button> <button class="btn btn-primary btn-sm" onclick="generateUserFiche(' + u.id + ')">PDF</button></td></tr>';
        }).join('');
    });
}

function loadCurrentAssignments() {
    fetch(API_BASE + '/assignments/history').then(r => r.json()).then(assignments => {
        const tbody = document.getElementById('current-assignments-list');
        if (!assignments || assignments.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5"><div class="empty-state"><p>Aucune affectation</p></div></td></tr>';
            return;
        }
        tbody.innerHTML = assignments.map(a => {
            const aff = a.affectataire || {};
            const name = aff.fullName || aff.nom || aff.prenom || '-';
            const equip = a.equipment || {};
            const startDate = a.startDate || '-';
            return '<tr><td><strong>' + name + '</strong></td><td>' + (equip.model || '-') + '</td><td>' + startDate + '</td><td><span class="status-badge status-ACTIVE">Affecté</span></td><td><button class="btn btn-primary btn-sm" onclick="generateEquipmentPdf(' + (equip.id || 'null') + ')">PDF Équipement</button></td></tr>';
        }).join('');
    }).catch(err => console.error('Assignments load error:', err));
}

function loadTickets() {
    const role = currentUser.role;
    const url = role === 'PERSONNEL'
        ? API_BASE + '/tickets/my?userId=' + currentUser.id
        : API_BASE + '/tickets';
    
    fetch(url).then(r => r.json()).then(tickets => {
        const tbody = document.getElementById('tickets-list');
        if (!tickets || tickets.length === 0) {
            tbody.innerHTML = '<tr><td colspan="8"><div class="empty-state"><p>Aucun ticket</p></div></td></tr>';
            return;
        }
        tbody.innerHTML = tickets.map(t => {
            const statusLabels = {'OUVERT': 'Ouvert', 'EN_COURS': 'En cours', 'CLOTURE': 'Cloturé'};
            const priorityLabels = {'LOW': 'Basse', 'MEDIUM': 'Moyenne', 'HIGH': 'Haute', 'URGENT': 'Urgente'};
            const label = statusLabels[t.status] || t.status;
            const pLabel = priorityLabels[t.priority] || t.priority;
            const equipStr = (t.equipmentType || '') + (t.equipmentName ? ' - ' + t.equipmentName : '');
            
            let actions = '';
            if (role === 'ADMIN' || role === 'TECHNICIEN') {
                if (t.status === 'OUVERT') {
                    actions += '<button class="btn btn-warning btn-sm" onclick="updateTicketStatus(' + t.id + ', \'EN_COURS\')">Prendre en charge</button> ';
                }
                if (t.status === 'EN_COURS') {
                    actions += '<button class="btn btn-success btn-sm" onclick="updateTicketStatus(' + t.id + ', \'CLOTURE\')">Fermer</button> ';
                }
            }
            return '<tr><td>#' + t.id + '</td><td>' + t.title + '</td><td>' + (equipStr || '-') + '</td><td><span class="priority-' + t.priority + '">' + pLabel + '</span></td><td><span class="status-badge status-' + t.status + '">' + label + '</span></td><td>' + (t.requesterName || '-') + '</td><td>' + (t.createdAt || '-') + '</td><td>' + actions + '</td></tr>';
        }).join('');
    });
}

function updateTicketStatus(ticketId, newStatus) {
    const labels = {'EN_COURS': 'prendre en charge', 'CLOTURE': 'fermer'};
    if (!confirm('Voulez-vous ' + (labels[newStatus] || 'changer le statut') + ' ce ticket ?')) return;
    
    fetch(API_BASE + '/tickets/' + ticketId + '/status', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status: newStatus })
    }).then(r => {
        if (!r.ok) throw new Error('Erreur');
        return r.json();
    }).then(() => {
        showToast('Succès', 'Statut du ticket mis à jour!', 'success');
        loadTickets();
        loadDashboard();
    }).catch(() => showToast('Erreur', 'Erreur lors de la mise à jour', 'error'));
}

function loadProductSelectors() {
    fetch(API_BASE + '/equipment').then(r => r.json()).then(products => {
        window.allProductsData = products;
        const assignSelect = document.getElementById('assign-product-id');
        if (assignSelect) {
            assignSelect.innerHTML = '<option value="">Sélectionner</option>';
            products.forEach(p => {
                const isAffected = p.status && p.status.toUpperCase() === 'ASSIGNED';
                if (!isAffected) {
                    const label = (p.name || p.model) + ' - ' + p.serialNumber;
                    assignSelect.innerHTML += '<option value="' + p.id + '">' + label + '</option>';
                }
            });
        }
    });
}

function onEquipmentSelect() {
    const id = document.getElementById('assign-product-id').value;
    const details = document.getElementById('assign-equipment-details');
    if (!id || !window.allProductsData) { details.style.display = 'none'; return; }
    const eq = window.allProductsData.find(p => p.id == id);
    if (!eq) { details.style.display = 'none'; return; }
    document.getElementById('assign-eq-name').textContent = eq.name || '-';
    document.getElementById('assign-eq-brand').textContent = eq.brand || '-';
    document.getElementById('assign-eq-model').textContent = eq.model || '-';
    document.getElementById('assign-eq-serial').textContent = eq.serialNumber || '-';
    details.style.display = 'block';
}

function onAffectataireSelect() {
    const id = document.getElementById('assign-user-id').value;
    const details = document.getElementById('assign-affectataire-details');
    if (!id || !window.allAffectatairesData) { details.style.display = 'none'; return; }
    const aff = window.allAffectatairesData.find(a => a.id == id);
    if (!aff) { details.style.display = 'none'; return; }
    document.getElementById('assign-aff-name').textContent = aff.nom || aff.username || '-';
    document.getElementById('assign-aff-department').textContent = aff.department || '-';
    document.getElementById('assign-aff-fonction').textContent = aff.fonction || '-';
    details.style.display = 'block';
}

function showAddAssignmentModal() {
    const today = new Date().toISOString().split('T')[0];
    document.getElementById('assign-date').value = today;
    openModal('add-assignment-modal');
    // refresh selectors
    loadProductSelectors();
    loadUserSelectors();
}

function loadUserSelectors() {
    fetch(API_BASE + '/affectataires').then(r => r.json()).then(affectataires => {
        window.allAffectatairesData = affectataires;
        const assignUserSelect = document.getElementById('assign-user-id');
        
        if (assignUserSelect) {
            assignUserSelect.innerHTML = '<option value="">Sélectionner</option>';
            affectataires.forEach(a => { 
                const name = a.nom || a.username;
                assignUserSelect.innerHTML += '<option value="' + a.id + '">' + name + ' (' + (a.department || 'N/A') + ')</option>'; 
            });
        }
    });
}

function handleAssignment(e) {
    if (e) e.preventDefault();
    const productId = document.getElementById('assign-product-id').value;
    const userId = document.getElementById('assign-user-id').value;
    const notes = document.getElementById('assign-notes').value;
    const directionOrigine = document.getElementById('assign-dir-origine').value;
    const directionDestination = document.getElementById('assign-dir-destination').value;
    const dateVal = document.getElementById('assign-date').value;
    
    if (!productId || !userId) {
        showToast('Erreur', 'Veuillez sélectionner un équipement et un affectataire', 'error');
        return;
    }
    
    const body = {
        productId: parseInt(productId),
        userId: parseInt(userId),
        assignedBy: currentUser.id,
        notes,
        directionOrigine,
        directionDestination
    };
    
    if (dateVal) {
        body.startDate = dateVal + 'T00:00:00';
    }
    
    const btn = document.getElementById('assign-submit-btn');
    btn.disabled = true;
    btn.textContent = 'Affectation en cours...';
    
    fetch(API_BASE + '/assignments/assign', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    }).then(r => {
        if (!r.ok) throw new Error('HTTP ' + r.status);
        return r.json();
    }).then(data => {
        if (!data || !data.id) throw new Error('Réponse vide');
        showToast('Succès', 'Affectation réussie!', 'success');
        document.getElementById('assignment-form').reset();
        document.getElementById('assign-equipment-details').style.display = 'none';
        document.getElementById('assign-affectataire-details').style.display = 'none';
        closeModal('add-assignment-modal');
        loadProducts();
        loadCurrentAssignments();
        loadProductSelectors();
        loadDashboard();
        // Auto-generate PDF
        window.open(API_BASE + '/pdf/fiche-affectation/user/' + userId, '_blank');
    }).catch(err => {
        showToast('Erreur', "Erreur lors de l'affectation: " + err.message, 'error');
    }).finally(() => {
        btn.disabled = false;
        btn.textContent = 'Affecter';
    });
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

function handleCreateTicket(e) {
    if (e) e.preventDefault();
    const title = document.getElementById('ticket-title').value;
    const equipmentType = document.getElementById('ticket-eq-type').value;
    const equipmentName = document.getElementById('ticket-eq-name').value;
    const priority = document.getElementById('ticket-priority').value;
    const interventionType = document.getElementById('ticket-intervention-type').value;
    const description = document.getElementById('ticket-description').value;
    
    if (!title || !equipmentType || !interventionType || !description) {
        showToast('Erreur', 'Veuillez remplir tous les champs obligatoires', 'error');
        return;
    }
    
    const ticket = {
        title: title,
        equipmentType: equipmentType,
        equipmentName: equipmentName,
        priority: priority,
        interventionType: interventionType,
        description: description,
        requesterId: currentUser.id,
        requesterName: currentUser.fullName,
        requesterEmail: currentUser.email || ''
    };
    
    const btn = document.getElementById('create-ticket-btn');
    btn.disabled = true;
    btn.textContent = 'Création en cours...';
    
    fetch(API_BASE + '/tickets', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(ticket)
    }).then(r => {
        if (!r.ok) throw new Error('HTTP ' + r.status);
        return r.json();
    }).then(() => {
        showToast('Succès', 'Ticket créé! Les techniciens ont été notifiés.', 'success');
        document.getElementById('ticket-form').reset();
        document.getElementById('ticket-priority').value = 'MEDIUM';
        loadTickets();
        loadDashboard();
    }).catch(err => {
        showToast('Erreur', 'Erreur: ' + err.message, 'error');
    }).finally(() => {
        btn.disabled = false;
        btn.textContent = 'Créer le ticket';
    });
}

function showAddProductModal() { openModal('add-product-modal'); }
function showAddUserModal() { openModal('add-user-modal'); }

function addProduct() {
    const product = {
        name: document.getElementById('product-name').value,
        model: document.getElementById('product-model').value,
        serialNumber: document.getElementById('product-serial').value,
        brand: document.getElementById('product-brand').value,
        category: document.getElementById('product-category').value,
        createdBy: currentUser.id
    };
    
    if (!product.name || !product.model || !product.serialNumber) {
        showToast('Erreur', 'Nom, modèle et numéro de série sont obligatoires', 'error');
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

function addUser() {
    const nom = document.getElementById('user-name').value;
    const affectataire = {
        username: nom.toLowerCase().replace(/\s+/g, '.'),
        nom: nom,
        email: document.getElementById('user-email').value,
        department: document.getElementById('user-department').value,
        fonction: document.getElementById('user-fonction').value
    };
    
    if (!nom) {
        showToast('Erreur', 'Le nom est obligatoire', 'error');
        return;
    }
    
    fetch(API_BASE + '/affectataires', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(affectataire)
    }).then(r => r.json()).then(() => {
        showToast('Succès', 'Affectataire ajouté avec succès!', 'success');
        closeModal('add-user-modal');
        document.getElementById('add-user-form').reset();
        loadUsers();
        loadUserSelectors();
    }).catch(() => showToast('Erreur', "Erreur lors de l'ajout de l'affectataire", 'error'));
}

function viewProductHistory(productId) {
    showToast('Info', 'Fonctionnalité en cours de développement', 'info');
}

function viewUserDetail(userId) {
    fetch(API_BASE + '/affectataires/' + userId).then(r => r.json()).then(user => {
        fetch(API_BASE + '/affectataires/' + userId + '/assignments').then(r => r.json()).then(assignments => {
            const currentAssignments = assignments.filter(a => !a.endDate);
            document.getElementById('user-detail-title').textContent = user.nom || user.username || '-';
            document.getElementById('user-detail-content').innerHTML = '<div class="form-row"><div class="form-group"><label>Email</label><p>' + (user.email || '-') + '</p></div><div class="form-group"><label>CIN</label><p>' + (user.cin || '-') + '</p></div></div><div class="form-row"><div class="form-group"><label>Département</label><p>' + (user.department || '-') + '</p></div><div class="form-group"><label>Fonction</label><p>' + (user.fonction || '-') + '</p></div></div><h4 style="margin:24px 0 16px">Équipements affectés (' + currentAssignments.length + ')</h4>' + (currentAssignments.length ? '<table class="data-table"><thead><tr><th>Modèle</th><th>N° Série</th><th>Date</th></tr></thead><tbody>' + currentAssignments.map(a => '<tr><td>' + (a.equipment?.model || '-') + '</td><td><code>' + (a.equipment?.serialNumber || '-') + '</code></td><td>' + formatDateSafe(a.startDate) + '</td></tr>').join('') + '</tbody></table>' : '<p>Aucun équipement affecté</p>');
            openModal('user-detail-modal');
        });
    });
}

function generateUserFiche(userId) {
    fetch(API_BASE + '/affectataires/' + userId + '/assignments').then(r => r.json()).then(assignments => {
        const active = assignments.filter(a => !a.endDate);
        if (active.length === 0) {
            showToast('Attention', 'Cet affectataire n\'a aucun équipement affecté', 'error');
            return;
        }
        window.open(API_BASE + '/pdf/fiche-affectation/user/' + userId, '_blank');
    }).catch(() => {
        window.open(API_BASE + '/pdf/fiche-affectation/user/' + userId, '_blank');
    });
}

function generateEquipmentPdf(equipmentId) {
    window.open(API_BASE + '/pdf/fiche-affectation/equipment/' + equipmentId, '_blank');
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

function doLogout() {
    if (confirm('Voulez-vous vous déconnecter?')) {
        sessionStorage.clear();
        window.location.href = '/login';
    }
}

window.onclick = function(event) {
    ['add-product-modal', 'add-assignment-modal', 'user-detail-modal'].forEach(id => {
        if (event.target === document.getElementById(id)) closeModal(id);
    });
};

document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
        ['add-product-modal', 'add-assignment-modal', 'user-detail-modal'].forEach(id => {
            closeModal(id);
        });
    }
});