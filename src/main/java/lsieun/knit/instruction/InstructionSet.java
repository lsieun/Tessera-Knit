package lsieun.knit.instruction;

import java.util.ArrayList;
import java.util.List;

public class InstructionSet {
    private final String id;
    private final String name;
    private final List<KnitInstruction> instructions = new ArrayList<>();

    public InstructionSet(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public void addInstruction(KnitInstruction instruction) {
        this.instructions.add(instruction);
    }

    public List<KnitInstruction> getInstructions() {
        return List.copyOf(instructions);
    }

    // Getter...
}