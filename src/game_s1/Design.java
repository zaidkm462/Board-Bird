package game_s1;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Paths;

import javax.swing.*;

public class Design {
    JFrame window;
    JScrollPane scrollPane;
    JLayeredPane master;
    int rows = 60;
    int cols = 40;
    int sides_wall = 6;
    int down_wall = 10;
    int block_w = 80;
    int block_h = 55;
    BoardButton[][] game_array = new BoardButton[rows][cols];
    JButton bird;
    ImageIcon bird_img_L;
    ImageIcon bird_img_R;
    ImageIcon bird_nok_L;
    ImageIcon bird_nok_R;
    ImageIcon bird_bnc_R;

    ImageIcon smoke;
    ImageIcon heart;
    ImageIcon heart2;
    JLabel life1;
    JLabel life2;
    JLabel life3;
    JLabel money_label;
    JLabel level_label;
    JLabel record_label;
    int level;
    int money = 100;
    int win_money;
    int life_count = 3;
    JLabel curret_life = life3;
    boolean bird_dir = true;
    int bird_i = 3;
    int bird_j = (cols - 1) / 2;
    boolean game_paused = false;
    boolean movement_busy = false;
    Timer life_timer;
    Enter controls;
    KeyEventDispatcher keyboard_dispatcher;

    int camera_h = 0;
    int camera_v = 0;
    int shake_h = 0;
    int shake_v = 0;
    static final Color SKY_COLOR = Color.decode("#9cbcc6");
    static final Color STONE_COLOR = Color.decode("#523c37");
    static final Color LEAF_COLOR = Color.decode("#30891d");
    private static final ScoreRepository SCORE_REPOSITORY =
            new ScoreRepository(Paths.get("scores.json"));
    String best_name = "None";
    int best_level = 0;

    public Design() {
        this(1);
    }

    public Design(int level) {
        this.level = Math.max(1, level);
        win_money = 300 + (this.level - 1) * 200;
        load_record();

        create_window();
    }

    public class Enter implements KeyListener {
        Design d;
        JScrollBar h_scroll;
        JScrollBar v_scroll;

