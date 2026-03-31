package lsieun.knit.instruction;

public abstract class AbstractInstruction implements KnitInstruction {
    protected final String id;
    protected final String name;

    protected AbstractInstruction(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override public String getId() { return id; }
    @Override public String getName() { return name; }
}