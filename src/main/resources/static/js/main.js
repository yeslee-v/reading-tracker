const STATE_LABELS = {
  IN_PROGRESS: '읽는 중',
  COMPLETED: '완독',
  ARCHIVED: '보관',
};

const elements = {
  userInfo: document.getElementById('userInfo'),
  unauthenticated: document.getElementById('unauthenticated'),
  appContent: document.getElementById('appContent'),
  bookList: document.getElementById('bookList'),
  emptyState: document.getElementById('emptyState'),
  addBookBtn: document.getElementById('addBookBtn'),
  summaryInProgress: document.getElementById('summaryInProgress'),
  summaryCompleted: document.getElementById('summaryCompleted'),
  summaryArchived: document.getElementById('summaryArchived'),
  stateButtons: Array.from(
      document.querySelectorAll('[data-role="state-button"]')),
  addModal: document.getElementById('addBookModal'),
  closeAddModalBtn: document.getElementById('closeAddModalBtn'),
  editModal: document.getElementById('editBookModal'),
  closeEditModalBtn: document.getElementById('closeEditModalBtn'),
  searchInput: document.getElementById('searchInput'),
  searchBtn: document.getElementById('searchBtn'),
  searchResults: document.getElementById('searchResults'),
  selectedBookDetails: document.getElementById('selectedBookDetails'),
  totalPagesInput: document.getElementById('totalPagesInput'),
  submitAddBookBtn: document.getElementById('submitAddBookBtn'),
  editBookInfo: document.getElementById('editBookInfo'),
  editCurrentPageInput: document.getElementById('editCurrentPageInput'),
  editStateSelect: document.getElementById('editStateSelect'),
  submitEditBookBtn: document.getElementById('submitEditBookBtn'),
  toast: document.getElementById('toast'),
};

let currentState = 'IN_PROGRESS';
let currentUser = null;
let selectedBook = null;
let isLoadingBooks = false;
let searchResultsCache = [];
let editingBook = null;
let editInitialValues = null;
const bookCache = new Map();

init();

function init() {
  populateStateSelect(elements.editStateSelect);
  bindEvents();
  loadCurrentUser();
}

function bindEvents() {
  elements.stateButtons.forEach((button) => {
    button.addEventListener('click', () => {
      if (button.dataset.state === currentState) {
        return;
      }
      currentState = button.dataset.state ?? currentState;
      updateStateSelection();
      loadBooks();
    });
  });

  elements.addBookBtn.addEventListener('click', openAddBookModal);
  elements.closeAddModalBtn.addEventListener('click', closeAddBookModal);
  elements.addModal.addEventListener('click', (event) => {
    if (event.target === elements.addModal) {
      closeAddBookModal();
    }
  });
  elements.closeEditModalBtn.addEventListener('click', closeEditModal);
  elements.editModal.addEventListener('click', (event) => {
    if (event.target === elements.editModal) {
      closeEditModal();
    }
  });

  elements.searchBtn.addEventListener('click', handleSearch);
  elements.searchInput.addEventListener('keydown', (event) => {
    if (event.key === 'Enter') {
      event.preventDefault();
      handleSearch();
    }
  });

  elements.submitAddBookBtn.addEventListener('click', handleAddBookSubmit);
  elements.totalPagesInput.addEventListener('input',
      updateAddSubmitButtonState);
  elements.editCurrentPageInput.addEventListener('input',
      handleEditInputsChange);
  elements.editStateSelect.addEventListener('change', handleEditInputsChange);
  elements.submitEditBookBtn.addEventListener('click', handleEditSubmit);

  elements.searchResults.addEventListener('click', (event) => {
    const item = event.target.closest('.search-result-item');
    if (!item) {
      return;
    }
    const index = Number(item.dataset.index);
    const book = searchResultsCache[index];
    if (book) {
      selectSearchResult(book, item);
    }
  });

  elements.bookList.addEventListener('click', (event) => {
    const button = event.target.closest('button[data-action]');
    if (!button) {
      return;
    }

    const card = button.closest('.book-card');
    const bookId = Number(card?.dataset.id);
    if (!bookId) {
      return;
    }

    if (button.dataset.action === 'open-edit') {
      const book = bookCache.get(bookId);
      if (book) {
        openEditModal(book);
      }
    }
  });

  document.addEventListener('keydown', (event) => {
    if (event.key === 'Escape') {
      closeAddBookModal();
      closeEditModal();
    }
  });

  updateStateSelection();
}

