import java.util.*;

public class Player {
    private Token token;
    private List<Tile> hand;
    private String name;
    // list of colors in the order that the game will be played
    private List<Integer> colors;
    private boolean isWinner;

    Player(Token t, List<Tile> hand) {
        this.token = t;
        this.hand = hand;
    }

    public String getName() {
        return this.name;
    }

    public void initialize(int color, List<Integer> colors) {
        this.token = new Token(color);
        this.colors = colors;
    }

    public Token placePawn(Board b) {
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
                    x = sideIndex / 2;
                    y = -1;
                    indexOnTile = sideIndex % 2;
                    break;
                }
                case 1: {
                    x = 6;
                    y = sideIndex / 2;
                    indexOnTile = sideIndex % 2 + 2;
                    break;
                }
                case 2: {
                    x = sideIndex / 2;
                    y = 6;
                    indexOnTile = sideIndex % 2 + 4;
                    break;
                }
                case 3: {
                    x = -1;
                    y = sideIndex / 2;
                    indexOnTile = sideIndex % 2 + 6;
                    break;
                }
                default: {
                    throw new IllegalArgumentException("Error: Unable to pick starting position on board");
                }
            }
            found = true;
            for (Token t : b.tokenList) {
                if (x == t.getPosition()[0] && y == t.getPosition()[1] && indexOnTile == t.getIndex()) {
                    found = false;
                    break;
                }
            }
        }
        if (x == Integer.MAX_VALUE || y == Integer.MAX_VALUE || indexOnTile == Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Error: Unable to pick starting position on board");
        }
        updateToken(new Token(this.token.getColor(), indexOnTile, new int[]{x, y}));
        b.addToken(this.token);
        return this.token;
    }

    public void endGame(Board b, List<Integer> colors) {
        this.colors = colors;
        if (b.tokenList.contains(this.token)) {
            this.isWinner = true;
        }
        this.isWinner = false;
    }


    public Tile playTurn(Board b, String strategy, int tilesLeft) {
        List<Tile> legalMoves = new ArrayList<>();
        List<Tile> legalTiles = new ArrayList<>();
        for (Tile t : getHand()) {
            Tile copy = t.copyTile();
            for (int i = 0; i < 4; i++) {
                if (Server.getInstance().legalPlay(this, b, copy)) {
                    legalMoves.add(copy);
                    if (!legalTiles.contains(t)) legalMoves.add(t);
                }
                copy.rotateTile();
            }
        }
        switch (strategy) {
            case "R": {
                Random rand = new Random();
                return legalMoves.get(rand.nextInt(legalMoves.size()));
            }

            case "LS": {
                Collections.sort(legalMoves, new SymmetricComparator());
                return legalMoves.get(0);
            }

            case "MS": {
                Collections.sort(legalMoves, new SymmetricComparator());
                return legalMoves.get(legalMoves.size() - 1);
            }

            default: {
                throw new IllegalArgumentException("Input strategy cannot' be identified");
            }
        }
    }

    /**
     * Get a player's token
     *
     * @return a token
     */
    public Token getToken() {
        return this.token;
    }

    /**
     * Update a player's token
     *
     * @param token new token
     */
    public void updateToken(Token token) {
        this.token = token;
    }

    /**
     * Player draws a tile
     *
     * @param t tile to be added to the player's hand
     */
    public void draw(Tile t) {
        hand.add(t);
    }

    /**
     * Simulate player choosing a tile to place
     *
     * @param t tile to be placed
     */
    public void deal(Tile t) {
        hand.remove(t);
    }

    /**
     * Get a player's hand
     *
     * @return a list of tiles on player's hand
     */
    public List<Tile> getHand() {
        return this.hand;
    }

    public static void reorderPath(Tile t) {
        for (int[] array : t.paths) {
            Arrays.sort(array);
        }
        Arrays.sort(t.paths, new ListFirstElementComparator());
    }

    public static void main(String [] args) {
        int[][] path = new int[][] {{2,7}, {3,6}, {4,0}, {5,1}};
        Tile t = new Tile(path);
        reorderPath(t);
        System.out.print("yes");
    }
}

class SymmetricComparator implements Comparator<Tile> {
    @Override
    // a < b return -1
    // a > b return 1
    // else return 0
    public int compare(Tile a, Tile b){
        return -1;
    }
}

class ListFirstElementComparator implements Comparator<int[]> {
    @Override
    public int compare(int[] a, int[] b) {
        if (a[0] == b[0]) {
            return 0;
        }
        return a[0] < b[0] ? -1 : 1;
    }
}