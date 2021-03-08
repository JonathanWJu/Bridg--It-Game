import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.function.BiFunction;

import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

class Cell implements Iterable<Cell> {
  int player;
  int x; 
  int y;
  Cell top;
  Cell right;
  Cell bottom;
  Cell left;
  
  // constructs an instance of a Cell with no adjacent cells
  Cell(int player, int x, int y) {
    /* player value represents who has taken this cell
     * 0 = nobody
     * 1 = player 1
     * 2 = player 2
     */
    this.player = player;
    this.x = x;
    this.y = y;
    this.top = null;
    this.right = null;
    this.bottom = null;
    this.left = null;

  }
  
  // constructs a cell with the given adjacent cells
  Cell(int player, int x, int y, Cell top, Cell right, Cell bottom, Cell left) {
    this.player = player;
    this.x = x;
    this.y = y;
    this.top = top;
    this.right = right;
    this.bottom = bottom;
    this.left = left;
    
    this.top.setBottom(this);
    this.right.setLeft(this);
    this.bottom.setTop(this);
    this.left.setRight(this);
  }
  
  // creates an iterator that iterates over the adjacent cells in clockwise order from the top
  public Iterator<Cell> iterator() {
    return new SideIterator(
        new ArrayList<Cell>(Arrays.asList(
            this.top, 
            this.right, 
            this.bottom, 
            this.left)));
  }
  
  // has somebody taken this cell
  boolean taken() {
    return this.player != 0;
  }
  

  // sets the left field to the given cell
  void setLeft(Cell c) {
    this.left = c;
    c.right = this;
  }
  
  //sets the right field to the given cell
  void setRight(Cell c) {
    this.right = c;
    c.left = this;
  }
  
  //sets the top field to the given cell
  void setTop(Cell c) {
    this.top = c;
    c.bottom = this;
  }
  
  //sets the bottom field to the given cell
  void setBottom(Cell c) {
    this.bottom = c;
    c.top = this;
  }
  
  // draws an image of this cell
  WorldImage drawCell() {
    ArrayList<Color> colors = new ArrayList<Color>(
        Arrays.asList(
            Color.WHITE, 
            Color.PINK, 
            Color.MAGENTA));
    return new RectangleImage(50, 50, OutlineMode.SOLID, colors.get(this.player));
  }
  
  // determines if this cell is connected to one of the edges in the given direction
  // is initiated with the board size and an empty list of visited cells
  boolean hasPath(int boardSize, 
      ArrayList<Cell> visited, 
      BiFunction<Integer, Cell, Boolean> direction) {
    visited.add(this);
    for (Cell c : this) {
      if (c != null 
          && visited.indexOf(c) == -1 
          && c.player == this.player) {
        if (direction.apply(boardSize, c) || c.hasPath(boardSize, visited, direction)) {
          return true;
        }
      }
    }
    return false;
  }

}

// iterator class for Cell which iterates through the linked cells in clockwise order
// starting at the top
class SideIterator implements Iterator<Cell> {
  ArrayList<Cell> sides;
  int currIndex = 0;
  
  SideIterator(ArrayList<Cell> sides) {
    this.sides = sides;
  }

  // is there a next value to return
  public boolean hasNext() {
    return this.currIndex < 4;
  }
  
  // returns the next value in the iterator
  public Cell next() {
    Cell answer = this.sides.get(currIndex);
    this.currIndex += 1;
    return answer;
  }
}

// enforces the rules of the game (no changing the cells on the border)
class ValidMove implements BiFunction<Integer, BridgItWorld, Boolean> {
  public Boolean apply(Integer t, BridgItWorld u) {
    Cell cell = u.board.get(t);
    return cell.x > 0
        && cell.x < u.boardSize - 1
        && cell.y > 0
        && cell.y < u.boardSize - 1;
  } 
}

// is cell u at the top of the board of size t
class UpDirection implements BiFunction<Integer, Cell, Boolean> {
  public Boolean apply(Integer t, Cell u) {
    return u.y == 0;
  }
}

//is cell u at the bottom of the board of size t
class DownDirection implements BiFunction<Integer, Cell, Boolean> {
  public Boolean apply(Integer t, Cell u) {
    return u.y == t - 1;
  }
}

