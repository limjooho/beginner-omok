package beginner; // 게임 클래스와 같은 패키지에서 검사한다.

import org.junit.jupiter.api.Test; // @Test를 붙인 메서드를 자동으로 검사한다.
import static org.junit.jupiter.api.Assertions.*; // 예상한 결과와 실제 결과를 비교한다.

// 화면을 켜지 않고 게임 규칙만 확인하는 파일이다. 게임을 공부한 다음 읽으면 된다.
class OmokGameTest {
    @Test // 모든 승리 방향을 실제 착수 순서로 검사한다.
    void allThirteenDirectionsWin() {
        int[][] lines = { // 검사할 직선 방향을 직접 적는다.
            {1, 0, 0}, {0, 1, 0}, {0, 0, 1}, // 세 축 방향이다.
            {1, 1, 0}, {1, -1, 0}, {1, 0, 1}, {1, 0, -1}, // 가로가 포함된 평면 대각선이다.
            {0, 1, 1}, {0, 1, -1}, // 높이와 앞뒤의 평면 대각선이다.
            {1, 1, 1}, {1, 1, -1}, {1, -1, 1}, {1, -1, -1} // 공간 대각선이다.
        };
        for (int[] line : lines) { // 각 방향마다 새 게임을 만든다.
            OmokGame game = new OmokGame(); // 다른 방향의 돌과 섞이지 않도록 초기화한다.
            int sign = 1; // 기본 이동 방향이다.
            if (line[1] < 0) { // 아래로 향하는 방향인 경우이다.
                sign = -1; // 같은 직선을 아래층부터 쌓도록 반대로 읽는다.
            }
            int decoy = 0; // 차례를 맞추기 위한 다른 기둥 번호이다.
            for (int i = 0; i < 5; i++) { // 목표 직선에 흑돌 다섯 개를 놓는다.
                int x = 8 + i * line[0] * sign; // 목표 가로 위치이다.
                int y = i * line[1] * sign; // 목표 층이다.
                int z = 8 + i * line[2] * sign; // 목표 앞뒤 위치이다.
                while (game.getEmptyFloor(x, z) < y) { // 필요한 받침을 아래부터 채운다.
                    if (game.getTurn() == 1) { // 받침용 백돌을 놓기 전에 흑돌 차례인 경우이다.
                        assertTrue(game.place(0, decoy * 2)); // 흑돌은 떨어진 다른 칸에 놓는다.
                        decoy++; // 다음 보조 돌은 다른 기둥을 쓴다.
                    }
                    assertTrue(game.place(x, z)); // 백돌 받침을 한 층 놓는다.
                }
                if (game.getTurn() == 2) { // 목표 흑돌 차례를 맞춰야 하는 경우이다.
                    assertTrue(game.place(0, decoy * 2)); // 백돌도 떨어진 칸에 놓는다.
                    decoy++; // 다음 보조 돌의 위치를 바꾼다.
                }
                assertTrue(game.place(x, z)); // 목표 위치에 흑돌을 놓는다.
                game.choosePie(false); // 첫 수 뒤에는 진영을 유지한다.
                if (i < 4) { // 아직 흑돌이 네 개 이하인 경우이다.
                    assertEquals(0, game.getWinner()); // 일찍 승리 처리되면 안 된다.
                }
            }
            assertEquals(1, game.getWinner()); // 해당 방향에서 흑돌이 승리해야 한다.
            assertEquals(5, game.getWinningStones().size()); // 금색으로 표시할 돌도 다섯 개이다.
        }
    }

    @Test // 파이 룰의 선택 전후를 검사한다.
    void pieChoiceBlocksMovesAndChangesOwners() {
        OmokGame game = new OmokGame(); // 빈 게임을 만든다.
        game.choosePie(true); // 첫 수 전의 잘못된 선택을 시도한다.
        assertFalse(game.isSwapped()); // 선택이 적용되면 안 된다.
        assertTrue(game.place(9, 9)); // 첫 흑돌을 놓는다.
        assertTrue(game.isPiePending()); // 후공의 선택을 기다려야 한다.
        assertFalse(game.place(0, 0)); // 선택 전에는 다음 수를 막아야 한다.
        assertEquals(1, game.getMoveCount()); // 막힌 수가 기록되면 안 된다.
        game.choosePie(true); // 2P가 흑돌을 가져간다.
        assertEquals(2, game.getPlayer(1)); // 흑돌 소유자는 2P이다.
        assertEquals(1, game.getPlayer(2)); // 백돌 소유자는 1P이다.
        assertEquals(2, game.getTurn()); // 다음 돌 색은 여전히 백돌이다.
        game.choosePie(false); // 선택이 끝난 뒤 다시 바꾸려고 시도한다.
        assertTrue(game.isSwapped()); // 기존 선택을 유지해야 한다.
    }

