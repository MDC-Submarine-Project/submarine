package dev.sebastianb.boids;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

/**
 * Handles cube controls and buoyancy/ballast physics.
 */
public class CubeController {

    private final btRigidBody cubeBody;
    private final float horizontalForce;
    private final float verticalForce;

    // Ballast system
    private float ballastLevel = 0f;     // 0 = empty (max buoyant), 1 = full (sinks)
    private final float maxBallast = 1f;
    private final float minBallast = 0f;

    public CubeController(btRigidBody cubeBody, float horizontalForce, float verticalForce) {
        this.cubeBody = cubeBody;
        this.horizontalForce = horizontalForce;
        this.verticalForce = verticalForce;
    }

    /**
     * Adds a click listener to the provided button to apply a control action.
     */
    public void addControl(TextButton button, String action) {
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                switch (action) {
                    case "up":
                        cubeBody.applyCentralImpulse(new Vector3(0, 0, -horizontalForce));
                        break;
                    case "down":
                        cubeBody.applyCentralImpulse(new Vector3(0, 0, horizontalForce));
                        break;
                    case "left":
                        cubeBody.applyCentralImpulse(new Vector3(-horizontalForce, 0, 0));
                        break;
                    case "right":
                        cubeBody.applyCentralImpulse(new Vector3(horizontalForce, 0, 0));
                        break;
                    case "dive":
                        cubeBody.applyCentralImpulse(new Vector3(0, -verticalForce, 0));
                        break;
                    case "surface":
                        cubeBody.applyCentralImpulse(new Vector3(0, verticalForce, 0));
                        break;
                    case "ballastUp":
                        adjustBallast(0.1f);
                        break;
                    case "ballastDown":
                        adjustBallast(-0.1f);
                        break;
                }
            }
        });
    }

    /**
     * Adjusts ballast level. Higher ballast means less buoyant (sinks more).
     */
    private void adjustBallast(float delta) {
        ballastLevel = Math.max(minBallast, Math.min(maxBallast, ballastLevel + delta));
        System.out.println("Ballast Level: " + ballastLevel);
    }

    /**
     * Simulates buoyancy based on cube position and ballast.
     * @param waterLevel - the y-coordinate of the ocean surface (0f = surface)
     */
    public void applyBuoyancy(float waterLevel) {
        Vector3 pos = cubeBody.getWorldTransform().getTranslation(new Vector3());
        Vector3 velocity = cubeBody.getLinearVelocity();

        // Neutral buoyancy force: counteracts gravity near the surface
        float depth = waterLevel - pos.y;

        // Basic buoyancy calculation: stronger when submerged, weaker when full ballast
        float buoyancyStrength = 15f * (1f - ballastLevel);

        // Apply upward force if below surface
        if (pos.y < waterLevel) {
            float upwardForce = buoyancyStrength * (1f - ballastLevel) - (depth * 0.2f);
            cubeBody.applyCentralForce(new Vector3(0, upwardForce, 0));
        }

        // Apply gentle damping to simulate water drag
        Vector3 drag = velocity.cpy().scl(-1.5f);
        cubeBody.applyCentralForce(drag);
    }
}