//is cell u at the right of the board of size t
class RightDirection implements BiFunction<Integer, Cell, Boolean> {
  public Boolean apply(Integer t, Cell u) {
    return u.x == t - 1;
  }
}

//is cell u at the left of the board of size t
class LeftDirection implements BiFunction<Integer, Cell, Boolean> {
  public Boolean apply(Integer t, Cell u) {
    return u.x == 0;
  }
}



// A class to represent the world of the game
class BridgItWorld extends World {

  int boardSize;
  ArrayList<Cell> board;
  int currentPlayer;
  
  // constructs an instance of the world
  BridgItWorld(int boardSize) {
    if (boardSize >= 3 && boardSize % 2 != 0) {
      this.boardSize = boardSize;
    }
    else {
      throw new IllegalArgumentException(
          "Board size must be an odd number greater than 3");
    }
    this.board = this.genBoard(boardSize);
    this.currentPlayer = 1;
  }
  
  // generates a size X size board of cells which cyclically reference each other
  // cells are linked across the cardinal directions
  ArrayList<Cell> genBoard(int size) {
    ArrayList<Cell> result = new ArrayList<Cell>();
    
    for (int j = 0; j < size; j++) {
      for (int i = 0; i < size; i++) {
        result.add(new Cell(0, i, j));
      }
    }
    for (Cell c : result) {
      if (c.x % 2 != 0 && c.y % 2 == 0) {
        c.player = 2;
      }
      if (c.x % 2 == 0 && c.y % 2 != 0) {
        c.player = 1;
      }
    }
    
    return this.linkCells(result);
  }
  
  
  // takes a board as input and links the cells such that adjacent cells will reference each other 
  // e.g. two cells above/below each other will reference each other with this.top and this.bottom
  ArrayList<Cell> linkCells(ArrayList<Cell> b) {
    for (Cell c : b) {
      if (c.x != 0) {
        c.setLeft(b.get(b.indexOf(c) - 1));
      }
      if (c.x != this.boardSize - 1) {
        c.setRight(b.get(b.indexOf(c) + 1));
      }
      if (c.y != 0) {
        c.setTop(b.get(b.indexOf(c) - this.boardSize));
      }
      if (c.y != this.boardSize - 1) {
        c.setBottom(b.get(b.indexOf(c) + this.boardSize));
      }
    }
    return b;
  }
  
  // Produces a world image of the given board
  WorldImage drawBoard(ArrayList<Cell> b) {
    int dimension = this.boardSize * 50;
    WorldImage boardImage = new RectangleImage(
        dimension, 
        dimension, 
        OutlineMode.SOLID, 
        Color.DARK_GRAY);
    for (Cell c : b) {
      boardImage = new OverlayOffsetAlign(
          AlignModeX.CENTER, 
          AlignModeY.MIDDLE, 
          c.drawCell(), 
          ((dimension / 2) - 25) - c.x * 50,  
          ((dimension / 2) - 25) - c.y * 50, 
          boardImage);
    }

    return boardImage;
  }
  
  // handles mouse input for the game
  // after each click, checks if a path has been created from the clicked cell
  public void onMouseClicked(Posn pos) {
    
    int x = (int) pos.x / 50;
    int y = (int) pos.y / 50;
    int index = x + (y * this.boardSize);
    if (index >= 0 && index <= Math.pow(this.boardSize, 2)) {
      if (new ValidMove().apply(index, this) 
          && !this.board.get(index).taken()) {
        this.board.get(index).player = this.currentPlayer;
        this.currentPlayer = 3 - this.currentPlayer;
      }
    }
    if (this.board.get(index).hasPath(
        boardSize, new ArrayList<Cell>(), new LeftDirection())
        && this.board.get(index).hasPath(
            boardSize, new ArrayList<Cell>(), new RightDirection())) {
      this.endOfWorld("Player 1 Wins!");
    }
    else if (this.board.get(index).hasPath(
        boardSize, new ArrayList<Cell>(), new UpDirection())
        && this.board.get(index).hasPath(
            boardSize, new ArrayList<Cell>(), new DownDirection())) {
      this.endOfWorld("Player 2 Wins!");
    }

  }
  
  
  // creates the world scene for the bridgit world
  public WorldScene makeScene() {
    WorldScene ws = new WorldScene(50 * this.boardSize, 50 * this.boardSize);
    ws.placeImageXY(this.drawBoard(this.board), this.boardSize * 25, this.boardSize * 25);
    return ws;
  }
  
