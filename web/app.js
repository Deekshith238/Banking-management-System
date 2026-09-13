/**
 * NovaBank Web Dashboard Application Logic
 * Pure Vanilla JavaScript (ES6+ SPA without React) - Indian Rupee (INR ₹) Locale
 */

const API_BASE = '/api';

// Application State
let state = {
  dbConnected: false,
  stats: {},
  customers: [],
  accounts: [],
  transactions: []
};

// Chart instances
let balanceChart = null;
let txChart = null;

// Helper to format currency in Indian Rupees (₹) with Indian numbering format
function formatINR(val) {
  const num = parseFloat(val || 0);
  return '₹' + num.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

// Initialize Application
document.addEventListener('DOMContentLoaded', () => {
  initIcons();
  initClock();
  initEventListeners();
  loadAllData();

  // Periodic polling every 10 seconds
  setInterval(loadAllData, 10000);
});

function initIcons() {
  if (window.lucide) {
    lucide.createIcons();
  }
}

function initClock() {
  const clockEl = document.getElementById('clock-widget');
  function updateClock() {
    const now = new Date();
    clockEl.textContent = now.toLocaleTimeString('en-IN');
  }
  updateClock();
  setInterval(updateClock, 1000);
}

function initEventListeners() {
  // Navigation Tabs
  document.querySelectorAll('.nav-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const tabName = btn.getAttribute('data-tab');
      switchTab(tabName);
    });
  });

  // Operations Sub-Tabs
  document.querySelectorAll('.ops-tab-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.ops-tab-btn').forEach(b => b.classList.remove('active'));
      document.querySelectorAll('.op-form-pane').forEach(p => p.classList.remove('active'));
      
      btn.classList.add('active');
      const op = btn.getAttribute('data-op');
      const pane = document.getElementById(`op-form-${op}`);
      if (pane) pane.classList.add('active');
    });
  });

  // Refresh Button
  document.getElementById('refresh-btn').addEventListener('click', async () => {
    const icon = document.querySelector('#refresh-btn i');
    if (icon) icon.classList.add('spin-anim');
    await loadAllData();
    showToast('Dashboard data updated', 'success');
    if (icon) icon.classList.remove('spin-anim');
  });

  // Quick Action Button
  document.getElementById('quick-action-btn').addEventListener('click', () => {
    switchTab('operations');
  });

  // Quick Transfer Form
  document.getElementById('quick-transfer-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const fromAcc = document.getElementById('qt-from-account').value;
    const toAcc = document.getElementById('qt-to-account').value.trim();
    const amount = parseFloat(document.getElementById('qt-amount').value);

    await executeTransfer(fromAcc, toAcc, amount, "Quick Transfer (UPI/IMPS)");
    document.getElementById('quick-transfer-form').reset();
  });

  // Deposit Form
  document.getElementById('form-deposit').addEventListener('submit', async (e) => {
    e.preventDefault();
    const accNo = document.getElementById('dep-account').value;
    const amount = parseFloat(document.getElementById('dep-amount').value);
    const remarks = document.getElementById('dep-remarks').value;

    await executeDeposit(accNo, amount, remarks);
    document.getElementById('form-deposit').reset();
  });

  // Withdraw Form
  document.getElementById('form-withdraw').addEventListener('submit', async (e) => {
    e.preventDefault();
    const accNo = document.getElementById('wd-account').value;
    const amount = parseFloat(document.getElementById('wd-amount').value);
    const remarks = document.getElementById('wd-remarks').value;

    await executeWithdraw(accNo, amount, remarks);
    document.getElementById('form-withdraw').reset();
  });

  // Transfer Form
  document.getElementById('form-transfer').addEventListener('submit', async (e) => {
    e.preventDefault();
    const fromAcc = document.getElementById('tf-from-account').value;
    const toAcc = document.getElementById('tf-to-account').value;
    const amount = parseFloat(document.getElementById('tf-amount').value);
    const remarks = document.getElementById('tf-remarks').value;

    await executeTransfer(fromAcc, toAcc, amount, remarks);
    document.getElementById('form-transfer').reset();
  });

  // Add Customer Form
  document.getElementById('form-add-customer').addEventListener('submit', async (e) => {
    e.preventDefault();
    const name = document.getElementById('cust-name').value;
    const email = document.getElementById('cust-email').value;
    const phone = document.getElementById('cust-phone').value;
    const address = document.getElementById('cust-address').value;

    try {
      const res = await fetch(`${API_BASE}/customers`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name, email, phone, address })
      });
      const data = await res.json();
      if (res.ok) {
        showToast(`Customer registered successfully! (ID: #${data.customerId})`, 'success');
        closeModal('modal-add-customer');
        document.getElementById('form-add-customer').reset();
        await loadAllData();
      } else {
        showToast(data.error || 'Failed to register customer', 'error');
      }
    } catch (err) {
      showToast(err.message, 'error');
    }
  });

  // Open Account Form
  document.getElementById('form-open-account').addEventListener('submit', async (e) => {
    e.preventDefault();
    const customerId = document.getElementById('acc-customer-select').value;
    const accountType = document.getElementById('acc-type-select').value;
    const initialDeposit = document.getElementById('acc-deposit-input').value;

    try {
      const res = await fetch(`${API_BASE}/accounts`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ customerId, accountType, initialDeposit })
      });
      const data = await res.json();
      if (res.ok) {
        showToast(`Account ${data.accountNumber} opened successfully!`, 'success');
        closeModal('modal-open-account');
        document.getElementById('form-open-account').reset();
        await loadAllData();
      } else {
        showToast(data.error || 'Failed to open account', 'error');
      }
    } catch (err) {
      showToast(err.message, 'error');
    }
  });

  // Statement Filter Form
  document.getElementById('statement-filter-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const accNo = document.getElementById('stmt-account-select').value;
    await fetchTransactions(accNo);
  });

  // CSV Export Button
  const exportCsvBtn = document.getElementById('export-csv-btn');
  if (exportCsvBtn) {
    exportCsvBtn.addEventListener('click', () => {
      exportStatementToCSV();
    });
  }

  // Print Statement Button
  const printStmtBtn = document.getElementById('print-stmt-btn');
  if (printStmtBtn) {
    printStmtBtn.addEventListener('click', () => {
      window.print();
    });
  }

  // Customer Search Filter
  document.getElementById('customer-search-input').addEventListener('input', (e) => {
    const q = e.target.value.toLowerCase().trim();
    renderCustomers(state.customers.filter(c => 
      c.name.toLowerCase().includes(q) || 
      c.email.toLowerCase().includes(q) || 
      (c.phone && c.phone.toLowerCase().includes(q)) ||
      c.customerId.toString().includes(q)
    ));
  });

  // Account Search Filter
  document.getElementById('account-search-input').addEventListener('input', (e) => {
    const q = e.target.value.toLowerCase().trim();
    renderAccounts(state.accounts.filter(a => 
      a.accountNumber.toLowerCase().includes(q) || 
      a.accountType.toLowerCase().includes(q) ||
      a.customerId.toString().includes(q)
    ));
  });

  // DB Config Form
  document.getElementById('db-config-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const url = document.getElementById('db-url-input').value;
    const user = document.getElementById('db-user-input').value;
    const password = document.getElementById('db-pass-input').value;

    try {
      const res = await fetch(`${API_BASE}/db-status`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ url, user, password })
      });
      const data = await res.json();
      updateDBStatusUI(data.connected);
      if (data.connected) {
        showToast('Connected to MySQL Database!', 'success');
      } else {
        showToast('Could not connect with provided credentials. Running in demo mode.', 'error');
      }
      await loadAllData();
    } catch (err) {
      showToast('Error updating database credentials', 'error');
    }
  });

  document.getElementById('test-db-btn').addEventListener('click', async () => {
    await checkDBStatus();
    showToast(state.dbConnected ? 'MySQL Connection Healthy' : 'Database Offline (Demo Mode Active)', state.dbConnected ? 'success' : 'error');
  });

  document.getElementById('acc-type-select').addEventListener('change', (e) => {
    const hint = document.getElementById('deposit-hint');
    if (e.target.value === 'SAVINGS') {
      hint.textContent = 'Savings Account requires at least ₹500 initial deposit.';
    } else {
      hint.textContent = 'Current Account supports up to ₹1,000 overdraft limit.';
    }
  });
}

