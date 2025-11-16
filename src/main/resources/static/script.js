// Kakao Map front: markers + route(directions) + edit/delete
// 이 파일은 다음 기능을 담당합니다.
// 1) 마커 로드/표시/토글(STUDENT/STOP)
// 2) 마커 편집/삭제 패널
// 3) 노선등록 모드로 정류장 선택 후, 서버에 길찾기 요청하여 폴리라인(경로) 그리기

let map;
// BusStop 기반으로 전환: STOP만 사용
const markersByType = { STOP: [] };
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
let savedRoutes = [];             // 저장된 노선 목록 캐시

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

// UI에서 학원 ID를 설정하고 마커 로드
function setAcademyAndLoad() {
    const input = document.getElementById('academyId');
    if (!input) return;
    const v = input.value.trim();
    if (!v) {
        alert('학원 ID(UUID)를 입력하세요.');
        return;
    }
    window.ACADEMY_ID = v;
    loadMarkers();
}

async function loadMarkers() {
    // Academy ID는 전역(window.ACADEMY_ID)에서 주입받습니다.
    const ACADEMY_ID = window.ACADEMY_ID || null;
    if (!ACADEMY_ID) {
        console.warn('ACADEMY_ID가 설정되지 않았습니다. window.ACADEMY_ID에 UUID를 설정하세요.');
        return;
    }
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

        // BusStop 목록 조회 (페이지 크게 가져와 일괄 표시)
        const res = await fetch(`/api/busstop/academies/${ACADEMY_ID}/bus-stops?page=0&size=1000`);
        const page = await res.json();
        const data = page.content || [];

        data.forEach(item => {
            const lat = Number(item.latitude);
            const lng = Number(item.longitude);
            if (Number.isNaN(lat) || Number.isNaN(lng)) return;
            const position = new kakao.maps.LatLng(lat, lng);
            const marker = new kakao.maps.Marker({
                map: map,
                position: position,
                image: markerImageForType('STOP')
            });

            const iwContent = `<div style="padding:5px;">
        <div><strong>${item.name ?? ''}</strong></div>
        <div>(BUS STOP)</div>
        <div>${lat.toFixed(5)}, ${lng.toFixed(5)}</div>
        <div style="margin-top:6px; text-align:right;">
          <button type="button" style="padding:4px 6px;" onclick="openEditPanelById('${item.id}')">편집</button>
          <button type="button" style="padding:4px 6px; margin-left:4px;" onclick="addStopToRoute('${item.id}')">경로추가</button>
        </div>
      </div>`;
            const infowindow = new kakao.maps.InfoWindow({ content: iwContent });
            allInfoWindows.push(infowindow);
            kakao.maps.event.addListener(marker, 'mouseover', () => infowindow.open(map, marker));
            kakao.maps.event.addListener(marker, 'mouseout', () => infowindow.close());
            kakao.maps.event.addListener(marker, 'click', () => {
                infowindow.open(map, marker);
            });

            (markersByType['STOP'] ||= []).push(marker);
            allMarkers.push(marker);
            markerStoreById.set(item.id, { marker, infowindow, item: { id: item.id, title: item.name, lat, lng, type: 'STOP' } });
        });

        // 타입 토글 반영
        const stopChecked = document.getElementById('toggleStop')?.checked ?? true;
        toggleType('STOP', stopChecked);
    } catch (e) {
        console.error('마커 로드 실패', e);
    }

    // 마커 재로딩 후에도 선택된 정류장이 있다면 번호 오버레이 복원
    if (selectedStopIds.length > 0) {
        rebuildRouteOverlays();
    }
}

