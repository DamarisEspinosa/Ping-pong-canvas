package com.z_iti_271311_u3_espinosa_castro_damaris_alexia;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

public class GameView extends View {
    private Paint paint;
    private int paddleWidth = 25;
    private int paddleHeight = 180;
    private int ballRadius = 25;

    private float leftPaddleY;
    private float rightPaddleY;
    private final int PADDLE_MARGIN = 30;

    // Variables para la pelota
    private float ballX;
    private float ballY;
    private float ballSpeedX = 10;  // Velocidad horizontal
    private float ballSpeedY = 10;  // Velocidad vertical
    private final float INITIAL_BALL_SPEED = 10; // Nueva constante para la velocidad inicial
    private final float MAX_PADDLE_EFFECT = 10; // Reducida de 15 a 8 para el efecto de las paletas


    // Variable para el bucle del juego
    private Handler handler;
    private final int FRAME_RATE = 60;
    private boolean isPlaying = true;

    private int player1Score = 0;
    private int player2Score = 0;
    private boolean isWaitingToStart = false;
    private long startWaitTime = 0;
    private static final long WAIT_TIME = 3000;

    private static final int WINNING_SCORE = 11;
    private boolean gameOver = false;
    private String winnerMessage = "";
    private int winnerColor = Color.GREEN;
    private int winningPlayer = 0; // 0 = ninguno, 1 = jugador 1, 2 = jugador 2
    private RectF restartButtonBounds;
    private boolean isRestartButtonVisible = false;
    private Paint buttonPaint;

    public interface ScoreUpdateListener {
        void onScoreUpdate(int player1Score, int player2Score);
    }

    private ScoreUpdateListener scoreUpdateListener;

    public void setScoreUpdateListener(ScoreUpdateListener listener) {
        this.scoreUpdateListener = listener;
    }


