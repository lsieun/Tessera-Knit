package lsieun.knit.event;

import lsieun.knit.context.InstructionBinding;
import lsieun.knit.instruction.KnitInstruction;
import lsieun.knit.model.KnitPass;

import java.util.Optional;

/**
 * 编织事务记录：系统的“数据中心仓库”单元。
 * 记录了谁（Instruction）在什么时候（Binding）做了什么（Action），
 * 以及由此产生的状态变化（Stitches Before/After）。
 */
public record KnitEvent(
        KnitInstruction instruction,   // 触发此事件的原始指令
        InstructionBinding binding,    // 该事件在指令执行树中的逻辑位置
        int stitchesBefore,            // 动作执行前的针数
        int stitchesAfter,             // 动作执行后的针数
        Optional<KnitPass> pass        // 关联的物理行程（若仅为逻辑调针，则为空）
)
{
    /**
     * 判定该事件是否产生了实际的物理编织行程
     */
    public boolean isPhysical()
    {
        return pass.isPresent();
    }

    /**
     * 判定该事件是否纯粹是逻辑上的针数调整
     */
    public boolean isAdjustmentOnly()
    {
        return pass.isEmpty();
    }
}