    @Test // 진영을 유지하는 경우를 검사한다.
    void keepingSidesLeavesOwnersUnchanged() {
        OmokGame game = new OmokGame(); // 빈 게임을 만든다.
        game.place(0, 0); // 첫 수를 놓는다.
        game.choosePie(false); // 진영을 유지한다.
        assertEquals(1, game.getPlayer(1)); // 1P는 흑돌이다.
        assertEquals(2, game.getPlayer(2)); // 2P는 백돌이다.
        assertTrue(game.place(0, 0)); // 백돌을 같은 기둥에 쌓을 수 있다.
        assertEquals(1, game.getStone(0, 0, 0)); // 아래층은 흑돌이다.
        assertEquals(2, game.getStone(0, 1, 0)); // 위층은 백돌이다.
    }

    @Test // 기둥의 높이 제한과 보드 밖 좌표를 검사한다.
    void fullColumnAndOutsideCoordinatesDoNotChangeGame() {
        OmokGame game = new OmokGame(); // 빈 게임을 만든다.
        assertFalse(game.place(-1, 0)); // 음수 좌표에는 놓지 못한다.
        assertFalse(game.place(19, 0)); // 가장자리 밖에도 놓지 못한다.
        for (int i = 0; i < OmokGame.SIZE; i++) { // 한 기둥을 흑백 번갈아 채운다.
            assertTrue(game.place(3, 4)); // 남은 층에는 놓을 수 있다.
            game.choosePie(false); // 첫 수 직후에만 실제로 선택이 적용된다.
            assertEquals(i + 1, game.getMoveCount()); // 돌 개수를 확인한다.
        }
        assertFalse(game.place(3, 4)); // 20번째 돌은 놓지 못한다.
        assertEquals(-1, game.getEmptyFloor(3, 4)); // 빈층이 없다고 알려야 한다.
        assertEquals(19, game.getMoveCount()); // 실패한 수는 기록하지 않는다.
    }

    @Test // 여러 번 되돌려도 파이 룰 선택을 복원하는지 검사한다.
    void undoAndRedoRestorePieChoice() {
        OmokGame game = new OmokGame(); // 빈 게임을 만든다.
        game.place(9, 9); // 첫 수이다.
        game.choosePie(true); // 진영을 교환한다.
        game.place(9, 9); // 두 번째 수이다.
        game.place(9, 9); // 세 번째 수이다.
        game.undo(); // 세 번째 수를 지운다.
        game.undo(); // 두 번째 수를 지운다.
        assertTrue(game.isPiePending()); // 첫 수만 남았으므로 다시 선택해야 한다.
        game.undo(); // 첫 수까지 지운다.
        assertEquals(0, game.getMoveCount()); // 빈 게임이어야 한다.
        assertEquals(1, game.getTurn()); // 흑돌부터 다시 시작한다.
        game.redo(); // 첫 수를 복원한다.
        assertTrue(game.isPiePending()); // 첫 수 뒤의 대기 상태도 복원한다.
        game.redo(); // 두 번째 수를 복원한다.
        assertTrue(game.isSwapped()); // 그때의 진영 교환도 복원한다.
        game.redo(); // 세 번째 수를 복원한다.
        assertEquals(3, game.getMoveCount()); // 세 돌이 모두 돌아와야 한다.
        assertEquals(1, game.getStone(9, 2, 9)); // 세 번째 층에는 흑돌이 있어야 한다.
    }

    @Test // 되돌린 뒤 새 수를 두면 다시 실행 기록이 없어지는지 검사한다.
    void newMoveClearsRedoButInvalidMoveDoesNot() {
        OmokGame game = new OmokGame(); // 빈 게임을 만든다.
        game.place(0, 0); // 첫 수를 놓는다.
        game.choosePie(false); // 진영을 유지한다.
        game.place(1, 1); // 둘째 수를 놓는다.
        game.undo(); // 둘째 수를 지운다.
        assertFalse(game.place(2, 2)); // 파이 룰을 선택하지 않았으므로 실패한다.
        assertTrue(game.canRedo()); // 실패한 수는 다시 실행 기록을 지우지 않는다.
        game.choosePie(false); // 진영을 다시 선택한다.
        assertFalse(game.place(-1, 0)); // 범위를 벗어난 수도 실패한다.
        assertTrue(game.canRedo()); // 이때도 기록이 남아 있어야 한다.
        assertTrue(game.place(2, 2)); // 실제로 새 수를 놓는다.
        assertFalse(game.canRedo()); // 이제 예전 둘째 수는 다시 실행하지 못한다.
    }

