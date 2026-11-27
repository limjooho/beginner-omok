package beginner; // 3D 보드를 그리는 클래스이다.

import javafx.animation.Interpolator; // 애니메이션의 속도 변화를 정한다.
import javafx.animation.KeyFrame; // 애니메이션의 시간 지점을 나타낸다.
import javafx.animation.KeyValue; // 해당 시간에 도달할 값을 나타낸다.
import javafx.animation.Timeline; // 여러 시간 지점을 연결해 움직임을 만든다.
import javafx.geometry.Bounds; // 회전한 보드가 차지하는 크기를 확인한다.
import javafx.geometry.Point3D; // 마우스로 선택한 3D 위치를 나타낸다.
import javafx.scene.AmbientLight; // 전체 보드를 밝게 비춘다.
import javafx.scene.Group; // 여러 3D 도형을 한 묶음으로 만든다.
import javafx.scene.Node; // 선택한 도형을 저장하는 공통 타입이다.
import javafx.scene.PerspectiveCamera; // 멀리 있는 물체를 작게 보여 주는 카메라이다.
import javafx.scene.SceneAntialiasing; // 도형 가장자리를 부드럽게 표시한다.
import javafx.scene.SubScene; // 일반 화면 안에 3D 화면을 넣는다.
import javafx.scene.input.MouseButton; // 마우스의 왼쪽 버튼인지 확인한다.
import javafx.scene.input.MouseEvent; // 마우스의 위치와 선택 결과를 받는다.
import javafx.scene.layout.StackPane; // 3D 화면을 창 크기에 맞게 배치한다.
import javafx.scene.paint.Color; // 도형의 색을 지정한다.
import javafx.scene.paint.PhongMaterial; // 3D 도형의 표면 색을 지정한다.
import javafx.scene.shape.Box; // 나무판과 격자선을 만든다.
import javafx.scene.shape.CullFace; // 승리 표시의 안쪽 면도 보이게 한다.
import javafx.scene.shape.DrawMode; // 승리 표시를 선으로 그린다.
import javafx.scene.shape.Sphere; // 돌과 미리보기 구를 만든다.
import javafx.scene.transform.Rotate; // 보드를 회전시킨다.
import javafx.util.Duration; // 애니메이션 시간을 정한다.

// 화면 틀에서 사용하는 3D 보드이다. 게임 규칙은 OmokGame에 맡긴다.
public class BoardView extends StackPane { // 일반 화면 안에 3D 보드를 배치하는 영역이다.
    private final double CELL = 22; // 격자 사이의 거리이다.
    private final double RADIUS = 10; // 돌의 반지름이다.
    private OmokApp app; // 사용자가 클릭하면 화면 담당 클래스에 알려 준다.
    private OmokGame game; // 그려야 할 돌의 정보를 읽는다.
    private Group world = new Group(); // 나무판과 돌을 함께 회전시키는 묶음이다.
    private Group stones = new Group(); // 실제 돌들을 모아 둔다.
    private Group marks = new Group(); // 승리한 돌의 표시를 모아 둔다.
    private Rotate rotateX = new Rotate(36, Rotate.X_AXIS); // 위아래 회전이다.
    private Rotate rotateY = new Rotate(-18, Rotate.Y_AXIS); // 좌우 회전이다.
    private PerspectiveCamera camera = new PerspectiveCamera(true); // 3D 화면을 보는 카메라이다.
    private Box wood; // 클릭할 수 있는 나무판이다.
    private Sphere preview = new Sphere(RADIUS); // 다음 돌이 놓일 곳을 보여 준다.
    private SubScene scene3d; // 실제 3D 장면이다.
    private Timeline drop; // 현재 돌 낙하 애니메이션이다.
    private Timeline rotation; // 현재 시점 전환 애니메이션이다.
    private double pressX; // 누르기 시작한 가로 좌표이다.
    private double pressY; // 누르기 시작한 세로 좌표이다.
    private double startX; // 드래그 시작 때의 위아래 각도이다.
    private double startY; // 드래그 시작 때의 좌우 각도이다.
    private double zoom = 1; // 1은 기본 크기이고 작을수록 확대한다.
    private boolean dragged; // 회전 드래그를 착수 클릭과 구분한다.
    private int hoverX = -1; // 미리보기의 가로 칸이다. -1이면 선택하지 않은 상태이다.
    private int hoverZ = -1; // 미리보기의 앞뒤 칸이다.
    private boolean busy = false; // 돌이 떨어지는 동안 다른 착수를 잠시 막는다.

