package beginner; // 새로 만든 클래스들을 beginner 패키지에 모은다.

import javafx.application.Application; // JavaFX 프로그램을 시작할 때 사용하는 클래스이다.

// 실행 버튼을 누르는 시작 클래스이다. 화면과 게임 규칙은 다른 파일에 있다.
public class Main { // 실행을 시작하는 클래스이다.
    public static void main(String[] args) { // 자바 프로그램이 처음 실행하는 메서드이다.
        Application.launch(OmokApp.class, args); // OmokApp의 start 메서드를 실행한다.
    }
}
