package lsieun.knit.instruction;

public class ComplexInstruction implements KnitInstruction
{
    private final String id;
    private final String name;

    private final int intervalPasses; // 每次调针之间的编织行程数 (如 6)
    private final int stitchChange;   // 每次调针的变化量 (如 +1)
    private final int repeatTimes;    // 重复次数 (如 30)
    private final ExecutionOrder order; // 执行顺序

    public ComplexInstruction(String id, String name,
                              int intervalPasses, int stitchChange,
                              int repeatTimes, ExecutionOrder order)
    {
        this.id = id;
        this.name = name;
        this.intervalPasses = intervalPasses;
        this.stitchChange = stitchChange;
        this.repeatTimes = repeatTimes;
        this.order = order;
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public String getName()
    {
        return name;
    }

    public int getIntervalPasses()
    {
        return intervalPasses;
    }

    public int getStitchChange()
    {
        return stitchChange;
    }

    public int getRepeatTimes()
    {
        return repeatTimes;
    }

    public ExecutionOrder getOrder()
    {
        return order;
    }

    @Override
    public String toString()
    {
        return String.format(
                "%s: order = %s, intervalPasses = %d, stitchChange=%d, repeatTimes = %d",
                name, order, intervalPasses, stitchChange, repeatTimes);
    }
}