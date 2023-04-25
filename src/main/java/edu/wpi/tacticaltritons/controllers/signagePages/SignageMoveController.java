package edu.wpi.tacticaltritons.controllers.signagePages;

import edu.wpi.tacticaltritons.App;
import edu.wpi.tacticaltritons.database.DAOFacade;
import edu.wpi.tacticaltritons.database.LocationName;
import edu.wpi.tacticaltritons.database.Move;
import edu.wpi.tacticaltritons.database.Node;
import edu.wpi.tacticaltritons.pathfinding.AStarAlgorithm;
import edu.wpi.tacticaltritons.pathfinding.AlgorithmSingleton;
import edu.wpi.tacticaltritons.pathfinding.Directions;
import io.github.palexdev.materialfx.controls.MFXFilterComboBox;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polyline;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import net.kurobako.gesturefx.GesturePane;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

public class SignageMoveController {
    @FXML
    private TextArea textDirections;

    @FXML
    private GesturePane gesturePane;

    @FXML
    private ImageView lowerLevel1Image;

    @FXML
    private ImageView lowerLevel2Image;

    @FXML
    private ImageView floor1Image;

    @FXML
    private ImageView floor2Image;

    @FXML
    private ImageView floor3Image;


    @FXML
    private Group L1Group;
    @FXML
    private Group L2Group;
    @FXML
    private Group floor1Group;
    @FXML
    private Group floor2Group;
    @FXML
    private Group floor3Group;

    @FXML
    private StackPane directionsPane;

    @FXML
    private Text textForDirections;

    @FXML
    private Label locationNameDisplay, roomDisplay, floorDisplay, dateDisplay, signLocationDisplay, bottomTextDisplay;
    @FXML private VBox vBox;

    LocalDate localDate = LocalDate.of(2023,4,24);
    Date today = Date.valueOf(localDate);

    public void clearAllNodes() {
        floor1Group.getChildren().remove(1, floor1Group.getChildren().size());
        floor2Group.getChildren().remove(1, floor2Group.getChildren().size());
        floor3Group.getChildren().remove(1, floor3Group.getChildren().size());
        L1Group.getChildren().remove(1, L1Group.getChildren().size());
        L2Group.getChildren().remove(1, L2Group.getChildren().size());
    }

    public Circle drawCircle(double x, double y, Color color) {
        Circle circle = new Circle();
        circle.setVisible(true);
        circle.setFill(color);
        circle.setStroke(Color.BLACK);
        circle.setStrokeWidth(3.0f);
        circle.setCenterX(x);
        circle.setCenterY(y);
        circle.setRadius(10.0);
        return circle;
    }