    public BoardView(OmokApp app, OmokGame game) { // 화면과 게임 정보를 받아 3D 보드를 만든다.
        setMinSize(0, 0); // 안쪽 3D 장면의 현재 크기보다 창을 작게 줄일 수 있게 한다.
        setPrefSize(900, 550); // 보드가 처음 사용할 기본 크기이다.
        this.app = app; // 화면 담당 객체를 저장한다.
        this.game = game; // 게임 규칙 객체를 저장한다.
        makeBoard(); // 나무판과 격자선을 만든다.
        world.getChildren().addAll(stones, marks, preview); // 돌과 표시를 보드에 올린다.
        world.getTransforms().addAll(rotateX, rotateY); // 두 방향의 회전을 적용한다.
        preview.setOpacity(0.35); // 미리보기 돌을 반투명하게 만든다.
        preview.setMouseTransparent(true); // 미리보기가 마우스 선택을 가로채지 않게 한다.
        preview.setVisible(false); // 처음에는 미리보기를 숨긴다.
        marks.setMouseTransparent(true); // 승리 표시도 마우스 선택에서 제외한다.
        Group sceneRoot = new Group(world, new AmbientLight(Color.WHITE)); // 보드와 조명을 묶는다.
        scene3d = new SubScene(sceneRoot, 900, 550, true, SceneAntialiasing.BALANCED); // 깊이가 있는 장면을 만든다.
        scene3d.setFill(Color.web("#f1ede6")); // 보드 뒤의 배경색이다.
        scene3d.setCamera(camera); // 이 장면에 카메라를 연결한다.
        camera.setNearClip(0.1); // 가까이 있는 물체도 표시한다.
        camera.setFarClip(10000); // 축소해도 보드가 사라지지 않게 한다.
        getChildren().add(scene3d); // 3D 장면을 화면에 넣는다.
        scene3d.widthProperty().bind(widthProperty()); // 창의 가로 크기에 맞춘다.
        scene3d.heightProperty().bind(heightProperty()); // 창의 세로 크기에 맞춘다.
        widthProperty().addListener((a, b, c) -> fitCamera()); // 창 너비가 바뀌면 카메라 거리를 다시 정한다.
        heightProperty().addListener((a, b, c) -> fitCamera()); // 창 높이가 바뀌어도 다시 정한다.
        rotateX.angleProperty().addListener((a, b, c) -> fitCamera()); // 시점 버튼으로 회전하는 중에도 화면에 맞춘다.
        rotateY.angleProperty().addListener((a, b, c) -> fitCamera()); // 좌우 회전 중에도 화면에 맞춘다.
        connectMouse(); // 클릭, 회전, 확대 기능을 연결한다.
    }

    private double position(int index) { // 배열의 칸 번호를 3D 좌표로 바꾼다.
        return (index - (OmokGame.SIZE - 1) / 2.0) * CELL; // 가운데 칸을 0으로 맞춘다.
    }

    private double stoneY(int floor) { // 층 번호를 돌 중심의 높이로 바꾼다.
        return -5 - RADIUS - floor * CELL; // JavaFX는 음수 방향이 위쪽이다.
    }

