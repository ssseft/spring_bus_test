// Kakao Map front: markers + route(directions) + edit/delete
// 이 파일은 다음 기능을 담당합니다.
// 1) 마커 로드/표시/토글(STUDENT/STOP)
// 2) 마커 편집/삭제 패널
// 3) 노선등록 모드로 정류장 선택 후, 서버에 길찾기 요청하여 폴리라인(경로) 그리기

let map;
const markersByType = { STUDENT: [], STOP: [] };
let allMarkers = [];
let allInfoWindows = [];
// id -> { marker, infowindow, item }
const markerStoreById = new Map();
let currentEditItem = null; // { id, title, lat, lng, type }

// ---- 노선(경로) 관련 전역 상태 ----
let routeMode = false;            // 노선등록 모드 on/off
let selectedStopIds = [];         // 사용자가 선택한 정류장 id 순서 리스트
let currentPolyline = null;       // 지도에 그려진 경로 선
let routeOverlays = [];           // 번호 오버레이(CustomOverlay) 보관

function initMap() {
  const container = document.getElementById('map');
  const options = {
    center: new kakao.maps.LatLng(37.5665, 126.9780),
    level: 7
  };
  map = new kakao.maps.Map(container, options);
  ensureTypeToggleUI();
  ensureEditPanelUI();
  loadMarkers();
}

function markerImageForType(type) {
  const size = new kakao.maps.Size(32, 32);
  const imgStudent = '/img/marker_student.png';
  const imgStop = '/img/marker_stop.png';
  const src = (type === 'STOP') ? imgStop : imgStudent;
  return new kakao.maps.MarkerImage(src, size);
}

async function loadMarkers() {
  try {
    // 이전 상태 정리
    allMarkers.forEach(m => m.setMap(null));
    allMarkers = [];
    allInfoWindows.forEach(iw => { try { iw.close(); } catch (_) {} });
    allInfoWindows = [];
    markerStoreById.clear();
    Object.keys(markersByType).forEach(k => {
      markersByType[k].forEach(m => m.setMap(null));
      markersByType[k] = [];
    });

    const res = await fetch('/api/locations');
    const data = await res.json();

    data.forEach(item => {
      const position = new kakao.maps.LatLng(item.lat, item.lng);
      const marker = new kakao.maps.Marker({
        map: map,
        position: position,
        image: markerImageForType(item.type)
      });

      const iwContent = `<div style="padding:5px;">
        <div><strong>${item.title}</strong></div>
        <div>(${item.type})</div>
        <div>${item.lat.toFixed(5)}, ${item.lng.toFixed(5)}</div>
        <div style="margin-top:6px; text-align:right;">
          <button type="button" style="padding:4px 6px;" onclick="openEditPanelById(${item.id})">편집</button>
          <button type="button" style="padding:4px 6px; margin-left:4px;" onclick="addStopToRoute(${item.id})">경로추가</button>
        </div>
      </div>`;
      const infowindow = new kakao.maps.InfoWindow({ content: iwContent });
      allInfoWindows.push(infowindow);
      kakao.maps.event.addListener(marker, 'mouseover', () => infowindow.open(map, marker));
      kakao.maps.event.addListener(marker, 'mouseout', () => infowindow.close());
      kakao.maps.event.addListener(marker, 'click', () => {
        infowindow.open(map, marker);
      });

      const t = (item.type === 'STOP') ? 'STOP' : 'STUDENT';
      (markersByType[t] ||= []).push(marker);
      allMarkers.push(marker);
      markerStoreById.set(item.id, { marker, infowindow, item });
    });

    // 타입 토글 반영
    const studentChecked = document.getElementById('toggleStudent')?.checked ?? true;
    const stopChecked = document.getElementById('toggleStop')?.checked ?? true;
    toggleType('STUDENT', studentChecked);
    toggleType('STOP', stopChecked);
  } catch (e) {
    console.error('마커 로드 실패', e);
  }

  // 마커 재로딩 후에도 선택된 정류장이 있다면 번호 오버레이 복원
  if (selectedStopIds.length > 0) {
    rebuildRouteOverlays();
  }
}