    public void setTextDirections(List<Node> shortestPathMap) throws SQLException {
        Directions directions = new Directions(shortestPathMap);

        List<String> position = directions.position();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < position.size(); i++) {
            sb.append(position.get(i));
            sb.append("\n");
        }
        String allPositions = sb.toString();
        textDirections.setText(allPositions);
    }

    private void setLabels(Move move, String room) {
        locationNameDisplay.setText(move.getLocation().getLongName());
        roomDisplay.setText(Integer.toString(move.getNode().getNodeID()));
        floorDisplay.setText(move.getNode().getFloor());
        dateDisplay.setText(move.getMoveDate().toString());
        signLocationDisplay.setText(room);
    }

    private void setMapPath(Move move, String display) throws SQLException {
        List<Double> xCoord = new ArrayList<Double>(0);
        List<Double> yCoord = new ArrayList<Double>(0);
        List<Double> startEnd = new ArrayList<Double>(0);
        setLabels(move, display);
        xCoord.clear();
        yCoord.clear();
        startEnd.clear();
        clearAllNodes();
        int startNodeId = DAOFacade.getNode(display,today).getNodeID();
        signLocationDisplay.setText(Integer.toString(startNodeId));
        int endNodeId = move.getNode().getNodeID();
        Node endNode1 = null;
        Node startNode1 = null;
        List<Node> shortestPathMap = new ArrayList<>();
        try {
            AStarAlgorithm mapAlgorithm = new AStarAlgorithm();
            startNode1 = DAOFacade.getNode(startNodeId);
            endNode1 = DAOFacade.getNode(endNodeId);
            shortestPathMap = AlgorithmSingleton.getInstance().algorithm.findShortestPath(startNode1, endNode1);
            setTextDirections(shortestPathMap);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        List<Double> polyList = new ArrayList<>();
        Node lastNode = shortestPathMap.get(0);
        Node finalNode = shortestPathMap.get(shortestPathMap.size() - 2);
        int change = 0;
        for (Node node : shortestPathMap) {
            if ((!node.getFloor().equals(lastNode.getFloor())) || (node.getNodeID() == shortestPathMap.get(shortestPathMap.size() - 1).getNodeID())) {
                change++;

                Circle start = new Circle();
                if (startNode1 == shortestPathMap.get(0)) {
                    start = drawCircle(startNode1.getXcoord(), startNode1.getYcoord(), Color.GREEN);
                } else {
                    start = drawCircle(startNode1.getXcoord(), startNode1.getYcoord(), Color.BLUE);
                    Node finalStartNode = lastNode;
                }

                Circle end;
                if (node == shortestPathMap.get(shortestPathMap.size() - 1)) {
                    end = drawCircle(node.getXcoord(), node.getYcoord(), Color.RED);
                    finalNode = lastNode;
                } else {
                    end = drawCircle(node.getXcoord(), node.getYcoord(), Color.BLUE);

                    Node finalEndNode = node;
                    end.setOnMouseClicked(event1 -> {
                        String endFloor = finalEndNode.getFloor();
                    });
                }
                startNode1 = node;

                Polyline path = new Polyline();
                path.setStroke(Color.RED);
                path.setOpacity(0.8);
                path.setStrokeWidth(6.0f);
                path.getPoints().addAll(polyList);

                String startFloor = lastNode.getFloor();
                if (startFloor != null) {
                    switch (startFloor) {
                        case "L1":
                            this.L1Group.getChildren().add(path);
                            this.L1Group.getChildren().add(start);
                            this.L1Group.getChildren().add(end);
                            break;
                        case "L2":
                            this.L2Group.getChildren().add(path);
                            this.L2Group.getChildren().add(start);
                            this.L2Group.getChildren().add(end);
                            break;
                        case "1":
                            this.floor1Group.getChildren().add(path);
                            this.floor1Group.getChildren().add(start);
                            this.floor1Group.getChildren().add(end);
                            break;
                        case "2":
                            this.floor2Group.getChildren().add(path);
                            this.floor2Group.getChildren().add(start);
                            this.floor2Group.getChildren().add(end);
                            break;
                        case "3":
                            this.floor3Group.getChildren().add(path);
                            this.floor3Group.getChildren().add(start);
                            this.floor3Group.getChildren().add(end);
                            break;
                    }
                }
                polyList.clear();
            }

            polyList.add((double) node.getXcoord());
            polyList.add((double) node.getYcoord());
            lastNode = node;
        }

        polyList.add((double) finalNode.getXcoord());
        polyList.add((double) finalNode.getYcoord());
        Polyline path = new Polyline();
        path.setStroke(Color.RED);
        path.setOpacity(0.8);
        path.setStrokeWidth(6.0f);
        path.getPoints().addAll(polyList);

        String startFloor = lastNode.getFloor();
        if (startFloor != null) {
            switch (startFloor) {
                case "L1":
                    this.L1Group.getChildren().add(path);
                    break;
                case "L2":
                    this.L2Group.getChildren().add(path);
                    break;
                case "1":
                    this.floor1Group.getChildren().add(path);
                    break;
                case "2":
                    this.floor2Group.getChildren().add(path);
                    break;
                case "3":
                    this.floor3Group.getChildren().add(path);
                    break;
            }
        }
        polyList.clear();

        String startingFloor = shortestPathMap.get(0).getFloor();
        if (startingFloor != null) {

            switch (startingFloor) {
                case "L1":
                    L1Group.setVisible(true);
                    lowerLevel1Image.setVisible(true);
                    break;
                case "L2":
                    L2Group.setVisible(true);
                    lowerLevel2Image.setVisible(true);
                    break;
                case "1":
                    floor1Group.setVisible(true);
                    floor1Image.setVisible(true);
                    break;
                case "2":
                    floor2Group.setVisible(true);
                    floor2Image.setVisible(true);
                    break;
                case "3":
                    floor3Group.setVisible(true);
                    floor3Image.setVisible(true);
                    break;
            }
        }
        Point2D centerpoint = new Point2D(shortestPathMap.get(0).getXcoord() + 200, shortestPathMap.get(0).getYcoord());

        gesturePane.zoomTo(1, centerpoint);
        gesturePane.centreOn(centerpoint); //TODO fix

        this.gesturePane.setVisible(true);
        this.floor1Image.setVisible(true);
        gesturePane.setScrollBarPolicy(GesturePane.ScrollBarPolicy.NEVER);
        gesturePane.reset();
    }

    @FXML
    private void initialize() throws SQLException {
        floor1Group.setVisible(true);
        floor1Image.setVisible(true);
        Point2D point2D = new Point2D(floor1Image.getFitWidth()/2,floor1Image.getFitHeight()/2);
        gesturePane.zoomTo(0.25,point2D);

        lowerLevel1Image.setImage(App.lowerlevel1);
        lowerLevel2Image.setImage(App.lowerlevel2);
        floor1Image.setImage(App.firstfloor);
        floor2Image.setImage(App.secondfloor);
        floor3Image.setImage(App.thirdfloor);

        gesturePane.toBack();

        List<Move> moves = DAOFacade.getAllMoves();
        HashMap<Integer, Move> hash = new HashMap<>();
        Collections.sort(moves, new Comparator<Move>() {
            @Override
            public int compare(Move o1, Move o2) {
                return o1.getMoveDate().compareTo(o2.getMoveDate());
            }
        });

        bottomTextDisplay.setText("Choose display location");
        MFXFilterComboBox<String> displaySelect = new MFXFilterComboBox<>();
        displaySelect.setPromptText("Display location");
        displaySelect.setFloatingText("Display location");
        displaySelect.prefWidthProperty().set(300);
        for(LocationName location:DAOFacade.getAllLocationNames()){
            displaySelect.getItems().add(location.getLongName());
        }
        vBox.getChildren().add(displaySelect);
            displaySelect.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    try {
                        setMapPath(moves.get(0),displaySelect.getSelectedItem());
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    bottomTextDisplay.setText("All Moves:");
                    vBox.getChildren().remove(displaySelect);
                    for (Move move : moves) {
                        hash.put(move.getNode().getNodeID(), move);
                        if(move.getMoveDate().getTime() > (today.getTime() - 2629746e3)){ //one month before today
                            Button button = new Button(move.getLocation().getLongName() + ": " + move.getMoveDate());
                            button.getStyleClass().add("button-submit");
                            button.setPrefWidth(325);
                            button.setPrefHeight(30);
                            button.setFont(new Font(15));
                            vBox.getChildren().add(button);
                            button.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    try {
                                        setMapPath(move,displaySelect.getText());
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            });
                        }
                    }
                }
            });
//        }
    }

}