// Data Fetching
async function loadAllData() {
  await checkDBStatus();
  await fetchStats();
  await fetchCustomers();
  await fetchAccounts();
  await fetchTransactions();
}

async function checkDBStatus() {
  try {
    const res = await fetch(`${API_BASE}/db-status`);
    const data = await res.json();
    state.dbConnected = data.connected;
    updateDBStatusUI(data.connected);
  } catch (err) {
    state.dbConnected = false;
    updateDBStatusUI(false);
  }
}

function updateDBStatusUI(connected) {
  const textEl = document.getElementById('db-status-text');
  const titleEl = document.getElementById('diag-status-title');
  const descEl = document.getElementById('diag-status-desc');
  const iconCircle = document.getElementById('diag-icon-circle');

  if (connected) {
    textEl.textContent = 'Online - MySQL';
    textEl.style.color = '#10b981';
    titleEl.textContent = 'MySQL Connection Healthy';
    descEl.textContent = 'Connected to database at jdbc:mysql://localhost:3306/banking_db';
    if (iconCircle) iconCircle.className = 'status-icon-circle green';
  } else {
    textEl.textContent = 'Demo Mode (In-Memory)';
    textEl.style.color = '#f59e0b';
    titleEl.textContent = 'Demo Mode (In-Memory Repository)';
    descEl.textContent = 'MySQL Server offline. Operating seamlessly with embedded in-memory repository.';
    if (iconCircle) iconCircle.className = 'status-icon-circle yellow';
  }
}