    private void makeBoard() { // 나무판과 격자를 만든다.
        double width = OmokGame.SIZE * CELL + 16; // 가장자리 여백까지 포함한 너비이다.
        wood = new Box(width, 10, width); // 두께 10인 나무판이다.
        wood.setMaterial(new PhongMaterial(Color.web("#edc681"))); // 나무판을 연한 갈색으로 만든다.
        world.getChildren().add(wood); // 나무판을 보드 묶음에 넣는다.
        for (int i = 0; i < OmokGame.SIZE; i++) { // 가로선과 세로선을 각각 19개 만든다.
            Box lineX = new Box((OmokGame.SIZE - 1) * CELL, 0.5, 0.7); // 가로로 긴 선이다.
            lineX.setTranslateZ(position(i)); // 앞뒤 위치를 정한다.
            lineX.setTranslateY(-5.3); // 나무판 윗면보다 조금 높게 놓는다.
            lineX.setMaterial(new PhongMaterial(Color.SADDLEBROWN)); // 선의 색을 정한다.
            lineX.setMouseTransparent(true); // 선 대신 나무판을 클릭하게 한다.
            Box lineZ = new Box(0.7, 0.5, (OmokGame.SIZE - 1) * CELL); // 앞뒤로 긴 선이다.
            lineZ.setTranslateX(position(i)); // 가로 위치를 정한다.
            lineZ.setTranslateY(-5.3); // 나무판 위에 놓는다.
            lineZ.setMaterial(new PhongMaterial(Color.SADDLEBROWN)); // 같은 색을 사용한다.
            lineZ.setMouseTransparent(true); // 선은 마우스 선택에서 제외한다.
            world.getChildren().addAll(lineX, lineZ); // 두 선을 보드에 넣는다.
        }
        for (int x = 3; x <= 15; x += 6) { // 오목판의 기준점을 세 줄에 배치한다.
            for (int z = 3; z <= 15; z += 6) { // 각 줄에 점 세 개를 놓는다.
                Sphere dot = new Sphere(2); // 작은 구로 기준점을 만든다.
                dot.setTranslateX(position(x)); // 기준점의 가로 위치이다.
                dot.setTranslateY(-5.5); // 기준점의 높이이다.
                dot.setTranslateZ(position(z)); // 기준점의 앞뒤 위치이다.
                dot.setMaterial(new PhongMaterial(Color.SADDLEBROWN)); // 격자와 같은 색이다.
                dot.setMouseTransparent(true); // 기준점도 클릭을 가로채지 않는다.
                world.getChildren().add(dot); // 기준점을 보드에 넣는다.
            }
        }
    }

    private Sphere makeStone(int x, int y, int z, int color) { // 지정한 위치에 돌 하나를 만든다.
        Sphere stone = new Sphere(RADIUS, 20); // 둥근 돌이다. 20은 표면을 나누는 정도이다.
        Color paint = Color.WHITE; // 기본값은 백돌의 색이다.
        if (color == 1) { // 흑돌을 만드는 경우이다.
            paint = Color.web("#252530"); // 완전한 검정 대신 약간 밝은 색을 쓴다.
        }
        stone.setMaterial(new PhongMaterial(paint)); // 돌의 표면 색을 적용한다.
        stone.setTranslateX(position(x)); // 가로 위치를 정한다.
        stone.setTranslateY(stoneY(y)); // 층에 맞는 높이를 정한다.
        stone.setTranslateZ(position(z)); // 앞뒤 위치를 정한다.
        stone.setUserData(new int[]{x, z}); // 이 돌을 클릭하면 어느 기둥인지 알 수 있게 저장한다.
        return stone; // 완성한 돌을 돌려준다.
    }

    public void redraw() { // 되돌리기나 새 게임 후 보드의 돌을 다시 그린다.
        stones.getChildren().clear(); // 기존에 그린 돌을 지운다.
        marks.getChildren().clear(); // 기존 승리 표시도 지운다.
        for (int x = 0; x < OmokGame.SIZE; x++) { // 모든 가로 칸을 확인한다.
            for (int z = 0; z < OmokGame.SIZE; z++) { // 모든 앞뒤 칸을 확인한다.
                for (int y = 0; y < OmokGame.SIZE; y++) { // 각 기둥의 층을 확인한다.
                    int color = game.getStone(x, y, z); // 해당 칸의 돌 색을 읽는다.
                    if (color != 0) { // 빈칸이 아닌 경우만 그린다.
                        stones.getChildren().add(makeStone(x, y, z, color)); // 돌 하나를 추가한다.
                    }
                }
            }
        }
        showWin(); // 승리한 경우 연결된 돌을 표시한다.
        updatePreview(); // 다음 착수 위치도 갱신한다.
        fitCamera(); // 돌 높이에 맞게 화면을 조정한다.
    }

