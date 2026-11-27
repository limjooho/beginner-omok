package beginner; // 화면 코드와 같은 패키지이다.

import java.util.ArrayList; // 착수 기록을 순서대로 저장하기 위해 사용한다.

// JavaFX를 사용하지 않는 게임 규칙 클래스이다. 숫자와 배열만으로 게임을 관리한다.
public class OmokGame { // 화면과 별개로 게임 규칙을 처리하는 클래스이다.
    public static final int SIZE = 19; // 가로, 높이, 앞뒤의 칸 수이다.
    private int[][][] board = new int[SIZE][SIZE][SIZE]; // 0: 빈칸, 1: 흑돌, 2: 백돌이다.
    private ArrayList<Move> moves = new ArrayList<Move>(); // 현재 보드에 남아 있는 착수 기록이다.
    private ArrayList<Move> undone = new ArrayList<Move>(); // 되돌린 착수 기록이다.
    private int turn = 1; // 처음에는 흑돌 차례이다. 사람 번호와 돌 색을 구분한다.
    private int winner = 0; // 승자가 없으면 0이고, 승리하면 해당 돌 색을 저장한다.
    private boolean piePending = false; // 첫 수 이후 파이 룰 선택을 기다리는지 나타낸다.
    private boolean swapped = false; // 2P가 흑돌을 가져갔는지 나타낸다.
    private ArrayList<int[]> winningStones = new ArrayList<int[]>(); // 승리 표시를 붙일 돌들의 좌표이다.

    // 반대 방향은 함께 세기 때문에 26개 방향 중 13개만 적는다.
    private final int[][] directions = { // 각 행에 가로, 높이, 앞뒤의 이동량을 적는다.
        {1, 0, 0}, // 가로 방향이다.
        {0, 1, 0}, // 높이 방향이다.
        {0, 0, 1}, // 앞뒤 방향이다.
        {1, 1, 0}, // 가로와 높이가 함께 증가한다.
        {1, -1, 0}, // 가로가 증가하고 높이가 감소한다.
        {1, 0, 1}, // 바닥의 한쪽 대각선이다.
        {1, 0, -1}, // 바닥의 반대쪽 대각선이다.
        {0, 1, 1}, // 높이와 앞뒤가 함께 증가한다.
        {0, 1, -1}, // 높이가 증가하고 앞뒤가 감소한다.
        {1, 1, 1}, // 공간 대각선의 첫 번째 방향이다.
        {1, 1, -1}, // 공간 대각선의 두 번째 방향이다.
        {1, -1, 1}, // 공간 대각선의 세 번째 방향이다.
        {1, -1, -1} // 공간 대각선의 네 번째 방향이다.
    };

    public int getStone(int x, int y, int z) { // 화면에서 해당 칸의 돌을 확인할 때 사용한다.
        return board[x][y][z]; // 빈칸 또는 돌 색을 돌려준다.
    }

    public int getTurn() { // 다음에 놓을 돌의 색을 확인한다.
        return turn; // 1 또는 2를 돌려준다.
    }

    public int getMoveCount() { // 현재 놓인 돌 개수를 확인한다.
        return moves.size(); // 착수 기록의 개수가 돌 개수이다.
    }

    public int getWinner() { // 승리한 돌 색을 확인한다.
        return winner; // 아직 승리하지 않았으면 0이다.
    }

    public ArrayList<int[]> getWinningStones() { // 화면에서 승리한 돌 위치를 읽는다.
        return winningStones; // 이 목록은 화면에서 읽기만 한다.
    }

    public Move getLastMove() { // 화면에서 마지막 돌의 낙하를 그릴 때 사용한다.
        return moves.get(moves.size() - 1); // 착수 직후에만 호출하므로 기록이 존재한다.
    }

    public boolean isPiePending() { // 파이 룰 선택 중인지 확인한다.
        return piePending; // 선택을 기다리면 true이다.
    }

    public boolean isSwapped() { // 진영을 교환했는지 확인한다.
        return swapped; // 교환했다면 true이다.
    }

