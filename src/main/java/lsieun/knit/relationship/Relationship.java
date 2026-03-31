package lsieun.knit.relationship;

/**
 * 外部关系绑定
 *
 * @param <S> 源对象类型 (Source) - 例如: KnittingPassState
 * @param <T> 目标对象类型 (Target) - 例如: KnitInstruction 或 InstructionSet
 */
public record Relationship<S, T>(
        S source,
        T target,
        int index // 在目标容器中的序号
)
{
}