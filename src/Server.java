import java.util.*;

public class Server {
//    private Board board;
//    private ArrayList<Tile> drawPile;
//    private ArrayList<Player> inPlayer;
//    private ArrayList<Player> outPlayer;

    private ArrayList<Player> winners;
    private boolean isOver;

//    Server(Board b, ArrayList<Tile> drawPile, ArrayList<Player> inPlayer, ArrayList<Player> outPlayer) {
//        this.board = b;
//        this.drawPile = drawPile;
//        this.inPlayer = inPlayer;
//        this.outPlayer = outPlayer;
//    }

    // Singleton Pattern
    public static Server server = new Server();
    private Server() {}

    /**
     * Return false if
     * 1) the tile is not (a possibly rotated version of) one of the tiles of the player
     * 2) the placement of the tile is an elimination move for the player (unless all of
     * the possible moves are elimination moves
     * @param p         the player that attempts to place a tile
     * @param b         the board before the tile placement
     * @param t         the tile that the player wishes to place on the board
     * @return true if this play is legal
     */
    public boolean legalPlay(Player p, Board b, Tile t) {
        if (p.hand.size() == 0) {
            return false;
        }
        for (Tile pt : p.hand) {
            if (!t.isSameTile(pt)) {
                return false;
            }
        }
        // put tile ot place on board and simulate move
        int[] location = getAdjacentLocation(p.getToken());
        b.placeTile(t, location[0], location[1]);
        Token token = simulateMove(p.token, t, b);
        // delete tile to place
        b.deleteTile(location[0], location[1]);
        // if collision, return false
        if (hasToken(b, token.getPosition(), token.getIndex())) {
            return false;
        }
        // check whether other rotations are elimination moves
        boolean rotate = false;
        if (outOfBoard(token)){
            rotate = true;
        }
        if (rotate) {
            Tile newTile = new Tile(t.getPaths());
            for (int i = 0; i < 3; i++) {
                newTile.rotateTile();
                b.placeTile(newTile, location[0], location[1]);
                token = simulateMove(p.token, newTile, b);
                b.deleteTile(location[0], location[1]);
                if (outOfBoard(token)) {
                    continue;
                } else {
                    return false; // original rotation is illegal, as there is another legal rotation
                }
            }
            return true;          // all rotations are illegal, original rotation is legal
        } else {
            return true;          // original rotation is legal without considering other rotations
        }
    }

    public void playATurn(ArrayList<Tile> drawPile, ArrayList<Player> inP, ArrayList<Player> outP, Board b, Tile t) {
        // place a tile path
        Player currentP = inP.get(0);
        inP.remove(0);
        Token currentT = currentP.getToken();
        int[] location = getAdjacentLocation(currentT);
        b.placeTile(t, location[0], location[1]);
        // move the token and eliminate player if necessary
        while(!outOfBoard(currentT) && b.getTile(location[0], location[1]) != null) {
            currentT.setPosition(location[0], location[1]); // move to tile
            int pathStart = t.neighborIndex.get(currentT.getIndex());
            int pathEnd = t.getPathEnd(pathStart);
            currentT.setIndex(pathEnd);
            location = getAdjacentLocation(currentT);
        }
        // update player map on board -> physically move the token forward to the final location and index
        currentP.setToken(currentT);
        b.getPlayerMap().put(currentP, new int[] {currentT.getPosition()[0], currentT.getPosition()[1], currentT.getIndex()});
        // eliminate current player & recycle tiles in hand
        if (outOfBoard(currentT)) {
            for (Tile tile : currentP.getHand()) {
                drawPile.add(tile);
            }
            currentP.getHand().clear();
            outP.add(currentP);
        }
        // add to tail & draw tile
        else {
            if (drawPile.size() == 0) {
                currentP.getDragon();
            }
            Tile temp = drawPile.get(0);
            currentP.draw(temp);
            drawPile.remove(0);
            inP.add(currentP);
        }
        // determine whether game is over
        if (inP.size() == 1) {
            this.isOver = true;
            this.winners = inP;
        }
    }

    private Token simulateMove(Token token, Tile tileToPlace, Board board) {
        // location to place the tile
        int[] location = getAdjacentLocation(token);
        // return if reach the end of path
        if (tileToPlace == null && board.getTile(location[0], location[1]) == null) {
            return token;
        }
        // place tile & get path indices
        int pathStart = tileToPlace.neighborIndex.get(token.getIndex());
        // check for collision
        if (hasToken(b, location, pathStart)) {
            return token;
        }
        int pathEnd = tileToPlace.getPathEnd(pathStart);
        // recursion
        Token nt = new Token(pathEnd, location);
        int[] nl = getAdjacentLocation(nt);
        return simulateMove(nt, board.getTile(nl[0], nl[1]), board);
    }