  // draws the ending scene
  public WorldScene lastScene(String msg) {
    WorldScene ws = new WorldScene(50 * this.boardSize, 50 * this.boardSize);
    ws.placeImageXY(new TextImage(msg, Color.BLACK), this.boardSize * 25, this.boardSize * 25);
    return ws;
  }
}

class ExamplesBridgIt {
  void testingWorldConstructor(Tester t) {
    t.checkConstructorException(new IllegalArgumentException(
          "Board size must be an odd number greater than 3"), "BridgItWorld", 4);
    t.checkConstructorException(new IllegalArgumentException(
        "Board size must be an odd number greater than 3"), "BridgItWorld", 1);
    t.checkConstructorNoException("no exception", "BridgItWorld", 3);

  }
  
  // Since genBoard is dependent on linkCells to function
  // we consider this a suitable test for both
  void testgenBoard(Tester t) {
    BridgItWorld world = new BridgItWorld(3);
    ArrayList<Cell> linkedBoard = new ArrayList<Cell>();
    Cell cell1 = new Cell(0, 0, 0);
    Cell cell2 = new Cell(2, 1, 0);
    Cell cell3 = new Cell(0, 2, 0);
    Cell cell4 = new Cell(1, 0, 1);
    Cell cell5 = new Cell(0, 1, 1);
    Cell cell6 = new Cell(1, 2, 1);
    Cell cell7 = new Cell(0, 0, 2);
    Cell cell8 = new Cell(2, 1, 2);
    Cell cell9 = new Cell(0, 2, 2);
    
    cell5.setTop(cell2);
    cell5.setBottom(cell8);
    cell5.setLeft(cell4);
    cell5.setRight(cell6);
    cell1.setRight(cell2);
    cell2.setRight(cell3);
    cell3.setBottom(cell6);
    cell6.setBottom(cell9);
    cell9.setLeft(cell8);
    cell8.setLeft(cell7);
    cell7.setTop(cell4);
    cell4.setTop(cell1);
    
    
    linkedBoard.add(cell1);
    linkedBoard.add(cell2);
    linkedBoard.add(cell3);
    linkedBoard.add(cell4);
    linkedBoard.add(cell5);
    linkedBoard.add(cell6);
    linkedBoard.add(cell7);
    linkedBoard.add(cell8);
    linkedBoard.add(cell9);
    
    
    t.checkExpect(world.genBoard(3), linkedBoard);
  }
  
