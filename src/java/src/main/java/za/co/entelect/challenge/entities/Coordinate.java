package za.co.entelect.challenge.entities;

import java.util.ArrayList;
import java.util.List;

public class Coordinate extends Object {
    public int x;
    public int y;

    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

//    public int getX(){
//        return this.x;
//    }
//
//    public int getY(){
//        return this.y;
//    }


    public static Boolean isEqualCoordinate(Coordinate c1, Coordinate c2) {
        return c1.x == c2.x && c1.y == c2.y;
    }

    public Boolean isN() {
        return this.x == 0 && this.y > 0;
    }

    public Boolean isNE() {
        return this.x == this.y && this.x > 0 && this.y > 0;
    }

    public Boolean isE() {
        return this.x > 0 && this.y == 0;
    }

    public Boolean isSE() {
        return this.x == -this.y && this.y < 0 && this.x > 0;
    }

    public Boolean isS() {
        return this.x == 0 && this.y < 0;
    }

    public Boolean isSW() {
        return this.x == this.y && this.x < 0 && this.y < 0;
    }

    public Boolean isW() {
        return this.x < 0 && this.y == 0;
    }

    public Boolean isNW() {
        return this.y == -this.x && this.x < 0 && this.y > 0;
    }

    public static Coordinate[] getAllSurroundingCoordinate(){
        List<Coordinate> allSurroundingCoordinate = new ArrayList<>();

        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                if (i != 0 & j != 0) {
                    Coordinate c = new Coordinate(i, j);
                    allSurroundingCoordinate.add(c);
                }
            }
        }

        return allSurroundingCoordinate.toArray(new Coordinate[allSurroundingCoordinate.size()]);
    }

}