async function fetchStats() {
  try {
    const res = await fetch(`${API_BASE}/stats`);
    const data = await res.json();
    state.stats = data;

    document.getElementById('stat-balance').textContent = formatINR(data.totalBalance);
    document.getElementById('stat-customers').textContent = data.customerCount || 0;
    document.getElementById('stat-accounts').textContent = data.accountCount || 0;
    document.getElementById('stat-txs').textContent = data.txCount || 0;
  } catch (err) {
    console.error('Failed to fetch stats', err);
  }
}

async function fetchCustomers() {
  try {
    const res = await fetch(`${API_BASE}/customers`);
    const data = await res.json();
    state.customers = data;
    renderCustomers(data);
    populateCustomerDropdowns(data);
  } catch (err) {
    console.error('Failed to fetch customers', err);
  }
}

async function fetchAccounts() {
  try {
    const res = await fetch(`${API_BASE}/accounts`);
    const data = await res.json();
    state.accounts = data;
    renderAccounts(data);
    populateAccountDropdowns(data);
    renderBalanceBreakdownChart(data);
  } catch (err) {
    console.error('Failed to fetch accounts', err);
  }
}

async function fetchTransactions(accountNumber = '') {
  try {
    const url = accountNumber ? `${API_BASE}/transactions?accountNumber=${encodeURIComponent(accountNumber)}` : `${API_BASE}/transactions`;
    const res = await fetch(url);
    const data = await res.json();
    state.transactions = data;
    renderTransactions(data);
    renderRecentTransactions(data.slice(-5).reverse());
    renderTransactionTrendChart(data);
  } catch (err) {
    console.error('Failed to fetch transactions', err);
  }
}

// Chart Renderers using Chart.js
function renderBalanceBreakdownChart(accounts) {
  if (!window.Chart) return;
  const ctx = document.getElementById('chart-balance-types');
  if (!ctx) return;

  let savingsTotal = 0;
  let currentTotal = 0;

  accounts.forEach(a => {
    if (a.accountType === 'SAVINGS') savingsTotal += a.balance;
    else currentTotal += a.balance;
  });

  if (balanceChart) {
    balanceChart.destroy();
  }

  balanceChart = new Chart(ctx, {
    type: 'doughnut',
    data: {
      labels: ['Savings Accounts', 'Current Accounts'],
      datasets: [{
        data: [savingsTotal, currentTotal],
        backgroundColor: ['#06b6d4', '#6366f1'],
        borderWidth: 0,
        hoverOffset: 6
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: 'bottom',
          labels: { color: '#94a3b8', font: { family: 'Plus Jakarta Sans', size: 12 } }
        },
        tooltip: {
          callbacks: {
            label: function(context) {
              return ` ${context.label}: ${formatINR(context.raw)}`;
            }
          }
        }
      },
      cutout: '70%'
    }
  });
}

