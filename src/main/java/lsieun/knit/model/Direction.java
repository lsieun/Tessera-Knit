package lsieun.knit.model;

/**
 * 机头运动的物理方向
 */
public enum Direction
{
    LEFT_TO_RIGHT,
    RIGHT_TO_LEFT;

    /**
     * 获取相反方向
     */
    public Direction opposite()
    {
        return this == LEFT_TO_RIGHT ? RIGHT_TO_LEFT : LEFT_TO_RIGHT;
    }
}