package beginner; // 새로운 오목 프로그램의 패키지이다.

import javafx.animation.Animation; // 타이머의 무한 반복을 설정한다.
import javafx.animation.KeyFrame; // 1초마다 실행할 동작을 지정한다.
import javafx.animation.Timeline; // 경과 시간을 재는 타이머이다.
import javafx.application.Application; // JavaFX 화면 클래스의 부모이다.
import javafx.geometry.Insets; // 화면 가장자리의 여백을 정한다.
import javafx.geometry.Pos; // 버튼과 글자의 정렬 방향을 정한다.
import javafx.geometry.Rectangle2D; // 모니터에서 사용할 수 있는 화면 범위이다.
import javafx.scene.Scene; // 창 안에 표시할 전체 장면이다.
import javafx.scene.control.Button; // 사용자가 누르는 버튼이다.
import javafx.scene.control.Label; // 안내 문구를 보여 준다.
import javafx.scene.control.Tooltip; // 버튼 위에 마우스를 올리면 설명을 보여 준다.
import javafx.scene.layout.BorderPane; // 위쪽, 가운데, 아래쪽으로 화면을 나눈다.
import javafx.scene.layout.FlowPane; // 창이 좁아지면 버튼을 다음 줄로 배치한다.
import javafx.scene.layout.HBox; // 내용을 가로로 배치한다.
import javafx.scene.layout.VBox; // 내용을 세로로 배치한다.
import javafx.scene.paint.Color; // 차례 표시의 색을 지정한다.
import javafx.scene.shape.Circle; // 흑돌과 백돌 차례를 표시한다.
import javafx.stage.Popup; // 설정과 통계를 작은 창으로 보여 준다.
import javafx.stage.Screen; // 모니터 크기를 확인한다.
import javafx.stage.Stage; // 프로그램의 창이다.
import javafx.util.Duration; // 타이머의 실행 간격을 정한다.

// 먼저 화면 틀을 만들고, 버튼에 게임 기능을 연결하는 클래스이다.
public class OmokApp extends Application { // JavaFX 화면을 만드는 클래스이다.
    private OmokGame game = new OmokGame(); // 돌과 차례를 관리한다.
    private BoardView boardView; // 3D 보드를 그린다.
    private Label status = new Label(); // 차례 또는 승리 안내이다.
    private Label count = new Label(); // 놓은 돌 개수이다.
    private Label time = new Label("시간: 00:00"); // 게임 진행 시간이다.
    private Label hover = new Label(); // 마우스로 가리킨 좌표와 층이다.
    private Circle black = new Circle(9, Color.BLACK); // 흑돌 차례 표시이다.
    private Circle white = new Circle(9, Color.WHITE); // 백돌 차례 표시이다.
    private Button undo = new Button("되돌리기"); // 마지막 수를 지운다.
    private Button redo = new Button("다시 실행"); // 지운 수를 다시 놓는다.
    private Button swap = new Button("교환 (2P가 흑)"); // 후공이 흑돌을 선택한다.
    private Button keep = new Button("유지 (2P가 백)"); // 후공이 백돌을 유지한다.
    private FlowPane pieRow = new FlowPane(10, 8); // 파이 룰 안내와 선택 버튼을 담는다.
    private Timeline clock; // 1초마다 시간을 늘리는 타이머이다.
    private int seconds = 0; // 게임을 시작한 뒤 지난 초이다.

