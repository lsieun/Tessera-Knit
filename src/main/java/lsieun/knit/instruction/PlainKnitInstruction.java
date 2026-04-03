package lsieun.knit.instruction;

public class PlainKnitInstruction implements KnitInstruction {
    private final String id;
    private final String name;
    private final int totalPasses;

    public PlainKnitInstruction(String id, String name, int totalPasses) {
        this.id = id;
        this.name = name;
        this.totalPasses = totalPasses;
    }

    @Override
    public String getId() { return id; }

    @Override
    public String getName() { return name; }

    public int getTotalPasses() { return totalPasses; }

    @Override
    public String toString()
    {
        return String.format("%s: totalPasses=%s", name, totalPasses);
    }
}