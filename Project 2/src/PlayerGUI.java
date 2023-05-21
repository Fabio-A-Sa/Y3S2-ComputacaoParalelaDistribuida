import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

public class PlayerGUI {

    private final JFrame f;

    private final JLabel timeLabel = new JLabel();
    private int currentTime = 0;

    //Timer that fires every 1 second
    private final Timer timer = new Timer(1000, e -> {
        currentTime++;
        timeLabel.setText("Time: " + currentTime + "s");
    });

    private final int timeout;

    JButton rollButton = new JButton("Roll");

    JLabel roundLabel = new JLabel("");
    JLabel diceLabel = new JLabel("0");
    JLabel opponentDiceLabel = new JLabel("0");

    public PlayerGUI(int timeoutTime) {
        f = new JFrame("Dice Meister");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        f.setSize(400, 400);
        f.setLayout(null);//using no layout managers
        f.setVisible(true);

        roundLabel.setBounds(50, 50, 100, 30);
        diceLabel.setBounds(50, 100, 300, 30);
        opponentDiceLabel.setBounds(50, 150, 300, 30);
        rollButton.setBounds(50, 200, 100, 30);

        this.timeout = timeoutTime;

        timer.start();
    }

    private void clearInterface() {
        f.getContentPane().removeAll();
        paintInterface();
    }

    private void paintInterface() {
        f.revalidate();
        f.repaint();
    }

    public String mainMenu() {
        this.clearInterface();

        String [] buttons = {"Login", "Register", "Reconnect", "Quit"};
        final String[] result = new String[1];
        CountDownLatch latch = new CountDownLatch(1);

        for (int i = 0; i < 4; i++) {
            JButton b = new JButton(buttons[i]);
            b.setBounds(130,100+50*i,100, 40);
            int finalI = i;
            b.addActionListener(e -> {
                result[0] = Integer.toString(finalI + 1);
                latch.countDown();
            });
            f.add(b);
        }

        this.paintInterface();

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result[0];
    }

    public String[] loginAndRegister(boolean invalidCredentials, boolean takenUsername) {
        this.clearInterface();

        JLabel usernameLabel = new JLabel("Username:");
        JTextField usernameField = new JTextField(10);
        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField(10);
        JButton submitButton = new JButton("Submit");
        final String[] result = new String[3];
        CountDownLatch latch = new CountDownLatch(1);

        usernameLabel.setBounds(50, 50, 100, 30);
        usernameField.setBounds(150, 50, 150, 30);
        passwordLabel.setBounds(50, 100, 100, 30);
        passwordField.setBounds(150, 100, 150, 30);
        submitButton.setBounds(150, 150, 100, 30);

        submitButton.addActionListener(e -> {
            result[0] = usernameField.getText();
            result[1] = new String(passwordField.getPassword());
            result[2] = "";
            latch.countDown();
        });

        f.add(usernameLabel);
        f.add(usernameField);
        f.add(passwordLabel);
        f.add(passwordField);
        f.add(submitButton);

        JButton backButton = new JButton("Back");
        backButton.setBounds(50, 150, 100, 30);
        f.add(backButton);
        backButton.addActionListener(e -> {
            result[0] = "BACK";
            result[1] = "BACK";
            result[2] = "BACK";
            latch.countDown();
        });

        if (invalidCredentials) {
            JLabel errorLabel = new JLabel("Invalid username or password");
            errorLabel.setBounds(50, 200, 200, 30);
            f.add(errorLabel);
        }
        else if(takenUsername) {
            JLabel errorLabel = new JLabel("Username already taken");
            errorLabel.setBounds(50, 200, 200, 30);
            f.add(errorLabel);
        }

        this.paintInterface();

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result;
    }

