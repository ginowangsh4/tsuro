package tsuro.parser;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import tsuro.*;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class TileParser implements IParser<Tile> {
    private DocumentBuilder db;

    public TileParser(DocumentBuilder db) {
        this.db = db;
    }

    public Document buildXML(Tile t) {
        Document doc = db.newDocument();
//        Element ent = doc.createElement("ent");
        Element tile = doc.createElement("tile");
        int[][] paths = t.paths;
        for (int i = 0; i < 4; i++) {
            Element connect = doc.createElement("connect");
            Element n1 = doc.createElement("n");
            Element n2 = doc.createElement("n");
            n1.appendChild(doc.createTextNode(Integer.toString(paths[i][0])));
            n2.appendChild(doc.createTextNode(Integer.toString(paths[i][1])));
            connect.appendChild(n1);
            connect.appendChild(n2);
            tile.appendChild(connect);
        }
//        ent.appendChild(tile);
        doc.appendChild(tile);
        return doc;
    }

    public Tile fromXML(Document doc) {
//        Node ent = doc.getFirstChild();
//        if (!ent.getNodeName().equals("ent")) {
//            throw new IllegalArgumentException("Parse Error: Cannot find <ent></ent>");
//        }
        Node tile = doc.getFirstChild();
        if (!tile.getNodeName().equals("tile")) {
            throw new IllegalArgumentException("Parse Error: Cannot find <tile></tile>");
        }
        int[][] paths = new int[4][2];
        NodeList connects = doc.getElementsByTagName("connect");
        for (int i = 0; i < connects.getLength(); i++) {
            Node connect = connects.item(i);
            Node n1 = connect.getFirstChild();
            Node n2 = connect.getLastChild();
            paths[i][0] = Integer.parseInt(n1.getTextContent());
            paths[i][1] = Integer.parseInt(n2.getTextContent());
        }
        return new Tile(paths);
    }

    public static void main(String[] args) throws Exception {

        int[] p1= new int[]{0,7};
        int[] p2= new int[]{1,2};
        int[] p3= new int[]{3,4};
        int[] p4= new int[]{5,6};
        int[][] paths = new int[][]{p1,p2,p3,p4};
        Tile t1 = new Tile(paths);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        TileParser tileParser = new TileParser(db);
        Document doc = tileParser.buildXML(t1);
        System.out.println(Parser.documentToString(doc));
    }
}
