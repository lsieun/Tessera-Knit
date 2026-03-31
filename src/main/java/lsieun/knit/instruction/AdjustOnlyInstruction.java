package lsieun.knit.instruction;

import lsieun.knit.model.KnittingPassState;

import java.util.ArrayList;
import java.util.List;

public class AdjustOnlyInstruction extends AbstractInstruction
{
    private final int stitchChange; // 例如 -20 (平收)

    public AdjustOnlyInstruction(String id, String name, int stitchChange)
    {
        super(id, name);
        this.stitchChange = stitchChange;
    }

    @Override
    public InstructionOutput execute(final KnittingPassState lastKnownState)
    {
        // 1. 语义准备：物理行程集合为空
        // 因为“仅调整”指令不驱动机头走过织物
        List<KnittingPassState> passes = new ArrayList<>();

        // 2. 逻辑演变：基于上一个已知状态进行“变异”
        // 我们直接获取上一个状态的针数，并应用调整值
        int lastStitchCount = lastKnownState.stitchCount();
        int currentStitchCount = lastStitchCount + stitchChange;

        // 3. 构建最终态 (Final State)
        // 注意：位置（PassType）和转数（CourseIndex）保持不变，仅更新针数
        KnittingPassState finalState = lastKnownState.mutate()
                .stitchCount(currentStitchCount)
                .build();

        // 4. 返回包裹
        // Engine 拿到这个包裹后，发现 passes 为空，不会渲染任何动作，
        // 但会通过 finalState 将最新的针数传递给下一个指令。
        return new InstructionOutput(passes, finalState);
    }

    public int getStitchChange()
    {
        return stitchChange;
    }
}