package lsieun.knit.instruction;

import lsieun.knit.model.KnittingPassState;

public interface KnitInstruction
{
    String getId();    // 唯一标识 (UUID或序列)

    String getName();  // 人性化名称 (如 "袖口加针")

    /**
     * 输入上一个状态，输出完整的物理行程和最终达成状态
     */
    InstructionOutput execute(final KnittingPassState lastKnownState);
}