    public int getPlayer(int color) { // 돌 색에 해당하는 사람 번호를 구한다.
        if (swapped) { // 진영을 교환한 경우이다.
            return 3 - color; // 흑돌 1은 2P, 백돌 2는 1P가 된다.
        }
        return color; // 교환하지 않았다면 흑돌은 1P, 백돌은 2P이다.
    }

    public boolean isOver() { // 승리 또는 무승부로 끝났는지 확인한다.
        return winner != 0 || moves.size() == SIZE * SIZE * SIZE; // 승자가 있거나 모든 칸이 차면 끝난다.
    }

    public int getEmptyFloor(int x, int z) { // 선택한 기둥에서 가장 아래의 빈칸을 찾는다.
        if (x < 0 || x >= SIZE || z < 0 || z >= SIZE) { // 보드 밖 좌표를 걸러낸다.
            return -1; // 놓을 수 없다는 뜻이다.
        }
        for (int y = 0; y < SIZE; y++) { // 맨 아래층부터 위로 올라간다.
            if (board[x][y][z] == 0) { // 빈칸을 찾았는지 확인한다.
                return y; // 돌이 놓일 층을 돌려준다.
            }
        }
        return -1; // 맨 위까지 차 있으면 놓을 수 없다.
    }

    public boolean place(int x, int z) { // 현재 차례의 돌을 놓는다.
        if (isOver() || piePending) { // 게임 종료 또는 파이 룰 선택 중에는 놓지 못한다.
            return false; // 착수에 실패했다는 뜻이다.
        }
        int y = getEmptyFloor(x, z); // 돌이 놓일 층을 구한다.
        if (y == -1) { // 보드 밖이거나 가득 찬 기둥인지 확인한다.
            return false; // 기존 기록을 바꾸지 않고 종료한다.
        }
        Move move = new Move(x, y, z, turn, swapped); // 이번 착수의 정보를 만든다.
        addMove(move); // 보드와 착수 기록에 반영한다.
        undone.clear(); // 새로운 수를 두었으므로 예전에 되돌린 수는 버린다.
        return true; // 정상적으로 놓았다는 뜻이다.
    }

    private void addMove(Move move) { // 일반 착수와 다시 실행이 공통으로 사용하는 부분이다.
        board[move.x][move.y][move.z] = move.color; // 배열에 돌을 저장한다.
        moves.add(move); // 착수 기록의 마지막에 추가한다.
        if (checkWin(move)) { // 방금 놓은 돌로 5목 이상이 되었는지 확인한다.
            winner = move.color; // 승리한 돌 색을 저장한다.
        }
        turn = 3 - move.color; // 흑돌 다음은 백돌이고 백돌 다음은 흑돌이다.
        if (moves.size() == 1) { // 첫 번째 수를 놓은 경우이다.
            piePending = true; // 후공이 진영을 선택할 때까지 기다린다.
        }
    }

    public void choosePie(boolean swap) { // true이면 진영 교환, false이면 유지이다.
        if (piePending) { // 첫 수 직후에만 선택할 수 있다.
            swapped = swap; // 선택한 진영 상태를 저장한다.
            piePending = false; // 이제 다음 돌을 놓을 수 있다.
        }
    }

    public boolean canUndo() { // 되돌릴 수 있는지 확인한다.
        return !moves.isEmpty(); // 착수 기록이 있으면 되돌릴 수 있다.
    }

    public boolean canRedo() { // 다시 실행할 수 있는지 확인한다.
        return !undone.isEmpty(); // 되돌린 기록이 있으면 다시 실행할 수 있다.
    }

    public void undo() { // 마지막으로 놓은 돌을 하나 지운다.
        if (!canUndo()) { // 돌이 하나도 없는지 확인한다.
            return; // 지울 것이 없으므로 끝낸다.
        }
        Move move = moves.remove(moves.size() - 1); // 마지막 착수 기록을 꺼낸다.
        board[move.x][move.y][move.z] = 0; // 그 칸을 빈칸으로 바꾼다.
        undone.add(move); // 다시 실행할 수 있도록 기록한다.
        turn = move.color; // 방금 지운 돌의 색으로 차례를 돌린다.
        winner = 0; // 승리한 직후라도 되돌리면 게임을 계속할 수 있다.
        winningStones.clear(); // 승리한 돌의 표시도 지운다.
        if (moves.size() <= 1) { // 첫 수 선택 시점까지 되돌아온 경우이다.
            swapped = false; // 진영 교환을 초기화한다.
            piePending = moves.size() == 1; // 첫 돌만 남았다면 파이 룰을 다시 선택한다.
        }
    }

