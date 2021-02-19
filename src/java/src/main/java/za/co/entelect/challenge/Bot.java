package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.CellType;
import za.co.entelect.challenge.enums.Direction;

import java.util.*;
import java.util.stream.Collectors;
import java.lang.Math; 

public class Bot {

    private GameState gameState;
    private Opponent opponent;
    private MyWorm currentWorm;
    private Worm[] enemyWorms;

    public Bot(Random random, GameState gameState) {
        this.random = random;
        this.gameState = gameState;
        this.opponent = gameState.opponents[0];
        this.currentWorm = getCurrentWorm(gameState);
        this.enemyWorms = opponent.worms;
    }

    private MyWorm getCurrentWorm(GameState gameState) {
        return Arrays.stream(gameState.myPlayer.worms)
                .filter(myWorm -> myWorm.id == gameState.currentWormId)
                .findFirst()
                .get();
    }

    public Command run() {

        
        AttackCommand bestAttackCommand = getBestAttackCommand();//returns best attack command for this instance


        if (bestAttackCommand.damage != 0) {//check the damage attribute from bestAttack

            if (bestAttackCommand.command == "shoot") {

                return new ShootCommand(resolveDirection(currentWorm.position, bestAttackCommand.coordinate));

            } else if (bestAttackCommand.command == "bananabomb") {

                return new BananaBombCommand(bestAttackCommand.coordinate.x, bestAttackCommand.coordinate.y);

            } else {

                return new SnowballCommand(bestAttackCommand.coordinate.x, bestAttackCommand.coordinate.y);

            }
        }
        else {
            Coordinate nextMove = pursuitEnemy();//get nextmove
            Cell nextCell = getCell(nextMove.x, nextMove.y);//get next cell based on next move

            if (nextCell.type==CellType.AIR){
                return new MoveCommand(nextMove.x,nextMove.y);
            }
            else if(nextCell.type==CellType.DIRT){
                return new DigCommand(nextMove.x,nextMove.y);
            }
        }
        //if nothing else can be done
        return new DoNothingCommand();
    }

    private Cell getCell(int x, int y){//returns cell from in position x,y on the map
        return gameState.map[y][x];
    }

    int getLivingWormCount(Worm[] wormArray) {//returns number of worms with health>0 from an array of worms

        int count = 0;

        for (Worm w: wormArray) {
            if (w.health > 0) {
                count += 1;
            }
        }

        return count;
    }
        
    private int euclideanDistance(int aX, int aY, int bX, int bY) {//counts euclidean distance between 2 positions
        return (int) (Math.sqrt(Math.pow(aX - bX, 2) + Math.pow(aY - bY, 2)));
    }

    private Direction resolveDirection(Position a, Coordinate b) {//returns a direction from position a to coordinate b
        StringBuilder builder = new StringBuilder();

        int verticalComponent = b.y - a.y;
        int horizontalComponent = b.x - a.x;

        if (verticalComponent < 0) {
            builder.append('N');
        } else if (verticalComponent > 0) {
            builder.append('S');
        }

        if (horizontalComponent < 0) {
            builder.append('W');
        } else if (horizontalComponent > 0) {
            builder.append('E');
        }

        return Direction.valueOf(builder.toString());
    }

    private Worm getClosestEnemy() { //returns closest enemy worm from enemyworm array
        int myX = currentWorm.position.x;
        int myY = currentWorm.position.y;

        int currentClosestDistance = 9999999;

        Worm closestWorm = enemyWorms[0]; //the public enemy worm array is used here

        for (int i = 0; i < 3; i++) {
            if (enemyWorms[i].health > 0) {
                int eucledianDistance = euclideanDistance(myX, myY, enemyWorms[i].position.x, enemyWorms[i].position.y);
                if (currentClosestDistance > eucledianDistance) {//check if euclidean distance is closer than the current closest distance
                    currentClosestDistance = eucledianDistance;
                    closestWorm = enemyWorms[i]; //assigns closest worm from array of enemy worm
                }
            }
        }

        return closestWorm;

    }