function populateStateSelect(selectElement) {
  if (!selectElement) {
    return;
  }
  selectElement.innerHTML = Object.entries(STATE_LABELS)
  .map(([value, label]) => `<option value="${value}">${label}</option>`)
  .join('');
}

async function loadCurrentUser() {
  try {
    const response = await apiRequest('/api/auth/user');
    // TODO: /api/users/me를 호출하도록 변경해 DB 기준 최신 닉네임과 정보를 사용한다.
    if (!response || !response.authenticated) {
      showUnauthenticated();
      return;
    }

    currentUser = response;
    console.log(currentUser);
    renderUserPanel();
    showApp();
    await loadBooks();
  } catch (error) {
    console.error(error);
    showToast('로그인 정보를 불러오지 못했습니다.', 'error');
  }
}

function renderUserPanel() {
  const nickname = currentUser.nickname ?? '사용자';
  elements.userInfo.innerHTML = `<strong>${nickname}</strong>`;
}

function showUnauthenticated() {
  elements.unauthenticated.classList.remove('hidden');
  elements.appContent.classList.add('hidden');
}

function showApp() {
  elements.unauthenticated.classList.add('hidden');
  elements.appContent.classList.remove('hidden');
}

function updateStateSelection() {
  elements.stateButtons.forEach((btn) => {
    btn.classList.toggle('active', btn.dataset.state === currentState);
  });
}

async function loadBooks() {
  if (isLoadingBooks) {
    return;
  }
  isLoadingBooks = true;
  try {
    const response = await apiRequest(`/api/books?state=${currentState}`);
    updateSummary(response.summary);
    renderBookList(response.books);
  } catch (error) {
    console.error(error);
    showToast('도서 목록을 불러오지 못했습니다.', 'error');
  } finally {
    isLoadingBooks = false;
  }
}

function updateSummary(summary) {
  if (!summary) {
    return;
  }
  elements.summaryInProgress.textContent = summary.inProgress ?? 0;
  elements.summaryCompleted.textContent = summary.completed ?? 0;
  elements.summaryArchived.textContent = summary.archived ?? 0;
}

function renderBookList(books = []) {
  elements.bookList.innerHTML = '';
  if (!books.length) {
    elements.emptyState.classList.remove('hidden');
    return;
  }
  elements.emptyState.classList.add('hidden');

  const fragment = document.createDocumentFragment();
  bookCache.clear();

  books.forEach((book) => {
    const card = document.createElement('article');
    card.className = 'book-card';
    card.dataset.id = book.id;
    bookCache.set(book.id, book);

    const safeTitle = escapeHtml(book.title);
    const safeMeta = escapeHtml(formatBookMeta(book.author, book.publisher));

    card.innerHTML = `
      <div class="book-header">
        <div>
          <h3>${safeTitle}</h3>
          <div class="book-meta">${safeMeta}</div>
        </div>
        <button class="button secondary button--compact" data-action="open-edit">수정</button>
      </div>
      <div class="book-progress">
        <div class="progress-bar"><span style="width: ${book.progress}%"></span></div>
        <div class="progress-info">${book.currentPage ?? 0} / ${book.totalPages
    ?? '-'}쪽 (${book.progress}%)</div>
      </div>
    `;

    fragment.appendChild(card);
  });

  elements.bookList.appendChild(fragment);
}

function openEditModal(book) {
  editingBook = book;
  editInitialValues = {
    currentPage:
        Number.isFinite(book.currentPage) && book.currentPage > 0
            ? book.currentPage : null,
    state: book.state,
  };

  const safeTitle = escapeHtml(book.title);
  const safeMeta = escapeHtml(formatBookMeta(book.author, book.publisher));
  elements.editBookInfo.classList.remove('empty');
  elements.editBookInfo.innerHTML = `
    <strong>${safeTitle}</strong>
    <div class="book-meta">${safeMeta}</div>
    <small>총 페이지: ${book.totalPages ?? '-'}쪽</small>
  `;

  const nextPageValue = editInitialValues.currentPage ?? '';
  elements.editCurrentPageInput.value = nextPageValue;
  elements.editCurrentPageInput.max = book.totalPages ?? '';
  elements.editCurrentPageInput.placeholder = book.totalPages
      ? `1 ~ ${book.totalPages}쪽`
      : '현재 페이지를 입력';
  elements.editStateSelect.value = book.state;
  elements.editModal.classList.remove('hidden');
  updateEditSubmitButtonState();
  elements.editCurrentPageInput.focus();
}

