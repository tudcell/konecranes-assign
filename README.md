# Konecranes AI Vehicle Simulation

A Java 11 simulation system built for the Konecranes assignment.

The application runs a coordinator process plus multiple independently spawned vehicle JVM processes. Vehicles communicate with the coordinator asynchronously over TCP, publish their state continuously, receive environment updates, and apply local AI-based collision avoidance and safety behavior.

---

## Table of contents

- [Run guide](#run-guide)
- [API examples](#api-examples)
- [Project description](#project-description)
- [Functionalities](#functionalities)
- [User flow](#user-flow)
- [Wire protocol](#wire-protocol)
- [Architecture](#architecture)
- [AI and safety behavior](#ai-and-safety-behavior)
- [Configuration](#configuration)
- [Project structure](#project-structure)

---

## Run guide

### Prerequisites

- Java 11 or newer (JDK)
- IntelliJ IDEA

### Build and run in IntelliJ

1. Open the project in IntelliJ IDEA.
2. Open the **Maven** tool window.
3. Under **Lifecycle**, run **clean**.
4. Then under **Lifecycle**, run **package**.
5. After the build finishes, run the `ApplicationLauncher` class:
   `com.example.konecranes.ApplicationLauncher`

### Open the web UI

After the application starts, open your browser and go to:

```
http://localhost:8080
```

---

## API examples

Spawn 3 vehicles:

```bash
curl -X POST http://localhost:8080/api/vehicles/spawn \
  -H "Content-Type: application/json" \
  -d '{"count":3}'
```

Override direction:

```bash
curl -X POST http://localhost:8080/api/vehicles/VH-XXXX/direction \
  -H "Content-Type: application/json" \
  -d '{"directionDeg":180}'
```

Override speed:

```bash
curl -X POST http://localhost:8080/api/vehicles/VH-XXXX/speed \
  -H "Content-Type: application/json" \
  -d '{"speed":30}'
```

Read the current simulation snapshot:

```bash
curl http://localhost:8080/api/simulation/snapshot
```

Open the live snapshot stream (SSE):

```
GET /api/simulation/stream
```

---

## Project description

This project is a Java 11 vehicle simulation built for the Konecranes assignment.

The system is split into two runtime roles:

### Coordinator process

The coordinator is the main Spring Boot application. It is responsible for:

- Serving the UI
- Exposing REST endpoints and SSE
- Accepting vehicle TCP connections
- Storing global vehicle state (in-memory registry)
- Broadcasting per-vehicle environment updates
- Spawning and managing child vehicle JVM processes

### Vehicle processes

Each vehicle runs as its own JVM process. A vehicle is responsible for:

- Maintaining its own runtime loop (ticks)
- Applying movement updates locally
- Receiving environment updates from the coordinator
- Receiving manual control commands
- Publishing `STATE_UPDATE` messages to the coordinator
- Running local AI-based decision logic and doing last-moment safety maneuvers

---

## Functionalities

Core features:

- Spawn multiple vehicles at runtime from the UI or API
- Manual control of vehicle direction and speed
- Live browser visualization of vehicle movement
- Server-Sent Events (SSE) snapshot streaming
- Local vehicle-side collision risk estimation and maneuver selection
- Each vehicle runs as an independent JVM process
- Asynchronous JSON-over-TCP wire protocol between coordinator and vehicles

Manual override behavior:

- Manual control (direction/speed) temporarily takes priority; AI resumes after the manual override window.

---

## User flow

1. User opens the web UI at `http://localhost:8080`.
2. User spawns one or more vehicles via UI or `POST /api/vehicles/spawn`.
3. Coordinator launches one child JVM process per vehicle (same jar with `--mode=vehicle` and args).
4. Each vehicle connects to the TCP gateway and sends a `REGISTER` message.
5. Coordinator replies with `REGISTER_ACK` and an initial `ENVIRONMENT_UPDATE`.
6. Vehicles start their local runtime loops (movement tick, AI/control tick, outbound state publish).
7. Coordinator receives `STATE_UPDATE` messages, updates the registry, and broadcasts environment updates.
8. Scheduler publishes snapshots; the UI receives them via SSE and re-renders.
9. User can send manual direction/speed commands; vehicles enter a `USER_OVERRIDE` window before AI resumes.

---

## Wire protocol

Coordinator and vehicles exchange line-delimited JSON `WireMessage` objects over TCP. Each message is structured as:

```json
{ "type": "REGISTER", "payload": {  } }
```

Common message types:

- `REGISTER`
- `REGISTER_ACK`
- `STATE_UPDATE`
- `ENVIRONMENT_UPDATE`
- `CONTROL_COMMAND`
- `DISCONNECT`

---

## Architecture

The codebase follows a layered (ports & adapters) structure.

Main layers:

- `adapter.in` — inbound delivery implementations (REST controllers, TCP gateway)
- `application` / use-cases — orchestration and business logic
- `application.port.in` — inbound ports (use-case contracts)
- `application.port.out` — outbound ports (dependency contracts)
- `adapter.out` — concrete outbound adapters (persistence, TCP messaging, process launcher)
- `vehicle` — runtime logic executed inside each spawned vehicle JVM


## AI and safety behavior

Vehicle-side logic is intentionally simple and explainable.

Decision flow per vehicle:

1. Receive nearby vehicle context
2. Estimate collision risk (pairwise predictions)
3. Simulate candidate actions (KEEP_COURSE, TURN_LEFT, TURN_RIGHT, SLOW_DOWN)
4. Select lowest-risk maneuver using a scoring/penalty system
5. Apply immediate safety behavior (soft brake or hard stop) if a collision is imminent

The safety engine is a last-moment override that applies braking factors and emergency maneuvers independent of the AI decision when necessary.

---

## Configuration

Runtime settings live in `src/main/resources/application.yml` (namespace: `simulation`). The Spring bean `com.example.konecranes.config.SimulationProperties` binds these values.

Important configuration areas:

- World size (`simulation.world.*`)
- TCP gateway (`simulation.gateway.*`)
- Scheduler cadence (`simulation.scheduler.*`)
- Vehicle defaults and tuning (`simulation.vehicle.*` and `simulation.vehicle.tuning.*`)

**Important**: `simulation.vehicle.jarPath` must point to the packaged application JAR so the coordinator can spawn child vehicle processes.

---

## Project structure

```
src/main/java/com/example/konecranes
├── adapter
│   ├── in
│   │   ├── rest
│   │   └── tcp
│   └── out
│       ├── persistence
│       ├── process
│       └── tcp
├── application
│   ├── port
│   │   ├── in
│   │   └── out
├── ai
├── config
├── messaging
├── model
└── vehicle
```

---
