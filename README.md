# GurionRock Robot SLAM in Java

A Java-based SLAM (Simultaneous Localization and Mapping) system for coordinating and processing data from multiple sensors in a robotic platform.  
Developed as part of the Systems Programming Lab at Ben-Gurion University.

---

## 📌 Overview

This project simulates a complete multi-sensor robotic system for SLAM, including modules for:

- Sensor event handling and messaging
- Parallel microservices for LIDAR, camera, GPS+IMU
- Object detection and tracking
- Fusion-based pose estimation
- Fault handling and system termination

The system is built using a **microservice architecture** and supports an **event-driven model** for communication via a custom message bus.

---

## 🧩 Architecture

The system is structured around the following key components:

- **MicroService Framework** — base classes for message-based microservices
- **Sensors**:
  - `CameraService`
  - `LiDarService`
  - `PoseService` (GPS+IMU fusion)
- **Fusion Module**:
  - `FusionSlamService` — fuses sensor data for accurate pose
- **Message Types** — events and broadcasts for internal communication
- **Central Orchestrator** — `TimeService` for synchronized ticks

---

## 🗂️ Project Structure

```bash
src/
├── main/java/bgu/spl/mics/                # Messaging framework
├── main/java/bgu/spl/mics/application/    # Entry point and sensor services
├── main/java/bgu/spl/mics/application/objects  # Domain models (Pose, Output, etc.)
├── main/java/bgu/spl/mics/application/messages # Events & Broadcasts
├── test/java/                             # Unit tests
pom.xml                                    # Maven configuration
.gitignore