    public void animateStone(int x, int y, int z, int color) { // 새 돌이 떨어지는 모습을 만든다.
        Sphere stone = makeStone(x, y, z, color); // 마지막 돌의 모양을 만든다.
        stones.getChildren().add(stone); // 기존 돌 위에 새 돌을 추가한다.
        double end = stoneY(y); // 착지할 높이이다.
        busy = true; // 낙하 중임을 기록한다.
        preview.setVisible(false); // 낙하 중에는 미리보기를 숨긴다.
        fitCamera(); // 추가된 돌까지 화면에 들어오게 한다.
        drop = new Timeline(); // 낙하를 재생할 타임라인을 만든다.
        drop.getKeyFrames().add(new KeyFrame(Duration.ZERO, new KeyValue(stone.translateYProperty(), end - 160))); // 위에서 출발한다.
        drop.getKeyFrames().add(new KeyFrame(Duration.millis(300), new KeyValue(stone.translateYProperty(), end, Interpolator.EASE_IN))); // 바닥으로 떨어진다.
        drop.getKeyFrames().add(new KeyFrame(Duration.millis(420), new KeyValue(stone.translateYProperty(), end - 12))); // 한 번 튀어 오른다.
        drop.getKeyFrames().add(new KeyFrame(Duration.millis(520), new KeyValue(stone.translateYProperty(), end))); // 다시 착지한다.
        drop.getKeyFrames().add(new KeyFrame(Duration.millis(590), new KeyValue(stone.translateYProperty(), end - 4))); // 작게 한 번 더 튄다.
        drop.getKeyFrames().add(new KeyFrame(Duration.millis(660), new KeyValue(stone.translateYProperty(), end))); // 완전히 멈춘다.
        drop.setOnFinished(e -> { // 애니메이션이 끝났을 때 실행한다.
            busy = false; // 다시 조작할 수 있게 한다.
            redraw(); // 승리 표시와 미리보기를 갱신한다.
            app.refresh(); // 버튼과 차례 표시를 갱신한다.
        });
        drop.play(); // 애니메이션을 시작한다.
    }

    public boolean isBusy() { // 낙하 중인지 화면 클래스가 확인한다.
        return busy; // 낙하 중이면 true이다.
    }

    public void cancelDrop() { // 새 게임을 시작할 때 이전 애니메이션을 멈춘다.
        if (drop != null) { // 실행한 애니메이션이 있는지 확인한다.
            drop.stop(); // 이전 게임의 돌이 뒤늦게 움직이지 않게 한다.
        }
        busy = false; // 조작 잠금을 해제한다.
    }

    private int[] pickColumn(MouseEvent event) { // 클릭한 물체에서 기둥 번호를 구한다.
        Node node = event.getPickResult().getIntersectedNode(); // 마우스가 가리킨 도형이다.
        if (node == null) { // 아무 도형도 가리키지 않은 경우이다.
            return null; // 선택한 칸이 없다.
        }
        if (node.getUserData() != null) { // 돌에 기둥 번호가 저장되어 있는지 확인한다.
            return (int[]) node.getUserData(); // 해당 돌 위로 쌓을 수 있게 기둥 번호를 돌려준다.
        }
        if (node != wood) { // 나무판이나 돌이 아닌 경우이다.
            return null; // 착수 위치로 사용하지 않는다.
        }
        Point3D point = event.getPickResult().getIntersectedPoint(); // 나무판 안에서의 클릭 위치이다.
        if (point.getY() > -4.9) { // 나무판 옆면이나 아랫면인지 확인한다.
            return null; // 윗면에서만 착수하게 한다.
        }
        int x = (int) Math.round(point.getX() / CELL + (OmokGame.SIZE - 1) / 2.0); // 가장 가까운 가로 격자를 구한다.
        int z = (int) Math.round(point.getZ() / CELL + (OmokGame.SIZE - 1) / 2.0); // 가장 가까운 앞뒤 격자를 구한다.
        if (x < 0 || x >= OmokGame.SIZE || z < 0 || z >= OmokGame.SIZE) { // 격자 밖 여백인지 확인한다.
            return null; // 보드 밖에는 돌을 놓지 않는다.
        }
        return new int[]{x, z}; // 선택한 기둥의 두 좌표를 돌려준다.
    }

