package za.co.entelect.challenge.command;

import za.co.entelect.challenge.entities.Coordinate;

public class AttackCommand {

    public Coordinate coordinate;
    public String command;
    public int damage;

    public AttackCommand(Coordinate coordinate, String command, int damage) {
        this.coordinate = coordinate;
        this.command = command;
        this.damage = damage;
    }

}
