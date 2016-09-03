package com.example.laudien.tictactoe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Random;

public class GameActivity extends AppCompatActivity {

    // Der aktive Spieler; 0 = rot, 1 = gelb
    int activePlayer = 0;
    // Gewinnerpositionen
    int [][] winningPositions = {{0,1,2},{3,4,5},{6,7,8},{0,3,6},{1,4,7},{2,5,8},{0,4,8},{2,4,6}};
    // Status der jeweiligen Position; 0 = rot, 1 = gelb, 2 = unbelegt
    int[] positionState = {2,2,2,2,2,2,2,2,2};
    // Gibt an, ob das Spiel noch läuft
    boolean gameIsRunning = true;
    // Zeigt an, wer gewonnen hat
    int winner = 2;
    // der Player, der den Applaus abspielt
    MediaPlayer applause;
    // Gong
    MediaPlayer gong;
    // das Schiff
    ImageView ship;
    MediaPlayer shipSound;
    // Timer
    CountDownTimer timer;
    TextView counterTextView;
    // der zuletzt gesetzte Chip
    ImageView chip;
    // die Zeit, die der Spieler für seinen Zug hat
    Long playerTime;
    // the grid layout
    GridLayout board;
    Boolean kiIsUsed;

    public void placeChip(View view){
        chip = (ImageView) view;
        TextView winnerText = (TextView) findViewById(R.id.winnerText);
        LinearLayout winnerLayout = (LinearLayout) findViewById(R.id.winnerLayout);
        String winnerMessage = "";

        timer.start();

        if(positionState[Integer.parseInt(chip.getTag().toString())] == 2
                && gameIsRunning) {
            chip.setTranslationY(-1000f);
            chip.setImageResource(R.drawable.red);
            if (activePlayer == 1) {
                chip.setImageResource(R.drawable.yellow);
            }
            chip.animate().translationY(0f).rotation(3600).setDuration(300);

            // positionState verändern
            positionState[Integer.parseInt(chip.getTag().toString())] = activePlayer;

            // Prüfe, ob jemand gewonnen hat - und wenn ja: wer?
            for (int[] winningPosition : winningPositions) {
                if (positionState[winningPosition[0]] == activePlayer
                        && positionState[winningPosition[1]] == activePlayer
                        && positionState[winningPosition[2]] == activePlayer) {

                    // Applaus einspielen
                    applause.start();

                    // Gewinnernachricht in einen String packen
                    winnerMessage = getString(R.string.red_wins);
                    if (activePlayer == 1) {
                        winnerMessage = getString(R.string.yellow_wins);
                    }

                    // Anzeigen, wer gewonnen hat
                    winner = activePlayer;

                    // Spielfeld sperren
                    gameIsRunning = false;
                }
            }

            // Prüfe, ob unentschieden
            if (gameIsRunning && winner == 2) {
                boolean isUndecided = true;
                for (int currentPosition : positionState) {
                    if (currentPosition == 2) {
                        isUndecided = false;
                    }
                }
                // Zeige 'Unentschieden' an:
                if (isUndecided) {
                    winnerMessage = getString(R.string.draw);
                    gameIsRunning = false;
                }
            }

            if (!gameIsRunning) {
                // Countdown ausblenden und Timer zurücksetzen
                timer.cancel();
                counterTextView.setVisibility(View.INVISIBLE);

                // Im Textfeld die Gewinnernachricht anzeigen
                winnerText.setText(winnerMessage);

                // Winner-Layout sichtbar machen
                winnerLayout.setVisibility(winnerLayout.VISIBLE);
            }

            // aktiven Spieler ändern
            if (activePlayer == 1) {
                activePlayer = 0;
            } else {
                activePlayer = 1;
            }
        }
        // let the KI set its stone
        if (activePlayer == 1 && kiIsUsed && gameIsRunning) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    artificialIntelligence();
                }
            },2000);
        }
    }

    // Wenn auf "New Game" geklickt wurde
    public void newGame(View view){
        // Rot beginnt wieder
        activePlayer = 0;

        // Setzt den Gewinner zurück
        winner = 2;

        // Spielfeld zurücksetzen
        for(int i = 0; i <= 8; i++){
            positionState[i] = 2;
        }

        // Chips entfernen
        GridLayout board = (GridLayout)findViewById(R.id.board);
        for(int i = 0; i <= 8; i++){
           ((ImageView)board.getChildAt(i)).setImageResource(0);
        }

        // winnerLayout wieder unsichtbar machen
        LinearLayout winnerLayout = (LinearLayout) findViewById(R.id.winnerLayout);
        winnerLayout.setVisibility(winnerLayout.INVISIBLE);

        // den Applaus stoppen
        applause.stop();
        applause = MediaPlayer.create(this,R.raw.small_crowd_applause);

        // Timer neustarten, den Countdown sichtbar machen und auf playerTime setzen
        timer.cancel();
        counterTextView.setText(Long.toString(playerTime/1000));
        counterTextView.setVisibility(View.VISIBLE);
        timer.start();

        // Gong resetten und starten
        gong.stop();
        gong = MediaPlayer.create(this,R.raw.gong);
        gong.start();

        // Gibt das Spielfeld für Benutzereingaben frei
        gameIsRunning = true;
    }
    void artificialIntelligence(){
        // every time its the yellow player!
        // 1. defense an attack of the player:
        int position = searchPositions(0);
        int rand = 0;
        if(position != -1){
            placeChip((ImageView) board.getChildAt(position));
        } else{ // 2. if no enemy winning position was found, attack the player:
            position = searchPositions(1);
            if(position != -1){
                placeChip((ImageView) board.getChildAt(position));
            }else { // 3. if no possible win was found, place random:
                rand = new Random().nextInt(9);
                while (positionState[rand] != 2) {
                    rand = new Random().nextInt(9);
                }
                placeChip(board.getChildAt(rand));
            }
        }
    }

    int searchPositions(int playerID){
        // searches for a position where are 2/3 used from the player with playerID
        int counter;
        for(int[] possiblePosition : winningPositions){
            counter = 0;
            if(positionState[possiblePosition[0]] == playerID){
                counter++;
            }
            if(positionState[possiblePosition[1]] == playerID){
                counter++;
            }
            if(positionState[possiblePosition[2]] == playerID){
                counter++;
            }
            if(counter >= 2){
                // give back the position of the empty field that is needed for player with playerID to win
                if(positionState[possiblePosition[0]] == 2){
                    return possiblePosition[0];
                }
                if(positionState[possiblePosition[1]] == 2){
                    return possiblePosition[1];
                }
                if(positionState[possiblePosition[2]] == 2){
                    return possiblePosition[2];
                }
            }
        }
        // if nothing was found, return -1
        return -1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        board = (GridLayout)findViewById(R.id.board);

        shipSound = MediaPlayer.create(this,R.raw.foghorn);

        // get the players time from the shared preferences
        SharedPreferences savedTime = getSharedPreferences("savedTime",0);
        playerTime = (long)1000 * savedTime.getInt("savedTime",0);

        // get the information if the player plays against the KI
        SharedPreferences usesKI = getSharedPreferences("kiIsUsed",0);
        kiIsUsed = usesKI.getBoolean("kiIsUsed",false);

        // set the counter to the set time
        counterTextView = (TextView)findViewById(R.id.counterTextView);
        counterTextView.setText(Long.toString(playerTime/1000));

        // Button 'New Game' beschriften
        Button newGame = (Button)findViewById(R.id.newGame);
        newGame.setText(getString(R.string.new_game));

        // dem Mediaplayer für den Applaus den Sound hinzufügen
        applause = MediaPlayer.create(this,R.raw.small_crowd_applause);

        ship = (ImageView)findViewById(R.id.shipView);
        ship.setTranslationX(+1000f);

        timer = new CountDownTimer(100 + playerTime,1000) {
            @Override
            public void onTick(long l) {
                counterTextView.setText(Long.toString(l/1000));
            }

            @Override
            public void onFinish() {
                if(gameIsRunning){showShip();}
            }
        };

        // Play the gong sound
        gong = MediaPlayer.create(this,R.raw.gong);
        gong.start();

        // start the timer
        timer.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        timer.cancel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        timer.start();
    }

    void showShip(){
        timer.cancel();
        int rand = new Random().nextInt(9);

        // bestimme das nächste leere Feld
        while (positionState[rand] != 2) {
            rand = new Random().nextInt(9);
        }
        chip = (ImageView) board.getChildAt(rand);

        shipSound.start();
        ship.setTranslationX(0f);
        placeChip(chip);
        ship.animate().translationX(-1000f).setDuration(5000);
    }
}