  void testDraw(Tester t) {
    WorldImage whiteCell = new RectangleImage(50, 50, OutlineMode.SOLID, Color.WHITE);
    WorldImage pinkCell = new RectangleImage(50, 50, OutlineMode.SOLID, Color.PINK);
    WorldImage magentaCell = new RectangleImage(50, 50, OutlineMode.SOLID, Color.MAGENTA);
    
    Cell empty = new Cell(0, 0, 0);
    Cell player1 = new Cell(1, 0, 0);
    Cell player2 = new Cell(2, 0, 0);
    
    t.checkExpect(empty.drawCell(), whiteCell);
    t.checkExpect(player1.drawCell(), pinkCell);
    t.checkExpect(player2.drawCell(), magentaCell);
    
    BridgItWorld world = new BridgItWorld(3);
    
    WorldImage board = new OverlayOffsetAlign(
        AlignModeX.CENTER, AlignModeY.MIDDLE, 
        new RectangleImage(50, 50, OutlineMode.SOLID, Color.WHITE), 
        -50, -50, 
        new OverlayOffsetAlign(
            AlignModeX.CENTER, AlignModeY.MIDDLE, 
            new RectangleImage(50, 50, OutlineMode.SOLID, Color.MAGENTA), 
            0, -50, 
            new OverlayOffsetAlign(
                AlignModeX.CENTER, AlignModeY.MIDDLE, 
                new RectangleImage(50, 50, OutlineMode.SOLID, Color.WHITE), 
                50, -50, 
                new OverlayOffsetAlign(
                    AlignModeX.CENTER, AlignModeY.MIDDLE, 
                    new RectangleImage(50, 50, OutlineMode.SOLID, Color.PINK), 
                    -50, 0, 
                    new OverlayOffsetAlign(
                        AlignModeX.CENTER, AlignModeY.MIDDLE, 
                        new RectangleImage(50, 50, OutlineMode.SOLID, Color.WHITE), 
                        0, 0, 
                        new OverlayOffsetAlign(
                            AlignModeX.CENTER, AlignModeY.MIDDLE, 
                            new RectangleImage(50, 50, OutlineMode.SOLID, Color.PINK), 
                            50, 0, 
                            new OverlayOffsetAlign(
                                AlignModeX.CENTER, AlignModeY.MIDDLE, 
                                new RectangleImage(50, 50, OutlineMode.SOLID, Color.WHITE),
                                -50, 50, 
                                new OverlayOffsetAlign(
                                    AlignModeX.CENTER, AlignModeY.MIDDLE, 
                                    new RectangleImage(50, 50, OutlineMode.SOLID, Color.MAGENTA),
                                    0, 50, 
                                    new OverlayOffsetAlign(
                                        AlignModeX.CENTER, AlignModeY.MIDDLE, 
                                        new RectangleImage(50, 50, OutlineMode.SOLID, Color.WHITE),
                                        50, 50, 
                                        new RectangleImage(
                                            150, 150, OutlineMode.SOLID, Color.DARK_GRAY))))))))));
    t.checkExpect(world.drawBoard(world.board), board);
  }
  

  void testingSetRight(Tester t) {
    Cell cell1 = new Cell(0, 0, 0);
    Cell cell2 = new Cell(0, 1, 0);
    t.checkExpect(cell1.right, null);
    t.checkExpect(cell2.left, null);
    cell1.setRight(cell2);
    t.checkExpect(cell1.right, cell2);
    t.checkExpect(cell2.left, cell1);
  }
  
  void testingSetLeft(Tester t) {
    Cell cell1 = new Cell(0, 0, 0);
    Cell cell2 = new Cell(0, 1, 0);
    t.checkExpect(cell1.right, null);
    t.checkExpect(cell2.left, null);
    cell2.setLeft(cell1);
    t.checkExpect(cell1.right, cell2);
    t.checkExpect(cell2.left, cell1);
  }

  void testingSetTop(Tester t) {
    Cell cell1 = new Cell(0, 0, 0);
    Cell cell2 = new Cell(0, 0, 1);
    t.checkExpect(cell1.bottom, null);
    t.checkExpect(cell2.top, null);
    cell2.setTop(cell1);
    t.checkExpect(cell1.bottom, cell2);
    t.checkExpect(cell2.top, cell1);
  }
  
  void testingSetBottom(Tester t) {
    Cell cell1 = new Cell(0, 0, 0);
    Cell cell2 = new Cell(0, 0, 1);
    t.checkExpect(cell1.bottom, null);
    t.checkExpect(cell2.top, null);
    cell1.setBottom(cell2);
    t.checkExpect(cell1.bottom, cell2);
    t.checkExpect(cell2.top, cell1);
  }
  
  void testingTaken(Tester t) {
    Cell cell1 = new Cell(0, 0, 0);
    Cell cell2 = new Cell(1, 0, 0);
    Cell cell3 = new Cell(2, 0, 0);
    
    t.checkExpect(cell1.taken(), false);
    t.checkExpect(cell2.taken(), true);
    t.checkExpect(cell3.taken(), true);
  }
  
