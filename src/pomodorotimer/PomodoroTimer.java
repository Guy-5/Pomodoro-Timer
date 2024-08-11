/*  
 *  Pomodorotimer v1.2
 *  This program is a timer that utilizes the pomodoro method 
*/

package pomodorotimer;

// Swing components
import javax.swing.JFrame;
import javax.swing.*;
import java.awt.*;
// Widget
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
// Icon
import java.awt.Image;
import javax.swing.ImageIcon;
// Alarm
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.AudioInputStream;
import java.net.URL;

public class PomodoroTimer extends JFrame {
    
    // Adds JFrame elements
    // JFrame buttons
    private final JButton studyTimerBtn;
    private final JButton startPauseBtn;
    private final JButton shortBreakBtn;
    private final JButton longBreakBtn;
    //******************************
    
    //JFrame panel, label, and window
    private JLabel timerLabel; // Shows the time
    private final JLabel title; // Title of the application
    private final JLabel rounds; // How many rounds have been completed
    private JWindow widget;
    private JTextArea toDo; // To-do
    //******************************
    
    // Variables
    private Timer studyTimer;
    private Timer shortBreakTimer;
    private Timer longBreakTimer;
    private int minutesStudy = 25; // If set to 1 it is for testing purposes and I forgot to change it back
    private int seconds = 0;
    private int minutesShort = 5; // If set to 1 it is for testing purposes and I forgot to change it back
    private int minutesLong = 15; // If set to 1 it is for testing purposes and I forgot to change it back
    private int roundsComplete = 0;
    private boolean isRunning; // Is the timer currently running or paused
    private boolean isShortBreak; // Is the timer on short break
    private boolean isLongBreak; // Is the timer on long break
    private boolean isStudySession; // Determines if it counts as a round
    
    private int mouseX, mouseY; // Used for widget dragging
    //******************************