    private Coordinate pursuitEnemy() {//returns coordinate to which the worm that is selected should go this move, it will move towards the closest enemy
        Worm closestEnemyWorm = getClosestEnemy(); //uses getclosestenemy for closest enemy worm
        int myX = currentWorm.position.x;
        int myY = currentWorm.position.y;

        Coordinate[] allSurroundingCoordinate = Coordinate.getAllSurroundingCoordinate();//coordinate array for surrounding cells

        int closestAfterDistance  = 999999;
        int closestAddedX = 0;
        int closestAddedY = 0;

        for (Coordinate currentCoordinate: allSurroundingCoordinate){
            int addedMyX = myX + currentCoordinate.x;
            int addedMyY = myY + currentCoordinate.y;

            if (closestAfterDistance > euclideanDistance(addedMyX, addedMyY, closestEnemyWorm.position.x, closestEnemyWorm.position.y)) {
                //basically, if euclidiandistance between my worm and enemy worm is smaller than current closest distance, the coordinate's x&y changes accordingly
                closestAfterDistance = euclideanDistance(addedMyX, addedMyY, closestEnemyWorm.position.x, closestEnemyWorm.position.y);
                closestAddedX = addedMyX;
                closestAddedY = addedMyY;
            }
        }

        return new Coordinate(closestAddedX, closestAddedY); //the coordinate for closest worm, can be used in a move command
    }
//
    private Coordinate[] getValidShootCoordinate(int range, Worm attackingWorm) {//returns coordinate array where shooting is possible
        ArrayList<Coordinate> coordinateArray = new ArrayList<>();
        Position currentPosition = attackingWorm.position;

        // get the position for N and S
        for (int i = -range; i <= range; i++) {
            coordinateArray.add(new Coordinate(currentPosition.x, currentPosition.y + 1));
        }

        // get the position for E and W
        for (int i = -range; i <= range; i++) {
            coordinateArray.add(new Coordinate(currentPosition.x + i, currentPosition.y));
        }

        // get the position for SW and NE
        for (int i = -range; i <= range; i++) {
            coordinateArray.add(new Coordinate(currentPosition.x + i, currentPosition.y + i));
        }

        // get the position for NW and SE
        for (int i = -range; i <= range; i++) {
            coordinateArray.add(new Coordinate(currentPosition.x + i, currentPosition.y - i));
        }

        ArrayList<Coordinate> finalResult = new ArrayList<>();

        for (Coordinate c: coordinateArray){
            if(!Coordinate.isEqualCoordinate(c, new Coordinate(currentPosition.x, currentPosition.y))){
                finalResult.add(c);
            }
        }

        return finalResult.toArray(new Coordinate[finalResult.size()]);
    }

    private Coordinate[] getValidSpecialAttackCoordinate(int range, Worm attackingWorm) {
        //. To determine if a cell is in range, calculate its euclidean distance from the worm's position, ]
        //  round it downwards to the nearest integer (floor), and check if it is less than or equal to the max range

        ArrayList<Coordinate> coordinateArray = new ArrayList<>();

        for (int i=-range; i<=range; i++) {
            for (int j=-range; j<=range; j++) {
                if (euclideanDistance(
                    attackingWorm.position.x, attackingWorm.position.y,
                    attackingWorm.position.x + i, attackingWorm.position.y + j
                    ) <= range) {
                    coordinateArray.add(new Coordinate(attackingWorm.position.x + i, attackingWorm.position.y + j));
                }
            }
        }

        return coordinateArray.toArray(new Coordinate[coordinateArray.size()]);
    }



    private Coordinate[] getBananaBombImpactCoordinate(Coordinate epicenter, int radius){//returns coordinate array that corresponds with the radius of banana bomb
     //   ▓▓▓▓▓▓▓▓▓▓▓▓▓▓
     //   ▓▓▓▓▓▓░░▓▓▓▓▓▓
     //   ▓▓▓▓░░░░░░▓▓▓▓
     //   ▓▓░░░░██░░░░▓▓
     //   ▓▓▓▓░░░░░░▓▓▓▓
     //   ▓▓▓▓▓▓░░▓▓▓▓▓▓
     //   ▓▓▓▓▓▓▓▓▓▓▓▓▓▓
     // the radius looks like this
        ArrayList<Coordinate> coordinateArray = new ArrayList<>();

        for (int i = -radius; i <= radius; i++) {

            int jMax = radius - Math.abs(i); //this is to create the shape of the radius

            for (int j = -jMax; j <= jMax; j++){
                coordinateArray.add(new Coordinate(epicenter.x + i, epicenter.y + j));
            }
        }

        return coordinateArray.toArray(new Coordinate[coordinateArray.size()]);
    }

    private Coordinate[] getSnowballImpactCoordinate(Coordinate epicenter, int radius){//returns coordinate array that corresponds with the radius of snow ball
    //░░░░░░░░░░
    //░░██████░░
    //░░██▓▓██░░
    //░░██████░░
    //░░░░░░░░░░
    //the radius looks like this
        ArrayList<Coordinate> coordinateArray = new ArrayList<>();

        for (int i = -radius; i <= radius; i++) {
            for (int j = -radius; j <= radius; j++) {
                Coordinate newPos = new Coordinate(epicenter.x + i, epicenter.y + j);
                coordinateArray.add(newPos);
            }
        }

        return coordinateArray.toArray(new Coordinate[coordinateArray.size()]);
     }

