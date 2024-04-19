package algoritmoGenetico;

import java.io.File;
import java.io.IOException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.annotation.JsonView;

public class GeneticAlgorithmConfig {

    private int mutationRate;
    private int popSize;
    private int popHereditary;
    private int popMutation;
    private int popCross;
    private int maxIterations;
    private int top;

    public static GeneticAlgorithmConfig fromJsonFile(String filePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(new File(filePath), GeneticAlgorithmConfig.class);
    }

    public int getMutationRate() {
        return mutationRate;
    }

    public int getPopSize() {
        return popSize;
    }

    public int getPopHereditary() {
        return popHereditary;
    }

    public int getPopMutation() {
        return popMutation;
    }

    public int getPopCross() {
        return popCross;
    }

    public int getMaxIterations() {
        return maxIterations;
    }

    public int getTop() {
        return top;
    }
}