function closeEditModal() {
  elements.editModal.classList.add('hidden');
  editingBook = null;
  editInitialValues = null;
  elements.editBookInfo.classList.add('empty');
  elements.editBookInfo.innerHTML = '<p>수정할 도서를 선택하세요.</p>';
  elements.editCurrentPageInput.value = '';
  elements.editCurrentPageInput.removeAttribute('max');
  elements.editStateSelect.value = Object.keys(STATE_LABELS)[0];
  elements.submitEditBookBtn.disabled = true;
}

function handleEditInputsChange() {
  updateEditSubmitButtonState();
}

function updateEditSubmitButtonState() {
  if (!editingBook || !editInitialValues) {
    elements.submitEditBookBtn.disabled = true;
    return;
  }

  const pageValue = elements.editCurrentPageInput.value.trim();
  const hasPageInput = pageValue !== '';
  let pageNumber = null;
  let isPageValid = true;
  if (hasPageInput) {
    pageNumber = Number(pageValue);
    isPageValid = Number.isFinite(pageNumber) && pageNumber > 0;
  }

  const hasPageChanged =
      hasPageInput && isPageValid && pageNumber
      !== editInitialValues.currentPage;
  const stateValue = elements.editStateSelect.value;
  const hasStateChanged = stateValue !== editInitialValues.state;
  const hasChanges = (hasPageChanged || hasStateChanged) && isPageValid;
  elements.submitEditBookBtn.disabled = !hasChanges;
}

async function handleEditSubmit() {
  if (!editingBook || !editInitialValues) {
    return;
  }

  const pageValue = elements.editCurrentPageInput.value.trim();
  const hasPageInput = pageValue !== '';
  let pageNumber = null;
  if (hasPageInput) {
    pageNumber = Number(pageValue);
    if (!Number.isFinite(pageNumber) || pageNumber <= 0) {
      showToast('유효한 페이지 수를 입력하세요.', 'error');
      return;
    }
  }

  const stateValue = elements.editStateSelect.value;
  const payload = {id: editingBook.id};
  if (hasPageInput && pageNumber !== editInitialValues.currentPage) {
    payload.currentPage = pageNumber;
  }
  if (stateValue !== editInitialValues.state) {
    payload.state = stateValue;
  }

  if (!('currentPage' in payload) && !('state' in payload)) {
    return;
  }

  try {
    await apiRequest('/api/books', {
      method: 'PATCH',
      body: payload,
    });
    showToast('도서 정보가 변경되었습니다.', 'success');
    closeEditModal();
    await loadBooks();
  } catch (error) {
    console.error(error);
    showToast(error.message ?? '도서 정보를 수정하지 못했습니다.', 'error');
  }
}

async function handleSearch() {
  const keyword = elements.searchInput.value.trim();
  if (!keyword) {
    showToast('검색어를 입력하세요.', 'error');
    return;
  }

  elements.searchResults.innerHTML = '<p class="book-meta">검색 중...</p>';
  selectedBook = null;
  updateSelectedBookDetails();
  updateAddSubmitButtonState();

  try {
    const response = await apiRequest(
        `/api/books/search?query=${encodeURIComponent(keyword)}`);
    searchResultsCache = response.items ?? [];
    renderSearchResults();
  } catch (error) {
    console.error(error);
    showToast('도서 검색에 실패했습니다.', 'error');
    elements.searchResults.innerHTML = '<p class="book-meta">검색 결과를 가져올 수 없습니다.</p>';
  }
}

function renderSearchResults() {
  if (!searchResultsCache.length) {
    elements.searchResults.innerHTML = '<p class="book-meta">검색 결과가 없습니다.</p>';
    return;
  }

  const fragment = document.createDocumentFragment();
  searchResultsCache.forEach((item, index) => {
    const div = document.createElement('div');
    div.className = 'search-result-item';
    div.dataset.index = index;
    div.innerHTML = `
      <strong>${escapeHtml(item.title)}</strong>
      <div class="book-meta">${escapeHtml(
        formatBookMeta(item.author, item.publisher))}</div>
    `;
    fragment.appendChild(div);
  });

  elements.searchResults.innerHTML = '';
  elements.searchResults.appendChild(fragment);
}

