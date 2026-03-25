# Konecranes AI Vehicle Simulation

## 1) Run Guide

### Prerequisites
- Java 11+
- Maven

### Build
```bash
mvn clean package
```

### Start the coordinator process
```bash
java -jar target/konecranes-ai-sim-1.0.0.jar
```

### Or run directly from IDE
You can also run `ApplicationLauncher` directly (no manual args needed for coordinator mode):

- Main class: `com.example.konecranes.ApplicationLauncher`
- Mode selection: default is coordinator; vehicle mode is only used internally with `--mode=vehicle`

### Open the web UI
```text
http://localhost:8080
```

### Spawn vehicles
Use the UI controls, or call the API directly:

```bash
curl -X POST http://localhost:8080/api/vehicles/spawn \
  -H 'Content-Type: application/json' \
  -d '{"count":3}'
```

### Send manual control
Override direction:

```bash
curl -X POST http://localhost:8080/api/vehicles/VH-XXXX/direction \
  -H 'Content-Type: application/json' \
  -d '{"directionDeg":180}'
```

Override speed:

```bash
curl -X POST http://localhost:8080/api/vehicles/VH-XXXX/speed \
  -H 'Content-Type: application/json' \
  -d '{"speed":30}'
```

## 2) Project Description and Functionalities

This project is a Java 11 simulation for my Konecranes assignment. It runs one coordinator process and multiple independently spawned vehicle JVM processes that communicate asynchronously over TCP.

### Core functionalities
- Spawn 1..N (N=25 currently, but it can be changed) vehicles at runtime from the UI/API
- Manually control each vehicle direction and speed
- Visualize live vehicle movement in a browser UI
- Stream live world snapshots via SSE (`/api/simulation/stream`)
- Run AI-based collision risk estimation and maneuver selection

### Coordinator responsibilities
- Serves UI and REST APIs
- Accepts and manages vehicle TCP sessions
- Stores global vehicle state in registry
- Broadcasts per-vehicle environment updates
- Publishes simulation snapshots to SSE subscribers
- Spawns and owns child vehicle processes

### Vehicle process responsibilities
- Performs motion ticks and AI/control ticks
- Publishes `STATE_UPDATE` messages to coordinator
- Applies inbound `ENVIRONMENT_UPDATE` and `CONTROL_COMMAND`
- Handles immediate safety maneuvers locally

### Wire protocol
Line-delimited JSON `WireMessage{type,payload}` over TCP with message types:
- `REGISTER`
- `REGISTER_ACK`
- `STATE_UPDATE`
- `ENVIRONMENT_UPDATE`
- `CONTROL_COMMAND`
- `DISCONNECT`

## 3) User Flow

1. User opens UI at `http://localhost:8080`.
2. User spawns vehicles from UI (or `POST /api/vehicles/spawn`).
3. Coordinator launches child JVMs (`--mode=vehicle`) for each vehicle.
4. Each vehicle connects to TCP gateway and sends `REGISTER`.
5. Coordinator responds with `REGISTER_ACK` + first `ENVIRONMENT_UPDATE`.
6. Vehicle loops start (movement, AI/control, outbound state publish).
7. Coordinator receives `STATE_UPDATE`, updates registry, and broadcasts environment.
8. Scheduler publishes snapshots; UI receives updates through SSE and re-renders.
9. User sends manual direction/speed commands; vehicle enters `USER_OVERRIDE` window, then AI resumes.

## 4) Architecture Diagram

```text
+----------------------+        +-----------------------------------+
| User / Browser       |<------>| Static UI (app.js)               |
+----------------------+        +----------------+------------------+
                                                 | REST/SSE
                                                 v
                          +------------------------------------------+
                          | adapter.in.rest                          |
                          | - VehicleController                      |
                          | - SimulationController                   |
                          +----------------+-------------------------+
                                           |
                                           v
                          +------------------------------------------+
                          | application.port.in (Inbound Ports)      |
                          +----------------+-------------------------+
                                           |
                                           v
                          +------------------------------------------+
                          | application (Use Cases / Services)       |
                          | - spawn, command, session, snapshot      |
                          +----------------+-------------------------+
                                           |
                                           v
                          +------------------------------------------+
                          | application.port.out (Outbound Ports)    |
                          +----------------+-------------------------+
                             |                 |                  |
                             v                 v                  v
                  +------------------+  +------------------+  +-------------------+
                  | adapter.out.     |  | adapter.out.tcp  |  | adapter.out.      |
                  | persistence      |  | VehicleConnection|  | process           |
                  | VehicleRegistry  |  | Manager          |  | Jvm...Launcher    |
                  +------------------+  +---------+--------+  +---------+---------+
                                               TCP |                     |
                                                   v                     v
                                   +---------------------------+   +------------------+
                                   | adapter.in.tcp            |   | Vehicle JVMs     |
                                   | VehicleGatewayServer      |<->| vehicle.* (N)    |
                                   | VehicleSessionHandler     |   | --mode=vehicle   |
                                   +---------------------------+   +------------------+
```

## Notes
- Runtime knobs are in `src/main/resources/application.yml` (`simulation.*`).
- `simulation.vehicle.jarPath` must point to the packaged jar so coordinator can spawn vehicles.
