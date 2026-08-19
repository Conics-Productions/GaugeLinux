package com.gauge;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main extends Application {

    // ==================================================
    // FENÊTRE
    // ==================================================

    private static final double WIDTH = 1000;
    private static final double HEIGHT = 700;

    // ==================================================
    // JOUEUR
    // ==================================================

    private static final double SPEED = 4.0;
    private static final double GRAVITY = 0.5;
    private static final double JUMP_FORCE = -12.0;

    private double playerX = 0;
    private double playerZ = 0;

    private double velocityY = 0;

    private boolean onGround = true;

    private Box player;

    // ==================================================
    // CAMÉRA
    // ==================================================

    private PerspectiveCamera camera;

    // ==================================================
    // TOUCHES
    // ==================================================

    private final Set<KeyCode> keys = new HashSet<>();

    // ==================================================
    // COLLISIONS
    // ==================================================

    private final List<Box> obstacles = new ArrayList<>();

    // ==================================================
    // PORTAIL
    // ==================================================

    private Box bluePortal;

    // ==================================================
    // MONDE 3D
    // ==================================================

    private Group world;

    private SubScene game3D;

    // ==================================================
    // INTERFACE
    // ==================================================

    private StackPane root;

    private HBox pauseMenu;

    private boolean paused = false;

    // ==================================================
    // START
    // ==================================================

    @Override
    public void start(Stage stage) {

        // ==================================================
        // MONDE
        // ==================================================

        world = new Group();

        // ==================================================
        // SOL
        // ==================================================

        Box ground = new Box(
                1200,
                20,
                1200
        );

        PhongMaterial groundMaterial =
                new PhongMaterial();

        groundMaterial.setDiffuseColor(
                Color.DARKGREEN
        );

        groundMaterial.setSpecularColor(
                Color.GREEN
        );

        ground.setMaterial(
                groundMaterial
        );

        ground.setTranslateY(50);

        world.getChildren().add(ground);

        // ==================================================
        // JOUEUR
        // ==================================================

        player = new Box(
                50,
                70,
                50
        );

        PhongMaterial playerMaterial =
                new PhongMaterial();

        playerMaterial.setDiffuseColor(
                Color.LIMEGREEN
        );

        playerMaterial.setSpecularColor(
                Color.WHITE
        );

        player.setMaterial(
                playerMaterial
        );

        player.setTranslateX(playerX);
        player.setTranslateY(5);
        player.setTranslateZ(playerZ);

        world.getChildren().add(player);

        // ==================================================
        // BLOCS
        // ==================================================

        addBlock(
                -250,
                0,
                150,
                100,
                100,
                100,
                Color.GRAY
        );

        addBlock(
                250,
                0,
                150,
                100,
                100,
                100,
                Color.GRAY
        );

        addBlock(
                -200,
                0,
                350,
                150,
                150,
                150,
                Color.DARKSLATEGRAY
        );

        addBlock(
                200,
                0,
                350,
                150,
                150,
                150,
                Color.DARKSLATEGRAY
        );

        // ==================================================
        // LUMIÈRES
        // ==================================================

        AmbientLight ambientLight =
                new AmbientLight(
                        Color.color(
                                0.45,
                                0.45,
                                0.45
                        )
                );

        PointLight sun =
                new PointLight(Color.WHITE);

        sun.setTranslateY(-300);
        sun.setTranslateZ(-300);

        world.getChildren().addAll(
                ambientLight,
                sun
        );

        // ==================================================
        // CAMÉRA
        // ==================================================

        camera =
                new PerspectiveCamera(true);

        camera.setNearClip(0.1);
        camera.setFarClip(3000);

        camera.setTranslateX(playerX);
        camera.setTranslateY(-300);
        camera.setTranslateZ(playerZ - 600);

        camera.setRotationAxis(
                Rotate.X_AXIS
        );

        camera.setRotate(-25);

        // ==================================================
        // SUBSCENE 3D
        // ==================================================

        game3D = new SubScene(
                world,
                WIDTH,
                HEIGHT,
                true,
               javafx.scene.SceneAntialiasing.BALANCED
        );

        game3D.setFill(
                Color.SKYBLUE
        );

        game3D.setCamera(
                camera
        );

        // ==================================================
        // ROOT
        // ==================================================

        root = new StackPane();

        root.setPrefSize(
                WIDTH,
                HEIGHT
        );

        root.getChildren().add(
                game3D
        );

        // ==================================================
        // MENU
        // ==================================================

        createPauseMenu();

        // ==================================================
        // SCÈNE
        // ==================================================

        Scene scene = new Scene(
                root,
                WIDTH,
                HEIGHT
        );

        // ==================================================
        // CLAVIER
        // ==================================================

        scene.setOnKeyPressed(event -> {

            if (event.getCode() == KeyCode.ESCAPE) {

                togglePause();

                return;
            }

            if (paused) {
                return;
            }

            keys.add(
                    event.getCode()
            );

            // SAUT

            if (event.getCode() == KeyCode.SPACE
                    && onGround) {

                velocityY =
                        JUMP_FORCE;

                onGround = false;
            }
        });

        scene.setOnKeyReleased(event -> {

            keys.remove(
                    event.getCode()
            );
        });

        // ==================================================
        // SOURIS
        // ==================================================

        game3D.setOnMousePressed(event -> {

            if (paused) {
                return;
            }

            if (event.getButton()
                    == MouseButton.PRIMARY) {

                shootBluePortal();
            }
        });

        // ==================================================
        // FENÊTRE
        // ==================================================

        stage.setTitle(
                "Gauge"
        );

        stage.setScene(
                scene
        );

        stage.setResizable(
                false
        );

        stage.show();

        // ==================================================
        // FOCUS
        // ==================================================

        root.setFocusTraversable(
                true
        );

        root.requestFocus();

        // ==================================================
        // GAME LOOP
        // ==================================================

        startGameLoop();
    }

    // ==================================================
    // MENU ESC
    // ==================================================

    private void createPauseMenu() {

        pauseMenu = new HBox();

        pauseMenu.setAlignment(
                Pos.CENTER
        );

        pauseMenu.setSpacing(
                180
        );

        pauseMenu.setPrefSize(
                WIDTH,
                HEIGHT
        );

        pauseMenu.setStyle(
                """
                -fx-background-color:
                    rgba(0, 0, 0, 0.55);
                """
        );

        // ==================================================
        // GAUGE À GAUCHE
        // ==================================================

        VBox titleBox =
                new VBox();

        titleBox.setAlignment(
                Pos.CENTER_LEFT
        );

        titleBox.setPrefWidth(
                380
        );

        Label title =
                new Label(
                        "GAUGE"
                );

        title.setStyle(
                """
                -fx-text-fill: white;
                -fx-font-size: 64px;
                -fx-font-weight: bold;
                """
        );

        Label pausedText =
                new Label(
                        "GAME PAUSED"
                );

        pausedText.setStyle(
                """
                -fx-text-fill:
                    rgba(255,255,255,0.55);

                -fx-font-size: 15px;
                """
        );

        titleBox.getChildren().addAll(
                title,
                pausedText
        );

        // ==================================================
        // OPTIONS À DROITE
        // ==================================================

        VBox buttons =
                new VBox();

        buttons.setAlignment(
                Pos.CENTER_LEFT
        );

        buttons.setSpacing(
                14
        );

        buttons.setPrefWidth(
                260
        );

        // RESUME

        Button resume =
                createMenuButton(
                        "RESUME"
                );

        resume.setOnAction(event -> {

            setPaused(false);
        });

        // SETTINGS

        Button settings =
                createMenuButton(
                        "SETTINGS"
                );

        settings.setOnAction(event -> {

            System.out.println(
                    "Settings - bientôt..."
            );
        });

        // QUIT

        Button quit =
                createMenuButton(
                        "QUIT"
                );

        quit.setOnAction(event -> {

            System.exit(0);
        });

        buttons.getChildren().addAll(
                resume,
                settings,
                quit
        );

        // ==================================================
        // AJOUT
        // ==================================================

        pauseMenu.getChildren().addAll(
                titleBox,
                buttons
        );

        // ==================================================
        // CACHÉ AU DÉPART
        // ==================================================

        pauseMenu.setVisible(
                false
        );

        root.getChildren().add(
                pauseMenu
        );
    }

    // ==================================================
    // BOUTON
    // ==================================================

    private Button createMenuButton(
            String text
    ) {

        Button button =
                new Button(text);

        button.setPrefWidth(
                250
        );

        button.setPrefHeight(
                52
        );

        button.setAlignment(
                Pos.CENTER_LEFT
        );

        button.setStyle(
                """
                -fx-background-color:
                    transparent;

                -fx-text-fill: white;

                -fx-font-size: 19px;

                -fx-font-weight: bold;

                -fx-padding:
                    10 20 10 20;

                -fx-cursor: hand;
                """
        );

        // Effet quand la souris passe dessus

        button.setOnMouseEntered(event -> {

            button.setStyle(
                    """
                    -fx-background-color:
                        rgba(255,255,255,0.12);

                    -fx-text-fill: white;

                    -fx-font-size: 19px;

                    -fx-font-weight: bold;

                    -fx-padding:
                        10 20 10 20;

                    -fx-cursor: hand;
                    """
            );
        });

        button.setOnMouseExited(event -> {

            button.setStyle(
                    """
                    -fx-background-color:
                        transparent;

                    -fx-text-fill: white;

                    -fx-font-size: 19px;

                    -fx-font-weight: bold;

                    -fx-padding:
                        10 20 10 20;

                    -fx-cursor: hand;
                    """
            );
        });

        return button;
    }

    // ==================================================
    // PAUSE
    // ==================================================

    private void togglePause() {

        setPaused(
                !paused
        );
    }

    private void setPaused(
            boolean value
    ) {

        paused = value;

        pauseMenu.setVisible(
                paused
        );

        keys.clear();

        // ==================================================
        // FLOU
        // ==================================================

        if (paused) {

            GaussianBlur blur =
                    new GaussianBlur(12);

            game3D.setEffect(
                    blur
            );

        } else {

            game3D.setEffect(
                    null
            );

            root.requestFocus();
        }
    }

    // ==================================================
    // BLOC
    // ==================================================

    private void addBlock(
            double x,
            double y,
            double z,
            double width,
            double height,
            double depth,
            Color color
    ) {

        Box block =
                new Box(
                        width,
                        height,
                        depth
                );

        PhongMaterial material =
                new PhongMaterial();

        material.setDiffuseColor(
                color
        );

        block.setMaterial(
                material
        );

        block.setTranslateX(x);
        block.setTranslateY(y);
        block.setTranslateZ(z);

        world.getChildren().add(
                block
        );

        obstacles.add(
                block
        );
    }

    // ==================================================
    // PORTAIL BLEU
    // ==================================================

    private void shootBluePortal() {

        if (bluePortal != null) {

            world.getChildren().remove(
                    bluePortal
            );
        }

        bluePortal =
                new Box(
                        80,
                        120,
                        8
                );

        PhongMaterial portalMaterial =
                new PhongMaterial();

        portalMaterial.setDiffuseColor(
                Color.BLUE
        );

        portalMaterial.setSpecularColor(
                Color.CYAN
        );

        bluePortal.setMaterial(
                portalMaterial
        );

        bluePortal.setTranslateX(
                playerX
        );

        bluePortal.setTranslateY(
                player.getTranslateY()
        );

        bluePortal.setTranslateZ(
                playerZ + 120
        );

        world.getChildren().add(
                bluePortal
        );
    }

    // ==================================================
    // GAME LOOP
    // ==================================================

    private void startGameLoop() {

        AnimationTimer gameLoop =
                new AnimationTimer() {

            @Override
            public void handle(
                    long now
            ) {

                if (!paused) {

                    update();
                }
            }
        };

        gameLoop.start();
    }

    // ==================================================
    // UPDATE
    // ==================================================

    private void update() {

        double oldX =
                playerX;

        double oldZ =
                playerZ;

        // ==================================================
        // DÉPLACEMENT
        // ==================================================

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

        player.setTranslateX(
                playerX
        );

        player.setTranslateZ(
                playerZ
        );

        // ==================================================
        // COLLISION HORIZONTALE
        // ==================================================

        for (Box obstacle : obstacles) {

            if (Collision.intersects(
                    player,
                    obstacle
            )) {

                playerX =
                        oldX;

                playerZ =
                        oldZ;

                player.setTranslateX(
                        playerX
                );

                player.setTranslateZ(
                        playerZ
                );

                break;
            }
        }

        // ==================================================
        // GRAVITÉ
        // ==================================================

        velocityY +=
                GRAVITY;

        player.setTranslateY(
                player.getTranslateY()
                        + velocityY
        );

        onGround =
                false;

        // ==================================================
        // COLLISION VERTICALE
        // ==================================================

        for (Box obstacle : obstacles) {

            if (Collision.intersects(
                    player,
                    obstacle
            )) {

                var bounds =
                        obstacle.getBoundsInParent();

                // TOMBE

                if (velocityY > 0) {

                    double top =
                            bounds.getMinY();

                    double halfHeight =
                            player.getHeight()
                                    / 2.0;

                    player.setTranslateY(
                            top - halfHeight
                    );

                    velocityY = 0;

                    onGround = true;
                }

                // TÊTE

                else if (velocityY < 0) {

                    double bottom =
                            bounds.getMaxY();

                    double halfHeight =
                            player.getHeight()
                                    / 2.0;

                    player.setTranslateY(
                            bottom + halfHeight
                    );

                    velocityY = 0;
                }

                break;
            }
        }

        // ==================================================
        // SOL
        // ==================================================

        double groundY = 5;

        if (player.getTranslateY()
                >= groundY) {

            player.setTranslateY(
                    groundY
            );

            velocityY = 0;

            onGround = true;
        }

        // ==================================================
        // CAMÉRA
        // ==================================================

        camera.setTranslateX(
                playerX
        );

        camera.setTranslateZ(
                playerZ - 600
        );
    }

    // ==================================================
    // COLLISION
    // ==================================================

    private static class Collision {

        private static boolean intersects(
                Box a,
                Box b
        ) {

            return a.getBoundsInParent()
                    .intersects(
                            b.getBoundsInParent()
                    );
        }
    }

    // ==================================================
    // MAIN
    // ==================================================

    public static void main(
            String[] args
    ) {

        launch(args);
    }
}