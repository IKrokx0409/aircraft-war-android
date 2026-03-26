package edu.hitsz;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import edu.hitsz.application.Game; // 导入你的 Game 类

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 实例化你重构好的 SurfaceView 游戏引擎
        Game game = new Game(this);

        // 把手机屏幕的视图直接替换成游戏画面
        setContentView(game);
    }
}