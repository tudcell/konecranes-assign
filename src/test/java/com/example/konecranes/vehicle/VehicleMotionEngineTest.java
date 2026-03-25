package com.example.konecranes.vehicle;
import com.example.konecranes.model.VehicleState;
import com.example.konecranes.model.VehicleStatus;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
class VehicleMotionEngineTest {
    @Test
    void rotateTowardsTargetRespectsMaxTurnPerTick() {
        VehicleProcessConfig config = configWith("--maxTurnDegPerTick=12");
        VehicleMotionEngine engine = new VehicleMotionEngine(config, 90.0);
        VehicleState state = state("v1", 100, 100, 0, 40);
        engine.rotateTowardsTarget(state);
        assertEquals(12.0, state.getDirectionDeg(), 0.0001);
    }
    @Test
    void rotateTowardsTargetUsesShortestPathAcrossZero() {
        VehicleProcessConfig config = configWith("--maxTurnDegPerTick=8");
        VehicleMotionEngine engine = new VehicleMotionEngine(config, 10.0);
        VehicleState state = state("v2", 100, 100, 350, 40);
        engine.rotateTowardsTarget(state);
        assertEquals(358.0, state.getDirectionDeg(), 0.0001);
    }
    @Test
    void bounceIfNeededReflectsDirectionAndRecoversFromStop() {
        VehicleProcessConfig config = configWith("--worldWidth=500", "--worldHeight=400", "--initialSpeed=55");
        VehicleMotionEngine engine = new VehicleMotionEngine(config, 30.0);
        VehicleState state = state("v3", 10, 200, 30, 0);
        engine.bounceIfNeeded(state);
        assertEquals(16.0, state.getX(), 0.0001);
        assertEquals(150.0, state.getDirectionDeg(), 0.0001);
        assertEquals(55.0, state.getSpeed(), 0.0001);
        assertEquals(VehicleStatus.ACTIVE, state.getStatus());
        assertEquals(150.0, engine.getTargetDirection(), 0.0001);
    }
    @Test
    void bounceIfNeededOnTopBoundaryReflectsVerticalDirection() {
        VehicleProcessConfig config = configWith("--worldWidth=500", "--worldHeight=400");
        VehicleMotionEngine engine = new VehicleMotionEngine(config, 300.0);
        VehicleState state = state("v4", 200, 5, 300, 10);
        engine.bounceIfNeeded(state);
        assertEquals(16.0, state.getY(), 0.0001);
        assertEquals(60.0, state.getDirectionDeg(), 0.0001);
    }
    private VehicleProcessConfig configWith(String... extraArgs) {
        String[] args = new String[1 + extraArgs.length];
        args[0] = "--vehicleId=test-vehicle";
        System.arraycopy(extraArgs, 0, args, 1, extraArgs.length);
        return VehicleProcessConfig.fromArgs(args);
    }
    private VehicleState state(String id, double x, double y, double directionDeg, double speed) {
        VehicleState state = new VehicleState();
        state.setId(id);
        state.setX(x);
        state.setY(y);
        state.setDirectionDeg(directionDeg);
        state.setSpeed(speed);
        state.setRadius(16.0);
        return state;
    }
}
