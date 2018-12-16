import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class GeneticAlgorithm {
    private Matrix population;
    ArrayList<Integer> fighters;
    Random rand;
    int bounds;
    int winnerChance = 52; // 0-51 loser is purged
    int mutationRateIndex = 292;
    int chanceOfPromotionIndex = 291;
    int deviationValueIndex = 293;
    int numberOfMates = 294;
    static long timer = 0;
    List<Double> fitnessValues;
    ArrayList<Integer> parents;
    List<Double> copyOfFitness;
    int numberOfWins = 0;
    List<Double> fitnessCopy;


    GeneticAlgorithm(Matrix population) {
        this.population = population;
        fighters = new ArrayList<Integer>();
        fitnessValues = new ArrayList<Double>();

        for (int i = 0; i < 100; i++) {
            fitnessValues.add(0.0);
        }
        parents = new ArrayList<Integer>();
        rand = new Random();
        bounds = 99;
    }

    int generateNumber() {
        return rand.nextInt(bounds);
    }

    public void setFitnessValues(List<Double> fitnessValues) {
        this.fitnessValues = fitnessValues;
    }

    double generateDouble(double lower, double upper) {
        return ThreadLocalRandom.current().nextDouble(lower, upper);

    }

    void update() {
        this.mutate();
        int selection = rand.nextInt(9);
        if (selection == 0) {
            this.NaturalSelection();
        }
        calculateFitness();
        List<Double> fitnessValues = getFitnessValues();
        fitnessCopy = new ArrayList<>(fitnessValues);
        fitnessCopy.sort(Comparator.reverseOrder());
        //Print most fit
//        System.out.println("Population: " + fitnessValues.size());
//        System.out.print(fitnessValues);
        System.out.println(fitnessCopy.get(0));
    }


    void copyFitness() {
        copyOfFitness = new ArrayList<Double>(fitnessValues);
    }

    void mutate() {
        for (int i = 0; i < 100; i++) {
            double[] chromosome = population.row(i);
            if (generateDouble(0, 1) < chromosome[mutationRateIndex]) {

                int index = rand.nextInt(298);
                //System.out.println("Value before mutation: " + chromosome[index]);
                if (index == chanceOfPromotionIndex) {
                    chromosome[index] = ThreadLocalRandom.current().nextDouble(0.80, 0.90);
                } else if (index == mutationRateIndex) {
                    chromosome[index] = ThreadLocalRandom.current().nextDouble(0.50, 0.70);
                } else if (index == deviationValueIndex) {
                    chromosome[index] = ThreadLocalRandom.current().nextDouble(0.1, 0.15);
                } else if (index == numberOfMates) {
                    chromosome[index] = ThreadLocalRandom.current().nextInt(3, 6);
                } else
                    chromosome[index] = (getElementValue(chromosome, deviationValueIndex) * rand.nextGaussian()) + chromosome[index];
                //System.out.println("Value after mutation: " + chromosome[index]);
            }

        }
    }

    void NaturalSelection() {
        while (fighters.size() < 2) {
            int generateIndex = generateNumber();
            if (!fighters.contains(generateIndex)) {
                fighters.add(generateIndex);
            }
        }
        int result = makeThemFight(fighters.get(0), fighters.get(1));
        if (result == -1) {
            int winner = fighters.get(1);
            int loser = fighters.get(0);
            System.out.println("winner: " + winner);
            System.out.println("loser: " + loser);
            toPurge(winner, loser);
        } else if (result == 1) {
            int winner = fighters.get(0);
            int loser = fighters.get(1);
            System.out.println("winner: " + winner);
            System.out.println("loser: " + loser);
            toPurge(winner, loser);
        } else
            System.out.println("Tie!");
        fighters.clear();

    }

    int makeThemFight(int index1, int index2) {
        double[] chromosome1 = population.row(index1);
        double[] chromosome2 = population.row(index2);
        int result = 0;
        try {
            result = Controller.doBattleNoGui(new NeuralAgent(chromosome1), new NeuralAgent(chromosome2));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    void toPurge(int winner, int loser) {
        double[] winnerChromosome = population.row(winner);
        double[] loserChromosome = population.row(loser);
        if (generateDouble(0, 1) < getElementValue(winnerChromosome, chanceOfPromotionIndex)) {
            System.out.println("Purging the loser!");
            purge(loser);
        } else {
            System.out.println("Purging the winner!");
            purge(winner);
        }
    }

    void purge(int index) {
        double[] toReplace = population.row(index);
        crossOver(toReplace);
    }

    void crossOver(double[] toReplace) {
        double closest = Double.MAX_VALUE;
        int closestIndex = Integer.MAX_VALUE;
        int parent1Index = generateNumber();
        double[] parent1 = population.row(parent1Index);

        while (parents.size() < getElementValue(parent1, numberOfMates)) {
            int generateIndex = generateNumber();
            if (!parents.contains(generateIndex)) {
                parents.add(generateIndex);
            }
        }

        for (int i = 0; i < parents.size(); i++) {
            double closeness = difference(parent1, population.row(parents.get(i)));
            if (closeness < closest) {
                closest = closeness;
                closestIndex = parents.get(i);
            }
        }

        double[] chosenParent = population.row(closestIndex);

        for (int i = 0; i < parent1.length; i++) {
            if (generateNumber() < 50) {
                //System.out.println("Replacing : " + toReplace[i] + " with " + parent1[i]);
                toReplace[i] = parent1[i];

            } else {
                //System.out.println("Replacing : " + toReplace[i] + " with " + chosenParent[i]);
                toReplace[i] = chosenParent[i];
            }
        }
    }

    double difference(double[] parent1, double[] parent2) {
        ArrayList<Double> differences = new ArrayList<Double>();
        double total = 0;
        for (int i = 0; i < parent1.length; i++) {
            differences.add(Math.abs((parent1[i] - parent2[i])));
        }

        for (Double sum : differences) {
            total = (total + sum);
        }

        return total;
    }



    public double getFitnessAgainstReflex(NeuralAgent agent) {
        int result = 0;
        double fitness = 0;
        BattleResult battleResult = new BattleResult();
        try {
            result = Controller.doBattleNoGuiTimer(agent, new ReflexAgent(), battleResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (result == 1) {
            fitness = (1 / (double) battleResult.getBattleTimer()) * 1000000;
            System.out.println("Won!");
            System.out.println("Winning weights: " + Arrays.toString(agent.getTmpWeights()));
            numberOfWins++;
            System.out.println("Wins: " + numberOfWins);
        } else if (result == -1) {
            fitness = (-1 / (double) battleResult.getBattleTimer()) * 1000000;
        } else {
            //Tie or time ran out.
            fitness = 0;
        }
        return fitness;
    }


    public List<Double> getFitnessValues() {
        return fitnessValues;
    }

    public void calculateFitness() {
        //numberOfWins = 0;
        for (int i = 0; i < 100; i++) {
            double[] chromosome = population.row(i);
            //System.out.println(Arrays.toString(chromosome));
            double fitness = getFitnessAgainstReflex(new NeuralAgent(chromosome));
            fitnessValues.set(i, fitness);
        }
    }

    double getElementValue(double[] chromosome, int index) {
        return chromosome[index];
    }

    double getFitnessByIndex(int index) {
        return fitnessValues.get(index);
    }

    double getCopyFitnessByIndex(int index) {
        return copyOfFitness.get(index);
    }

    void printFitness() {
        System.out.println("Highest fitness: " + getCopyFitnessByIndex(0));
    }
}