function renderTransactionTrendChart(transactions) {
  if (!window.Chart) return;
  const ctx = document.getElementById('chart-tx-activity');
  if (!ctx) return;

  let depositCount = 0;
  let withdrawCount = 0;
  let transferCount = 0;

  transactions.forEach(t => {
    if (t.transactionType === 'DEPOSIT') depositCount++;
    else if (t.transactionType === 'WITHDRAWAL') withdrawCount++;
    else transferCount++;
  });

  if (txChart) {
    txChart.destroy();
  }

  txChart = new Chart(ctx, {
    type: 'bar',
    data: {
      labels: ['Deposits', 'Withdrawals', 'Transfers'],
      datasets: [{
        label: 'Count',
        data: [depositCount, withdrawCount, transferCount],
        backgroundColor: ['#10b981', '#f43f5e', '#6366f1'],
        borderRadius: 8
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      scales: {
        x: { ticks: { color: '#94a3b8' }, grid: { display: false } },
        y: { ticks: { color: '#94a3b8', precision: 0 }, grid: { color: 'rgba(255,255,255,0.05)' } }
      },
      plugins: {
        legend: { display: false }
      }
    }
  });
}

// Render Functions
function renderCustomers(customers) {
  const container = document.getElementById('customers-cards-grid');
  if (!customers || customers.length === 0) {
    container.innerHTML = '<div class="span-2 text-center py-5">No customer profiles found.</div>';
    return;
  }

  container.innerHTML = customers.map(c => `
    <div class="cust-card">
      <div class="cust-header">
        <div class="cust-avatar">${c.name ? c.name.charAt(0).toUpperCase() : 'C'}</div>
        <div class="cust-info">
          <h4>${escapeHtml(c.name)}</h4>
          <span>Customer ID: #${c.customerId}</span>
        </div>
      </div>
      <div class="cust-details">
        <p><i data-lucide="mail"></i> ${escapeHtml(c.email)}</p>
        <p><i data-lucide="phone"></i> ${escapeHtml(c.phone || 'N/A')}</p>
        <p><i data-lucide="map-pin"></i> ${escapeHtml(c.address || 'No address provided')}</p>
      </div>
    </div>
  `).join('');

  initIcons();
}

function renderAccounts(accounts) {
  const tbody = document.getElementById('accounts-table-body');
  if (!accounts || accounts.length === 0) {
    tbody.innerHTML = '<tr><td colspan="6" class="text-center py-4">No accounts found.</td></tr>';
    return;
  }

  tbody.innerHTML = accounts.map(a => `
    <tr>
      <td><strong>${escapeHtml(a.accountNumber)}</strong></td>
      <td>#${a.customerId}</td>
      <td>
        <span class="badge ${a.accountType === 'SAVINGS' ? 'badge-savings' : 'badge-current'}">
          ${a.accountType}
        </span>
      </td>
      <td><strong class="text-green">${formatINR(a.balance)}</strong></td>
      <td>${a.createdAt ? new Date(a.createdAt).toLocaleDateString('en-IN') : 'N/A'}</td>
      <td>
        <button class="btn btn-secondary icon-btn" onclick="filterAccountStatement('${a.accountNumber}')" title="View Statement" aria-label="View Statement">
          <i data-lucide="file-text"></i>
        </button>
      </td>
    </tr>
  `).join('');

  initIcons();
}

function renderTransactions(transactions) {
  const tbody = document.getElementById('statement-tbody');
  if (!transactions || transactions.length === 0) {
    tbody.innerHTML = '<tr><td colspan="8" class="text-center py-4">No transaction logs available.</td></tr>';
    return;
  }

  tbody.innerHTML = transactions.map(t => {
    const isIncome = t.transactionType === 'DEPOSIT' || t.transactionType === 'TRANSFER_IN';
    return `
      <tr>
        <td>#${t.transactionId}</td>
        <td><strong>${escapeHtml(t.accountNumber)}</strong></td>
        <td><span class="badge ${isIncome ? 'badge-success' : 'badge-danger'}">${t.transactionType}</span></td>
        <td><strong class="${isIncome ? 'text-green' : 'text-rose'}">${isIncome ? '+' : '-'}${formatINR(t.amount)}</strong></td>
        <td>${formatINR(t.balanceAfter)}</td>
        <td>${t.targetAccountNumber ? escapeHtml(t.targetAccountNumber) : '-'}</td>
        <td>${escapeHtml(t.remarks || '')}</td>
        <td>${t.timestamp ? new Date(t.timestamp).toLocaleString('en-IN') : ''}</td>
      </tr>
    `;
  }).join('');
}

function renderRecentTransactions(txs) {
  const tbody = document.getElementById('recent-tx-tbody');
  if (!txs || txs.length === 0) {
    tbody.innerHTML = '<tr><td colspan="6" class="text-center py-4">No recent activity.</td></tr>';
    return;
  }

  tbody.innerHTML = txs.map(t => {
    const isIncome = t.transactionType === 'DEPOSIT' || t.transactionType === 'TRANSFER_IN';
    return `
      <tr>
        <td>#${t.transactionId}</td>
        <td><strong>${escapeHtml(t.accountNumber)}</strong></td>
        <td><span class="badge ${isIncome ? 'badge-success' : 'badge-danger'}">${t.transactionType}</span></td>
        <td><strong class="${isIncome ? 'text-green' : 'text-rose'}">${isIncome ? '+' : '-'}${formatINR(t.amount)}</strong></td>
        <td>${formatINR(t.balanceAfter)}</td>
        <td>${t.timestamp ? new Date(t.timestamp).toLocaleTimeString('en-IN') : ''}</td>
      </tr>
    `;
  }).join('');
}

// Dropdown Populators
function populateCustomerDropdowns(customers) {
  const select = document.getElementById('acc-customer-select');
  select.innerHTML = '<option value="">Select customer...</option>' + 
    customers.map(c => `<option value="${c.customerId}">#${c.customerId} - ${escapeHtml(c.name)} (${escapeHtml(c.email)})</option>`).join('');
}

function populateAccountDropdowns(accounts) {
  const dropdowns = document.querySelectorAll('.account-dropdown');
  const optionsHtml = accounts.map(a => `<option value="${a.accountNumber}">${a.accountNumber} - [${a.accountType}] (${formatINR(a.balance)})</option>`).join('');
  
  dropdowns.forEach(select => {
    const defaultText = select.id === 'stmt-account-select' ? '<option value="">-- All System Accounts --</option>' : '<option value="">Select account...</option>';
    select.innerHTML = defaultText + optionsHtml;
  });
}

// Operations execution
async function executeDeposit(accountNumber, amount, remarks) {
  try {
    const res = await fetch(`${API_BASE}/deposit`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ accountNumber, amount, remarks })
    });
    const data = await res.json();
    if (res.ok) {
      showToast(`Deposited ${formatINR(amount)} into ${accountNumber}`, 'success');
      showReceipt('DEPOSIT', accountNumber, amount, data.account ? data.account.balance : 0, null, remarks);
      await loadAllData();
    } else {
      showToast(data.error || 'Deposit failed', 'error');
    }
  } catch (err) {
    showToast(err.message, 'error');
  }
}