    /**
     * Check whether a player's token is on tile at the location and the index
     * @param b
     * @param location
     * @param indexOnTile
     * @return whether a player's token is on the tile at input location
     *         and at the index on the tile
     */
    private boolean hasToken(Board b, int[] location, int indexOnTile) {
        for (Player p : b.getPlayerMap().keySet()) {
            int[] temp = b.getPlayerMap().get(p);
            if (location[0] == temp[0] && location[1] == temp[1] && location[2] == indexOnTile) {
                return true;
            }
        }
        return false;
    }

    /**
     * Token looks ahead according to its own position
     * @param token the token of player currently making the move
     * @return array of location [x,y] of the block ahead
     */
    private int[] getAdjacentLocation(Token token){
        int[] next = new int[2];
        int x = token.getPosition()[0];
        int y = token.getPosition()[1];
        int indexOnTile = token.getIndex();
        if (indexOnTile == 0 || indexOnTile == 1) {
            next[0] = x;
            next[1] = y - 1;
        } else if (indexOnTile == 2 || indexOnTile == 3) {
            next[0] = x + 1;
            next[1] = y;
        } else if (indexOnTile == 4 || indexOnTile == 7) {
            next[0] = x;
            next[1] = y + 1;
        } else {
            next[0] = x - 1;
            next[1] = y;
        }
        return next;
    }

    /**
     * Check whether the token is on the edge of the board
     * so we need to eliminate the player holding that token
     * @param token
     * @return
     */
    public boolean outOfBoard(Token token){
        int ti = token.getIndex();
        int[] tl = token.getPosition();
        if ((ti == 0 || ti == 1) && tl[1] == 0 ||
                (ti == 2 || ti == 3) && tl[0] == 5 ||
                (ti == 4 || ti == 5) && tl[1] == 5 ||
                (ti == 6 || ti == 7) && tl[0] == 0) {
            return true;
        }
        return false;
    }

    /* Test */
    static Board b;
    static Token token;
    static Player p;
    static Tile tile;

    // Legal 1: place a tile, not tile around it on board
    static public void createExample1() {
        b = new Board();
        token = new Token(0, 4, new int[] {0,0});
        tile = new Tile(new int[][] {{0,7}, {1,4}, {2,5}, {3,6}});
        ArrayList<Tile> hand = new ArrayList<>();
        p = new Player(token, hand);
        p.draw(tile);
    }

    // Legal 2: place a tile, move to some tile not at edge
    static public void createExample2() {
        b = new Board();
        token = new Token(0, 4,new int[] {0,0});
        Tile tile1 = new Tile(new int[][] {{0,7}, {1,4}, {2,5}, {3,6}});
        Tile tile2= new Tile(new int[][] {{0,5}, {1,2}, {3,6}, {4,7}});
        b.placeTile(tile1, 0, 0);
        b.placeTile(tile2,1,1);
        tile = new Tile(new int[][] {{0,5}, {1,2}, {3,4}, {6,7}});
        ArrayList<Tile> hand = new ArrayList<>();
        p = new Player(token, hand);
        p.draw(tile);
    }

    // Legal 3: place a tile, all rotation leads to elimination
    static public void createExample3() {
        b = new Board();
        token = new Token(0, 1,new int[] {0,1});
        Tile tile1 = new Tile(new int[][] {{0,7}, {1,4}, {2,5}, {3,6}});
        b.placeTile(tile1, 0, 1);
        tile = new Tile(new int[][] {{0,5}, {1,4}, {2,7}, {3,6}});
        ArrayList<Tile> hand = new ArrayList<>();
        p = new Player(token, hand);
        p.draw(tile);
    }

    // Illegal 1: tile to be placed is not in player's hand
    static public void createExample4() {
        b = new Board();
        token = new Token(0, 1,new int[] {0,1});
        Tile tile1 = new Tile(new int[][] {{0,7}, {1,4}, {2,5}, {3,6}});
        b.placeTile(tile1, 0, 1);
        tile = new Tile(new int[][] {{0,5}, {1,4}, {2,7}, {3,6}});
        ArrayList<Tile> hand = new ArrayList<>();
        p = new Player(token, hand);
    }

    public static void main(String argv[]) {
        createExample1();
        Tester.check(server.legalPlay(p, b, tile) == true, "Legal 1");
        createExample2();
        Tester.check(server.legalPlay(p, b, tile) == true, "Legal 2");
        createExample3();
        Tester.check(server.legalPlay(p, b, tile) == true, "Legal 3");
        createExample4();
        Tester.check(server.legalPlay(p, b, tile) == false, "Illegal 1");
    }
}
