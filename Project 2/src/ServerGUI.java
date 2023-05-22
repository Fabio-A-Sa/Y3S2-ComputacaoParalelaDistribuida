import javax.swing.*;

public class ServerGUI {

    private final JFrame f;
    private final JLabel timeLabel = new JLabel();
    private final JLabel numberOfGames = new JLabel();
    private final JLabel leaderboard = new JLabel();
    private final JLabel queuePlayers = new JLabel();
    private int currentTime = 0;

    // Timer that fires every 1 second
    private final Timer timer = new Timer(1000, e -> {
        currentTime++;
        timeLabel.setText("Time: " + currentTime + "s");
    });

    public ServerGUI() {

        f = new JFrame("Dice Meister");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(600, 400);
        f.setLayout(null);//using no layout managers
        f.setVisible(true);

        timeLabel.setBounds(25, 25, 100, 30);
        numberOfGames.setBounds(25, 75, 200, 30);
        queuePlayers.setBounds(25, 125, 200, 90);
        leaderboard.setBounds(225, 25, 300, 90);
        timeLabel.setText("Time: " + currentTime + "s");

        f.add(timeLabel);
        f.add(queuePlayers);
        f.add(numberOfGames);
        f.add(leaderboard);

        this.paintInterface();
        timer.start();
    }

    private void paintInterface() {
        f.revalidate();
        f.repaint();
    }

    public void setQueue(String queueSize, String[] players) {
        StringBuilder playersString = new StringBuilder("<html>Players in queue: " + queueSize + "<br>");
        for (String s : players) {
            playersString.append(s).append("<br>");
        }
        playersString.append("</html>");
        queuePlayers.setText(playersString.toString());
    }

    public void setGames(String games) {
        numberOfGames.setText("Number of active games: "+ games);
    }

    public void setLeaderboard(String[] leaderboard) {
        StringBuilder leaderboardString = new StringBuilder("<html>Leaderboard:<br>");
        for (String s : leaderboard) {
            leaderboardString.append(s).append("<br>");
        }
        leaderboardString.append("</html>");
        this.leaderboard.setText(leaderboardString.toString());
    }
}
