package za.co.entelect.challenge.entities;

import java.util.Optional;

import com.google.gson.annotations.SerializedName;

import za.co.entelect.challenge.command.SnowballCommand;

public class Worm {
    @SerializedName("id")
    public int id;

    @SerializedName("health")
    public int health;

    @SerializedName("position")
    public Position position;

    @SerializedName("diggingRange")
    public int diggingRange;

    @SerializedName("movementRange")
    public int movementRange;

    @SerializedName("profession")
    public String profession;

    @SerializedName("bananaBombs")
    public BananaBombWeapon bananaBombWeapon;

    @SerializedName("snowballs")
    public SnowballWeapon snowballWeapon;

    public Boolean canBananaBomb() {
        if (this.profession.equals("Agent")) {
            return this.bananaBombWeapon.count > 0;
        } else {
            return false;
        }
    }

    public Boolean canSnowball() {
        if (this.profession.equals("Technologist")) {
            return this.snowballWeapon.count > 0;
        } else {
            return false;
        }
    }

}