async function submitLocation() {
  const title = document.getElementById('title').value.trim();
  const address = document.getElementById('address').value.trim();
  const type = document.getElementById('type').value;
  if (!title || !address) {
    alert('명칭과 주소를 입력해 주세요');
    return;
  }
  try {
    const res = await fetch('/api/locations', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ title, address, type })
    });
    if (res.ok) {
      alert('등록 성공! 마커를 새로고침합니다');
      loadMarkers();
    } else {
      const msg = await res.text();
      alert('등록 실패: ' + msg);
    }
  } catch (e) {
    alert('요청 실패: ' + e);
  }
}

// Kakao JS SDK autoload=false
kakao.maps.load(initMap);

function ensureTypeToggleUI() {
  const panelDiv = document.querySelector('.panel > div');
  if (!panelDiv) return;
  if (document.getElementById('toggleStudent')) return;

  const mkLabel = (id, text, onChange) => {
    const label = document.createElement('label');
    label.style.marginLeft = '12px';
    const input = document.createElement('input');
    input.type = 'checkbox';
    input.id = id;
    input.checked = true;
    input.addEventListener('change', (e) => onChange(e.target.checked));
    label.appendChild(input);
    label.appendChild(document.createTextNode(' ' + text));
    return label;
  };

  panelDiv.appendChild(
    mkLabel('toggleStudent', 'STUDENT 표시', (checked) => toggleType('STUDENT', checked))
  );
  panelDiv.appendChild(
    mkLabel('toggleStop', 'STOP 표시', (checked) => toggleType('STOP', checked))
  );
}

function toggleType(type, show) {
  const arr = markersByType[type] || [];
  arr.forEach(m => m.setMap(show ? map : null));
  if (!show) {
    allInfoWindows.forEach(iw => { try { iw.close(); } catch (_) {} });
  }
}