    public PomodoroTimer() {
        // Gets icon
        ImageIcon icon = new ImageIcon(getClass().getResource("/resources/timer.png"));
        Image image = icon.getImage();
        // Adds icon 
        setIconImage(image);
        //******************************
        
        // Title and screen size
        setTitle("Pomodoro Timer");
        setSize(600,325);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //******************************
        
        // Title
        title = new JLabel("Pomodoro Timer");
        title.setFont(new Font("Arial", Font.BOLD, 50));
        //******************************
        
        // Label
        timerLabel = new JLabel();
        timerLabel.setFont(new Font("Arial", Font.PLAIN, 100));
        rounds = new JLabel("Rounds Completed: " + roundsComplete);
        rounds.setFont(new Font("Arial", Font.PLAIN, 15));
        //******************************
        
        // Text Area
        toDo = new JTextArea();
        toDo.setLineWrap(true); // Allows for line wrap
        toDo.setWrapStyleWord(true); // Allows words to wrap
        //******************************
        
        // Buttons
        studyTimerBtn = new JButton("Study");
        studyTimerBtn.setFont(new Font("Arial", Font.PLAIN, 10));
        startPauseBtn = new JButton("Start/Pause");
        startPauseBtn.setFont(new Font("Arial", Font.PLAIN, 10));
        shortBreakBtn = new JButton("Short Break");
        shortBreakBtn.setFont(new Font("Arial", Font.PLAIN, 10));
        longBreakBtn = new JButton("Long Break");
        longBreakBtn.setFont(new Font("Arial", Font.PLAIN, 10));
        //******************************
        
        // Adds the components and sets the location
        setLayout(null);
        add(toDo);
        toDo.setBounds(3, 70, 200, 230);
        add(title);
        title.setBounds(90, 5, 500, 50);
        add(timerLabel);
        timerLabel.setBounds(205, 70, 265, 90);
        add(rounds);
        rounds.setBounds(410, 170, 275, 50);
        add(startPauseBtn);
        startPauseBtn.setBounds(295, 185, 100, 25);
        add(studyTimerBtn);
        studyTimerBtn.setBounds(475, 75, 100, 25);
        add(shortBreakBtn);
        shortBreakBtn.setBounds(475, 105, 100, 25);
        add(longBreakBtn);
        longBreakBtn.setBounds(475, 135, 100, 25);
        //******************************

        // Base time
        timerLabel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 3));
        timerLabel.setText(String.format("%02d:" + "%02d", 0, seconds));
        //******************************

        // Initialize the study timer
        studyTimer = new Timer(1000, e -> {
            updateStudyTimer();
            updateWidgetTimer();
        }); //******************************
        
        // Initialize short break timer
        shortBreakTimer = new Timer(1000, e -> {
            updateShortBreakTimer();
            updateWidgetTimer();
        }); //******************************
    
        // Initialize long break timer
        longBreakTimer = new Timer(1000, e -> {
            updateLongBreakTimer();
            updateWidgetTimer();
        }); //******************************
    
        // Button Actions
        
        // Pause/Start Button
        startPauseBtn.addActionListener(e -> {
            if (!isRunning) {
                startTimer();
            } else {
                pauseTimer();
            }
        }); //******************************
        
        // Study button
        studyTimerBtn.addActionListener(e -> {
            if (!isRunning) {
                resetTimer(25, true);
            } else {
                pauseTimer();
                resetTimer(25, true);
            }
        }); //******************************

        // Short Break Button
        shortBreakBtn.addActionListener(e -> {
            if (!isRunning) {
                isShortBreak = true;
                isLongBreak = false;
                resetTimer(5, false);
            } else {
                pauseTimer();
                isShortBreak = true;
                isLongBreak = false;
                resetTimer(5, false);
            }
        }); //******************************

        // Long Break Button
        longBreakBtn.addActionListener(e -> {
            if (!isRunning) {
                isShortBreak = false;
                isLongBreak = true;
                resetTimer(15, false);
            } else {
                pauseTimer();
                isShortBreak = false;
                isLongBreak = true;
                resetTimer(15, false);
            }
        }); //******************************
        
        // Creates the widget
        createWidget();
        // Action for the widget
        addWindowStateListener(e -> {
            if ((e.getNewState() & Frame.ICONIFIED) == Frame.ICONIFIED) {
                widget.setVisible(true);
            } else {
                widget.setVisible(false);
            }
        }); //******************************
    } // End of public pomodorotimer()
    
    // Widget class
    private void createWidget() {
        widget = new JWindow();
        JLabel widgetLabel = new JLabel(String.format("%02d:%02d", minutesStudy, seconds), SwingConstants.CENTER);
        widgetLabel.setFont(new Font("Arial", Font.PLAIN, 25));
        widget.add(widgetLabel);
        widget.setSize(80, 40);
        widget.setLocationRelativeTo(null);
        widget.setAlwaysOnTop(true);
        
        // Add mouse listeners for dragging
        widget.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }
        }); //******************************
        
        widget.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int x = e.getXOnScreen() - mouseX;
                int y = e.getYOnScreen() - mouseY;
                widget.setLocation(x, y);
            }
        }); //******************************
        
    } //******************************
    
    // Update the widget timer
    private void updateWidgetTimer() {
        JLabel widgetLabel = (JLabel) widget.getContentPane().getComponent(0);
        if (isStudySession) {
            widgetLabel.setText(String.format("%02d:%02d", minutesStudy, seconds));
        } else if (isShortBreak) {
            widgetLabel.setText(String.format("%02d:%02d", minutesShort, seconds));
        } else if (isLongBreak) {
            widgetLabel.setText(String.format("%02d:%02d", minutesLong, seconds));
        }
    } //******************************
        
    // Updates the timer label
    private void resetTimer(int minutes, boolean isStudy) {
        isStudySession = isStudy;
        if (isStudy) {
            minutesStudy = minutes;
            seconds = 0;
            isRunning = false;
            updateTimerLabel(minutesStudy, seconds);
        } else {
            if (isShortBreak) {
                minutesShort = minutes;
                seconds = 0;
                isRunning = false;
                updateTimerLabel(minutesShort, seconds);
            } else if (isLongBreak) {
                minutesLong = minutes;
                seconds = 0;
                isRunning = false;
                updateTimerLabel(minutesLong, seconds);
            }
        }
    } //******************************
    
    // Starts the timer
    private void startTimer() {
        isRunning = true;
        if(isStudySession) {
            studyTimer.start();
        } else if(isShortBreak) {
            shortBreakTimer.start();
        } else if(isLongBreak) {
            longBreakTimer.start();
        }
    } //******************************
    
    // Pauses the timer
    private void pauseTimer() {
        isRunning = false;
        if(isStudySession) {
            studyTimer.stop();
        } else if(isShortBreak) {
            shortBreakTimer.stop();
        } else if(isLongBreak) {
            longBreakTimer.stop();
        }
    } //******************************
    
    // Updates the timer for the study session
    private void updateStudyTimer() {
        if(seconds == 0) {
            if(minutesStudy == 0) {
                studyTimer.stop();
                isRunning = false;
                roundsComplete++;
                playAlarm();
                rounds.setText("Rounds Completed: " + roundsComplete);
                if(roundsComplete % 3 != 0) {
                    isShortBreak = true;
                    isLongBreak = false;
                    resetTimer(5, false);
                } else {
                    isShortBreak = false;
                    isLongBreak = true;
                    resetTimer(15, false);
                }
                return;  
            }
            minutesStudy--;
            seconds = 59;
        } else {
            seconds--;
        }
        updateTimerLabel(minutesStudy, seconds);
    } //******************************
    
    // Updates the timer label for short break
    private void updateShortBreakTimer() {
        if(seconds == 0) {
            if(minutesShort == 0) {
                shortBreakTimer.stop();
                isRunning = false;
                playAlarm();
                resetTimer(25, true);
                return;
            }
            minutesShort--;
            seconds = 59;
        } else {
            seconds--;
        }
        updateTimerLabel(minutesShort, seconds);
    } //******************************
    
    // Updates the timer for long break
    private void updateLongBreakTimer() {
        if(seconds == 0) {
            if(minutesLong == 0) {
                longBreakTimer.stop();
                isRunning = false;
                playAlarm();
                resetTimer(25, true);
                return;
            }
            minutesLong--;
            seconds = 59;
        } else {
            seconds--;
        }
        updateTimerLabel(minutesLong, seconds);
    } //******************************
    
    // Updates the timer as each second passes by
    private void updateTimerLabel(int minutes, int seconds) {
        timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
    } //*****************************
    
    // Alarm method
    public void playAlarm() {
        try {
            URL soundURL = getClass().getResource("/resources/alarm.wav");
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundURL);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (Exception e){
            e.printStackTrace();
        }
    } //******************************
    
    // Main argument
    public static void main(String[] args) {
        PomodoroTimer frame = new PomodoroTimer();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}