package lsieun.knit.instruction;

import lsieun.knit.model.KnittingPassState;

import java.util.List;

/**
 * 封装指令执行的全部产物：物理行程 + 逻辑终点状态
 */
public record InstructionOutput(
        List<KnittingPassState> passes,
        KnittingPassState finalState // 指令执行完后，机器应该处于的确定状态
)
{
}

