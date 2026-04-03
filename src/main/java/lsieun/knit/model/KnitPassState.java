package lsieun.knit.model;

/**
 * 代表编织机器在某一个单程（Pass）中的完整状态
 */
public record KnitPassState(
        int courseIndex,
        PassType passType,
        int stitchCount,
        Direction direction
)
{

    public Builder mutate()
    {
        return new Builder(this);
    }

    public static class Builder
    {
        private int courseIndex;
        private PassType passType;
        private int stitchCount;
        private Direction direction;

        private Builder()
        {
            this.courseIndex = 0;
            this.passType = PassType.BACK;
            this.stitchCount = 0;
            this.direction = passType.getDefaultDirection();
        }

        // 内部构造，从现有状态克隆
        private Builder(KnitPassState state)
        {
            this.courseIndex = state.courseIndex();
            this.passType = state.passType();
            this.stitchCount = state.stitchCount();
            this.direction = state.direction();
        }

        public Builder courseIndex(int val)
        {
            this.courseIndex = val;
            return this;
        }

        /**
         * 设置行程类型，并自动同步关联的方向
         */
        public Builder passType(PassType val)
        {
            this.passType = val;
            this.direction = val.getDefaultDirection(); // 自动联动
            return this;
        }

        public Builder stitchCount(int val)
        {
            this.stitchCount = val;
            return this;
        }

        /**
         * 允许手动覆盖方向（应对极少数特殊工艺需求）
         */
        public Builder direction(Direction val)
        {
            this.direction = val;
            return this;
        }

        public KnitPassState build()
        {
            return new KnitPassState(courseIndex, passType, stitchCount, direction);
        }
    }

    /**
     * 生成编织流程的逻辑原点（初始状态）
     * * @param stitchCount 起始针数
     *
     * @return 一个位于 Course 0, BACK 结束（机头在左）, 指定针数的初始状态
     */
    public static KnitPassState initial(int stitchCount)
    {
        return new KnitPassState.Builder()
                .courseIndex(0)
                .passType(PassType.BACK) // 默认为 BACK 结束，确保第一个指令的 next() 是 GO
                .stitchCount(stitchCount)
                .build();
    }

    /**
     * 如果你需要更灵活的初始位置（比如机头起始在右侧）
     */
    public static KnitPassState initial(int courseIndex, PassType passType, int stitchCount)
    {
        return new KnitPassState.Builder()
                .courseIndex(courseIndex)
                .passType(passType)
                .stitchCount(stitchCount)
                .build();
    }
}