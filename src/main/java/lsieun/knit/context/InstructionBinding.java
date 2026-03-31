package lsieun.knit.context;

import lsieun.knit.instruction.KnitInstruction;

/**
 * 身份证明：记录一个行程在指令执行树中的具体位置
 */
public record InstructionBinding(
        KnitInstruction instruction, // 所属指令
        int repeatIndex,             // 第几次重复 (1 ~ repeatTimes)
        int passIndexInRepeat        // 在该次重复中的第几个行程 (0 ~ intervalPasses-1)
)
{
}