async function executeWithdraw(accountNumber, amount, remarks) {
  try {
    const res = await fetch(`${API_BASE}/withdraw`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ accountNumber, amount, remarks })
    });
    const data = await res.json();
    if (res.ok) {
      showToast(`Withdrew ${formatINR(amount)} from ${accountNumber}`, 'success');
      showReceipt('WITHDRAWAL', accountNumber, amount, data.account ? data.account.balance : 0, null, remarks);
      await loadAllData();
    } else {
      showToast(data.error || 'Withdrawal failed', 'error');
    }
  } catch (err) {
    showToast(err.message, 'error');
  }
}

async function executeTransfer(fromAccount, toAccount, amount, remarks) {
  try {
    const res = await fetch(`${API_BASE}/transfer`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ fromAccount, toAccount, amount, remarks })
    });
    const data = await res.json();
    if (res.ok) {
      showToast(`Transferred ${formatINR(amount)} from ${fromAccount} to ${toAccount}`, 'success');
      showReceipt('TRANSFER', fromAccount, amount, 0, toAccount, remarks);
      await loadAllData();
    } else {
      showToast(data.error || 'Transfer failed', 'error');
    }
  } catch (err) {
    showToast(err.message, 'error');
  }
}

// Receipt Modal
function showReceipt(type, accNo, amount, balance, targetAcc, remarks) {
  document.getElementById('rcpt-type').textContent = type;
  document.getElementById('rcpt-acc-no').textContent = accNo;
  document.getElementById('rcpt-amount').textContent = formatINR(amount);
  document.getElementById('rcpt-balance').textContent = formatINR(balance);
  
  const targetRow = document.getElementById('rcpt-target-row');
  if (targetAcc) {
    targetRow.style.display = 'flex';
    document.getElementById('rcpt-target-acc').textContent = targetAcc;
  } else {
    targetRow.style.display = 'none';
  }

  document.getElementById('rcpt-remarks').textContent = remarks || 'N/A';
  document.getElementById('rcpt-time').textContent = new Date().toLocaleString('en-IN');

  openModal('modal-receipt');
}