     private Boolean canAttackShoot() {
         
        // initializing the state of canAttackshoot, getting the shoot range
        Boolean canAttackShoot = false;
        int shootRange = currentWorm.weapon.range;

        // getting the valid coordinate for this attack
        Coordinate[] validShootCoordinateArray = getValidShootCoordinate(shootRange, currentWorm);

        // checking if there are living enemy worms in the valid shoot coordinate array
        for (Worm enemyWorm: enemyWorms) {

            if (enemyWorm.health > 0) {

                Coordinate enemyWormCoordinate = new Coordinate(enemyWorm.position.x, enemyWorm.position.y);

                for (Coordinate c: validShootCoordinateArray) {

                    if (Coordinate.isEqualCoordinate(enemyWormCoordinate, c)) {

                        canAttackShoot = true;
                    }

                }
            }


        }

        return canAttackShoot;
     }

     private Boolean notObstruction(Coordinate attackCoordinate) {

        // initializing the successAttack state
        Boolean successAttack = true;

        // checking if the current attack coordinate is a dirt or not
        successAttack = successAttack && getCell(attackCoordinate.x, attackCoordinate.y).type != CellType.DIRT;

        // checking if the current attack coordinate has a living friendly worm 
        for (Worm worm: gameState.myPlayer.worms) {
            if (worm.health > 0) {
                Coordinate myWormCoordinate = new Coordinate(worm.position.x, worm.position.y);
                successAttack = successAttack && !Coordinate.isEqualCoordinate(attackCoordinate, myWormCoordinate);
            }

        }

        return successAttack;

     }

