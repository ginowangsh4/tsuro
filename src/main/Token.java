import java.util.HashMap;
import java.util.Map;

public class Token {

    private final int color;
    private int indexOnTile;
    private int[] position;
    public final static Map<Integer, String> colorMap = new HashMap<Integer, String>() {{
            put(0, "blue");
            put(1, "red");
            put(2, "green");
            put(3, "orange");
            put(4, "sienna");
            put(5, "hotpink");
            put(6, "darkgreen");
            put(7, "purple");
    }};

    Token (int color, int indexOnTile, int[] position) {
        if (color < 0 || color > 7) {
            throw new IllegalArgumentException("Invalid token color");
        }
        if (!legalTokenPlacement(indexOnTile, position)) {
            throw new IllegalArgumentException("Invalid token position");
        }
        this.color = color;
        this.indexOnTile = indexOnTile;
        this.position = position;
    }

    /**
     * Check if index and posn are valid inputs.
     * @param index index on tile
     * @param posn posn [x,y]
     * @return
     */
    public boolean legalTokenPlacement(int index, int[] posn) {
        int x = posn[0];
        int y = posn[1];

        if ( x > -1 && x < 6 && y > -1 && y < 6) return true;
        if (x == -1 && y > -1 && y < 6) {
            if (index == 2 || index == 3) return true;
        }
        if (x == 6 && y > -1 && y < 6) {
            if (index == 6 || index == 7) return true;
        }
        if (y == -1 && x > -1 && x < 6) {
            if (index == 4 || index == 5) return true;
        }
        if (y == 6 && x > -1 && x < 6) {
            if (index == 0 || index == 1) return true;
        }
        return false;
    }

    public int getColor(){ return this.color; }

    public String getColorStr(){ return this.colorMap.get(this.color); }

    public int getIndex() { return indexOnTile; }

    public int[] getPosition() { return position; }

    /**
     * Check whether two tokens are the same based on color
     * @param t a token to be checked against
     * @return true of the two tokens are the same; false if not
     */
    public boolean equals(Token t) {
        return this.color == t.color ? true:false;
    }

    /**
     * Check whether the token is on the edge of the board
     * @return true if on the edge; false if not
     */
    public boolean isOffBoard() {
        int ti = this.getIndex();
        int[] tl = this.getPosition();
        if ((ti == 0 || ti == 1) && tl[1] == 0 ||
                (ti == 2 || ti == 3) && tl[0] == 5 ||
                (ti == 4 || ti == 5) && tl[1] == 5 ||
                (ti == 6 || ti == 7) && tl[0] == 0) {
            return true;
        }
        return false;
    }
}