    public String[] reconnect(boolean invalidToken) {
        this.clearInterface();

        JLabel tokenLabel = new JLabel("Token file name:");
        JTextField tokenField = new JTextField(10);
        JButton submitButton = new JButton("Submit");

        final String[] result = new String[2];
        CountDownLatch latch = new CountDownLatch(1);

        tokenLabel.setBounds(50, 50, 100, 30);
        tokenField.setBounds(150, 50, 150, 30);
        submitButton.setBounds(150, 100, 100, 30);

        submitButton.addActionListener(e -> {
            result[0] = tokenField.getText();
            result[1] = "";
            latch.countDown();
        });

        f.add(tokenLabel);
        f.add(tokenField);
        f.add(submitButton);

        JButton backButton = new JButton("Back");
        backButton.setBounds(50, 100, 100, 30);
        f.add(backButton);
        backButton.addActionListener(e -> {
            result[0] = "";
            result[1] = "BACK";
            latch.countDown();
        });

        if (invalidToken) {
            JLabel errorLabel = new JLabel("Invalid token");
            errorLabel.setBounds(50, 150, 200, 30);
            f.add(errorLabel);
        }

        this.paintInterface();

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result;
    }

    public void queue(String serverMessage) {

        this.clearInterface();

        currentTime = 0;

        timeLabel.setText("Time: 0s");
        JLabel queueLabel = new JLabel("Waiting for opponent...");
        JLabel serverMessageLabel = new JLabel(serverMessage);

        queueLabel.setBounds(150, 50, 200, 30);
        timeLabel.setBounds(150, 100, 100, 30);
        serverMessageLabel.setBounds(50, 150, 400, 30);

        f.add(queueLabel);
        f.add(timeLabel);
        f.add(serverMessageLabel);

        this.paintInterface();
    }

    private void updateGameValues(String round, String diceValue, String opponentDiceValue) {
        diceLabel.setText(diceValue);
        opponentDiceLabel.setText(opponentDiceValue);
        roundLabel.setText(round);
    }

    public void turn() {
        this.clearInterface();

        CountDownLatch latch = new CountDownLatch(1);

        // Create a timer that fires once after 30 seconds
        Timer timer = new Timer(timeout, e -> {
            latch.countDown();
        });
        timer.setRepeats(false); // Make sure the timer only fires once

        // Start the timer
        timer.start();

        rollButton.addActionListener(e -> {
            latch.countDown();
        });

        f.add(roundLabel);
        f.add(diceLabel);
        f.add(opponentDiceLabel);
        f.add(rollButton);

        this.paintInterface();

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void info() {
        this.clearInterface();

        f.add(roundLabel);
        f.add(diceLabel);
        f.add(opponentDiceLabel);

        this.paintInterface();

        this.paintInterface();
    }

    public void updateScore(String[] serverMessages) {
        System.out.println("Updating score");
        System.out.println(Arrays.toString(serverMessages));
        updateGameValues(serverMessages[1], serverMessages[2], serverMessages[3]);
    }

    public String gameOver(String serverMessage) {
        this.clearInterface();

        System.out.println("Game over " + serverMessage);


        JLabel gameOverLabel = new JLabel("Game over!");
        JLabel winnerLabel = new JLabel(serverMessage);
        JButton playAgainButton = new JButton("Play again");
        JButton quitButton = new JButton("Quit");

        final String[] result = new String[1];

        CountDownLatch latch = new CountDownLatch(1);

        gameOverLabel.setBounds(150, 50, 200, 30);
        winnerLabel.setBounds(150, 100, 200, 30);
        playAgainButton.setBounds(150, 150, 100, 30);
        quitButton.setBounds(150, 200, 100, 30);

        // Create a timer that fires once after 30 seconds
        Timer timer = new Timer(timeout, e -> {
            result[0] = "N";
            latch.countDown();
        });

        timer.setRepeats(false); // Make sure the timer only fires once

        // Start the timer
        timer.start();

        playAgainButton.addActionListener(e -> {
            result[0] = "Y";
            playAgainButton.setEnabled(false);
            quitButton.setEnabled(false);
            playAgainButton.setBackground(Color.GREEN);
            latch.countDown();
        });

        quitButton.addActionListener(e -> {
            result[0] = "N";
            playAgainButton.setEnabled(false);
            quitButton.setEnabled(false);
            quitButton.setBackground(Color.GREEN);
            latch.countDown();
        });

        f.add(gameOverLabel);
        f.add(winnerLabel);
        f.add(playAgainButton);
        f.add(quitButton);

        this.paintInterface();

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result[0];
    }

    public void close() {
        timer.stop();
        f.dispose();
    }
}
