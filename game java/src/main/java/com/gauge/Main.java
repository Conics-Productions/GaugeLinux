package com.gauge;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Bounds;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main extends Application {

    private static final double WIDTH = 1000;
    private static final double HEIGHT = 700;

    // Déplacement
    private static final double SPEED = 4.0;

    // Physique
    private static final double GRAVITY = 0.5;
    private static final double JUMP_FORCE = -12.0;

    private final Set<KeyCode> keys = new HashSet<>();
    private final List<Box> obstacles = new ArrayList<>();

    // Position du joueur
    private double playerX = 0;
    private double playerZ = 0;

    // Vitesse verticale
    private double velocityY = 0;

    // Le joueur est-il au sol ?
    private boolean onGround = true;

    private Box player;
    private PerspectiveCamera camera;

    @Override
    public void start(Stage stage) {

        // =========================
        // MONDE 3D
        // =========================

        Group world = new Group();

        // =========================
        // SOL
        // =========================

        Box ground = new Box(1200, 20, 1200);

        PhongMaterial groundMaterial = new PhongMaterial();
        groundMaterial.setDiffuseColor(Color.DARKGREEN);
        groundMaterial.setSpecularColor(Color.GREEN);

        ground.setMaterial(groundMaterial);
        ground.setTranslateY(50);

        world.getChildren().add(ground);

        // =========================
        // JOUEUR
        // =========================

        player = new Box(50, 70, 50);

        PhongMaterial playerMaterial = new PhongMaterial();
        playerMaterial.setDiffuseColor(Color.LIMEGREEN);
        playerMaterial.setSpecularColor(Color.WHITE);

        player.setMaterial(playerMaterial);

        player.setTranslateX(playerX);
        player.setTranslateY(5);
        player.setTranslateZ(playerZ);

        world.getChildren().add(player);

        // =========================
        // BLOCS
        // =========================

        addBlock(
                world,
                -250, 0, 150,
                100, 100, 100,
                Color.GRAY
        );

        addBlock(
                world,
                250, 0, 150,
                100, 100, 100,
                Color.GRAY
        );

        addBlock(
                world,
                -200, 0, 350,
                150, 150, 150,
                Color.DARKSLATEGRAY
        );

        addBlock(
                world,
                200, 0, 350,
                150, 150, 150,
                Color.DARKSLATEGRAY
        );

        // =========================
        // LUMIÈRE
        // =========================

        AmbientLight ambientLight =
                new AmbientLight(Color.color(0.45, 0.45, 0.45));

        PointLight sun = new PointLight(Color.WHITE);

        sun.setTranslateY(-300);
        sun.setTranslateZ(-300);

        world.getChildren().addAll(
                ambientLight,
                sun
        );

        // =========================
        // CAMÉRA
        // =========================

        camera = new PerspectiveCamera(true);

        camera.setNearClip(0.1);
        camera.setFarClip(3000);

        camera.setTranslateX(playerX);
        camera.setTranslateY(-300);
        camera.setTranslateZ(playerZ - 600);

        camera.setRotationAxis(Rotate.X_AXIS);
        camera.setRotate(-25);

        // =========================
        // SCÈNE
        // =========================

        Scene scene = new Scene(
                world,
                WIDTH,
                HEIGHT,
                true,
                SceneAntialiasing.BALANCED
        );

        scene.setFill(Color.SKYBLUE);
        scene.setCamera(camera);

        // =========================
        // CLAVIER
        // =========================

        scene.setOnKeyPressed(event -> {

            keys.add(event.getCode());

            // SAUT
            if (event.getCode() == KeyCode.SPACE && onGround) {

                velocityY = JUMP_FORCE;
                onGround = false;
            }
        });

        scene.setOnKeyReleased(event -> {
            keys.remove(event.getCode());
        });

        // =========================
        // FENÊTRE
        // =========================

        stage.setTitle("Gauge");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

        world.setFocusTraversable(true);
        world.requestFocus();

        // =========================
        // GAME LOOP
        // =========================

        startGameLoop();
    }

    // =========================
    // CRÉER UN BLOC
    // =========================

    private void addBlock(
            Group world,
            double x,
            double y,
            double z,
            double width,
            double height,
            double depth,
            Color color
    ) {

        Box block = new Box(
                width,
                height,
                depth
        );

        PhongMaterial material =
                new PhongMaterial();

        material.setDiffuseColor(color);

        block.setMaterial(material);

        block.setTranslateX(x);
        block.setTranslateY(y);
        block.setTranslateZ(z);

        world.getChildren().add(block);

        // Le bloc est solide
        obstacles.add(block);
    }

    // =========================
    // GAME LOOP
    // =========================

    private void startGameLoop() {

        AnimationTimer gameLoop =
                new AnimationTimer() {

            @Override
            public void handle(long now) {

                update();
            }
        };

        gameLoop.start();
    }

    // =========================
    // UPDATE
    // =========================

    private void update() {

        // Ancienne position
        double oldX = playerX;
        double oldZ = playerZ;

        // =========================
        // DÉPLACEMENT HORIZONTAL
        // =========================

        if (keys.contains(KeyCode.W)
                || keys.contains(KeyCode.Z)) {

            playerZ += SPEED;
        }

        if (keys.contains(KeyCode.S)) {

            playerZ -= SPEED;
        }

        if (keys.contains(KeyCode.A)
                || keys.contains(KeyCode.Q)) {

            playerX -= SPEED;
        }

        if (keys.contains(KeyCode.D)) {

            playerX += SPEED;
        }

        // Appliquer la nouvelle position horizontale
        player.setTranslateX(playerX);
        player.setTranslateZ(playerZ);

        // =========================
        // COLLISION HORIZONTALE
        // =========================

        for (Box obstacle : obstacles) {

            if (Collision.intersects(player, obstacle)) {

                playerX = oldX;
                playerZ = oldZ;

                player.setTranslateX(playerX);
                player.setTranslateZ(playerZ);

                break;
            }
        }

        // =========================
        // GRAVITÉ
        // =========================

        velocityY += GRAVITY;

        double newY =
                player.getTranslateY() + velocityY;

        player.setTranslateY(newY);

        onGround = false;

        // =========================
        // COLLISION VERTICALE
        // =========================

        for (Box obstacle : obstacles) {

            if (Collision.intersects(player, obstacle)) {

                Bounds obstacleBounds =
                        obstacle.getBoundsInParent();

                Bounds playerBounds =
                        player.getBoundsInParent();

                // Le joueur tombe sur le bloc
                if (velocityY > 0) {

                    double playerHalfHeight =
                            player.getHeight() / 2.0;

                    double newPlayerY =
                            obstacleBounds.getMinY()
                            - playerHalfHeight;

                    player.setTranslateY(newPlayerY);

                    velocityY = 0;
                    onGround = true;
                }

                // Le joueur frappe le dessous
                else if (velocityY < 0) {

                    double playerHalfHeight =
                            player.getHeight() / 2.0;

                    double newPlayerY =
                            obstacleBounds.getMaxY()
                            + playerHalfHeight;

                    player.setTranslateY(newPlayerY);

                    velocityY = 0;
                }

                break;
            }
        }

        // =========================
        // COLLISION AVEC LE SOL
        // =========================

        double groundY = 5;

        if (player.getTranslateY() >= groundY) {

            player.setTranslateY(groundY);

            velocityY = 0;
            onGround = true;
        }

        // =========================
        // CAMÉRA
        // =========================

        camera.setTranslateX(playerX);
        camera.setTranslateZ(playerZ - 600);
    }

    // =========================
    // MAIN
    // =========================

    public static void main(String[] args) {

        launch(args);
    }
}