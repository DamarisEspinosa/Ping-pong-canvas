package com.z_iti_271311_u3_espinosa_castro_damaris_alexia;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity {
    private GameView gameView;

    private TextView scorePlayer1;
    private TextView scorePlayer2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_game);

        scorePlayer1 = findViewById(R.id.scorePlayer1);
        scorePlayer2 = findViewById(R.id.scorePlayer2);

        FrameLayout gameContainer = findViewById(R.id.gameContainer);
        gameView = new GameView(this);
        gameContainer.addView(gameView);

        // Configurar el listener para actualizar los puntajes
        gameView.setScoreUpdateListener(new GameView.ScoreUpdateListener() {
            @Override
            public void onScoreUpdate(final int player1Score, final int player2Score) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        scorePlayer1.setText(String.valueOf(player1Score));
                        scorePlayer2.setText(String.valueOf(player2Score));
                    }
                });
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameView.pauseGame();
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameView.resumeGame();
    }
}