        Enter(Design d) {
            life_timer = new Timer(30000, evt -> {
                life_control(false);
            });
            life_timer.start();

            this.d = d;
            this.h_scroll = d.scrollPane.getHorizontalScrollBar();
            this.v_scroll = d.scrollPane.getVerticalScrollBar();
        }

        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyPressed(KeyEvent e) {

            if (game_paused || movement_busy)
                return;

            int code = e.getKeyCode();

            if (code == KeyEvent.VK_RIGHT) {
                bird_dir = true;
                if (can_move_to(bird_i, bird_j + 1)) {
                    bird_j++;
                    movement_busy = true;
                    jumping(true);
                } else {
                    bird.setIcon(bird_img_R);
                }

            } else if (code == KeyEvent.VK_LEFT) {

                bird_dir = false;
                if (can_move_to(bird_i, bird_j - 1)) {
                    bird_j--;
                    movement_busy = true;
                    jumping(false);
                } else {
                    bird.setIcon(bird_img_L);
                }

            } else if (code == KeyEvent.VK_SPACE) {

                int target_column = bird_j + (bird_dir ? 1 : -1);
                if (is_inside(bird_i + 1, target_column)) {
                    bird.setIcon(bird_dir ? bird_nok_R : bird_nok_L);

                    BoardButton tmp[] = {
                        game_array[bird_i][target_column],
                        game_array[bird_i + 1][target_column]
                    };

                    distroy(tmp);
                    drop(bird_i, target_column);
                    drop(bird_i + 1, target_column);
                }

            } else if (code == KeyEvent.VK_DOWN) {

                if (is_inside(bird_i + 1, bird_j)) {
                    BoardButton tmp[] = {
                        game_array[bird_i + 1][bird_j]
                    };

                    distroy(tmp);
                    drop(bird_i + 1, bird_j);
                    movement_busy = true;
                    gravity();
                }
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
        }

        public void update_camera() {
            h_scroll.setValue(camera_h + shake_h);
            v_scroll.setValue(camera_v + shake_v);
        }

        public void gravity() {

            if (game_paused || !is_inside(bird_i, bird_j)) {
                movement_busy = false;
                return;
            }

            eat();

            if (game_paused || !is_inside(bird_i + 1, bird_j)) {
                movement_busy = false;
                return;
            }

            if (game_array[bird_i + 1][bird_j].type == 'G'
                    || game_array[bird_i + 1][bird_j].type == 'W') {
                movement_busy = false;
                bounce();
                return;
            }

            int[] c = {0};
            Timer[] timer = new Timer[1];

            timer[0] = new Timer(10, evt -> {

                camera_v += 4;
                update_camera();

                c[0] += 4;

                if (c[0] >= block_h) {
                    timer[0].stop();

                    camera_v -= (c[0] - block_h);
                    update_camera();

                    gravity();
                }
            });

            timer[0].start();

            bird_i++;
        }

        public void jumping(boolean bird_dir) {

            if (game_paused || !is_inside(bird_i, bird_j))
                return;

            int[] c = {0};
            Timer[] timer = new Timer[1];
            int s = (bird_dir) ? +1 : -1;

            timer[0] = new Timer(10, evt -> {

                camera_h += 6 * s;
                update_camera();

                c[0] += 6;

                if (c[0] >= block_w) {
                    timer[0].stop();

                    camera_h -= (c[0] - block_w) * s;
                    update_camera();

                    gravity();
                }
            });

            timer[0].start();
        }

        private boolean is_inside(int row, int column) {
            return row >= 0 && row < rows && column >= 0 && column < cols;
        }

        private boolean can_move_to(int row, int column) {
            if (!is_inside(row, column))
                return false;

            char type = game_array[row][column].type;
            return type != 'G' && type != 'W';
        }

        public void distroy(BoardButton[] b) {

            for (BoardButton i : b) {
                if (i.type == 'G') {
                    i.setIcon(smoke);
                    i.type = 'T';
                }
            }

            Timer timer = new Timer(300, evt -> {

                bird.setIcon((bird_dir) ? bird_img_R : bird_img_L);

                for (BoardButton i : b) {
                    if (i.type == 'T') {
                        i.setIcon(null);
                    }
                }

                ((Timer) evt.getSource()).stop();
            });

            timer.setRepeats(false);
            timer.start();
        }

        public void eat() {

            BoardButton btn = game_array[bird_i][bird_j];

            switch (btn.type) {

            case 'H':

                btn.setIcon(null);
                btn.type = 'T';
                life_control(true);

                break;

            case 'C':

                btn.setIcon(null);
                btn.type = 'T';

                money += 50;
                update_status_labels();

                if (money >= win_money) {
                    level_up();
                }

                break;

            case 'F':

                btn.setIcon(null);
                btn.type = 'T';
                life_control(false);

                shake();

                break;

            default:
                break;
            }
        }

        public void shake() {

            int[] count = {0};

            int[] offsets_v = {1, -1, 1, -1, 1, -1, 1};
            int[] offsets_h = {1, -1, 1, -1, 1, -1, 1};

            Timer[] timer = new Timer[1];

            timer[0] = new Timer(20, evt -> {

                shake_v = offsets_v[count[0]] * 4;
                shake_h = offsets_h[count[0]] * 4;

                update_camera();

                count[0]++;

                if (count[0] >= offsets_v.length) {

                    timer[0].stop();

                    shake_v = 0;
                    shake_h = 0;

                    update_camera();
                }
            });

            timer[0].start();
        }

        public void life_control(boolean earn) {

            if (earn) {

                if (life_count < 3) {

                    if (life_count == 1)
                        life2.setIcon(heart);

                    if (life_count == 2)
                        life3.setIcon(heart);

                    life_count++;
                }

            } else {

                if (life_count > 1) {

                    if (life_count == 2)
                        life2.setIcon(heart2);

                    if (life_count == 3)
                        life3.setIcon(heart2);

                    life_count--;

                } else {

                    life1.setIcon(heart2);

                    pause_and_show_loss_dialog();
                }
            }
        }

        public void bounce() {

            int normal_h = block_h;
            int compressed_h = 42;

            int[] h = {normal_h};
            boolean[] compressing = {true};

            ImageIcon normal_icon = bird_dir ? bird_img_R : bird_img_L;

            Timer[] timer = new Timer[1];

            timer[0] = new Timer(15, evt -> {

                if (compressing[0]) {

                    h[0] -= 3;

                    if (h[0] <= compressed_h) {
                        h[0] = compressed_h;
                        compressing[0] = false;
                    }

                } else {

                    h[0] += 3;

                    if (h[0] >= normal_h) {
                        h[0] = normal_h;
                        timer[0].stop();
                        bird.setIcon(normal_icon);
                        return;
                    }
                }

                Image scaled = normal_icon.getImage().getScaledInstance(
                        (block_w - 20),
                        h[0],
                        Image.SCALE_SMOOTH
                );

                bird.setIcon(new ImageIcon(scaled));
            });

            timer[0].start();
        }

        public void drop(int i, int j) {

            if (i < 0 || i >= rows)
                return;

            if (game_array[i][j].type != 'T')
                return;

            int first_element = i - 1;

            while (first_element >= 0) {
                char type = game_array[first_element][j].type;

                if (type != 'F' && type != 'C' && type != 'H')
                    break;

                first_element--;
            }

            if (first_element == i - 1)
                return;

            for (int destination = i; destination > first_element + 1; destination--) {
                BoardButton source = game_array[destination - 1][j];
                BoardButton target = game_array[destination][j];

                target.type = source.type;
                target.setIcon(source.getIcon());
            }

            BoardButton first = game_array[first_element + 1][j];
            first.type = 'T';
            first.setIcon(null);
        }
    }