  void testHasPath(Tester t) {
    ArrayList<Cell> linkedBoard = new ArrayList<Cell>();
    
    Cell cell1 = new Cell(0, 0, 0);
    Cell cell2 = new Cell(2, 1, 0);
    Cell cell3 = new Cell(0, 2, 0);
    Cell cell4 = new Cell(1, 0, 1);
    Cell cell5 = new Cell(2, 1, 1);
    Cell cell6 = new Cell(1, 2, 1);
    Cell cell7 = new Cell(0, 0, 2);
    Cell cell8 = new Cell(2, 1, 2);
    Cell cell9 = new Cell(0, 2, 2);
    
    cell5.setTop(cell2);
    cell5.setBottom(cell8);
    cell5.setLeft(cell4);
    cell5.setRight(cell6);
    cell1.setRight(cell2);
    cell2.setRight(cell3);
    cell3.setBottom(cell6);
    cell6.setBottom(cell9);
    cell9.setLeft(cell8);
    cell8.setLeft(cell7);
    cell7.setTop(cell4);
    cell4.setTop(cell1);
    
    
    linkedBoard.add(cell1);
    linkedBoard.add(cell2);
    linkedBoard.add(cell3);
    linkedBoard.add(cell4);
    linkedBoard.add(cell5);
    linkedBoard.add(cell6);
    linkedBoard.add(cell7);
    linkedBoard.add(cell8);
    linkedBoard.add(cell9);
    
    t.checkExpect(cell8.hasPath(3, new ArrayList<Cell>(), new UpDirection()), true);
    t.checkExpect(cell4.hasPath(3, new ArrayList<Cell>(), new RightDirection()), false);
    
    cell5.player = 1;
    
    t.checkExpect(cell8.hasPath(3, new ArrayList<Cell>(), new UpDirection()), false);
    t.checkExpect(cell4.hasPath(3, new ArrayList<Cell>(), new RightDirection()), true);
    
    // testing a non linear path
    /* For example: cells 1 > 2 > 5 > 8 > 9
     * --X 
     * X|X
     * X--
     */
    cell1.player = 1;
    cell9.player = 1;
    
    t.checkExpect(cell1.hasPath(3, new ArrayList<Cell>(), new RightDirection()), true);
  }
  
  void testIterator(Tester t) {
    Cell cell1 = new Cell(0, 1, 0);
    Cell cell2 = new Cell(0, 1, 2);
    Cell cell3 = new Cell(0, 2, 1);
    Cell cell4 = new Cell(0, 0, 1);
    Cell cell5 = new Cell(0, 1, 1, cell1, cell2, cell3, cell4);
    
    Iterator<Cell> sideIter = cell5.iterator();
    
    t.checkExpect(sideIter.next(), cell1, "top");
    t.checkExpect(sideIter.next(), cell2, "right");
    t.checkExpect(sideIter.next(), cell3, "bottom"); 
    t.checkExpect(sideIter.next(), cell4, "left");
  }
  
  void testingLastScene(Tester t) {
    World world1 = new BridgItWorld(3);
    WorldScene ws = new WorldScene(150, 150);
    ws.placeImageXY(new TextImage("Any String", Color.BLACK), 75, 75);
    
    t.checkExpect(world1.lastScene("Any String"), ws);
  }
  
  void testingMakeScene(Tester t) {
    BridgItWorld world1 = new BridgItWorld(3);
    WorldImage board = new OverlayOffsetAlign(
        AlignModeX.CENTER, AlignModeY.MIDDLE, 
        new RectangleImage(50, 50, OutlineMode.SOLID, Color.WHITE), 
        -50, -50, 
        new OverlayOffsetAlign(
            AlignModeX.CENTER, AlignModeY.MIDDLE, 
            new RectangleImage(50, 50, OutlineMode.SOLID, Color.MAGENTA), 
            0, -50, 
            new OverlayOffsetAlign(
                AlignModeX.CENTER, AlignModeY.MIDDLE, 
                new RectangleImage(50, 50, OutlineMode.SOLID, Color.WHITE), 
                50, -50, 
                new OverlayOffsetAlign(
                    AlignModeX.CENTER, AlignModeY.MIDDLE, 
                    new RectangleImage(50, 50, OutlineMode.SOLID, Color.PINK), 
                    -50, 0, 
                    new OverlayOffsetAlign(
                        AlignModeX.CENTER, AlignModeY.MIDDLE, 
                        new RectangleImage(50, 50, OutlineMode.SOLID, Color.WHITE), 
                        0, 0, 
                        new OverlayOffsetAlign(
                            AlignModeX.CENTER, AlignModeY.MIDDLE, 
                            new RectangleImage(50, 50, OutlineMode.SOLID, Color.PINK), 
                            50, 0, 
                            new OverlayOffsetAlign(
                                AlignModeX.CENTER, AlignModeY.MIDDLE, 
                                new RectangleImage(50, 50, OutlineMode.SOLID, Color.WHITE),
                                -50, 50, 
                                new OverlayOffsetAlign(
                                    AlignModeX.CENTER, AlignModeY.MIDDLE, 
                                    new RectangleImage(50, 50, OutlineMode.SOLID, Color.MAGENTA),
                                    0, 50, 
                                    new OverlayOffsetAlign(
                                        AlignModeX.CENTER, AlignModeY.MIDDLE, 
                                        new RectangleImage(50, 50, OutlineMode.SOLID, Color.WHITE),
                                        50, 50, 
                                        new RectangleImage(
                                            150, 150, OutlineMode.SOLID, Color.DARK_GRAY))))))))));
    
    WorldScene ws = new WorldScene(150, 150);
    ws.placeImageXY(board, 75, 75);
    
    t.checkExpect(world1.makeScene(), ws);
  }
  