    @Override // 부모 클래스 Application에 있는 start 메서드를 구현한다.
    public void start(Stage stage) { // JavaFX가 준비되면 이 메서드를 실행한다.
        boardView = new BoardView(this, game); // 화면과 규칙을 연결한 3D 보드를 만든다.
        BorderPane root = new BorderPane(); // 화면의 전체 틀을 만든다.
        root.setTop(makeMenu()); // 위쪽에는 메뉴와 상태 안내를 놓는다.
        root.setCenter(boardView); // 가운데에는 3D 보드를 놓는다.
        VBox bottom = new VBox(5); // 아래쪽에 두 줄 안내를 넣는다.
        bottom.setPadding(new Insets(10)); // 안내 주변에 여백을 둔다.
        bottom.setAlignment(Pos.CENTER); // 안내를 가운데 정렬한다.
        bottom.getChildren().addAll(hover, new Label("클릭: 돌 놓기 / 드래그: 회전 / 마우스 휠: 확대·축소")); // 사용 방법을 넣는다.
        root.setBottom(bottom); // 안내를 화면 아래쪽에 배치한다.
        root.setStyle("-fx-font-family: 'Malgun Gothic'; -fx-font-size: 14px; -fx-background-color: #f1ede6;"); // 기본 글꼴과 배경색이다.
        makeClock(); // 경과 시간을 재는 기능을 준비한다.
        connectButtons(); // 버튼을 누르면 실행할 기능을 연결한다.
        refresh(); // 첫 화면의 상태를 표시한다.
        Rectangle2D screen = Screen.getPrimary().getVisualBounds(); // 작업 표시줄을 제외한 화면 크기를 구한다.
        Scene scene = new Scene(root, Math.min(1050, screen.getWidth() * 0.92), Math.min(800, screen.getHeight() * 0.88)); // 모니터에 들어가는 크기로 만든다.
        stage.setScene(scene); // 창에 화면을 넣는다.
        stage.setTitle("3D 오목 - 학습용 버전"); // 창 제목이다.
        stage.setMinWidth(Math.min(600, screen.getWidth())); // 너무 좁게 줄이지 않도록 한다.
        stage.setMinHeight(Math.min(480, screen.getHeight())); // 보드가 보일 최소 높이를 정한다.
        stage.show(); // 완성한 창을 보여 준다.
    }

    private VBox makeMenu() { // 게임을 구현하기 전에 위쪽 화면 틀을 만든다.
        Button settings = new Button("시점 설정"); // 카메라 방향을 고르는 버튼이다.
        Button stats = new Button("통계"); // 돌 개수와 높이를 보는 버튼이다.
        Button restart = new Button("새 게임"); // 게임을 처음부터 시작하는 버튼이다.
        FlowPane buttons = new FlowPane(10, 8); // 메뉴 버튼을 줄에 맞게 배치한다.
        buttons.getChildren().addAll(settings, undo, redo, stats, restart); // 메뉴 버튼을 순서대로 넣는다.
        white.setStroke(Color.GRAY); // 흰 돌이 배경에서도 보이도록 테두리를 준다.
        HBox colors = new HBox(6, black, new Label("흑"), white, new Label("백")); // 돌 색과 차례 표시를 묶는다.
        Tooltip.install(colors, new Tooltip("첫 수는 흑돌입니다. 첫 수 뒤에 2P가 진영을 선택합니다.")); // 파이 룰 설명이다.
        FlowPane info = new FlowPane(16, 8); // 차례, 수, 시간을 담는다.
        info.getChildren().addAll(colors, status, count, time); // 상태 표시를 순서대로 넣는다.
        pieRow.getChildren().addAll(new Label("파이 룰: 2P가 진영을 선택하세요."), swap, keep); // 파이 룰 선택 줄이다.
        VBox menu = new VBox(12, buttons, info, pieRow); // 세 줄을 세로로 배치한다.
        menu.setPadding(new Insets(16)); // 메뉴 주변에 여백을 둔다.
        menu.setStyle("-fx-background-color: #e6e0f0;"); // 메뉴에 연보라 배경을 넣는다.
        settings.setOnAction(e -> showViews(settings)); // 시점 설정 창을 연다.
        stats.setOnAction(e -> showStats(stats)); // 통계 창을 연다.
        restart.setOnAction(e -> restart()); // 새 게임을 시작한다.
        return menu; // 완성한 메뉴를 돌려준다.
    }

