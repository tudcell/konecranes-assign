const canvas = document.getElementById('simCanvas');
const ctx = canvas.getContext('2d');
const vehicleSelect = document.getElementById('vehicleSelect');
let snapshot = { vehicles: [], world: { width: 1000, height: 700 }, collisionWarnings: 0 };
let selectedVehicleId = '';
let lastVehicleIdsKey = '';

async function fetchSnapshot() {
    const response = await fetch('/api/simulation/snapshot');
    snapshot = await response.json();
    renderSnapshot();
}

function connectStream() {
    const stream = new EventSource('/api/simulation/stream');
    stream.addEventListener('snapshot', (event) => {
        snapshot = JSON.parse(event.data);
        renderSnapshot();
    });
    stream.onerror = () => {
        stream.close();
        setTimeout(connectStream, 1000);
    };
}

function renderSnapshot() {
    canvas.width = snapshot.world.width;
    canvas.height = snapshot.world.height;
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    drawGrid();
    snapshot.vehicles.forEach(drawVehicle);
    syncVehicleOptions();

    document.getElementById('vehicleCount').textContent = snapshot.vehicles.length;
    document.getElementById('warningCount').textContent = snapshot.collisionWarnings;
    document.getElementById('updatedAt').textContent = new Date(snapshot.generatedAt || Date.now()).toLocaleTimeString();
}

function drawGrid() {
    ctx.save();
    ctx.strokeStyle = '#0f172a';
    ctx.lineWidth = 1;
    for (let x = 0; x <= canvas.width; x += 50) {
        ctx.beginPath();
        ctx.moveTo(x, 0);
        ctx.lineTo(x, canvas.height);
        ctx.stroke();
    }
    for (let y = 0; y <= canvas.height; y += 50) {
        ctx.beginPath();
        ctx.moveTo(0, y);
        ctx.lineTo(canvas.width, y);
        ctx.stroke();
    }
    ctx.restore();
}

function drawVehicle(vehicle) {
    const riskColor = vehicle.riskLevel === 'HIGH' ? '#ef4444' : vehicle.riskLevel === 'MEDIUM' ? '#f59e0b' : '#22c55e';
    ctx.save();
    ctx.translate(vehicle.x, vehicle.y);
    ctx.rotate(vehicle.directionDeg * Math.PI / 180);
    ctx.fillStyle = riskColor;
    ctx.beginPath();
    ctx.arc(0, 0, vehicle.radius, 0, Math.PI * 2);
    ctx.fill();

    ctx.strokeStyle = '#ffffff';
    ctx.lineWidth = 2;
    ctx.beginPath();
    ctx.moveTo(0, 0);
    ctx.lineTo(vehicle.radius + 10, 0);
    ctx.stroke();
    ctx.restore();

    ctx.fillStyle = '#e5e7eb';
    ctx.font = '12px Arial';
    ctx.fillText(`${vehicle.id} ${vehicle.currentAction}`, vehicle.x + vehicle.radius + 6, vehicle.y - vehicle.radius - 4);
}

function syncVehicleOptions() {
    const activeIds = snapshot.vehicles
        .filter(vehicle => vehicle.status !== 'DISCONNECTED')
        .map(vehicle => vehicle.id)
        .sort();
    const currentIdsKey = activeIds.join('|');

    // Do not mutate the select while the user is choosing an option.
    if (document.activeElement === vehicleSelect) {
        return;
    }

    if (currentIdsKey !== lastVehicleIdsKey) {
        vehicleSelect.innerHTML = '';
        activeIds.forEach(id => {
            const option = document.createElement('option');
            option.value = id;
            option.textContent = id;
            vehicleSelect.appendChild(option);
        });
        lastVehicleIdsKey = currentIdsKey;
    }

    if (selectedVehicleId && activeIds.includes(selectedVehicleId)) {
        vehicleSelect.value = selectedVehicleId;
        return;
    }

    if (activeIds.length > 0) {
        vehicleSelect.value = activeIds[0];
        selectedVehicleId = activeIds[0];
        return;
    }

    selectedVehicleId = '';
}

async function spawnVehicles() {
    const count = Number(document.getElementById('spawnCount').value);
    await fetch('/api/vehicles/spawn', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ count })
    });
}

async function sendDirection() {
    const vehicleId = vehicleSelect.value;
    if (!vehicleId) {
        return;
    }
    const directionDeg = Number(document.getElementById('directionInput').value);
    await fetch(`/api/vehicles/${vehicleId}/direction`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ directionDeg })
    });
}

async function sendSpeed() {
    const vehicleId = vehicleSelect.value;
    if (!vehicleId) {
        return;
    }
    const speed = Number(document.getElementById('speedInput').value);
    await fetch(`/api/vehicles/${vehicleId}/speed`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ speed })
    });
}

document.getElementById('spawnButton').addEventListener('click', spawnVehicles);
document.getElementById('directionButton').addEventListener('click', sendDirection);
document.getElementById('speedButton').addEventListener('click', sendSpeed);
vehicleSelect.addEventListener('change', () => {
    selectedVehicleId = vehicleSelect.value || '';
});
vehicleSelect.addEventListener('focus', () => {
    selectedVehicleId = vehicleSelect.value || selectedVehicleId;
});

fetchSnapshot();
connectStream();
