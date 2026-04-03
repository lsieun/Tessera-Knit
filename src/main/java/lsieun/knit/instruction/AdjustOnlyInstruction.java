package lsieun.knit.instruction;

public class AdjustOnlyInstruction implements KnitInstruction {
    private final String id;
    private final String name;
    private final int stitchChange; // 针数变化值 (正数为加针，负数为减针)

    public AdjustOnlyInstruction(String id, String name, int stitchChange) {
        this.id = id;
        this.name = name;
        this.stitchChange = stitchChange;
    }

    @Override
    public String getId() { return id; }

    @Override
    public String getName() { return name; }

    public int getStitchChange() { return stitchChange; }

    @Override
    public String toString()
    {
        return String.format("%s: stitchChange=%s", name, stitchChange);
    }
}