    private void connectButtons() { // 버튼 동작과 게임 규칙을 연결한다.
        undo.setOnAction(e -> { // 되돌리기 버튼을 눌렀을 때이다.
            if (!boardView.isBusy()) { // 돌이 떨어지는 중에는 실행하지 않는다.
                game.undo(); // 마지막 수를 지운다.
                afterHistoryChange(); // 화면과 시간을 갱신한다.
            }
        });
        redo.setOnAction(e -> { // 다시 실행 버튼을 눌렀을 때이다.
            if (!boardView.isBusy() && game.canRedo() && !game.isOver()) { // 다시 실행 가능한지 확인한다.
                game.redo(); // 되돌린 수와 파이 룰 상태를 복원한다.
                Move move = game.getLastMove(); // 다시 놓은 돌의 정보를 읽는다.
                boardView.animateStone(move.x, move.y, move.z, move.color); // 복원한 돌도 낙하 효과를 보여 준다.
                refresh(); // 버튼과 차례를 갱신한다.
            }
        });
        swap.setOnAction(e -> choosePie(true)); // 교환 버튼은 true를 전달한다.
        keep.setOnAction(e -> choosePie(false)); // 유지 버튼은 false를 전달한다.
    }

    private void makeClock() { // 1초 단위 타이머를 준비한다.
        clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> { // 1초마다 중괄호 안을 실행한다.
            seconds++; // 경과 시간을 1초 늘린다.
            updateTime(); // 화면의 시간도 바꾼다.
        }));
        clock.setCycleCount(Animation.INDEFINITE); // 멈추기 전까지 반복한다.
    }

    private void updateTime() { // 초 단위 숫자를 분과 초로 표시한다.
        time.setText(String.format("시간: %02d:%02d", seconds / 60, seconds % 60)); // 65초라면 01:05로 표시한다.
    }

    public void place(int x, int z) { // 보드에서 클릭한 칸에 돌을 놓는다.
        if (boardView.isBusy()) { // 앞선 돌이 아직 떨어지는 중인지 확인한다.
            return; // 낙하가 끝날 때까지 기다린다.
        }
        if (game.place(x, z)) { // 규칙에 맞으면 배열에 돌을 놓는다.
            Move move = game.getLastMove(); // 놓인 돌의 층과 색을 확인한다.
            boardView.animateStone(move.x, move.y, move.z, move.color); // 새 돌의 낙하를 보여 준다.
            refresh(); // 차례, 돌 개수, 파이 룰 안내를 갱신한다.
        }
    }

    private void choosePie(boolean swapSides) { // 후공의 파이 룰 선택을 적용한다.
        if (!boardView.isBusy()) { // 첫 돌이 착지한 후에 선택하게 한다.
            game.choosePie(swapSides); // 진영을 교환하거나 유지한다.
            refresh(); // 다음에 둘 사람을 다시 표시한다.
        }
    }

    private void afterHistoryChange() { // 되돌린 뒤에 화면을 맞춘다.
        if (game.getMoveCount() == 0) { // 첫 수까지 모두 되돌린 경우이다.
            seconds = 0; // 경과 시간을 초기화한다.
            clock.stop(); // 타이머의 1초 진행도 초기화한다.
        }
        boardView.redraw(); // 배열 상태에 맞춰 돌을 다시 그린다.
        refresh(); // 나머지 상태 안내를 갱신한다.
    }

    private void restart() { // 새 게임을 시작한다.
        boardView.cancelDrop(); // 이전 게임의 낙하 효과를 멈춘다.
        game.reset(); // 모든 돌과 기록을 초기화한다.
        seconds = 0; // 경과 시간을 초기화한다.
        clock.stop(); // 타이머도 처음 상태로 돌린다.
        boardView.redraw(); // 빈 보드를 그린다.
        refresh(); // 첫 차례를 표시한다.
    }

    public void refresh() { // 게임 상태가 바뀔 때 글자와 버튼을 함께 갱신한다.
        int color = game.getTurn(); // 다음에 놓을 돌 색이다.
        String colorName = "흑"; // 기본 색 이름이다.
        if (color == 2) { // 백돌 차례인지 확인한다.
            colorName = "백"; // 표시 이름을 백으로 바꾼다.
        }
        status.setText(game.getPlayer(color) + "P 차례 (" + colorName + ")"); // 사람 번호와 돌 색을 같이 표시한다.
        if (game.isPiePending()) { // 후공의 선택을 기다리는 중이다.
            status.setText("파이 룰 선택 대기 중"); // 아직 다음 사람이 착수하지 못한다고 안내한다.
        }
        if (game.getWinner() != 0) { // 승리한 경우이다.
            status.setText(game.getPlayer(game.getWinner()) + "P 승리! " + game.getWinningStones().size() + "목"); // 교환 후의 사람 번호로 승리를 표시한다.
        } else if (game.isOver()) { // 승자는 없지만 보드가 가득 찬 경우이다.
            status.setText("무승부입니다."); // 더 놓을 수 없다고 안내한다.
        }
        black.setOpacity(0.3); // 우선 두 색의 표시를 흐리게 한다.
        white.setOpacity(0.3); // 현재 차례에 해당하는 색만 밝게 할 예정이다.
        if (color == 1) { // 흑돌 차례인 경우이다.
            black.setOpacity(1); // 흑돌 표시를 밝게 한다.
        } else { // 백돌 차례인 경우이다.
            white.setOpacity(1); // 백돌 표시를 밝게 한다.
        }
        count.setText("놓은 돌: " + game.getMoveCount()); // 현재 돌 개수를 표시한다.
        undo.setDisable(!game.canUndo() || boardView.isBusy()); // 되돌릴 수 없으면 버튼을 비활성화한다.
        redo.setDisable(!game.canRedo() || game.isOver() || boardView.isBusy()); // 다시 실행할 수 없으면 비활성화한다.
        pieRow.setVisible(game.isPiePending()); // 선택할 때만 파이 룰 줄을 보여 준다.
        pieRow.setManaged(game.isPiePending()); // 숨긴 줄이 화면 공간을 차지하지 않게 한다.
        swap.setDisable(boardView.isBusy()); // 낙하 중에는 파이 룰 버튼을 잠근다.
        keep.setDisable(boardView.isBusy()); // 유지 버튼도 같은 조건으로 잠근다.
        if (game.getMoveCount() > 0 && !game.isOver()) { // 첫 수 이후 진행 중인 게임이다.
            clock.play(); // 경과 시간을 계속 센다.
        } else { // 시작 전이거나 종료한 경우이다.
            clock.pause(); // 시간을 세지 않는다.
        }
        updateTime(); // 시간 안내를 갱신한다.
        boardView.updatePreview(); // 선택한 칸의 다음 돌도 갱신한다.
    }

    public void showHover(String text) { // 보드 클래스에서 좌표 설명을 전달할 때 사용한다.
        hover.setText(text); // 안내 글자를 바꾼다.
    }

    private void showViews(Button anchor) { // 시점을 선택하는 작은 창을 만든다.
        FlowPane row = new FlowPane(8, 8); // 시점 버튼들을 배치한다.
        String[] names = {"앞", "뒤", "왼쪽", "오른쪽", "위", "아래", "기본"}; // 시점의 이름이다.
        double[] anglesX = {0, 0, 0, 0, 90, -90, 36}; // 각 시점의 위아래 각도이다.
        double[] anglesY = {0, 180, -90, 90, 0, 0, -18}; // 각 시점의 좌우 각도이다.
        for (int i = 0; i < names.length; i++) { // 시점마다 버튼을 하나 만든다.
            final int index = i; // 버튼이 나중에도 자기 번호를 기억하도록 별도로 저장한다.
            Button button = new Button(names[i]); // 이름을 붙인 버튼이다.
            button.setOnAction(e -> boardView.setView(anglesX[index], anglesY[index], index == 6)); // 해당 시점으로 회전한다.
            row.getChildren().add(button); // 버튼을 창에 넣는다.
        }
        showPopup(anchor, new VBox(row)); // 설정 버튼 아래에 보여 준다.
    }

    private void showStats(Button anchor) { // 현재 보드의 통계를 계산한다.
        int blackCount = 0; // 흑돌 개수이다.
        int whiteCount = 0; // 백돌 개수이다.
        int highest = 0; // 가장 높은 기둥의 층수이다.
        for (int x = 0; x < OmokGame.SIZE; x++) { // 가로 칸을 순서대로 확인한다.
            for (int z = 0; z < OmokGame.SIZE; z++) { // 앞뒤 칸을 순서대로 확인한다.
                for (int y = 0; y < OmokGame.SIZE; y++) { // 각 기둥의 모든 층을 확인한다.
                    int stone = game.getStone(x, y, z); // 돌 색을 읽는다.
                    if (stone == 1) { // 흑돌인지 확인한다.
                        blackCount++; // 흑돌 수를 늘린다.
                    } else if (stone == 2) { // 백돌인지 확인한다.
                        whiteCount++; // 백돌 수를 늘린다.
                    }
                    if (stone != 0 && y + 1 > highest) { // 지금까지 발견한 돌보다 높은지 확인한다.
                        highest = y + 1; // 가장 높은 층을 갱신한다.
                    }
                }
            }
        }
        String pie = "첫 수 후 파이 룰 선택"; // 게임 시작 전의 설명이다.
        if (game.isPiePending()) { // 선택을 기다리는 상태이다.
            pie = "파이 룰 선택 대기 중"; // 아직 결정되지 않았다고 표시한다.
        } else if (game.isSwapped()) { // 진영을 교환한 상태이다.
            pie = "진영 교환: 1P 백 / 2P 흑"; // 바뀐 소유자를 표시한다.
        } else if (game.getMoveCount() > 0) { // 첫 수 이후 진영을 유지한 상태이다.
            pie = "진영 유지: 1P 흑 / 2P 백"; // 원래 소유자를 표시한다.
        }
        VBox content = new VBox(8); // 통계 내용을 세로로 배치한다.
        content.getChildren().addAll(new Label("흑돌: " + blackCount), new Label("백돌: " + whiteCount), new Label("최고 높이: " + highest + "층"), new Label(pie)); // 통계를 글자로 넣는다.
        showPopup(anchor, content); // 통계 버튼 아래에 작은 창을 연다.
    }

    private void showPopup(Button anchor, VBox content) { // 설정과 통계 창의 공통 부분이다.
        Popup popup = new Popup(); // 작은 창을 만든다.
        content.setPadding(new Insets(12)); // 내용 주변에 여백을 준다.
        content.setStyle("-fx-background-color: #f9f5ff; -fx-border-color: #9b8eb0; -fx-font-size: 14px;"); // 창의 배경과 테두리이다.
        popup.getContent().add(content); // 창 안에 내용을 넣는다.
        popup.setAutoHide(true); // 바깥을 클릭하면 자동으로 닫는다.
        javafx.geometry.Bounds bounds = anchor.localToScreen(anchor.getBoundsInLocal()); // 버튼의 화면상 위치를 구한다.
        popup.show(anchor, bounds.getMinX(), bounds.getMaxY() + 5); // 버튼 바로 아래에 창을 띄운다.
    }

    @Override // JavaFX가 창을 닫을 때 호출하는 메서드이다.
    public void stop() { // 종료할 때 실행 중인 동작을 정리한다.
        clock.stop(); // 경과 시간 타이머를 멈춘다.
        boardView.cancelDrop(); // 돌 낙하도 멈춘다.
    }
}