    public GameView(Context context) {
        super(context);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        // Inicializar el paint para el botón
        buttonPaint = new Paint();
        buttonPaint.setAntiAlias(true);
        restartButtonBounds = new RectF();

        leftPaddleY = 0;
        rightPaddleY = 0;
        handler = new Handler();
        setSystemUiVisibility(
                SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | SYSTEM_UI_FLAG_FULLSCREEN
                        | SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private void restartGame() {
        // Reiniciar todas las variables del juego
        player1Score = 0;
        player2Score = 0;
        gameOver = false;
        isRestartButtonVisible = false;
        winnerMessage = "";
        winningPlayer = 0;

        // Notificar el cambio de puntaje
        if (scoreUpdateListener != null) {
            scoreUpdateListener.onScoreUpdate(player1Score, player2Score);
        }

        // Reiniciar la pelota y el juego
        ballX = getWidth() / 2;
        ballY = getHeight() / 2;
        // Restaurar las velocidades iniciales
        ballSpeedX = INITIAL_BALL_SPEED;
        ballSpeedY = INITIAL_BALL_SPEED;
        isWaitingToStart = true;
        startWaitTime = System.currentTimeMillis();
        isPlaying = true;
        startGame();

        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // Centrar las paletas y la pelota
        leftPaddleY = (h - paddleHeight) / 2;
        rightPaddleY = (h - paddleHeight) / 2;
        ballX = w / 2;
        ballY = h / 2;

        // Iniciar el bucle del juego
        startGame();
    }

    private void startGame() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isPlaying) {
                    updateGame();
                    handler.postDelayed(this, 1000 / FRAME_RATE);
                }
            }
        }, 1000 / FRAME_RATE);
    }

    private void updateGame() {
        if (gameOver) {
            invalidate();
            return;
        }

        if (isWaitingToStart) {
            if (System.currentTimeMillis() - startWaitTime >= WAIT_TIME) {
                isWaitingToStart = false;
                // Mantener la velocidad constante pero dar dirección aleatoria
                ballSpeedX = (Math.random() > 0.5 ? INITIAL_BALL_SPEED : -INITIAL_BALL_SPEED);
                ballSpeedY = (Math.random() > 0.5 ? INITIAL_BALL_SPEED : -INITIAL_BALL_SPEED);
            }
            invalidate();
            return;
        }

        ballX += ballSpeedX;
        ballY += ballSpeedY;

        if (ballY <= ballRadius || ballY >= getHeight() - ballRadius) {
            ballSpeedY = -ballSpeedY;
        }

        // Colisión con paleta izquierda
        if (ballX <= PADDLE_MARGIN + paddleWidth + ballRadius &&
                ballY >= leftPaddleY &&
                ballY <= leftPaddleY + paddleHeight) {
            ballX = PADDLE_MARGIN + paddleWidth + ballRadius;
            ballSpeedX = Math.abs(INITIAL_BALL_SPEED); // Asegurar velocidad constante
            float relativeIntersectY = (leftPaddleY + (paddleHeight / 2)) - ballY;
            float normalizedRelativeIntersectionY = (relativeIntersectY / (paddleHeight / 2));
            ballSpeedY = normalizedRelativeIntersectionY * -MAX_PADDLE_EFFECT;
        }

        // Colisión con paleta derecha
        if (ballX >= getWidth() - PADDLE_MARGIN - paddleWidth - ballRadius &&
                ballY >= rightPaddleY &&
                ballY <= rightPaddleY + paddleHeight) {
            ballX = getWidth() - PADDLE_MARGIN - paddleWidth - ballRadius;
            ballSpeedX = -Math.abs(INITIAL_BALL_SPEED); // Asegurar velocidad constante
            float relativeIntersectY = (rightPaddleY + (paddleHeight / 2)) - ballY;
            float normalizedRelativeIntersectionY = (relativeIntersectY / (paddleHeight / 2));
            ballSpeedY = normalizedRelativeIntersectionY * -MAX_PADDLE_EFFECT;
        }

        // Verificar si la pelota sale de los límites y actualizar puntajes
        if (ballX < 0) {
            player2Score++;
            checkWinCondition();
            if (scoreUpdateListener != null) {
                scoreUpdateListener.onScoreUpdate(player1Score, player2Score);
            }
            if (!gameOver) {
                resetBall();
            }
        } else if (ballX > getWidth()) {
            player1Score++;
            checkWinCondition();
            if (scoreUpdateListener != null) {
                scoreUpdateListener.onScoreUpdate(player1Score, player2Score);
            }
            if (!gameOver) {
                resetBall();
            }
        }

        invalidate();
    }

    private void checkWinCondition() {
        if (player1Score >= WINNING_SCORE) {
            gameOver = true;
            winnerMessage = "¡JUGADOR AMARILLO HA GANADO!";
            winningPlayer = 1;
            stopGame();
        } else if (player2Score >= WINNING_SCORE) {
            gameOver = true;
            winnerMessage = "¡JUGADOR AZUL HA GANADO!";
            winningPlayer = 2;
            stopGame();
        }
    }

    private void stopGame() {
        isPlaying = false;
        ballSpeedX = 0;
        ballSpeedY = 0;
        isRestartButtonVisible = true;
    }

    private void resetBall() {
        ballX = getWidth() / 2;
        ballY = getHeight() / 2;
        // No reiniciar la velocidad a 0, solo esperar
        isWaitingToStart = true;
        startWaitTime = System.currentTimeMillis();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Verificar si se hizo clic en el botón de reinicio
        if (gameOver && event.getAction() == MotionEvent.ACTION_DOWN) {
            float touchX = event.getX();
            float touchY = event.getY();

            if (restartButtonBounds.contains(touchX, touchY)) {
                restartGame();
                return true;
            }
        }

        // Solo procesar toques si el juego no ha terminado
        if (!gameOver) {
            int pointerCount = event.getPointerCount();
            float minY = 0;
            float maxY = getHeight() - paddleHeight;
            for (int i = 0; i < pointerCount; i++) {
                float touchX = event.getX(i);
                float touchY = event.getY(i);
                if (touchX < getWidth() / 2) {
                    leftPaddleY = Math.min(Math.max(touchY - paddleHeight / 2, minY), maxY);
                } else {
                    rightPaddleY = Math.min(Math.max(touchY - paddleHeight / 2, minY), maxY);
                }
            }
            invalidate();
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Fondo negro
        canvas.drawColor(Color.parseColor("#3E8C1E"));
        if (!gameOver) {
            // Línea central
            paint.setColor(Color.WHITE);
            paint.setStrokeWidth(7);
            canvas.drawLine(getWidth()/2, 0, getWidth()/2, getHeight(), paint);

            // Paddle izquierdo (amarillo)
            paint.setColor(Color.YELLOW);
            canvas.drawRect(
                    PADDLE_MARGIN,
                    leftPaddleY,
                    PADDLE_MARGIN + paddleWidth,
                    leftPaddleY + paddleHeight,
                    paint
            );

            // Paddle derecho (azul)
            paint.setColor(Color.BLUE);
            canvas.drawRect(
                    getWidth() - paddleWidth - PADDLE_MARGIN,
                    rightPaddleY,
                    getWidth() - PADDLE_MARGIN,
                    rightPaddleY + paddleHeight,
                    paint
            );

            // Pelota (blanca)
            paint.setColor(Color.WHITE);
            canvas.drawCircle(
                    ballX,
                    ballY,
                    ballRadius,
                    paint
            );

            // Si está esperando para iniciar, mostrar la cuenta regresiva
            if (isWaitingToStart) {
                paint.setColor(Color.BLACK);
                paint.setTextSize(50);
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                long remainingTime = (WAIT_TIME - (System.currentTimeMillis() - startWaitTime)) / 1000 + 1;
                canvas.drawText(String.valueOf(remainingTime), getWidth()/2, getHeight()/2 - 50, paint);
            }
        } else {
            // Dibujar mensaje de victoria
            paint.setColor(Color.GREEN);  // Color del texto
            paint.setTextSize(70);        // Tamaño grande para el mensaje
            paint.setTextAlign(Paint.Align.CENTER);

            // Dibujar el mensaje con el color del jugador ganador
            String playerColor = winningPlayer == 1 ? "AMARILLO" : "AZUL";
            paint.setColor(Color.GREEN);
            canvas.drawText("¡JUGADOR", getWidth()/2, getHeight()/2 - 40, paint);

            // Cambiar al color del jugador ganador
            paint.setColor(winningPlayer == 1 ? Color.YELLOW : Color.BLUE);
            canvas.drawText(playerColor, getWidth()/2, getHeight()/2 + 40, paint);

            paint.setColor(Color.GREEN);
            canvas.drawText("HA GANADO!", getWidth()/2, getHeight()/2 + 120, paint);

            // Dibujar botón de reinicio
            int buttonWidth = 400;
            int buttonHeight = 100;
            restartButtonBounds.set(
                    getWidth()/2 - buttonWidth/2,
                    getHeight()/2 + 200,
                    getWidth()/2 + buttonWidth/2,
                    getHeight()/2 + 200 + buttonHeight
            );
            // Fondo del botón
            buttonPaint.setColor(Color.BLACK);
            canvas.drawRoundRect(restartButtonBounds, 20, 30, buttonPaint);
            // Texto del botón
            paint.setColor(Color.WHITE);
            paint.setTextSize(40);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("VOLVER A JUGAR",
                    restartButtonBounds.centerX(),
                    restartButtonBounds.centerY() + 15,
                    paint);
        }
    }

    // Asegurarse de detener el juego cuando la vista se destruye
    public void pauseGame() {
        isPlaying = false;
    }

    public void resumeGame() {
        isPlaying = true;
        startGame();
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus) {
            setSystemUiVisibility(
                    SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | SYSTEM_UI_FLAG_FULLSCREEN
                            | SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }
}