    @Test // 마지막 돌의 양쪽을 함께 세는지 검사한다.
    void joiningTwoSidesWinsAndUndoReopensGame() {
        OmokGame game = new OmokGame(); // 빈 게임을 만든다.
        int[] order = {0, 1, 3, 4, 2}; // 마지막에 가운데 빈칸을 메운다.
        for (int i = 0; i < order.length; i++) { // 흑돌 다섯 개를 한 줄에 배치한다.
            assertTrue(game.place(order[i], 0)); // 흑돌을 놓는다.
            game.choosePie(true); // 첫 수 직후 진영 교환을 선택한다.
            if (i < 4) { // 마지막 승리 수 전까지만 백돌을 놓는다.
                assertEquals(0, game.getWinner()); // 네 개로는 승리하면 안 된다.
                assertTrue(game.place(i * 2, 18)); // 멀리 떨어진 곳에 백돌을 놓는다.
            }
        }
        assertEquals(1, game.getWinner()); // 흑돌이 5목을 완성했다.
        assertEquals(2, game.getPlayer(game.getWinner())); // 진영 교환을 했으므로 승리자는 2P이다.
        assertEquals(5, game.getWinningStones().size()); // 표시할 승리 돌은 다섯 개이다.
        assertFalse(game.place(10, 10)); // 종료 후에는 착수하지 못한다.
        game.undo(); // 마지막 승리 수를 지운다.
        assertFalse(game.isOver()); // 게임을 계속할 수 있어야 한다.
        assertTrue(game.getWinningStones().isEmpty()); // 승리 표시도 없어야 한다.
        game.redo(); // 승리 수를 다시 놓는다.
        assertEquals(1, game.getWinner()); // 다시 승리해야 한다.
    }

    @Test // 높이가 있는 공간 대각선도 검사한다.
    void spaceDiagonalWinsWithLegalSupportingMoves() {
        OmokGame game = new OmokGame(); // 빈 게임을 만든다.
        int decoy = 0; // 차례를 맞추기 위해 멀리 놓을 흑돌 번호이다.
        for (int i = 0; i < 5; i++) { // (0,0,0)부터 (4,4,4)까지 연결한다.
            for (int y = 0; y < i; y++) { // 목표 층 아래를 백돌로 받친다.
                if (game.getTurn() == 1) { // 백돌을 놓기 전에 흑돌 차례가 온 경우이다.
                    assertTrue(game.place(15, decoy * 2)); // 다른 줄에 흑돌을 놓는다.
                    decoy++; // 다음 흑돌은 떨어진 위치에 놓는다.
                }
                assertTrue(game.place(i, i)); // 받침용 백돌을 쌓는다.
            }
            assertTrue(game.place(i, i)); // 목표 층에 흑돌을 놓는다.
            game.choosePie(false); // 첫 수 직후에는 파이 룰을 선택한다.
        }
        assertEquals(1, game.getWinner()); // 공간 대각선 5목은 흑돌 승리이다.
        for (int i = 0; i < 5; i++) { // 목표 직선의 좌표를 확인한다.
            assertEquals(1, game.getStone(i, i, i)); // 높이까지 다른 다섯 흑돌이 연결되어야 한다.
        }
    }

    @Test // 새 게임이 기록까지 모두 지우는지 검사한다.
    void resetClearsBoardAndHistory() {
        OmokGame game = new OmokGame(); // 빈 게임을 만든다.
        game.place(0, 0); // 첫 수를 놓는다.
        game.choosePie(true); // 진영을 교환한다.
        game.place(1, 1); // 둘째 수를 놓는다.
        game.undo(); // 다시 실행 기록도 만든다.
        game.reset(); // 새 게임을 시작한다.
        assertEquals(0, game.getStone(0, 0, 0)); // 기존 돌이 사라져야 한다.
        assertEquals(0, game.getMoveCount()); // 착수 개수도 0이어야 한다.
        assertFalse(game.canUndo()); // 되돌릴 수 없어야 한다.
        assertFalse(game.canRedo()); // 다시 실행할 수도 없어야 한다.
        assertFalse(game.isSwapped()); // 진영 교환을 초기화해야 한다.
        assertFalse(game.isPiePending()); // 아직 첫 수를 놓지 않은 상태이다.
        assertFalse(game.isOver()); // 새 게임은 진행할 수 있어야 한다.
    }
}
