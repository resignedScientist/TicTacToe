package com.example.laudien.tictactoe.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.laudien.tictactoe.R;

import java.util.ArrayList;

import static com.example.laudien.tictactoe.MainActivity.animationDuration;
import static com.example.laudien.tictactoe.Objects.ArtificialIntelligence.EASY;
import static com.example.laudien.tictactoe.Objects.ArtificialIntelligence.HARD;
import static com.example.laudien.tictactoe.Objects.ArtificialIntelligence.MEDIUM;

public class SettingsFragment extends Fragment implements SeekBar.OnSeekBarChangeListener{
    public static final String PREFERENCES = "Settings";
    public static final String PREFERENCE_TIME = "savedTime";
    public static final String PREFERENCE_DIFFICULTY = "difficulty";
    public static final String PREFERENCE_ANIMATION_DURATION = "animationDuration";

    private SeekBar timeSeekBar, difficultySeekBar, seekBar_animation;
    private TextView timeTextView, difficultyTextView;
    private int time, difficulty;
    private ArrayList<OnSettingsChangedListener> listeners;
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listeners = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        timeSeekBar = (SeekBar)view.findViewById(R.id.timeSeekBar);
        difficultySeekBar = (SeekBar)view.findViewById(R.id.difficultySeekBar);
        seekBar_animation = (SeekBar)view.findViewById(R.id.seekBar_animation);
        seekBar_animation.setProgress((int)animationDuration);
        timeTextView = (TextView)view.findViewById(R.id.timeTextView);
        difficultyTextView = (TextView)view.findViewById(R.id.difficultyTextView);
        sharedPreferences = getActivity().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);

        // Initialize timeSeekBar
        timeSeekBar.setProgress(sharedPreferences.getInt(PREFERENCE_TIME, 20));
        timeTextView.setText(Integer.toString(timeSeekBar.getProgress()));
        timeSeekBar.setOnSeekBarChangeListener(this);

        // Initialize difficultySeekBar
        difficultySeekBar.setProgress(sharedPreferences.getInt(PREFERENCE_DIFFICULTY, 1));
        difficultyTextView.setText(difficultyToString(getContext(), difficultySeekBar.getProgress()));
        difficultySeekBar.setOnSeekBarChangeListener(this);

        // initialize seekbar_animation
        seekBar_animation.setOnSeekBarChangeListener(this);

        return view;
    }
    public static String difficultyToString(Context context, int difficulty){
        String diffString = "";
        switch (difficulty){
            case EASY:
                diffString = context.getResources().getString(R.string.easy);
                break;
            case MEDIUM:
                diffString = context.getResources().getString(R.string.medium);
                break;
            case HARD:
                diffString = context.getResources().getString(R.string.hard);
        }
        return diffString;
    }

    @Override
    public void onPause() {
        super.onPause();

        if(time != timeSeekBar.getProgress()){
            sharedPreferences.edit().putInt(PREFERENCE_TIME,timeSeekBar.getProgress()).apply();
            for(OnSettingsChangedListener listener : listeners)
                listener.onSettingsChanged(PREFERENCE_TIME);
        }
        if(difficulty != difficultySeekBar.getProgress()){
            sharedPreferences.edit().putInt(PREFERENCE_DIFFICULTY, difficultySeekBar.getProgress()).apply();
            Log.i("SettingsFragment", "Difficulty changed!");
            for(OnSettingsChangedListener listener : listeners)
                listener.onSettingsChanged(PREFERENCE_DIFFICULTY);
        }
        if(animationDuration != seekBar_animation.getProgress()){
            animationDuration = seekBar_animation.getProgress();
            sharedPreferences.edit().putLong(PREFERENCE_ANIMATION_DURATION, animationDuration).apply();
            for(OnSettingsChangedListener listener : listeners)
                listener.onSettingsChanged(PREFERENCE_ANIMATION_DURATION);
        }
    }

    public void addOnSettingsChangedListener(OnSettingsChangedListener listener){
        listeners.add(listener);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        switch (seekBar.getId()){
            case R.id.timeSeekBar:
                if(seekBar.getProgress() < 5){seekBar.setProgress(5);}
                timeTextView.setText(Integer.toString(seekBar.getProgress()));
                break;
            case R.id.difficultySeekBar:
                difficultyTextView.setText(difficultyToString(getContext(), i));
                break;
            case R.id.seekBar_animation:
                if(i < 100) seekBar.setProgress(100);
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public interface OnSettingsChangedListener{
        void onSettingsChanged(String preference);
    }
}