    public void redo() { // 마지막으로 되돌린 돌을 다시 놓는다.
        if (!canRedo() || isOver()) { // 다시 놓을 기록이 있는지 확인한다.
            return; // 다시 실행할 수 없으면 끝낸다.
        }
        Move move = undone.remove(undone.size() - 1); // 가장 최근에 되돌린 수를 꺼낸다.
        if (moves.size() == 1) { // 두 번째 수를 복원할 때이다.
            swapped = move.swapped; // 그 수를 둘 당시의 파이 룰 선택도 복원한다.
            piePending = false; // 선택이 복원되었으므로 기다림을 끝낸다.
        }
        addMove(move); // 같은 위치와 색으로 돌을 다시 놓는다.
    }

    public void reset() { // 새 게임을 시작할 때 모든 상태를 초기화한다.
        board = new int[SIZE][SIZE][SIZE]; // 모든 칸이 0인 새 배열을 만든다.
        moves.clear(); // 착수 기록을 비운다.
        undone.clear(); // 되돌린 기록도 비운다.
        turn = 1; // 흑돌부터 시작한다.
        winner = 0; // 승자가 없는 상태이다.
        winningStones.clear(); // 승리 표시를 초기화한다.
        piePending = false; // 아직 첫 수를 놓지 않았다.
        swapped = false; // 진영도 교환하지 않았다.
    }

    private boolean checkWin(Move move) { // 방금 놓은 돌을 지나는 직선들을 확인한다.
        for (int i = 0; i < directions.length; i++) { // 준비한 13개 방향을 하나씩 확인한다.
            int dx = directions[i][0]; // 가로로 이동하는 양이다.
            int dy = directions[i][1]; // 높이로 이동하는 양이다.
            int dz = directions[i][2]; // 앞뒤로 이동하는 양이다.
            int count = 1; // 방금 놓은 돌도 한 개로 센다.
            int forward = countStones(move, dx, dy, dz); // 한쪽 방향의 같은 색 돌 수이다.
            int backward = countStones(move, -dx, -dy, -dz); // 반대쪽 방향의 같은 색 돌 수이다.
            count += forward + backward; // 두 방향의 돌을 모두 더한다.
            if (count >= 5) { // 연속으로 다섯 개 이상 연결되었는지 확인한다.
                for (int k = -backward; k <= forward; k++) { // 연결된 직선의 처음부터 끝까지 이동한다.
                    winningStones.add(new int[]{move.x + dx * k, move.y + dy * k, move.z + dz * k}); // 표시할 좌표를 저장한다.
                }
                return true; // 5목을 완성했다.
            }
        }
        return false; // 모든 방향을 확인했지만 5목이 없다.
    }

    private int countStones(Move move, int dx, int dy, int dz) { // 한 방향의 연속된 돌 수를 센다.
        int count = 0; // 처음에는 아직 돌을 세지 않았다.
        int x = move.x + dx; // 시작 돌 바로 옆의 가로 좌표이다.
        int y = move.y + dy; // 시작 돌 바로 옆의 높이 좌표이다.
        int z = move.z + dz; // 시작 돌 바로 옆의 앞뒤 좌표이다.
        while (x >= 0 && x < SIZE && y >= 0 && y < SIZE && z >= 0 && z < SIZE) { // 보드 안에서만 조사한다.
            if (board[x][y][z] != move.color) { // 빈칸이나 다른 색 돌을 만났는지 확인한다.
                break; // 연결이 끊겼으므로 이 방향은 그만 센다.
            }
            count++; // 같은 색 돌을 하나 찾았다.
            x += dx; // 다음 가로 좌표로 이동한다.
            y += dy; // 다음 높이 좌표로 이동한다.
            z += dz; // 다음 앞뒤 좌표로 이동한다.
        }
        return count; // 이 방향에서 찾은 돌 개수를 돌려준다.
    }
}
