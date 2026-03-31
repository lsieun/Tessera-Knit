package lsieun.knit.model;

/**
 * 描述行程的逻辑类型及其关联的物理属性
 */
public enum PassType
{
    GO(Direction.LEFT_TO_RIGHT),    // 去程：默认向右
    BACK(Direction.RIGHT_TO_LEFT); // 回程：默认向左

    private final Direction defaultDirection;

    PassType(Direction direction)
    {
        this.defaultDirection = direction;
    }

    public Direction getDefaultDirection()
    {
        return defaultDirection;
    }

    public PassType next()
    {
        return this == GO ? BACK : GO;
    }
}