function selectSearchResult(book, element) {
  selectedBook = book;
  elements.searchResults.querySelectorAll('.search-result-item').forEach(
      (item) => {
        item.classList.toggle('active', item === element);
      });
  updateSelectedBookDetails();
  updateAddSubmitButtonState();
}

function updateSelectedBookDetails() {
  if (!selectedBook) {
    elements.selectedBookDetails.classList.add('empty');
    elements.selectedBookDetails.innerHTML = '<p>검색 결과에서 추가할 도서를 선택하세요.</p>';
    return;
  }

  elements.selectedBookDetails.classList.remove('empty');
  const safeTitle = escapeHtml(selectedBook.title);
  const safeMeta = escapeHtml(
      formatBookMeta(selectedBook.author, selectedBook.publisher));
  const bookLink = selectedBook.link ? encodeURI(selectedBook.link) : '#';
  elements.selectedBookDetails.innerHTML = `
    <strong>${safeTitle}</strong>
    <div>${safeMeta}</div>
    <a href="${bookLink}" target="_blank" rel="noopener">자세히 보기</a>
  `;
}

function updateAddSubmitButtonState() {
  const totalPages = Number(elements.totalPagesInput.value);
  const isReady = selectedBook && Number.isFinite(totalPages) && totalPages > 0;
  elements.submitAddBookBtn.disabled = !isReady;
}

async function handleAddBookSubmit() {
  const totalPages = Number(elements.totalPagesInput.value);
  if (!selectedBook || !Number.isFinite(totalPages) || totalPages <= 0) {
    showToast('도서를 선택하고 총 페이지를 입력하세요.', 'error');
    return;
  }

  const payload = {
    isbn: selectedBook.isbn,
    title: selectedBook.title,
    author: selectedBook.author,
    publisher: selectedBook.publisher,
    totalPages,
  };

  try {
    await apiRequest('/api/books', {
      method: 'POST',
      body: payload,
    });
    showToast('도서가 추가되었습니다.', 'success');
    closeAddBookModal();
    await loadBooks();
  } catch (error) {
    console.error(error);
    showToast(error.message ?? '도서 추가에 실패했습니다.', 'error');
  }
}

function openAddBookModal() {
  elements.addModal.classList.remove('hidden');
  elements.searchInput.focus();
}

function closeAddBookModal() {
  elements.addModal.classList.add('hidden');
  elements.searchInput.value = '';
  elements.searchResults.innerHTML = '';
  elements.totalPagesInput.value = '';
  selectedBook = null;
  updateSelectedBookDetails();
  updateAddSubmitButtonState();
}

async function apiRequest(url, {method = 'GET', body} = {}) {
  const options = {
    method,
    credentials: 'include',
    headers: {},
  };

  if (body !== undefined) {
    options.headers['Content-Type'] = 'application/json';
    options.body = JSON.stringify(body);
  }

  const response = await fetch(url, options);

  if (!response.ok) {
    if (response.status === 401) {
      showUnauthenticated();
    }

    let message = `요청에 실패했습니다. (${response.status})`;
    try {
      const errorPayload = await response.json();
      if (errorPayload?.message) {
        message = errorPayload.message;
      }
    } catch (error) {
      // ignore
    }
    throw new Error(message);
  }

  if (response.status === 204) {
    return null;
  }

  const contentType = response.headers.get('content-type');
  if (contentType && contentType.includes('application/json')) {
    return response.json();
  }

  return null;
}

function showToast(message, type = 'success') {
  elements.toast.textContent = message;
  elements.toast.className = `toast ${type}`;
  elements.toast.classList.remove('hidden');
  clearTimeout(showToast.timeoutId);
  showToast.timeoutId = setTimeout(() => {
    elements.toast.classList.add('hidden');
  }, 2500);
}

function formatBookMeta(author, publisher) {
  return [author, publisher].filter(Boolean).join(' · ');
}

function escapeHtml(value = '') {
  return String(value)
  .replace(/&/g, '&amp;')
  .replace(/</g, '&lt;')
  .replace(/>/g, '&gt;')
  .replace(/"/g, '&quot;')
  .replace(/'/g, '&#39;');
}
