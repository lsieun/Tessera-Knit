package lsieun.knit.instruction;

import lsieun.knit.model.*;

import java.util.ArrayList;
import java.util.List;

public class PlainKnitInstruction extends AbstractInstruction
{
    private final int totalPasses;

    public PlainKnitInstruction(String id, String name, int totalCourses)
    {
        super(id, name);
        this.totalPasses = totalCourses * 2;
    }

    @Override
    public InstructionOutput execute(final KnittingPassState lastKnownState)
    {
        List<KnittingPassState> passes = new ArrayList<>();
        // 语义：初始化“上一个状态”为传入的已知状态
        KnittingPassState lastState = lastKnownState;

        // totalPasses 是在构造函数中通过 totalCourses * 2 计算得出的整数
        for (int p = 0; p < totalPasses; p++) {
            // 1. 语义区分：定义本次行程的属性
            PassType currentPassType = lastState.passType().next();
            // 如果是去程(GO)，转数+1；如果是回程(BACK)，转数保持不变
            int currentCourseIdx = (currentPassType == PassType.GO)
                    ? lastState.courseIndex() + 1
                    : lastState.courseIndex();

            // 2. 构造本次行程状态（显式基于 lastState 演变）
            KnittingPassState currentState = lastState.mutate()
                    .courseIndex(currentCourseIdx)
                    .passType(currentPassType)
                    .stitchCount(lastState.stitchCount()) // 平织，针数维持现状
                    .build();

            // 3. 物理记录
            passes.add(currentState);

            // 4. 状态更迭：本次产物变为下一次的参照点
            lastState = currentState;
        }

        // 5. 语义终点：返回物理行程集合与最终逻辑状态
        KnittingPassState finalState = lastState;
        return new InstructionOutput(passes, finalState);
    }
}