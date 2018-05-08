import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MPlayer implements IPlayer {
    private String name;
    private int color;
    private List<Integer> colors; // A list of player's colors in the order that the game will be played
    public String Strategy; // Machine player's strategy
    private boolean isWinner;
    public State state;

    public enum State { INIT, PLACE, PLAY, END;}

    MPlayer (String Strategy) {
        if (!(Strategy.equals("R") || Strategy.equals("MS") || Strategy.equals("LS"))) {
            throw new IllegalArgumentException("Invalid strategy type for machine player");
        }
        this.Strategy = Strategy;
    }

    public void initialize (int color, List<Integer> colors) {
        if (color < 0 || color > 7) {
            throw new IllegalArgumentException("Invalid player's color");
        }
        for (int aColor : colors) {
            if (aColor < 0 || aColor > 7) {
                throw new IllegalArgumentException("Player list contains invalid" + "player color");
            }
        }
        if (!(state == State.END || state == null)){
            throw new IllegalArgumentException("Player cheated: cannot initialize at this time");
        }
        this.color = color;
        this.name = Token.colorMap.get(color);
        this.colors = colors;
        state = State.INIT;
    }

    public Token placePawn(Board b) {
        if (state != State.INIT) {
            throw new IllegalArgumentException("Player cheated: cannot place pawn at this time");
        }
        this.state = State.PLACE;

        if (!colors.contains(this.color)){
            throw new IllegalArgumentException("Player is not authorized to place pawn");
        }
        Random rand = new Random();
        int x = Integer.MAX_VALUE;
        int y = Integer.MAX_VALUE;
        int indexOnTile = Integer.MAX_VALUE;
        boolean found = false;
        while (!found) {
            // choose random number in {0,1,2,3}
            int side = rand.nextInt(4);
            // choose random number in {0,1,2,...,11}
            int sideIndex = rand.nextInt(12);
            switch (side) {
                case 0: {
                    x = sideIndex / 2; //from 1 to 5
                    y = -1;
                    indexOnTile = sideIndex % 2 + 4;
                    break;
                }
                case 1: {
                    x = 6;
                    y = sideIndex / 2;
                    indexOnTile = sideIndex % 2 + 6;
                    break;
                }
                case 2: {
                    x = sideIndex / 2;
                    y = 6;
                    indexOnTile = sideIndex % 2;
                    break;
                }
                case 3: {
                    x = -1;
                    y = sideIndex / 2;
                    indexOnTile = sideIndex % 2 + 2;
                    break;
                }
                default: {
                    throw new IllegalArgumentException("Error: Unable to pick starting position on board");
                }
            }
            found = true;
            for (Token t : b.getTokenList()) {
                if (x == t.getPosition()[0] && y == t.getPosition()[1] && indexOnTile == t.getIndex()) {
                    found = false;
                    break;
                }
            }
        }
        if (x == Integer.MAX_VALUE || y == Integer.MAX_VALUE || indexOnTile == Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Error: Unable to pick starting position on board");
        }
        Token newToken = new Token(this.color, indexOnTile, new int[]{x, y});
        return newToken;
    }

    public Tile playTurn(Board b, List<Tile> hand, int tilesLeft) {
        if (state != State.PLACE && state != State.PLAY) {
            throw new IllegalArgumentException("Player cheated: cannot play turn at this time");
        }
        state = State.PLAY;

        if (!colors.contains(this.color)){
            throw new IllegalArgumentException("Player is not authorized to place pawn");
        }
        List<Tile> legalMoves = new ArrayList<>();
        for (Tile t : hand) {
            Tile copy = t.copyTile();
            for (int i = 0; i < 4; i++) {
                SPlayer tempPlayer = new SPlayer(b.getToken(getColor()), hand, getName());
                tempPlayer.link(this);
                if (Server.getInstance().legalPlay(tempPlayer, b, copy)) {
                    legalMoves.add(copy.copyTile());
                }
                copy.rotateTile();
            }
        }
        switch (Strategy) {
            case "R": {
                Random rand = new Random();
                return legalMoves.get(rand.nextInt(legalMoves.size()));
            }

            case "MS": {
                Collections.sort(legalMoves, new SymmetricComparator());
                return legalMoves.get(0);
            }

            case "LS": {
                Collections.sort(legalMoves, new SymmetricComparator());
                return legalMoves.get(legalMoves.size() - 1);
            }

            default: {
                throw new IllegalArgumentException("Input strategy cannot be identified");
            }
        }
    }

    public void endGame(Board b, List<Integer> colors) {
        if (state != State.PLAY) {
            throw new IllegalArgumentException("Player cheated: cannot end game at this time");
        }
        state = State.END;

        this.colors = colors;
        if (colors.contains(this.color)) {
            this.isWinner = true;
        }
        this.isWinner = false;
    }

    public String getName() {
        return this.name;
    }

    public int getColor() {return this.color;}
}