// Export CSV Feature
function exportStatementToCSV() {
  if (!state.transactions || state.transactions.length === 0) {
    showToast('No transactions to export', 'error');
    return;
  }

  const headers = ['Transaction ID', 'Account Number', 'Type', 'Amount (INR)', 'Balance After (INR)', 'Target Account', 'Remarks', 'Timestamp'];
  const rows = state.transactions.map(t => [
    t.transactionId,
    `"${t.accountNumber}"`,
    `"${t.transactionType}"`,
    t.amount,
    t.balanceAfter,
    `"${t.targetAccountNumber || ''}"`,
    `"${(t.remarks || '').replace(/"/g, '""')}"`,
    `"${t.timestamp || ''}"`
  ]);

  const csvContent = 'data:text/csv;charset=utf-8,' + [headers.join(','), ...rows.map(e => e.join(','))].join('\n');
  const encodedUri = encodeURI(csvContent);
  const link = document.createElement('a');
  link.setAttribute('href', encodedUri);
  link.setAttribute('download', `NovaBank_Statement_${new Date().toISOString().slice(0,10)}.csv`);
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  showToast('Statement exported to CSV successfully', 'success');
}

// Helper Utilities
function switchTab(tabName) {
  document.querySelectorAll('.nav-btn').forEach(b => b.classList.remove('active'));
  document.querySelectorAll('.tab-pane').forEach(p => p.classList.remove('active'));

  const btn = document.querySelector(`.nav-btn[data-tab="${tabName}"]`);
  const pane = document.getElementById(`tab-${tabName}`);

  if (btn) btn.classList.add('active');
  if (pane) pane.classList.add('active');

  const titles = {
    dashboard: 'Dashboard Overview',
    customers: 'Customer Directory',
    accounts: 'Bank Accounts',
    operations: 'Financial Operations Hub',
    statements: 'Account Statements & Audit Trail',
    diagnostics: 'Database & System Health'
  };

  const subtitles = {
    dashboard: 'Real-time financial activity, portfolio distribution, and management',
    customers: 'View registered customers and add new profile records',
    accounts: 'Manage active Savings & Current accounts and inspect balances',
    operations: 'Execute real-time deposits, withdrawals, and atomic fund transfers',
    statements: 'Audit log of transaction history with CSV export and printing support',
    diagnostics: 'MySQL connectivity health check and configuration controls'
  };

  document.getElementById('page-title').textContent = titles[tabName] || 'Dashboard';
  document.getElementById('page-subtitle').textContent = subtitles[tabName] || 'Real-time banking operations';
}

function filterAccountStatement(accNo) {
  switchTab('statements');
  document.getElementById('stmt-account-select').value = accNo;
  fetchTransactions(accNo);
}

function openModal(id) {
  const modal = document.getElementById(id);
  if (modal) modal.classList.add('active');
}

function closeModal(id) {
  const modal = document.getElementById(id);
  if (modal) modal.classList.remove('active');
}

function showToast(msg, type = 'info') {
  const container = document.getElementById('toast-container');
  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  toast.innerHTML = `<i data-lucide="${type === 'success' ? 'check-circle' : 'alert-circle'}"></i> <span>${escapeHtml(msg)}</span>`;
  container.appendChild(toast);
  initIcons();

  setTimeout(() => {
    toast.remove();
  }, 4000);
}

function escapeHtml(str) {
  if (!str) return '';
  return str.toString().replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;");
}
