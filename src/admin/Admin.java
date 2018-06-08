package tsuro.admin;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import tsuro.Board;
import tsuro.MPlayer;
import tsuro.Tile;
import tsuro.Token;
import tsuro.parser.Parser;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.List;

// Localhost IP: "127.0.0.1"
// Richie IP: "10.105.75.35"
// Jin IP: "10.105.16.16"

public class Admin {
    private static DocumentBuilder db;
    private static AdminSocket socket;
    private static Parser parser;
    private static MPlayer mPlayer;

    // commend line arguments as "PORT_NUMBER PLAYER_NAME STRATEGY(R/MS/LS)"
    // if there is not argument for strategy, the default is to use a Random strategy
    public static void main(String[] args) throws Exception {
        // set up connection to local host
        db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        parser = new Parser(db);
        socket = new AdminSocket("127.0.0.1", Integer.parseInt(args[0]));
        switch (args[2]) {
            case "R":
                mPlayer = new MPlayer(MPlayer.Strategy.R, args[1]);
                break;
            case "LS":
                mPlayer = new MPlayer(MPlayer.Strategy.LS, args[1]);
                break;
            case "MS":
                mPlayer = new MPlayer(MPlayer.Strategy.MS, args[1]);
                break;
            default:
                mPlayer = new MPlayer(MPlayer.Strategy.R, args[1]);
        }
        while (socket.connectionEstablished()) {
            String res = socket.readInputFromServer();
            // server has closed the connection
            if (res == null) break;
            Document doc = parser.stringToDocument(res);
            Node node = doc.getFirstChild();
            switch (node.getNodeName()) {
                case "get-name":
                    processGetName();
                    break;
                case "initialize":
                    processInitialize(node);
                    break;
                case "place-pawn":
                    processPlacePawn(node);
                    break;
                case "play-turn":
                    processPlayTurn(node);
                    break;
                case "end-game":
                    processEndGame(node);
                    break;
                default:
                    throw new IllegalArgumentException("Admin: Invalid method call over network");
            }
        }
    }

    private static void processGetName() throws Exception {
        String playerName = mPlayer.getName();
        Document getNameResXML = parser.buildPlayerNameXML(playerName);
        sendXMLToClient(getNameResXML, "Admin: get-name complete");
    }

    private static void processInitialize(Node node) throws Exception {
        Node colorNode = node.getFirstChild();

        int color = Token.getColorInt(colorNode.getTextContent());

        Node colorsNode = colorNode.getNextSibling();
        Document colorsDoc = Parser.fromNodeToDoc(colorsNode, db);
        List<Integer> colors = parser.fromColorListSetXML(colorsDoc);

        mPlayer.initialize(color, colors);
        Document voidXML = parser.buildVoidXML();
        sendXMLToClient(voidXML, "Admin: initialize complete ");
    }

    private static void processPlacePawn(Node node) throws Exception {
        Node boardNode = node.getFirstChild();
        Document boardDoc = Parser.fromNodeToDoc(boardNode, db);
        Board board = parser.boardParser.fromXML(boardDoc);

        Token token = mPlayer.placePawn(board);
        Document pawnLocXML = parser.buildPawnLocXML(token.getPosition(), token.getIndex());
        sendXMLToClient(pawnLocXML, "Admin: place-pawn complete");
    }

    private static void processPlayTurn(Node node) throws Exception {
        Node boardNode = node.getFirstChild();
        Document boardDoc = Parser.fromNodeToDoc(boardNode, db);
        Board board = parser.boardParser.fromXML(boardDoc);

        Node setNode = boardNode.getNextSibling();
        Document setDoc = Parser.fromNodeToDoc(setNode, db);
        List<Tile> hand = parser.fromTileSetXML(setDoc);

        Node nNode = setNode.getNextSibling();
        int tilesLeft = Integer.parseInt(nNode.getFirstChild().getTextContent());

        Tile tile = mPlayer.playTurn(board, hand, tilesLeft);
        Document tileXML = parser.tileParser.buildXML(tile);
        sendXMLToClient(tileXML, "Admin: play-turn complete");
    }

    private static void processEndGame(Node node) throws Exception {
        Node boardNode = node.getFirstChild();
        Document boardDoc = Parser.fromNodeToDoc(boardNode, db);
        Board board = parser.boardParser.fromXML(boardDoc);

        Node setNode = boardNode.getNextSibling();
        Document setDoc = Parser.fromNodeToDoc(setNode, db);
        List<Integer> colors = parser.fromColorListSetXML(setDoc);

        mPlayer.endGame(board, colors);
        Document voidXML = parser.buildVoidXML();
        sendXMLToClient(voidXML, "Admin: end-game complete");
    }

    private static void sendXMLToClient(Document doc, String printMessage) throws Exception {
        String s = parser.documentToString(doc);
        System.out.println(printMessage + s);
        socket.writeOutputToServer(s);
    }
}