     private AttackCommand getBestShootCommand() {
         // initializing the default attackcommand, with damage 0
         // if the worm can't do this attack, then it will just return an attack command with 0 damage
         AttackCommand attackCommand = new AttackCommand(new Coordinate(0, 0), "shoot", 0);

         if (canAttackShoot()) {

            // get the valid coordinate for this attack
             Coordinate[] attackCoordinateArray = getValidShootCoordinate(currentWorm.weapon.range, currentWorm);

             for (Coordinate attackCoordinate: attackCoordinateArray) {

                for (Worm enemyWorm: enemyWorms) {
                    Coordinate enemyWormCoordinate = new Coordinate(enemyWorm.position.x, enemyWorm.position.y);

                    if (Coordinate.isEqualCoordinate(attackCoordinate, enemyWormCoordinate) && enemyWorm.health > 0) {

                        // calculating the enemy distance, based on the current worm position
                        int enemyDistanceX = enemyWormCoordinate.x - currentWorm.position.x;
                        int enemyDistanceY = enemyWormCoordinate.y - currentWorm.position.y;
                        Coordinate enemyDistance = new Coordinate(enemyDistanceX, enemyDistanceY);

                        // initializing if the coordinate that is checked having obstruction or not
                        Boolean zeroObstruct = true;


                        // check what is the direction of the coordinate based on the current worm coordinate
                        // for every direction, the coordinate will be check and handled diffrently
                        // check if the coordinate have obstruction
                        // check if the coordinate is before the current coordinate respective to the current worm 

                        if (enemyDistance.isN()) {

                            for (Coordinate checkCoordinate: attackCoordinateArray) {

                                int checkDistanceX = checkCoordinate.x - currentWorm.position.x;
                                int checkDistanceY = checkCoordinate.y - currentWorm.position.y;
                                Coordinate checkDistance = new Coordinate(checkDistanceX, checkDistanceY);

                                if (checkDistance.isN() && checkCoordinate.y < enemyWormCoordinate.y) {
                                    zeroObstruct =  zeroObstruct && notObstruction(checkCoordinate);
                                }
                            }

                        } else if(enemyDistance.isNE()){

                            for (Coordinate checkCoordinate: attackCoordinateArray) {

                                int checkDistanceX = checkCoordinate.x - currentWorm.position.x;
                                int checkDistanceY = checkCoordinate.y - currentWorm.position.y;
                                Coordinate checkDistance = new Coordinate(checkDistanceX, checkDistanceY);

                                if (checkDistance.isNE() && checkCoordinate.x < enemyWormCoordinate.x && checkCoordinate.y < enemyWormCoordinate.y) {
                                    zeroObstruct =  zeroObstruct && notObstruction(checkCoordinate);
                                }
                            }


                        } else if (enemyDistance.isE()) {

                            for (Coordinate checkCoordinate: attackCoordinateArray){

                                int checkDistanceX = checkCoordinate.x - currentWorm.position.x;
                                int checkDistanceY = checkCoordinate.y - currentWorm.position.y;
                                Coordinate checkDistance = new Coordinate(checkDistanceX, checkDistanceY);

                                if(checkDistance.isE() && checkCoordinate.x < enemyWormCoordinate.x){
                                    zeroObstruct =  zeroObstruct && notObstruction(checkCoordinate);
                                }
                            }

                        } else if (enemyDistance.isSE()) {

                            for (Coordinate checkCoordinate: attackCoordinateArray) {

                                int checkDistanceX = checkCoordinate.x - currentWorm.position.x;
                                int checkDistanceY = checkCoordinate.y - currentWorm.position.y;
                                Coordinate checkDistance = new Coordinate(checkDistanceX, checkDistanceY);

                                if (checkDistance.isSE() && checkCoordinate.y > enemyWormCoordinate.y && checkCoordinate.x < enemyWormCoordinate.x) {
                                    zeroObstruct =  zeroObstruct && notObstruction(checkCoordinate);
                                }
                            }

                        } else if (enemyDistance.isS()) {



                            for (Coordinate checkCoordinate: attackCoordinateArray) {

                                int checkDistanceX = checkCoordinate.x - currentWorm.position.x;
                                int checkDistanceY = checkCoordinate.y - currentWorm.position.y;
                                Coordinate checkDistance = new Coordinate(checkDistanceX, checkDistanceY);

                                if (checkDistance.isS() && checkCoordinate.y > enemyWormCoordinate.y) {
                                    zeroObstruct =  zeroObstruct && notObstruction(checkCoordinate);
                                }
                            }

                        } else if (enemyDistance.isSW()) {

                            for (Coordinate checkCoordinate: attackCoordinateArray) {

                                int checkDistanceX = checkCoordinate.x - currentWorm.position.x;
                                int checkDistanceY = checkCoordinate.y - currentWorm.position.y;
                                Coordinate checkDistance = new Coordinate(checkDistanceX, checkDistanceY);

                                if (checkDistance.isSW() && checkCoordinate.x > enemyWormCoordinate.x && checkCoordinate.y > enemyWormCoordinate.y) {
                                    zeroObstruct =  zeroObstruct && notObstruction(checkCoordinate);
                                }
                            }

                        } else if (enemyDistance.isW()) {

                            for (Coordinate checkCoordinate: attackCoordinateArray) {

                                int checkDistanceX = checkCoordinate.x - currentWorm.position.x;
                                int checkDistanceY = checkCoordinate.y - currentWorm.position.y;
                                Coordinate checkDistance = new Coordinate(checkDistanceX, checkDistanceY);

                                if (checkDistance.isW() && checkCoordinate.x > enemyWormCoordinate.x) {
                                    zeroObstruct =  zeroObstruct && notObstruction(checkCoordinate);
                                }
                            }

                        } else if (enemyDistance.isNW()) {

                            for (Coordinate checkCoordinate: attackCoordinateArray){

                                int checkDistanceX = checkCoordinate.x - currentWorm.position.x;
                                int checkDistanceY = checkCoordinate.y - currentWorm.position.y;
                                Coordinate checkDistance = new Coordinate(checkDistanceX, checkDistanceY);

                                if(checkDistance.isNW() && checkCoordinate.x > enemyWormCoordinate.x && checkCoordinate.y < enemyWormCoordinate.y){
                                    zeroObstruct =  zeroObstruct && notObstruction(checkCoordinate);
                                }
                            }

                        }

                        // return if the coordinate is valid and effective
                        // an attack command for shoot command is effective if it is not blocked
                        // not blocked by our own worms or dirt
                        if (zeroObstruct) { return new AttackCommand(enemyWormCoordinate, "shoot", currentWorm.weapon.damage); }
                    }
                }
             }
         }

         // returning default value for attack command if not found a valid and effective attack command
         return attackCommand;
     }