    private void create_window() {
        window = new JFrame();
        window.setTitle("The Board Bird");
        window.setSize(1000, 680);
        window.setExtendedState(JFrame.MAXIMIZED_BOTH);
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        ImageIcon block = new ImageIcon("block3.png");
        ImageIcon wall = new ImageIcon("wall.png");
        ImageIcon fire = new ImageIcon("fire.gif");
        ImageIcon corn = new ImageIcon("corn2.png");
        ImageIcon top = new ImageIcon("top.png");

        heart = new ImageIcon("heart.png");
        heart2 = new ImageIcon("heart2.png");
        bird_img_L = new ImageIcon("bird_L.png");
        bird_img_R = new ImageIcon("bird_R.png");
        bird_nok_R = new ImageIcon("bird_nok_R.png");
        bird_nok_L = new ImageIcon("bird_nok_L.png");
        bird_bnc_R = new ImageIcon("bird_R_bounced.png");
        smoke = new ImageIcon("smoke2.png");

        master = new JLayeredPane();
        master.setLayout(null);

        JPanel p2 = new JPanel();
        p2.setLayout(new GridLayout(rows, cols, 0, 0));
        p2.setBackground(STONE_COLOR);
        p2.setPreferredSize(new Dimension(cols * block_w, rows * block_h));
        p2.setBounds(0, 0, cols * block_w, rows * block_h);

        JPanel sea = new JPanel();
        sea.setBackground(SKY_COLOR);
        sea.setPreferredSize(new Dimension(0, 90));
        sea.setLayout(null);

        life1 = new JLabel();
        life1.setIcon(heart);
        life1.setBounds(0, 8, block_w, block_h);

        life2 = new JLabel();
        life2.setIcon(heart);
        life2.setBounds(70, 8, block_w, block_h);

        life3 = new JLabel();
        life3.setIcon(heart);
        life3.setBounds(140, 8, block_w, block_h);

        money_label = new JLabel();
        money_label.setHorizontalAlignment(SwingConstants.LEFT);
        money_label.setFont(new Font("Balbaleo", Font.BOLD, 30));
        money_label.setForeground(LEAF_COLOR);
        money_label.setBounds(18, 0, 220, 35);

        level_label = new JLabel();
        level_label.setHorizontalAlignment(SwingConstants.LEFT);
        level_label.setFont(new Font("Balbaleo", Font.BOLD, 18));
        level_label.setForeground(STONE_COLOR);
        level_label.setBounds(18, 35, 220, 25);

        record_label = new JLabel();
        record_label.setHorizontalAlignment(SwingConstants.LEFT);
        record_label.setFont(new Font("Balbaleo", Font.BOLD, 18));
        record_label.setForeground(STONE_COLOR);
        record_label.setBounds(18, 58, 300, 25);

        sea.add(life1);
        sea.add(life2);
        sea.add(life3);
        sea.add(money_label);
        sea.add(level_label);
        sea.add(record_label);
        update_status_labels();
        window.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                position_hearts(sea);
            }
        });

        scrollPane = new JScrollPane(p2);
        scrollPane.setInputMap(
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
                null
        );
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setBounds(0, 0, 900, 560);
        scrollPane.setFocusable(false);

        controls = new Enter(this);
        p2.addKeyListener(controls);
        p2.setFocusable(true);
        p2.setFocusTraversalKeysEnabled(false);
        p2.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent event) {
                p2.requestFocusInWindow();
            }
        });

        keyboard_dispatcher = event -> {
            if (event.getID() != KeyEvent.KEY_PRESSED
                    || !window.isActive()
                    || controls == null) {
                return false;
            }

            int code = event.getKeyCode();
            if (code == KeyEvent.VK_RIGHT
                    || code == KeyEvent.VK_LEFT
                    || code == KeyEvent.VK_DOWN
                    || code == KeyEvent.VK_SPACE) {
                controls.keyPressed(event);
                return true;
            }

            return false;
        };
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(keyboard_dispatcher);

        bird = new JButton("");
        bird.setContentAreaFilled(false);
        bird.setIcon(bird_img_R);
        bird.setBorderPainted(false);
        bird.setBounds(362, block_h * 3, block_w, block_h);

        for (int i = 0; i < rows; i++) {

            for (int j = 0; j < cols; j++) {

                BoardButton btn = new BoardButton('T');

                btn.setPreferredSize(new Dimension(block_w, block_h));
                btn.setMaximumSize(new Dimension(block_w, block_h));
                btn.setContentAreaFilled(false);
                btn.setBorderPainted(false);
                btn.setFocusable(false);

                if (i < 3) {
                    btn.setIcon(top);

                } else if (i == bird_i && (j == bird_j || j == bird_j + 1)) {

                } else if (j < sides_wall
                        || j > cols - sides_wall - 1
                        || i > rows - down_wall) {

                    btn.setIcon(wall);
                    btn.type = 'W';

                } else {

                    int rand = (int) (Math.random() * 100);

                    int fire_limit = Math.min(2 + level, 12);
                    if (rand < 90 - fire_limit) {

                        btn.setIcon(block);
                        btn.type = 'G';

                    } else if (rand < 90) {

                        btn.setIcon(fire);
                        btn.type = 'F';

                    } else if (rand < 98) {

                        btn.setIcon(corn);
                        btn.type = 'C';

                    } else {

                        btn.setIcon(heart);
                        btn.type = 'H';
                    }
                }

                p2.add(btn);
                game_array[i][j] = btn;
            }
        }

        master.add(scrollPane, Integer.valueOf(0));
        master.add(bird, Integer.valueOf(1));

        window.add(sea, BorderLayout.NORTH);
        window.add(master, BorderLayout.CENTER);
        window.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                resize_game_area();
            }
        });
        window.addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent event) {
                request_game_focus();
            }
        });
    }

    public void show() {

        window.setExtendedState(JFrame.MAXIMIZED_BOTH);
        window.validate();
        window.setVisible(true);
        window.toFront();
        window.requestFocus();
        SwingUtilities.invokeLater(() -> {
            resize_game_area();
            position_hearts(life1.getParent());
            request_game_focus();
        });

        int middle = (
                scrollPane.getHorizontalScrollBar().getMaximum()
                - scrollPane.getHorizontalScrollBar().getVisibleAmount()
        ) / 2;

        scrollPane.getHorizontalScrollBar().setValue(middle);

        camera_h = middle;
        camera_v = scrollPane.getVerticalScrollBar().getValue();

        shake_h = 0;
        shake_v = 0;
    }

    private void request_game_focus() {
        if (master != null)
            master.setFocusable(false);
        if (scrollPane != null)
            scrollPane.setFocusable(false);
        if (life1 != null)
            life1.getParent().setFocusable(false);
        if (game_array[0][0] != null)
            game_array[0][0].getParent().requestFocusInWindow();
    }

    private void position_hearts(java.awt.Component component) {
        if (!(component instanceof JPanel))
            return;

        int icon_width = heart.getIconWidth();
        int icon_height = heart.getIconHeight();
        int gap = 12;
        int right = component.getWidth() - 18;
        int y = 8;

        life3.setBounds(right - icon_width, y, icon_width, icon_height);
        life2.setBounds(right - (icon_width * 2 + gap), y, icon_width, icon_height);
        life1.setBounds(right - (icon_width * 3 + gap * 2), y, icon_width, icon_height);
    }

    private void resize_game_area() {
        if (window == null || master == null || scrollPane == null)
            return;

        Dimension size = window.getContentPane().getSize();
        if (size.width <= 0 || size.height <= 90)
            return;
        int game_height = Math.max(0, size.height - 90);
        master.setBounds(0, 0, size.width, game_height);
        scrollPane.setBounds(0, 0, size.width, game_height);
    }

    private void update_status_labels() {
        if (money_label != null) {
            money_label.setText("Money: " + money + " $");
            level_label.setText("Level: " + level);
            record_label.setText("Best: " + best_name + " " + best_level);
        }
    }

    private void level_up() {
    game_paused = true;
    if (life_timer != null)
        life_timer.stop();

    final JDialog congrats = new JDialog(window, "Congratulations", true);
    congrats.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    congrats.setResizable(false);

    JPanel panel = new JPanel();
    panel.setBackground(SKY_COLOR);
    panel.setBorder(BorderFactory.createEmptyBorder(28, 48, 28, 48));
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

    JLabel title = new JLabel("CONGRATULATIONS!");
    title.setAlignmentX(JComponent.CENTER_ALIGNMENT);
    title.setForeground(STONE_COLOR);
    title.setFont(new Font("SansSerif", Font.BOLD, 28));

    JLabel next = new JLabel("Level " + (level + 1));
    next.setAlignmentX(JComponent.CENTER_ALIGNMENT);
    next.setForeground(LEAF_COLOR);
    next.setFont(new Font("SansSerif", Font.BOLD, 22));

    panel.add(title);
    panel.add(Box.createVerticalStrut(10));
    panel.add(next);
    congrats.add(panel);
    congrats.pack();
    congrats.setLocationRelativeTo(window);

    boolean[] next_level_started = {false};
    Runnable start_next_level = () -> {
        if (next_level_started[0])
            return;

        next_level_started[0] = true;
        congrats.dispose();
        window.dispose();
        Design next_level = new Design(level + 1);
        next_level.show();
    };

    congrats.addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent event) {
            start_next_level.run();
        }
    });

    Timer next_level_timer = new Timer(3000, event -> {
        start_next_level.run();
    });
    next_level_timer.setRepeats(false);
    next_level_timer.start();
    congrats.setVisible(true);
    }

    private void pause_and_show_loss_dialog() {
    game_paused = true;
    if (life_timer != null)
        life_timer.stop();

    final JDialog loss_dialog = new JDialog(window, "The Bird Fell", true);
        loss_dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        loss_dialog.setResizable(false);

        JPanel panel = new JPanel();
        panel.setBackground(STONE_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 30, 24, 30));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("YOU LOSE!");
        title.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        title.setForeground(Color.decode("#f2c14e"));
        title.setFont(new Font("SansSerif", Font.BOLD, 30));

        JLabel level_text = new JLabel("You reached level " + level);
        level_text.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        level_text.setForeground(Color.WHITE);

        JLabel name_text = new JLabel("Enter your name");
        name_text.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        name_text.setForeground(Color.decode("#f2c14e"));

        JTextField name_field = new JTextField(16);
        name_field.setMaximumSize(new Dimension(240, 34));
        name_field.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        name_field.setBackground(SKY_COLOR);
        name_field.setForeground(STONE_COLOR);
        name_field.setCaretColor(STONE_COLOR);

        JPanel buttons = new JPanel();
        buttons.setBackground(STONE_COLOR);
        JButton play_again = new JButton("Play again");
        JButton exit = new JButton("Exit");
        style_action_button(play_again);
        style_action_button(exit);
        buttons.add(play_again);
        buttons.add(exit);

        play_again.addActionListener(event -> {
            if (level > best_level) {
                String name = name_field.getText().trim();
                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(
                            loss_dialog,
                            "Please enter your name.",
                            "Name required",
                            JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }
                best_name = name;
                best_level = level;
                save_record();
            }

            loss_dialog.dispose();
            window.dispose();
            Design restart = new Design(level);
            restart.show();
        });
        exit.addActionListener(event -> loss_dialog.dispose());

        panel.add(title);
        panel.add(Box.createVerticalStrut(10));
        panel.add(level_text);
        panel.add(Box.createVerticalStrut(16));
        panel.add(name_text);
        panel.add(Box.createVerticalStrut(6));
        panel.add(name_field);
        panel.add(Box.createVerticalStrut(18));
        panel.add(buttons);
        loss_dialog.add(panel);
        loss_dialog.pack();
        loss_dialog.setLocationRelativeTo(null);
        loss_dialog.setVisible(true);
    }

    private void style_action_button(JButton button) {
        button.setBackground(Color.decode("#f2c14e"));
        button.setForeground(STONE_COLOR);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
    }

    private void load_record() {
        ScoreRecord record = SCORE_REPOSITORY.load();
        best_name = record.name();
        best_level = record.level();
    }

    private void save_record() {
        SCORE_REPOSITORY.save(new ScoreRecord(best_name, best_level));
    }

    public static void main(String[] args) {
        Main.main(args);
    }
}