package game_s1;

import javax.swing.JButton;

/**
 * A single cell in the game board. The type determines its gameplay role.
 */
final class BoardButton extends JButton {
    private static final long serialVersionUID = 1L;

    char type;

    BoardButton(char type) {
        this.type = type;
    }
}