  void testValidMove(Tester t) {
    BridgItWorld world = new BridgItWorld(3);
    ArrayList<Cell> linkedBoard = new ArrayList<Cell>();
    Cell cell1 = new Cell(0, 0, 0);
    Cell cell2 = new Cell(2, 1, 0);
    Cell cell3 = new Cell(0, 2, 0);
    Cell cell4 = new Cell(1, 0, 1);
    Cell cell5 = new Cell(0, 1, 1);
    Cell cell6 = new Cell(1, 2, 1);
    Cell cell7 = new Cell(0, 0, 2);
    Cell cell8 = new Cell(2, 1, 2);
    Cell cell9 = new Cell(0, 2, 2);
    
    cell5.setTop(cell2);
    cell5.setBottom(cell8);
    cell5.setLeft(cell4);
    cell5.setRight(cell6);
    cell1.setRight(cell2);
    cell2.setRight(cell3);
    cell3.setBottom(cell6);
    cell6.setBottom(cell9);
    cell9.setLeft(cell8);
    cell8.setLeft(cell7);
    cell7.setTop(cell4);
    cell4.setTop(cell1);
    
    
    linkedBoard.add(cell1);
    linkedBoard.add(cell2);
    linkedBoard.add(cell3);
    linkedBoard.add(cell4);
    linkedBoard.add(cell5);
    linkedBoard.add(cell6);
    linkedBoard.add(cell7);
    linkedBoard.add(cell8);
    linkedBoard.add(cell9);
    
    t.checkExpect(new ValidMove().apply(1, world), false);
    t.checkExpect(new ValidMove().apply(7, world), false);
    t.checkExpect(new ValidMove().apply(0, world), false);
    t.checkExpect(new ValidMove().apply(2, world), false);
    t.checkExpect(new ValidMove().apply(4, world), true);
  }
  
  void testingDirections(Tester t) {
    Cell cell1 = new Cell(0, 1, 1);
    Cell cell2 = new Cell(0, 0, 0);
    Cell cell3 = new Cell(0, 2, 2);
    
    t.checkExpect(new UpDirection().apply(3, cell1), false);
    t.checkExpect(new DownDirection().apply(3, cell1), false);
    t.checkExpect(new LeftDirection().apply(3, cell1), false);
    t.checkExpect(new RightDirection().apply(3, cell1), false);
    
    t.checkExpect(new UpDirection().apply(3, cell2), true);
    t.checkExpect(new DownDirection().apply(3, cell2), false);
    t.checkExpect(new LeftDirection().apply(3, cell2), true);
    t.checkExpect(new RightDirection().apply(3, cell2), false);
    
    t.checkExpect(new UpDirection().apply(3, cell3), false);
    t.checkExpect(new DownDirection().apply(3, cell3), true);
    t.checkExpect(new LeftDirection().apply(3, cell3), false);
    t.checkExpect(new RightDirection().apply(3, cell3), true);
    
  }
  
  void testBridgItWorld(Tester t) {
    BridgItWorld w = new BridgItWorld(11);
    w.bigBang(w.boardSize * 50, w.boardSize * 50, 0.05);
  }
}