// ----- 저장된 노선 목록/상세 -----
async function loadRoutes() {
    const ACADEMY_ID = window.ACADEMY_ID || document.getElementById('academyId')?.value.trim();
    if (!ACADEMY_ID) {
        alert('학원 UUID가 필요합니다. 상단에 입력하세요.');
        return;
    }
    try {
        const res = await fetch(`/api/routes/academies/${ACADEMY_ID}`);
        if (!res.ok) {
            const msg = await res.text();
            alert('노선 목록 조회 실패: ' + msg);
            return;
        }
        const list = await res.json();
        savedRoutes = list || [];
        const sel = document.getElementById('routeList');
        sel.innerHTML = '';
        savedRoutes.forEach(r => {
            const min = Math.round((r.totalTimeSeconds || 0) / 60);
            const opt = document.createElement('option');
            opt.value = r.id;
            opt.textContent = `${r.name} (${min}분)`;
            sel.appendChild(opt);
        });
        if (savedRoutes.length === 0) {
            const opt = document.createElement('option');
            opt.textContent = '저장된 노선이 없습니다';
            sel.appendChild(opt);
        }
    } catch (e) {
        alert('노선 목록 오류: ' + e);
    }
}

async function showSelectedRoute() {
    const sel = document.getElementById('routeList');
    const id = sel?.value;
    if (!id) {
        alert('먼저 노선을 선택하세요.');
        return;
    }
    try {
        const res = await fetch(`/api/routes/${id}`);
        if (!res.ok) {
            const msg = await res.text();
            alert('노선 조회 실패: ' + msg);
            return;
        }
        const data = await res.json();
        const path = (data.path || []).map(p => new kakao.maps.LatLng(p.latitude ?? p.lat ?? p.y, p.longitude ?? p.lng ?? p.x));
        if (path.length === 0) {
            alert('저장된 경로가 비어 있습니다.');
            return;
        }
        if (currentPolyline) currentPolyline.setMap(null);
        currentPolyline = new kakao.maps.Polyline({
            map,
            path,
            strokeWeight: 6,
            strokeColor: '#007bff',
            strokeOpacity: 0.9,
            strokeStyle: 'solid'
        });
        currentPolyline.setMap(map);
        const bounds = new kakao.maps.LatLngBounds();
        path.forEach(p => bounds.extend(p));
        map.setBounds(bounds);
        if (data.distanceMeters || data.durationSeconds) {
            const km = (data.distanceMeters / 1000).toFixed(1);
            const min = Math.round((data.durationSeconds || 0) / 60);
            alert(`저장 노선 – 총 거리 ${km}km, 예상 ${min}분`);
        }
    } catch (e) {
        alert('노선 표시 오류: ' + e);
    }
}
async function submitLocation() {
    const ACADEMY_ID = window.ACADEMY_ID || document.getElementById('academyId')?.value.trim();
    const title = document.getElementById('title').value.trim();
    const address = document.getElementById('address').value.trim();
    if (!ACADEMY_ID) {
        alert('학원 UUID가 필요합니다. 상단에 입력하세요.');
        return;
    }
    if (!title || !address) {
        alert('명칭과 주소를 입력해 주세요');
        return;
    }
    try {
        const res = await fetch(`/api/busstop/academies/${ACADEMY_ID}/bus-stops/geocode`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name: title, address })
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
    if (document.getElementById('toggleStop')) return;

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
        try {
            const res = await fetch(`/api/busstop/bus-stops/${currentEditItem.id}`, {
                method: 'PATCH',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ name: title })
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
            const res = await fetch(`/api/busstop/bus-stops/${currentEditItem.id}`, {
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
    const store = markerStoreById.get(id);
    if (!store) {
        alert('대상을 찾을 수 없습니다.');
        return;
    }
    openEditPanel(store.item);
}

function openEditPanel(item) {
    currentEditItem = item;
    const panel = document.getElementById('editPanel');
    if (!panel) return;
    document.getElementById('editTitle').value = item.title || '';
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
        const ACADEMY_ID = window.ACADEMY_ID || document.getElementById('academyId')?.value.trim();
        if (!ACADEMY_ID) {
            alert('학원 UUID가 필요합니다. 상단에 입력하세요.');
            return;
        }
        const res = await fetch('/api/routes/preview', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ orderedBusStopIds: selectedStopIds })
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

        // 경로 저장 호출 (자동 저장)
        const routeName = (document.getElementById('routeName')?.value || '').trim();
        const saveRes = await fetch(`/api/routes/academies/${ACADEMY_ID}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name: routeName, orderedBusStopIds: selectedStopIds })
        });
        if (!saveRes.ok) {
            const msg = await saveRes.text();
            alert('경로 저장 실패: ' + msg);
            return;
        }
        const saved = await saveRes.json();
        alert('경로 저장 완료! Route ID: ' + saved.routeId);
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

// 패널 접기/펴기 토글 바인딩 및 상태 저장
(function initCollapsibles(){
  function bind(panelId, bodyId, btnId, storageKey){
    const body = document.getElementById(bodyId);
    const btn = document.getElementById(btnId);
    if (!body || !btn) return;
    // 초기 상태 적용
    const saved = localStorage.getItem(storageKey);
    if (saved === 'collapsed') {
      body.style.display = 'none';
      btn.textContent = '펴기';
    } else {
      body.style.display = '';
      btn.textContent = '접기';
    }
    btn.addEventListener('click', () => {
      const collapsed = body.style.display === 'none';
      if (collapsed) {
        body.style.display = '';
        btn.textContent = '접기';
        localStorage.setItem(storageKey, 'expanded');
      } else {
        body.style.display = 'none';
        btn.textContent = '펴기';
        localStorage.setItem(storageKey, 'collapsed');
      }
    });
  }
  // 상세/수정 패널 바인딩
  bind('scheduleDetail','scheduleDetailBody','btnToggleScheduleDetail','panel.scheduleDetail');
  bind('scheduleEdit','scheduleEditBody','btnToggleScheduleEdit','panel.scheduleEdit');
})();

// 스케줄 생성 섹션 접기/펴기 (기존 마크업을 감싸지 않고 주요 블록들을 토글)
(function initCreateSectionToggle(){
  const createBtn = document.getElementById('btnCreateSchedule');
  const startTime = document.getElementById('scheduleStartTime');
  const assignCont = document.getElementById('assignmentContainer');
  if (!createBtn || !startTime || !assignCont) return;
  const actionRow = createBtn.parentElement;
  const assignRow = assignCont.parentElement;
  const fieldsRow = startTime.closest('div');
  // 헤더(h4) 옆에 토글 버튼 추가
  const root = fieldsRow?.parentElement;
  const h4 = root?.querySelector('h4');
  if (!h4) return;
  let toggle = document.getElementById('btnToggleScheduleCreate');
  if (!toggle) {
    toggle = document.createElement('button');
    toggle.id = 'btnToggleScheduleCreate';
    toggle.type = 'button';
    toggle.style.padding = '4px 8px';
    toggle.textContent = '접기';
    h4.insertAdjacentElement('afterend', toggle);
  }
  const storageKey = 'panel.scheduleCreate';
  function setCollapsed(collapsed){
    const disp = collapsed ? 'none' : '';
    if (fieldsRow) fieldsRow.style.display = disp;
    if (assignRow) assignRow.style.display = disp;
    if (actionRow) actionRow.style.display = disp;
    toggle.textContent = collapsed ? '펴기' : '접기';
    localStorage.setItem(storageKey, collapsed ? 'collapsed' : 'expanded');
  }
  // 초기 상태 적용
  const saved = localStorage.getItem(storageKey);
  if (saved === 'collapsed') setCollapsed(true); else setCollapsed(false);
  toggle.addEventListener('click', () => {
    const collapsed = (fieldsRow?.style.display === 'none');
    setCollapsed(!collapsed);
  });
})();

// ----------------------------
// Schedule 생성 연동 (API: POST /api/schedules)
// ----------------------------
(function scheduleIntegration() {
  const DAY_ORDER = ['SUN','MON','TUE','WED','THU','FRI','SAT'];
  function repeatDaysToMask(daysArr) {
    return (daysArr || []).reduce((mask, d) => mask | (1 << DAY_ORDER.indexOf(d)), 0);
  }
  function isUuid(v){ return typeof v === 'string' && /^[0-9a-fA-F-]{36}$/.test(v); }

  async function fetchRouteStopsFromApi(routeId) {
    const res = await fetch(`/api/routes/${routeId}/stops`);
    if (!res.ok) throw new Error(await res.text() || '노선 정류장 로드 실패');
    return res.json(); // [{ routeStopId, busStopId, busStopName, order }]
  }

  async function fetchAcademyBusStops(academyId) {
    const res = await fetch(`/api/busstop/academies/${academyId}/bus-stops?page=0&size=1000`);
    if (!res.ok) throw new Error(await res.text() || '정류장 로드 실패');
    const page = await res.json();
    return page.content || [];
  }

  async function ensureBusStopsCache() {
    const academyId = (window.ACADEMY_ID || document.getElementById('academyId')?.value || '').trim();
    if (!academyId) throw new Error('학원 UUID가 필요합니다.');
    if (!window.BUS_STOPS_CACHE || !Array.isArray(window.BUS_STOPS_CACHE)) {
      window.BUS_STOPS_CACHE = await fetchAcademyBusStops(academyId);
    }
    return window.BUS_STOPS_CACHE;
  }

  function busStopOptionsHtml(list) {
    return (list || []).map(s => `<option value="${s.busStopId || s.id}">${s.busStopName || s.name || s.id}</option>`).join('');
  }

  function addAssignmentRow() {
    const cont = document.getElementById('assignmentContainer');
    if (!cont) return;
    const row = document.createElement('div');
    row.className = 'assign-row';
    row.style.marginBottom = '4px';
    row.innerHTML = `
      <input class="student-id-input" placeholder="학생 UUID" size="36" />
      <select class="busstop-select" style="min-width:220px;"></select>
      <button type="button" class="btn-remove">삭제</button>
    `;
    row.querySelector('.btn-remove').addEventListener('click', () => row.remove());
    cont.appendChild(row);
    // 옵션 채우기: 노선 선택 시 해당 노선 정류장, 없으면 학원 전체 정류장
    const currentRouteId = (document.getElementById('routeList')?.value || '').trim();
    if (isUuid(currentRouteId)) {
      fetchRouteStopsFromApi(currentRouteId)
        .then(list => { row.querySelector('.busstop-select').innerHTML = busStopOptionsHtml(list); })
        .catch(err => console.error(err));
    } else {
      ensureBusStopsCache()
        .then(list => { row.querySelector('.busstop-select').innerHTML = busStopOptionsHtml(list); })
        .catch(err => console.error(err));
    }
  }

  function collectRepeatDays() {
    return [...document.querySelectorAll('input[name="scheduleDays[]"]:checked')].map(i => i.value);
  }

  async function onCreateSchedule() {
    const createBtn = document.getElementById('btnCreateSchedule');
    // 중복 클릭 방지
    if (onCreateSchedule._inFlight) return;
    onCreateSchedule._inFlight = true;
    createBtn && (createBtn.disabled = true);
    try {
      const academyId = (window.ACADEMY_ID || document.getElementById('academyId')?.value || '').trim();
      const routeId = (document.getElementById('routeList')?.value || '').trim();
      const name = (document.getElementById('scheduleName')?.value || '').trim() || '스케줄';
      const startTime = document.getElementById('scheduleStartTime')?.value || '';
      const boardingStatus = document.getElementById('boardingStatus')?.value || 'PICKUP';
      const daysArr = collectRepeatDays();
      const repeatDays = repeatDaysToMask(daysArr);

      if (!academyId) return alert('학원 UUID를 입력하세요.');
      if (!routeId) return alert('노선을 선택하세요.');
      if (!startTime) return alert('시작시간을 선택하세요.');
      if (repeatDays === 0) return alert('반복 요일을 1개 이상 선택하세요.');

      // assignments 수집
      const assignments = [...document.querySelectorAll('#assignmentContainer .assign-row')]
        .map(r => ({
          studentId: (r.querySelector('.student-id-input')?.value || '').trim(),
          busStopId: (r.querySelector('.busstop-select')?.value || '').trim(),
        }))
        .filter(a => a.studentId && a.busStopId);

      if (assignments.length === 0) return alert('학생 배정(학생+정류장)을 1개 이상 추가하세요.');

      // 중복 학생 방지(프론트 체크)
      const dup = new Set();
      for (const a of assignments) {
        if (dup.has(a.studentId)) return alert('같은 학생이 중복되었습니다.');
        dup.add(a.studentId);
      }

      const payload = { academyId, routeId, name, repeatDays, startTime, boardingStatus, assignments };

      const res = await fetch('/api/schedules', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
      if (!res.ok) throw new Error(await res.text() || '스케줄 생성 실패');
      const created = await res.json();
      const sid = created.id || '';
      document.getElementById('scheduleIdForView').value = sid;
      try {
        if (sid) {
          const dres = await fetch(`/api/schedules/${sid}`);
          if (dres.ok) {
            const det = await dres.json();
            renderScheduleDetail(det);
          }
        }
      } catch (_) {}
      alert('스케줄이 생성되었습니다.');
    } catch (e) {
      console.error(e);
      alert(e.message || '스케줄 생성에 실패했습니다.');
    } finally {
      onCreateSchedule._inFlight = false;
      createBtn && (createBtn.disabled = false);
    }
  }

  async function onViewSchedule() {
    const id = (document.getElementById('scheduleIdForView')?.value || '').trim();
    if (!id) return alert('스케줄 ID를 입력하세요.');
    try {
      const res = await fetch(`/api/schedules/${id}`);
      if (!res.ok) throw new Error(await res.text() || '조회 실패');
      const detail = await res.json();
      renderScheduleDetail(detail);
    } catch (e) {
      console.error(e);
      alert(e.message || '스케줄 조회 실패');
    }
  }

  // 초기 바인딩
  document.getElementById('btnAddAssignment')?.addEventListener('click', addAssignmentRow);
  document.getElementById('btnCreateSchedule')?.addEventListener('click', onCreateSchedule);
  document.getElementById('btnViewSchedule')?.addEventListener('click', onViewSchedule);
  document.getElementById('btnDeleteSchedule')?.addEventListener('click', onDeleteSchedule);

  // Fallback: 위 바인딩이 실패하는 환경을 위해 위임 클릭 핸들러 추가(행 추가만 처리)
  document.addEventListener('click', (e) => {
    const t = e.target;
    if (!t) return;
    if (t.id === 'btnAddAssignment') {
      addAssignmentRow();
    }
    if (t.id === 'btnAssignAdd') {
      (async () => {
        try {
          const wrap = document.getElementById('scheduleDetail');
          const sid = wrap?.dataset?.scheduleId || document.getElementById('scheduleIdForView')?.value || '';
          if (!sid) return alert('먼저 스케줄을 조회하세요.');
          const detRes = await fetch(`/api/schedules/${sid}`);
          if (!detRes.ok) throw new Error('스케줄 정보를 가져오지 못했습니다.');
          const detail = await detRes.json();
          const routeId = document.getElementById('editRouteSelect')?.value || detail.routeId;
          if (!routeId) return alert('노선을 먼저 선택하세요.');
          const [students, stops] = await Promise.all([
            fetch('/api/students').then(r=>{ if(!r.ok) throw new Error('학생 목록 실패'); return r.json(); }),
            fetch(`/api/routes/${routeId}/stops`).then(r=>{ if(!r.ok) throw new Error('정류장 목록 실패'); return r.json(); })
          ]);
          const assignedSet = new Set((detail.assignments||[]).map(a => String(a.studentId)));
          const unassigned = students.filter(s => !assignedSet.has(String(s.id)));
          if (unassigned.length === 0) return alert('추가할 수 있는 학생이 없습니다.');
          const tbody = document.getElementById('editStudentsBody');
          const row = document.createElement('tr');
          row.innerHTML = `
            <td style="border-bottom:1px solid #eee; padding:6px;">
              <select class="add-student-select" style="min-width:220px;">
                ${unassigned.map(s => `<option value="${s.id}">${s.name || s.id}</option>`).join('')}
              </select>
            </td>
            <td style="border-bottom:1px solid #eee; padding:6px;" data-status>미배정</td>
            <td style="border-bottom:1px solid #eee; padding:6px;">
              <select class="add-stop-select" style="min-width:220px;">
                ${optionHtml(stops)}
              </select>
            </td>
            <td style="border-bottom:1px solid #eee; padding:6px;">
              <button type="button" class="btnAddSave">추가</button>
              <button type="button" class="btnAddCancel">취소</button>
            </td>`;
          tbody.prepend(row);
          row.querySelector('.btnAddSave').onclick = async () => {
            const studentId = row.querySelector('.add-student-select')?.value;
            const busStopId = row.querySelector('.add-stop-select')?.value;
            if (!studentId) return alert('학생을 선택하세요.');
            if (!busStopId) return alert('정류장을 선택하세요.');
            const res = await fetch(`/api/schedules/${sid}/assignments`, { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify([{ studentId, busStopId }]) });
            if (!res.ok) throw new Error(await res.text() || '추가 실패');
            const dres = await fetch(`/api/schedules/${sid}`);
            if (dres.ok) { const det = await dres.json(); renderScheduleDetail(det); }
          };
          row.querySelector('.btnAddCancel').onclick = () => row.remove();
        } catch (err) {
          console.error(err);
          alert(err.message || '학생 추가 중 오류');
        }
      })();
    }
  });

  // 노선 변경 시, 배정 행의 정류장 드롭다운을 해당 노선 정류장으로 갱신
  document.getElementById('routeList')?.addEventListener('change', async (e) => {
    const routeId = (e.target?.value || '').trim();
    const selects = document.querySelectorAll('#assignmentContainer .busstop-select');
    if (!isUuid(routeId) || selects.length === 0) return;
    try {
      const list = await fetchRouteStopsFromApi(routeId);
      const html = busStopOptionsHtml(list);
      selects.forEach(sel => { sel.innerHTML = html; });
    } catch (err) {
      console.error(err);
      alert('노선 정류장을 불러오지 못했습니다.');
    }
  });

  // ---- 상세 렌더링 ----
  function maskToDays(mask){
    const dayOrder = ['SUN','MON','TUE','WED','THU','FRI','SAT'];
    const kor = {SUN:'일',MON:'월',TUE:'화',WED:'수',THU:'목',FRI:'금',SAT:'토'};
    const out=[]; for(let i=0;i<7;i++){ if(mask & (1<<i)){ const d=dayOrder[i]; out.push(kor[d]||d);} }
    return out.length? out.join(', ') : '-';
  }
  function fmtTime(t){
    if(!t) return '-';
    // LocalTime 직렬화가 "HH:mm:ss" 또는 "HH:mm" 케이스 모두 대응
    const s = String(t);
    const m = s.match(/^\d{2}:\d{2}/);
    return m? m[0] : s;
  }
  function renderScheduleDetail(d){
    if(!d) return;
    const sum = document.getElementById('scheduleSummary');
    const body = document.getElementById('scheduleAssignmentsBody');
    const empty = document.getElementById('scheduleEmpty');
    const wrap = document.getElementById('scheduleDetail');
    if(!sum || !body || !empty) return;

    const days = maskToDays(d.repeatDays||0);
    const active = d.isActive ? '활성' : '비활성';
    sum.innerHTML = `
      <div><strong>${d.name || '(이름 없음)'}</strong> (${active})</div>
      <div>스케줄 ID: <code>${d.id}</code></div>
      <div>학원: <code>${d.academyId || '-'}</code> · 노선: <code>${d.routeId || '-'}</code></div>
      <div>요일: ${days} · 시작: ${fmtTime(d.startTime)} · 종료: ${fmtTime(d.endTime)} · 타입: ${d.boardingStatus || '-'}</div>
    `;

    // 현재 표시중인 스케줄 ID 보관
    try { if (wrap) { wrap.dataset.scheduleId = d.id || ''; } } catch(_) {}
    const assigns = Array.isArray(d.assignments) ? d.assignments : [];
    body.innerHTML = '';
    if(assigns.length === 0){
      empty.style.display = 'block';
      return;
    }
    empty.style.display = 'none';
    assigns.forEach((a, idx) => {
      const tr = document.createElement('tr');
      tr.innerHTML = `
        <td style="border-bottom:1px solid #eee; padding:6px;">${idx+1}</td>
        <td style="border-bottom:1px solid #eee; padding:6px;"><code>${a.studentId}</code></td>
        <td style="border-bottom:1px solid #eee; padding:6px;"><code>${a.busStopId}</code></td>
      `;
      body.appendChild(tr);
    });

    // 강조 + 스크롤 이동으로 가시성 향상
    try {
      if (wrap) {
        // 접혀있다면 펼친다
        const body = document.getElementById('scheduleDetailBody');
        const toggleBtn = document.getElementById('btnToggleScheduleDetail');
        if (body && body.style.display === 'none') {
          body.style.display = '';
          if (toggleBtn) toggleBtn.textContent = '접기';
          localStorage.setItem('panel.scheduleDetail','expanded');
        }
        wrap.style.boxShadow = '0 0 0 2px #ffd54f inset';
        wrap.scrollIntoView({ behavior: 'smooth', block: 'start' });
        setTimeout(() => { wrap.style.boxShadow = 'none'; }, 1200);
      }
    } catch(_) {}

    // 수정 UI 로딩
    try { loadScheduleEditUI(d); } catch(e) { console.error('edit ui load failed', e); }
  }

  // ---------- 수정 UI 로딩 ----------
  async function fetchAllStudents() {
    const res = await fetch('/api/students');
    if (!res.ok) throw new Error(await res.text() || '학생 목록 로드 실패');
    return res.json(); // [{id,name}]
  }

  async function fetchRouteStopsFromApi(routeId) {
    const res = await fetch(`/api/routes/${routeId}/stops`);
    if (!res.ok) throw new Error(await res.text() || '노선 정류장 로드 실패');
    return res.json();
  }

  function optionHtml(list, selectedId) {
    const opts = [`<option value="">정류장 선택</option>`];
    (list||[]).forEach(s => {
      const val = s.busStopId || s.id;
      const name = s.busStopName || s.name || s.id;
      const sel = (selectedId && String(selectedId) === String(val)) ? ' selected' : '';
      opts.push(`<option value="${val}"${sel}>${name}</option>`);
    });
    return opts.join('');
  }

  async function loadScheduleEditUI(detail) {
    if (!detail) return;
    const sid = detail.id;
    const academyId = detail.academyId;
    const currentRouteId = detail.routeId;
    if (academyId) { window.ACADEMY_ID = academyId; }

    // 1) 노선 목록 바인딩
    try {
      // 기존 로드 함수 재사용
      await loadRoutes();
      const sel = document.getElementById('editRouteSelect');
      const routeSel = document.getElementById('routeList');
      const opts = routeSel?.innerHTML || '';
      if (sel) {
        sel.innerHTML = opts;
        sel.value = currentRouteId || '';
      }
    } catch (e) { console.error(e); }

    // 2) 학생/정류장/배정 테이블 구성
    const students = await fetchAllStudents();
    const stops = currentRouteId ? await fetchRouteStopsFromApi(currentRouteId) : [];
    const assignedMap = new Map();
    (detail.assignments || []).forEach(a => assignedMap.set(String(a.studentId), a.busStopId));

    const tbody = document.getElementById('editStudentsBody');
    if (!tbody) return;
    tbody.innerHTML = '';
    // 현재 배정된 학생만 렌더링
    assignedMap.forEach((assignedStop, studentId) => {
      const tr = document.createElement('tr');
      const stObj = students.find(s => String(s.id) === String(studentId));
      const displayName = (stObj && (stObj.name || stObj.id)) || studentId;
      tr.innerHTML = `
        <td style="border-bottom:1px solid #eee; padding:6px;">${displayName}</td>
        <td style="border-bottom:1px solid #eee; padding:6px;" data-status>배정됨</td>
        <td style="border-bottom:1px solid #eee; padding:6px;">
          <select class=\"edit-stop-select\" data-student-id=\"${studentId}\" style=\"min-width:220px;\">${optionHtml(stops, assignedStop)}</select>
        </td>
        <td style=\"border-bottom:1px solid #eee; padding:6px;\">
          <button type=\"button\" class=\"btnAssignSave\" data-student-id=\"${studentId}\">저장</button>
          <button type=\"button\" class=\"btnAssignRemove\" data-student-id=\"${studentId}\">제거</button>
        </td>`;
      tbody.appendChild(tr);
    });

    // 3) 액션 바인딩 (이벤트 위임)
    // 기존 핸들러 누적 방지: 새 tbody로 교체하거나, 한 번만 바인딩되었는지 체크
    if (!tbody._bound) {
      tbody._bound = true;
      tbody.addEventListener('click', async (e) => {
        const t = e.target;
        if (!(t instanceof HTMLElement)) return;
        const studentId = t.getAttribute('data-student-id');
        if (!studentId) return;
        if (t.classList.contains('btnAssignSave')) {
        const select = tbody.querySelector(`select.edit-stop-select[data-student-id="${studentId}"]`);
        const busStopId = select?.value || '';
        if (!busStopId) return alert('정류장을 선택하세요.');
        try {
          if (assignedMap.has(String(studentId))) {
            // PATCH
            const res = await fetch(`/api/schedules/${sid}/assignments/${studentId}`, {
              method: 'PATCH', headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify({ studentId, busStopId })
            });
            if (!res.ok) throw new Error(await res.text() || '수정 실패');
          } else {
            // POST (단건 as 배열)
            const res = await fetch(`/api/schedules/${sid}/assignments`, {
              method: 'POST', headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify([{ studentId, busStopId }])
            });
            if (!res.ok) throw new Error(await res.text() || '추가 실패');
          }
          // 재조회
          const dres = await fetch(`/api/schedules/${sid}`);
          if (dres.ok) { const det = await dres.json(); renderScheduleDetail(det); }
        } catch (err) { console.error(err); alert(err.message || '저장 실패'); }
      }
      if (t.classList.contains('btnAssignRemove')) {
        if (!assignedMap.has(String(studentId))) return alert('배정이 없습니다.');
        if (!confirm('이 학생 배정을 제거할까요?')) return;
        try {
          const res = await fetch(`/api/schedules/${sid}/assignments/${studentId}`, { method: 'DELETE' });
          if (!res.ok) throw new Error(await res.text() || '제거 실패');
          const dres = await fetch(`/api/schedules/${sid}`);
          if (dres.ok) { const det = await dres.json(); renderScheduleDetail(det); }
        } catch (err) { console.error(err); alert(err.message || '제거 실패'); }
      }
      });
    }

    // 4) 노선 변경: 버튼
    const changeBtn = document.getElementById('btnChangeRoute');
    if (changeBtn) changeBtn.onclick = async () => {
      const newRouteId = document.getElementById('editRouteSelect')?.value || '';
      if (!newRouteId) return alert('노선을 선택하세요.');
      if (!confirm('노선을 변경하면 모든 학생 배정이 초기화됩니다. 진행할까요?')) return;
      try {
        const res = await fetch(`/api/schedules/${sid}:change-route`, {
          method: 'POST', headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ routeId: newRouteId })
        });
        if (!res.ok) throw new Error(await res.text() || '노선 변경 실패');
        const det = await res.json();
        renderScheduleDetail(det);
      } catch (err) { console.error(err); alert(err.message || '노선 변경 실패'); }
    };
  }
})();

// 글로벌 삭제 핸들러(이벤트 바인딩은 내부에서 수행)
async function onDeleteSchedule(){
  try {
    const wrap = document.getElementById('scheduleDetail');
    const inputId = (document.getElementById('scheduleIdForView')?.value || '').trim();
    const sid = (wrap?.dataset?.scheduleId || inputId || '').trim();
    if (!sid) return alert('삭제할 스케줄 ID가 없습니다. 먼저 조회하세요.');
    if (!confirm('이 스케줄을 삭제할까요? 되돌릴 수 없습니다.')) return;
    const res = await fetch(`/api/schedules/${sid}`, { method: 'DELETE' });
    if (!res.ok) throw new Error(await res.text() || '삭제 실패');
    // UI 초기화
    document.getElementById('scheduleSummary').innerHTML = '';
    document.getElementById('scheduleAssignmentsBody').innerHTML = '';
    document.getElementById('scheduleEmpty').style.display = 'block';
    if (wrap) wrap.dataset.scheduleId = '';
    alert('삭제되었습니다.');
  } catch (e) {
    console.error(e);
    alert(e.message || '삭제 중 오류가 발생했습니다.');
  }
}