     private AttackCommand getBestBananabombCommand() {
         // initializing the default attackcommand, with damage 0
         // if the worm can't do this attack, then it will just return an attack command with 0 damage
         Coordinate attackCoordinate = new Coordinate(0, 0);
         AttackCommand attackCommand = new AttackCommand(attackCoordinate, "bananabomb", 0);

         if (currentWorm.canBananaBomb()) {
             
            // get the range and impact radius 
            int bananaBombRange = currentWorm.bananaBombWeapon.range;
            int bananaBombRadius = currentWorm.bananaBombWeapon.damageRadius;
            
            // get the valid coordinate for this attack
             Coordinate[] validBananaBombCoordinate = getValidSpecialAttackCoordinate(bananaBombRange, currentWorm);

            for (Coordinate bananaBombCoordinate: validBananaBombCoordinate) {

                // get the impact area when this attack is used in the particular coordinate               
                Coordinate[] bananaBombImpactCoordinateArray = getBananaBombImpactCoordinate(bananaBombCoordinate, bananaBombRadius);
                int maxDamageBananaBomb = 0;

                // to be an efective attack command, it has to damage atleast totalLivingEnemyWorm count - 1
               // for example, if the enemy team has 3 alive worm, than this attack have to atleast hit 2 enemyworm
                int minDamageBananaBomb = (getLivingWormCount(enemyWorms) - 1) * currentWorm.bananaBombWeapon.damage;

                for (Worm enemyWorm: enemyWorms) {

                    Coordinate enemyWormCoordinate = new Coordinate(enemyWorm.position.x, enemyWorm.position.y);

                    for (Coordinate c: bananaBombImpactCoordinateArray) {

                        if (Coordinate.isEqualCoordinate(enemyWormCoordinate, c)) {
                            maxDamageBananaBomb += currentWorm.bananaBombWeapon.damage;

                        }

                    }

                }

                // checking if the attack command with the current coordinate is a effective attack command
                if (maxDamageBananaBomb > attackCommand.damage && maxDamageBananaBomb >= minDamageBananaBomb) {
                    attackCommand = new AttackCommand(bananaBombCoordinate, "bananabomb", maxDamageBananaBomb);
                }
            }
         }

         return attackCommand;
     }

     private AttackCommand getBestSnowballCommand() {
        // initializing the default attackcommand, with damage 0
        // if the worm can't do this attack, then it will just return an attack command with 0 damage
        Coordinate attackCoordinate = new Coordinate(0, 0);
        AttackCommand attackCommand = new AttackCommand(attackCoordinate, "snowball", 0);

        if (currentWorm.canSnowball()) {

           // get the range and impact radius 
           int snowballRange = currentWorm.snowballWeapon.range;
           int snowballRadius = currentWorm.snowballWeapon.freezeRadius;

           // get the valid coordinate for this attack
            Coordinate[] validSnowballCoordinate = getValidSpecialAttackCoordinate(snowballRange, currentWorm);

           for (Coordinate snowballCoordinate: validSnowballCoordinate) {

               // get the impact area when this attack is used in the particular coordinate               
               Coordinate[] snowballImpactCoordinateArray = getSnowballImpactCoordinate(snowballCoordinate, snowballRadius);
               int maxDamageSnowball = 0;
               
               // to be an efective attack command, it has to damage atleast totalLivingEnemyWorm count - 1
               // for example, if the enemy team has 3 alive worm, than this attack have to atleast hit 2 enemyworm
               int minDamageSnowball = getLivingWormCount(enemyWorms) - 1;

               for (Worm enemyWorm: enemyWorms) {

                   // getting the coordinate of the enemyworm
                   Coordinate enemyWormCoordinate = new Coordinate(enemyWorm.position.x, enemyWorm.position.y);

                   for (Coordinate c: snowballImpactCoordinateArray) {

                       if (Coordinate.isEqualCoordinate(enemyWormCoordinate, c)) {
                           maxDamageSnowball += 1;

                       }

                   }

               }

               // checking if the attack command with the current coordinate is a effective attack command
               if (maxDamageSnowball > attackCommand.damage && maxDamageSnowball >= minDamageSnowball) {
                   attackCommand = new AttackCommand(snowballCoordinate, "snowball", maxDamageSnowball);
               }
           }
        }

        // returning the attack command with the biggest total damage to the enemy team
        return attackCommand;
     }

     private AttackCommand getBestAttackCommand() {


         // get the best attack command for all types of attack
         // attack command that is not valid or not effective will have attribut damage with the value of 0
         AttackCommand bestShootCommand = getBestShootCommand();
         AttackCommand bestBananaBombCommand = getBestBananabombCommand();
         AttackCommand bestSnowBallCommand = getBestSnowballCommand();

         
         // the algorithm will prioritize special attack such as bananabomb and snowball
         // even if all the attack command are not effective or not valid, it will return an attack command with attribut damage with value of 0
         // this attack command will be handled later in the main run function
         if (bestBananaBombCommand.damage > 0) {

             return bestBananaBombCommand;

         } else if (bestSnowBallCommand.damage > 0) { //

             return bestSnowBallCommand;

         } else {

             return bestShootCommand;

         }

     }
}
