package tsuro;

import org.w3c.dom.Document;
import tsuro.parser.Parser;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.List;

public class HPlayer implements IPlayer {

    private BufferedReader in;
    private PrintWriter out;
    private Parser parser;

    private String name;
    private int color;
    private List<Integer> colors;
    private boolean isWinner;

    public HPlayer(String name) throws Exception {
        ServerSocket socketListener = new ServerSocket(10000);
        Socket socket = socketListener.accept();
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.parser = new Parser(DocumentBuilderFactory.newInstance().newDocumentBuilder());
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void initialize (int color, List<Integer> colors) {
        this.color = color;
        this.colors = colors;
    }

    public Token placePawn(Board b) throws Exception {
        generateBoardImage(parser.boardParser.buildXML(b), -1, -1);
        out.println("<place-pawn>");
        // format: [side, index on side]
        String[] chosenSideIndex= in.readLine().split(",");
        return generateTokenFromSideAndIndex(color, chosenSideIndex[0], Integer.parseInt(chosenSideIndex[1]));
    }

    public Tile playTurn(Board b, List<Tile> hand, int tilesLeft) throws Exception {
        generateImages(b, hand);
        out.println("<play-turn>");
        // inform GUI about hand size
        out.println(String.valueOf(hand.size()));
        // format: [index of tile in hand, index of tile rotation]
        String[] chosenTile = in.readLine().split(",");
        return generateTile(hand, Integer.parseInt(chosenTile[0]), Integer.parseInt(chosenTile[1]));
    }

    public void endGame(Board b, List<Integer> colors) throws Exception {
        generateBoardImage(parser.boardParser.buildXML(b), -1, -1);
        out.println("<end-game>");
        String winnerList = "";
        for (int i = 0; i < colors.size(); i++) {
            String colorString = Token.colorMap.get(colors.get(i));
            winnerList = winnerList + colorString;
            if (i != colors.size() - 1) {
                winnerList += ", ";
            }
        }
        out.println(winnerList);
    }

    private Token findMyToken(Board b) {
        for (SPlayer sp : b.getSPlayerList()) {
            if (sp.getToken().getColor() == color) {
                return sp.getToken();
            }
        }
        throw new IllegalArgumentException("Cannot find token on board");
    }

    public static Token generateTokenFromSideAndIndex(int colorIndex, String side, int index) throws Exception {
        if (index < 0 || index > 11) {
            throw new Exception("Index is not valid");
        }
        int indexOnTile, x, y;
        switch (side) {
            case "TOP":
                x = index / 2;
                y = -1;
                indexOnTile = Tile.neighborIndex.get(index % 2);
                break;
            case "BOTTOM":
                x = index / 2;
                y = 6;
                indexOnTile = index % 2;
                break;
            case "LEFT":
                x = -1;
                y = index / 2;
                indexOnTile = index % 2 + 2;
                break;
            default: // RIGHT
                x = 6;
                y = index / 2;
                indexOnTile = Tile.neighborIndex.get(index % 2 + 2);
                break;
        }
        int[] pos = new int[]{x,y};
        return new Token(colorIndex, pos, indexOnTile);
    }

    private Tile generateTile(List<Tile> hand, int handIndex, int rotationIndex) {
        Tile move = hand.get(handIndex).copyTile();
        for (int i = 0; i < rotationIndex; i++) {
            move.rotateTile();
        }
        return move;
    }

    private void generateImages(Board b, List<Tile> hand) throws Exception {
        Document boardXML = parser.boardParser.buildXML(b);
        generateBoardImage(boardXML, -1, -1);
        Token token = findMyToken(b);
        int[] location = Board.getAdjacentLocation(token);
        for (int i = 0; i < hand.size(); i++) {
            Tile copy = hand.get(i).copyTile();
            for (int j = 0; j < 4; j++) {
                Board tempBoard = parser.boardParser.fromXML(boardXML);
                tempBoard.placeTile(copy, location[0], location[1]);
                for (SPlayer sp : tempBoard.getSPlayerList()) {
                    sp.updateToken(tempBoard.simulateMove(sp.getToken()));
                }
                generateBoardImage(parser.boardParser.buildXML(tempBoard), i, j);
                System.out.println("HPlayer: play-turn board image (" + i + ", " + j + ") generated");
                copy.rotateTile();
            }
        }
        for (int i = 0; i < hand.size(); i++) {
            generateTileImage(parser.tileParser.buildXML(hand.get(i)), i);
            System.out.println("HPlayer: play-turn tile image (" + i + ") generated");
        }
    }

    private void generateBoardImage(Document doc, int tileIndex, int rotationIndex) throws Exception {
        String command = "./visualize -b -i image/board/" + tileIndex + "/" + rotationIndex + ".png";
        if (tileIndex == -1 && rotationIndex == -1) {
            command = "./visualize -b -i image/board/board.png";
        }
        String line;
        Process p = Runtime.getRuntime().exec(command);
        PrintWriter out = new PrintWriter(p.getOutputStream(), true);
        out.println(parser.documentToString(doc));
        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        while ((line = in.readLine()) != null) {
            System.out.println(line);
        }
        in.close();
    }

    private void generateTileImage(Document doc, int tileIndex) throws Exception {
        String command = "./visualize -t -i image/hand/" + tileIndex + ".png";
        String line;
        Process p = Runtime.getRuntime().exec(command);
        PrintWriter out = new PrintWriter(p.getOutputStream(), true);
        out.println(parser.documentToString(doc));
        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        while ((line = in.readLine()) != null) {
            System.out.println(line);
        }
        in.close();
    }
}
