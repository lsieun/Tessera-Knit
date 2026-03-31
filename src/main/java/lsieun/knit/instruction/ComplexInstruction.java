package lsieun.knit.instruction;

import lsieun.knit.model.*;

import java.util.ArrayList;
import java.util.List;

public class ComplexInstruction extends AbstractInstruction
{
    private final int intervalPasses; // 👈 核心修改：使用行程数，1.5转对应 3
    private final int stitchChange;
    private final int repeatTimes;
    private final ExecutionOrder order;

    public ComplexInstruction(String id, String name, int intervalPasses, int change, int repeat, ExecutionOrder order)
    {
        super(id, name);
        this.intervalPasses = intervalPasses;
        this.stitchChange = change;
        this.repeatTimes = repeat;
        this.order = order;
    }


    @Override
    @SuppressWarnings("UnnecessaryLocalVariable")
    public InstructionOutput execute(final KnittingPassState lastKnownState)
    {
        List<KnittingPassState> passes = new ArrayList<>();
        KnittingPassState lastState = lastKnownState;


        for (int r = 1; r <= repeatTimes; r++) {
            int lastStitchCount = lastState.stitchCount();    // 上一次的针数
            int currentStitchCount = lastStitchCount;         // 本次的针数

            // 1. 先调针，后编织
            if (order == ExecutionOrder.ADJUST_THEN_KNIT) {
                // 第 1 步，针数调整
                currentStitchCount += stitchChange;

                // 第 2 步，在不编织的情况下，更新状态
                lastState = lastState.mutate()
                        .stitchCount(currentStitchCount)
                        .build();

                if (r == repeatTimes) {
                    break;
                }
            }

            // 2. 编织
            for (int p = 0; p < intervalPasses; p++) {
                // 第 1 步，自动推导：由 lastState 推导 currentState 的方向和类型
                PassType currentPassType = lastState.passType().next();
                int currentCourseIdx = (currentPassType == PassType.GO) ? lastState.courseIndex() + 1 : lastState.courseIndex();

                KnittingPassState currentState = lastState.mutate()
                        .courseIndex(currentCourseIdx)
                        .passType(currentPassType)
                        .stitchCount(currentStitchCount)
                        .build();

                // 第 2 步，添加 currentState
                passes.add(currentState);

                // 第 3 步，由“新”变“旧”
                lastState = currentState;
            }

            // 3. 先编织，后调针
            if (order == ExecutionOrder.KNIT_THEN_ADJUST) {
                // 第 1 步，针数调整
                currentStitchCount += stitchChange;

                // 第 2 步，在不编织的情况下，更新状态
                lastState = lastState.mutate()
                        .stitchCount(currentStitchCount)
                        .build();
            }
        }

        // 关键点：即使循环结束了，如果最后一次调针改变了 currentStitchCount
        // 我们在这里基于 lastState 生成一个最终的逻辑状态返回给 Engine
        KnittingPassState finalState = lastState;

        return new InstructionOutput(passes, finalState);
    }

    // 提供一个静态工厂方法，让用户依然可以按“转”来输入
    public static ComplexInstruction fromCourse(String id, String name, double courses, int change, int repeat, ExecutionOrder order)
    {
        int passes = (int) Math.round(courses * 2);
        return new ComplexInstruction(id, name, passes, change, repeat, order);
    }
}