    private void connectMouse() { // e -> 는 해당 마우스 동작이 일어나면 오른쪽 코드를 실행한다는 뜻이다.
        scene3d.setOnMousePressed(e -> { // 마우스를 누르는 순간이다.
            if (rotation != null) { // 자동 시점 이동 중일 수 있다.
                rotation.stop(); // 이제 직접 회전할 수 있게 자동 이동을 멈춘다.
            }
            pressX = e.getSceneX(); // 누른 가로 위치를 저장한다.
            pressY = e.getSceneY(); // 누른 세로 위치를 저장한다.
            startX = rotateX.getAngle(); // 시작 위아래 각도를 저장한다.
            startY = rotateY.getAngle(); // 시작 좌우 각도를 저장한다.
            dragged = false; // 아직 드래그하지 않았다.
        });
        scene3d.setOnMouseDragged(e -> { // 누른 채로 마우스를 움직이는 동안이다.
            double dx = e.getSceneX() - pressX; // 가로로 움직인 거리이다.
            double dy = e.getSceneY() - pressY; // 세로로 움직인 거리이다.
            if (Math.abs(dx) + Math.abs(dy) > 5) { // 작은 손 떨림을 클릭과 구분한다.
                dragged = true; // 이번 동작은 회전이다.
                rotateY.setAngle(startY - dx * 0.4); // 좌우로 회전한다.
                rotateX.setAngle(Math.max(-90, Math.min(90, startX + dy * 0.4))); // 위아래 회전 범위를 제한한다.
                hoverX = -1; // 회전 중에는 선택 위치를 지운다.
                updatePreview(); // 미리보기를 숨긴다.
                fitCamera(); // 회전한 보드가 화면에 맞도록 조정한다.
            }
        });
        scene3d.setOnMouseReleased(e -> { // 마우스 버튼을 놓는 순간이다.
            if (!dragged && e.getButton() == MouseButton.PRIMARY) { // 왼쪽 클릭이고 회전하지 않은 경우이다.
                int[] column = pickColumn(e); // 선택한 기둥을 찾는다.
                if (column != null) { // 유효한 기둥을 선택한 경우이다.
                    app.place(column[0], column[1]); // 화면 담당 클래스에 착수를 요청한다.
                }
            }
        });
        scene3d.setOnMouseMoved(e -> { // 마우스를 누르지 않고 움직이는 순간이다.
            int[] column = pickColumn(e); // 마우스 아래의 기둥을 찾는다.
            hoverX = -1; // 기본값은 선택 없음이다.
            if (column != null) { // 기둥을 가리킨 경우이다.
                hoverX = column[0]; // 가로 칸을 저장한다.
                hoverZ = column[1]; // 앞뒤 칸을 저장한다.
            }
            updatePreview(); // 착수 위치와 좌표를 보여 준다.
        });
        scene3d.setOnMouseExited(e -> { // 마우스가 보드 화면을 벗어날 때이다.
            hoverX = -1; // 선택 위치를 지운다.
            updatePreview(); // 미리보기를 숨긴다.
        });
        scene3d.setOnScroll(e -> { // 마우스 휠을 돌릴 때이다.
            zoom -= e.getDeltaY() * 0.001; // 휠 방향에 따라 거리를 조절한다.
            zoom = Math.max(0.4, Math.min(2, zoom)); // 확대와 축소의 범위를 제한한다.
            fitCamera(); // 바뀐 거리를 적용한다.
        });
    }

    public void updatePreview() { // 선택 위치에 반투명 돌을 보여 준다.
        preview.setVisible(false); // 우선 미리보기를 숨긴다.
        app.showHover(""); // 기존 좌표 설명을 지운다.
        if (hoverX < 0 || game.isOver() || game.isPiePending() || busy) { // 미리보기를 표시할 수 없는 상태이다.
            return; // 다음 돌을 보여 주지 않는다.
        }
        int floor = game.getEmptyFloor(hoverX, hoverZ); // 다음 돌이 놓일 층을 구한다.
        String text = "좌표: " + (hoverX + 1) + ", " + (hoverZ + 1); // 사람이 읽는 좌표는 1부터 표시한다.
        if (floor == -1) { // 기둥이 가득 찬 경우이다.
            app.showHover(text + " / 가득 참"); // 가득 찼다는 안내를 보여 준다.
            return; // 미리보기 돌을 그리지 않는다.
        }
        app.showHover(text + " / " + (floor + 1) + "층"); // 놓일 층까지 보여 준다.
        Color color = Color.WHITE; // 기본 미리보기 색은 백돌이다.
        if (game.getTurn() == 1) { // 흑돌 차례인 경우이다.
            color = Color.BLACK; // 검정 미리보기를 사용한다.
        }
        preview.setMaterial(new PhongMaterial(color)); // 차례에 맞는 색을 적용한다.
        preview.setTranslateX(position(hoverX)); // 미리보기의 가로 위치이다.
        preview.setTranslateY(stoneY(floor)); // 미리보기의 층 위치이다.
        preview.setTranslateZ(position(hoverZ)); // 미리보기의 앞뒤 위치이다.
        preview.setVisible(true); // 미리보기를 보여 준다.
    }