// ----- 편집 패널 -----
function ensureEditPanelUI() {
  const panel = document.querySelector('.panel');
  if (!panel) return;
  if (document.getElementById('editPanel')) return;

  const div = document.createElement('div');
  div.id = 'editPanel';
  div.style.display = 'none';
  div.style.marginTop = '8px';
  div.style.padding = '8px';
  div.style.border = '1px solid #ddd';
  div.style.background = '#fafafa';

  div.innerHTML = `
    <div style="margin-bottom:6px; font-weight:600;">마커 편집</div>
    <div>
      <input id="editTitle" placeholder="명칭" size="24" />
      <select id="editType">
        <option value="STUDENT">STUDENT</option>
        <option value="STOP">STOP</option>
      </select>
    </div>
    <div style="margin-top:6px;">
      <button id="btnSaveEdit" type="button">저장</button>
      <button id="btnDeleteEdit" type="button" style="margin-left:6px;">삭제</button>
      <button id="btnCancelEdit" type="button" style="margin-left:6px;">취소</button>
    </div>
  `;

  panel.appendChild(div);

  document.getElementById('btnSaveEdit').addEventListener('click', async () => {
    if (!currentEditItem) return;
    const title = document.getElementById('editTitle').value.trim();
    const type = document.getElementById('editType').value;
    try {
      const res = await fetch(`/api/locations/${currentEditItem.id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ title, type })
      });
      if (!res.ok) {
        const msg = await res.text();
        alert('수정 실패: ' + msg);
        return;
      }
      alert('수정 완료');
      closeEditPanel();
      allInfoWindows.forEach(iw => { try { iw.close(); } catch (_) {} });
      loadMarkers();
    } catch (e) {
      alert('수정 오류: ' + e);
    }
  });

  document.getElementById('btnDeleteEdit').addEventListener('click', async () => {
    if (!currentEditItem) return;
    if (!confirm('정말 삭제하시겠습니까?')) return;
    try {
      const res = await fetch(`/api/locations/${currentEditItem.id}`, {
        method: 'DELETE'
      });
      if (!res.ok) {
        const msg = await res.text();
        alert('삭제 실패: ' + msg);
        return;
      }
      alert('삭제 완료');
      closeEditPanel();
      loadMarkers();
    } catch (e) {
      alert('삭제 오류: ' + e);
    }
  });

  document.getElementById('btnCancelEdit').addEventListener('click', () => closeEditPanel());
}

function openEditPanelById(id) {
  fetch('/api/locations')
    .then(r => r.json())
    .then(list => {
      const found = list.find(x => x.id === id);
      if (!found) {
        alert('대상을 찾을 수 없습니다.');
        return;
      }
      openEditPanel(found);
    })
    .catch(e => alert('아이템 조회 오류: ' + e));
}

function openEditPanel(item) {
  currentEditItem = item;
  const panel = document.getElementById('editPanel');
  if (!panel) return;
  document.getElementById('editTitle').value = item.title || '';
  document.getElementById('editType').value = item.type || 'STUDENT';
  panel.style.display = 'block';
}

function closeEditPanel() {
  currentEditItem = null;
  const panel = document.getElementById('editPanel');
  if (panel) panel.style.display = 'none';
}

// ----- 노선(경로) 기능 -----
function toggleRouteMode() {
  routeMode = !routeMode;
  document.getElementById('btnRouteMode').textContent = '노선등록 모드: ' + (routeMode ? 'ON' : 'OFF');
  if (!routeMode) {
    // 모드 종료 시에는 선택만 유지(원하면 clearRoute 버튼으로 완전 초기화)
  }
}

function addStopToRoute(id) {
  if (!routeMode) {
    alert('먼저 "노선등록 모드"를 켜주세요.');
    return;
  }
  if (!selectedStopIds.includes(id)) {
    selectedStopIds.push(id);
  }
  // 간단 안내
  const order = selectedStopIds.indexOf(id) + 1;
  const info = markerStoreById.get(id)?.infowindow;
  if (info) {
    try { info.close(); } catch (_) {}
  }
  alert('정류장 선택됨: ' + id + ' (순서 ' + order + ')');

  // 번호 오버레이 갱신
  rebuildRouteOverlays();
}

async function drawRoute() {
  if (selectedStopIds.length < 2) {
    alert('정류장을 2개 이상 선택하세요.');
    return;
  }
  try {
    const res = await fetch('/api/routes/directions', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ orderedStopIds: selectedStopIds })
    });
    if (!res.ok) {
      const t = await res.text();
      alert('경로 요청 실패: ' + t);
      return;
    }
    const data = await res.json();
    const path = (data.path || []).map(p => new kakao.maps.LatLng(p.latitude ?? p.lat ?? p.y, p.longitude ?? p.lng ?? p.x));
    if (path.length === 0) {
      alert('그릴 경로가 없습니다.');
      return;
    }
    if (currentPolyline) currentPolyline.setMap(null);
    currentPolyline = new kakao.maps.Polyline({
      map,
      path,
      strokeWeight: 6,
      strokeColor: '#e83e8c',
      strokeOpacity: 0.9,
      strokeStyle: 'solid'
    });
    currentPolyline.setMap(map);

    // 지도 화면을 경로에 맞게 맞춤
    const bounds = new kakao.maps.LatLngBounds();
    path.forEach(p => bounds.extend(p));
    map.setBounds(bounds);

    // 간단 요약(거리/시간)
    if (data.distanceMeters || data.durationSeconds) {
      const km = (data.distanceMeters / 1000).toFixed(1);
      const min = Math.round((data.durationSeconds || 0) / 60);
      alert(`총 거리 ${km}km, 예상 ${min}분`);
    }
  } catch (e) {
    alert('경로 처리 오류: ' + e);
  }
}

function clearRoute() {
  selectedStopIds = [];
  if (currentPolyline) {
    currentPolyline.setMap(null);
    currentPolyline = null;
  }
  clearRouteOverlays();
}

// ----- 번호 오버레이(1,2,3...) 표시 -----
function clearRouteOverlays() {
  routeOverlays.forEach(ov => ov.setMap(null));
  routeOverlays = [];
}

function rebuildRouteOverlays() {
  // 기존 오버레이 제거 후, 선택 순서대로 다시 생성
  clearRouteOverlays();

  selectedStopIds.forEach((id, idx) => {
    const store = markerStoreById.get(id);
    const item = store?.item;
    if (!item) return;
    const pos = new kakao.maps.LatLng(item.lat, item.lng);
    const num = idx + 1;
    const content = `<div style="background:#e83e8c;color:#fff;border-radius:14px;min-width:24px;height:24px;line-height:24px;padding:0 6px;text-align:center;font-weight:700;border:2px solid #fff;box-shadow:0 2px 6px rgba(0,0,0,.25);font-size:12px;">${num}</div>`;
    const overlay = new kakao.maps.CustomOverlay({ position: pos, content, yAnchor: 1, zIndex: 99 });
    overlay.setMap(map);
    routeOverlays.push(overlay);
  });
}
