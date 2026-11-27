package beginner; // 게임의 다른 클래스들과 같은 패키지를 사용한다.

// 돌 하나를 놓았을 때의 정보를 저장한다. 되돌리기와 다시 실행에 사용한다.
public class Move { // 착수 정보 하나를 담는 클래스이다.
    public final int x; // 가로 위치이다. final이므로 저장한 뒤 바꾸지 않는다.
    public final int y; // 층 위치이다. 0층이 가장 아래이다.
    public final int z; // 앞뒤 위치이다.
    public final int color; // 1은 흑돌이고 2는 백돌이다.
    public final boolean swapped; // 이 수를 놓을 당시 진영을 교환했는지 저장한다.

    public Move(int x, int y, int z, int color, boolean swapped) { // 착수 기록을 만든다.
        this.x = x; // 매개변수 x를 이 객체의 x에 저장한다.
        this.y = y; // 매개변수 y를 이 객체의 y에 저장한다.
        this.z = z; // 매개변수 z를 이 객체의 z에 저장한다.
        this.color = color; // 놓은 돌의 색을 저장한다.
        this.swapped = swapped; // 진영 교환 여부를 저장한다.
    }
}