    public void setView(double x, double y, boolean resetZoom) { // 버튼에서 지정한 시점으로 이동한다.
        if (rotation != null) { // 이미 다른 시점으로 이동 중일 수 있다.
            rotation.stop(); // 이전 이동을 멈춘다.
        }
        if (resetZoom) { // 기본 시점 버튼을 누른 경우이다.
            zoom = 1; // 확대와 축소도 초기화한다.
        }
        double current = rotateY.getAngle(); // 현재 좌우 각도이다.
        double target = y; // 목표 좌우 각도이다.
        while (target - current > 180) { // 한쪽으로 너무 멀리 돌아야 하는 경우이다.
            target -= 360; // 같은 방향을 나타내는 한 바퀴 작은 각도로 바꾼다.
        }
        while (target - current < -180) { // 반대쪽으로 너무 멀리 돌아야 하는 경우이다.
            target += 360; // 같은 방향을 나타내는 한 바퀴 큰 각도로 바꾼다.
        }
        rotation = new Timeline(new KeyFrame(Duration.millis(400), // 0.4초 동안 시점을 옮긴다.
            new KeyValue(rotateX.angleProperty(), x, Interpolator.EASE_BOTH), // 위아래 각도를 바꾼다.
            new KeyValue(rotateY.angleProperty(), target, Interpolator.EASE_BOTH))); // 좌우 각도를 바꾼다.
        rotation.play(); // 시점 이동을 시작한다.
        fitCamera(); // 각도가 같아도 초기화한 확대 비율은 적용한다.
    }

    private void fitCamera() { // 회전한 보드의 크기에 맞추어 카메라를 배치한다.
        Bounds bounds = world.getBoundsInParent(); // 회전까지 적용된 보드의 범위이다.
        double width = Math.max(1, scene3d.getWidth()); // 가로 길이가 0이 되는 것을 막는다.
        double height = Math.max(1, scene3d.getHeight()); // 세로 길이가 0이 되는 것을 막는다.
        double size = Math.max(bounds.getHeight(), bounds.getWidth() * height / width); // 가로와 세로가 모두 들어갈 크기를 구한다.
        double distance = size * 2 + bounds.getDepth() / 2 + 80; // 시야각 30도에서 넉넉히 들어오도록 약 두 배 떨어진다.
        camera.setTranslateX((bounds.getMinX() + bounds.getMaxX()) / 2); // 보드의 가로 중앙을 바라본다.
        camera.setTranslateY((bounds.getMinY() + bounds.getMaxY()) / 2); // 보드와 돌의 세로 중앙을 바라본다.
        camera.setTranslateZ((bounds.getMinZ() + bounds.getMaxZ()) / 2 - distance * zoom); // 계산한 거리에 카메라를 둔다.
    }

    private void showWin() { // 승리한 돌들에 금색 테두리를 붙인다.
        for (int i = 0; i < game.getWinningStones().size(); i++) { // 승리한 직선의 돌들을 확인한다.
            int[] point = game.getWinningStones().get(i); // 돌의 세 좌표를 읽는다.
            Sphere mark = new Sphere(RADIUS + 2, 12); // 돌보다 조금 큰 구이다.
            mark.setDrawMode(DrawMode.LINE); // 면 대신 선으로 표시한다.
            mark.setCullFace(CullFace.NONE); // 안쪽 선도 보이게 한다.
            mark.setMaterial(new PhongMaterial(Color.GOLD)); // 승리 표시를 금색으로 정한다.
            mark.setTranslateX(position(point[0])); // 승리한 돌의 가로 위치이다.
            mark.setTranslateY(stoneY(point[1])); // 승리한 돌의 높이이다.
            mark.setTranslateZ(position(point[2])); // 승리한 돌의 앞뒤 위치이다.
            marks.getChildren().add(mark); // 표시를 보드에 추가한다.
        }
    }
}
