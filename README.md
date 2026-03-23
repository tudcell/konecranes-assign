# Konecranes AI Vehicle Simulation

A Java 11 implementation of the Konecranes case with:
- independent vehicle processes
- asynchronous inter-process communication over TCP
- multi-threaded simulation per vehicle
- web UI for vehicle creation, control, and visualization
- AI-inspired predictive collision avoidance

## Architecture

### Coordinator process
Spring Boot application responsible for:
- serving the web UI and REST API
- accepting vehicle TCP connections
- maintaining global vehicle registry
- broadcasting environment updates to vehicles
- exposing live snapshots over Server-Sent Events
- spawning additional vehicle JVM processes

### Vehicle process
Each vehicle runs as its own JVM process and contains:
- movement thread
- AI decision thread
- outbound publishing thread
- inbound gateway listener loop

### Communication protocol
Line-delimited JSON messages over TCP.
Message types:
- `REGISTER`
- `REGISTER_ACK`
- `STATE_UPDATE`
- `ENVIRONMENT_UPDATE`
- `CONTROL_COMMAND`
- `DISCONNECT`

## AI collision handling
The AI layer is intentionally pragmatic and explainable:
1. predict short-horizon trajectories
2. score pairwise risk using current distance, projected minimum distance, closing velocity, and heading convergence
3. evaluate candidate maneuvers
4. choose the maneuver with lowest predicted risk

Candidate actions:
- keep course
- turn left
- turn right
- slow down
- emergency stop

## SOLID-oriented design
- `VehicleRegistry`: single responsibility for in-memory state storage
- `VehicleConnectionManager`: single responsibility for outbound gateway writes
- `VehicleSessionService`: registration and lifecycle orchestration
- `VehicleUpdateService`: state update handling
- `AvoidanceDecisionEngine` and `RiskEstimator`: AI logic isolated from transport and UI
- `VehicleSpawnerService`: process bootstrapping isolated from coordination logic

## Run

### 1. Build
Use a JDK 11+ environment with Maven:

```bash
mvn clean package
```

### 2. Start coordinator
```bash
java -jar target/konecranes-ai-sim-1.0.0.jar
```

### 3. Open UI
Navigate to:

```text
http://localhost:8080
```

### 4. Spawn vehicles
Use the UI or call:

```bash
curl -X POST http://localhost:8080/api/vehicles/spawn \
  -H 'Content-Type: application/json' \
  -d '{"count":3}'
```

## Manual control APIs

### Override direction
```bash
curl -X POST http://localhost:8080/api/vehicles/VH-XXXX/direction \
  -H 'Content-Type: application/json' \
  -d '{"directionDeg":180}'
```

### Override speed
```bash
curl -X POST http://localhost:8080/api/vehicles/VH-XXXX/speed \
  -H 'Content-Type: application/json' \
  -d '{"speed":30}'
```

## Notes
- `simulation.vehicle.jarPath` points to the packaged application jar and is used by the coordinator to spawn new vehicle processes.
- Vehicles use boundary bounce behavior to stay inside the world.
- Manual commands place the vehicle in `USER_OVERRIDE` action mode.

## Suggested next improvements
- replace raw TCP with Netty or gRPC for stronger transport concerns
- add persistence for session replay and analytics
- add integration tests for transport and AI decision logic
- add graceful process shutdown and heartbeat timeouts
